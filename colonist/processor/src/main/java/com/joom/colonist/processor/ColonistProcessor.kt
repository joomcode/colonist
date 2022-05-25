/*
 * Copyright 2019 SIA Joom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joom.colonist.processor

import com.joom.colonist.processor.analysis.AnnotationIndex
import com.joom.colonist.processor.analysis.ColonyMarkerParser
import com.joom.colonist.processor.analysis.ColonyMarkerParserImpl
import com.joom.colonist.processor.analysis.ColonyParser
import com.joom.colonist.processor.analysis.ColonyParserImpl
import com.joom.colonist.processor.analysis.SettlerAcceptorParserImpl
import com.joom.colonist.processor.analysis.SettlerDiscoverer
import com.joom.colonist.processor.analysis.SettlerDiscovererImpl
import com.joom.colonist.processor.analysis.SettlerParserImpl
import com.joom.colonist.processor.analysis.SettlerProducerParserImpl
import com.joom.colonist.processor.analysis.SettlerSelectorParserImpl
import com.joom.colonist.processor.commons.StandaloneClassWriter
import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.commons.closeQuietly
import com.joom.colonist.processor.generation.ColonyPatcher
import com.joom.colonist.processor.logging.getLogger
import com.joom.colonist.processor.model.Colony
import com.joom.colonist.processor.model.ColonyMarker
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerSelector
import com.joom.grip.Grip
import com.joom.grip.GripFactory
import com.joom.grip.classes
import com.joom.grip.io.FileSource
import com.joom.grip.io.IoFactory
import com.joom.grip.mirrors.Annotated
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.getObjectTypeByInternalName
import java.io.Closeable
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.toList
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class ColonistProcessor(
  inputs: List<File>,
  outputs: List<File>,
  private val grip: Grip,
  private val annotationIndex: AnnotationIndex,
  private val colonyMarkerParser: ColonyMarkerParser,
  private val colonyParser: ColonyParser,
  private val settlerDiscoverer: SettlerDiscoverer,
  private val errorReporter: ErrorReporter
) : Closeable {

  private val logger = getLogger()

  private val fileSourcesAndSinks = inputs.zip(outputs) { input, output ->
    val source = IoFactory.createFileSource(input)
    val sink = IoFactory.createFileSink(input, output)
    source to sink
  }

  fun processClasses() {
    val colonies = findColonies()
    checkErrors()
    copyAndPatchClasses(colonies)
  }

  override fun close() {
    fileSourcesAndSinks.forEach {
      it.first.closeQuietly()
      it.second.closeQuietly()
    }
  }

  private fun findColonies(): Collection<Colony> {
    val markers = findColonyMarkers()
    val markersWithSettlers = findSettlersForColonyMarkers(markers)
    return findColonies(markersWithSettlers)
  }

  private fun findColonyMarkers(): Collection<ColonyMarker> {
    val colonyAnnotationTypes = annotationIndex.findClassesWithAnnotation(Types.COLONY_TYPE)
    return colonyAnnotationTypes.mapNotNull { colonyAnnotationType ->
      try {
        colonyMarkerParser.parseColonyMarker(colonyAnnotationType)
      } catch (exception: Exception) {
        errorReporter.reportError(exception)
        null
      }
    }
  }

  private fun findSettlersForColonyMarkers(colonyMarkers: Collection<ColonyMarker>): Collection<ColonyMarkerWithSettlers> {
    val cache = ConcurrentHashMap<SettlerSelector, Collection<Settler>>()
    return colonyMarkers
      .parallelStream()
      .map { marker ->
        val selector = marker.settlerSelector
        val settlers = cache.computeIfAbsent(selector) {
          settlerDiscoverer.discoverSettlers(selector)
        }
        ColonyMarkerWithSettlers(marker, settlers)
      }
      .toList()
  }

  private fun findColonies(colonyMarkersWithSettlers: Collection<ColonyMarkerWithSettlers>): Collection<Colony> {
    return colonyMarkersWithSettlers.flatMap { findColonies(it.colonyMarker, it.settlers) }
  }

  private fun findColonies(colonyMarker: ColonyMarker, settlers: Collection<Settler>): Collection<Colony> {
    val colonyTypes = annotationIndex.findClassesWithAnnotation(colonyMarker.type)
    return colonyTypes.mapNotNull { colonyType ->
      try {
        colonyParser.parseColony(colonyType, colonyMarker, settlers)
      } catch (exception: Exception) {
        errorReporter.reportError(exception)
        null
      }
    }
  }

  private fun copyAndPatchClasses(colonies: Collection<Colony>) {
    val colonyTypeToColoniesMap = colonies.groupBy { it.type }
    fileSourcesAndSinks.parallelStream().forEach { (fileSource, fileSink) ->
      logger.debug("Copy from {} to {}", fileSource, fileSink)
      fileSource.listFiles { path, type ->
        logger.debug("Copy file {} of type {}", path, type)
        when (type) {
          FileSource.EntryType.CLASS -> {
            val classReader = ClassReader(fileSource.readFile(path))
            val classWriter = StandaloneClassWriter(
              classReader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, grip.classRegistry
            )
            val classType = getObjectTypeByInternalName(classReader.className)
            val classVisitor = createClassVisitorForType(classType, classWriter, colonyTypeToColoniesMap)
            classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)
            fileSink.createFile(path, classWriter.toByteArray())
          }

          FileSource.EntryType.FILE -> fileSink.createFile(path, fileSource.readFile(path))
          FileSource.EntryType.DIRECTORY -> fileSink.createDirectory(path)
        }
      }

      fileSink.flush()
    }

    checkErrors()
  }

  private fun createClassVisitorForType(
    type: Type.Object,
    input: ClassVisitor,
    colonyTypeToColoniesMap: Map<Type.Object, Collection<Colony>>
  ): ClassVisitor {
    return colonyTypeToColoniesMap[type]?.let { ColonyPatcher(input, it) } ?: input
  }

  private fun checkErrors() {
    if (errorReporter.hasErrors()) {
      throw ProcessingException(composeErrorMessage())
    }
  }

  private fun composeErrorMessage(): String {
    return errorReporter.getErrors().joinToString("\n") { it.message.orEmpty() }
  }

  private data class ColonyMarkerWithSettlers(
    val colonyMarker: ColonyMarker,
    val settlers: Collection<Settler>
  )

  companion object {
    fun process(parameters: ColonistParameters) {
      val errorReporter = ErrorReporter()
      val grip = GripFactory.INSTANCE.create(parameters.inputs + parameters.classpath + parameters.bootClasspath)

      warmUpGripCaches(grip, parameters.inputs)

      val annotationIndex = buildAnnotationIndex(grip, parameters.inputs + parameters.classpath)

      val colonyMarkerParser = ColonyMarkerParserImpl(
        grip = grip,
        settlerSelectorParser = SettlerSelectorParserImpl,
        settlerProducerParser = SettlerProducerParserImpl,
        settlerAcceptorParser = SettlerAcceptorParserImpl
      )

      val colonyParser = ColonyParserImpl(
        grip = grip, errorReporter = errorReporter
      )

      val settlerParser = SettlerParserImpl(
        grip = grip,
        settlerProducerParser = SettlerProducerParserImpl,
        settlerAcceptorParser = SettlerAcceptorParserImpl
      )

      val settlerDiscoverer = SettlerDiscovererImpl(
        grip = grip,
        inputs = parameters.inputs + parameters.classpath,
        settlerParser = settlerParser,
        errorReporter = errorReporter
      )

      ColonistProcessor(
        inputs = parameters.inputs,
        outputs = parameters.outputs,
        grip = grip,
        annotationIndex = annotationIndex,
        colonyMarkerParser = colonyMarkerParser,
        colonyParser = colonyParser,
        settlerDiscoverer = settlerDiscoverer,
        errorReporter = errorReporter
      ).use {
        it.processClasses()
      }
    }

    private fun warmUpGripCaches(grip: Grip, inputs: List<File>) {
      inputs.flatMap { grip.fileRegistry.findTypesForFile(it) }
        .parallelStream()
        .map { grip.classRegistry.getClassMirror(it) }
        .toList()
    }

    private fun buildAnnotationIndex(grip: Grip, inputs: List<File>): AnnotationIndex {
      return AnnotationIndex.build {
        val query = grip select classes from inputs where annotated()
        for (mirror in query.execute().classes) {
          for (annotation in mirror.annotations) {
            addAnnotatedType(mirror.type, annotation.type)
          }
        }
      }
    }

    private fun annotated() = { _: Grip, mirror: Annotated -> mirror.annotations.isNotEmpty() }
  }
}
