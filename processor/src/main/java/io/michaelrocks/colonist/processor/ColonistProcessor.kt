/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.colonist.processor

import io.michaelrocks.colonist.processor.analysis.AnnotationIndex
import io.michaelrocks.colonist.processor.analysis.ColonyMarkerParserImpl
import io.michaelrocks.colonist.processor.analysis.ColonyParserImpl
import io.michaelrocks.colonist.processor.analysis.SettlerAcceptorParserImpl
import io.michaelrocks.colonist.processor.analysis.SettlerFactoryParserImpl
import io.michaelrocks.colonist.processor.analysis.SettlerMarkerParserImpl
import io.michaelrocks.colonist.processor.analysis.SettlerParserImpl
import io.michaelrocks.colonist.processor.commons.StandaloneClassWriter
import io.michaelrocks.colonist.processor.commons.Types
import io.michaelrocks.colonist.processor.commons.closeQuietly
import io.michaelrocks.colonist.processor.generation.Patcher
import io.michaelrocks.colonist.processor.logging.getLogger
import io.michaelrocks.colonist.processor.model.Colony
import io.michaelrocks.colonist.processor.model.ColonyMarker
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.classpath
import io.michaelrocks.grip.io.FileSource
import io.michaelrocks.grip.io.IoFactory
import io.michaelrocks.grip.mirrors.Annotated
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.Closeable

class ColonistProcessor(parameters: ColonistParameters) : Closeable {

  private val logger = getLogger()

  private val grip: Grip = GripFactory.create(parameters.inputs + parameters.classpath + parameters.bootClasspath)
  private val errorReporter = ErrorReporter()

  private val fileSourcesAndSinks = parameters.inputs.zip(parameters.outputs) { input, output ->
    val source = IoFactory.createFileSource(input)
    val sink = IoFactory.createFileSink(input, output)
    source to sink
  }

  private val annotationIndex = buildAnnotationIndex(grip)
  private val colonyMarkerParser =
    ColonyMarkerParserImpl(grip, SettlerMarkerParserImpl, SettlerFactoryParserImpl, SettlerAcceptorParserImpl)
  private val colonyParser =
    ColonyParserImpl(grip, annotationIndex, SettlerParserImpl(grip, SettlerFactoryParserImpl, SettlerAcceptorParserImpl), errorReporter)

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

  private fun buildAnnotationIndex(grip: Grip): AnnotationIndex {
    return AnnotationIndex.build {
      val query = grip select classes from classpath where annotated()
      for (mirror in query.execute().classes) {
        for (annotation in mirror.annotations) {
          addAnnotatedType(mirror.type, annotation.type)
        }
      }
    }
  }

  private fun findColonies(colonyMarkers: Collection<ColonyMarker> = findColonyMarkers()): Collection<Colony> {
    return colonyMarkers.flatMap { findColonies(it) }
  }

  private fun findColonies(colonyMarker: ColonyMarker): Collection<Colony> {
    val colonyTypes = annotationIndex.findClassesWithAnnotation(colonyMarker.type)
    return colonyTypes.mapNotNull { colonyType ->
      try {
        colonyParser.parseColony(colonyType, colonyMarker)
      } catch (exception: Exception) {
        errorReporter.reportError(exception)
        null
      }
    }
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

  private fun copyAndPatchClasses(colonies: Collection<Colony>) {
    val colonyTypeToColoniesMap = colonies.groupBy { it.type }
    fileSourcesAndSinks.forEach { (fileSource, fileSink) ->
      logger.debug("Copy from {} to {}", fileSource, fileSink)
      fileSource.listFiles { path, type ->
        logger.debug("Copy file {} of type {}", path, type)
        when (type) {
          FileSource.EntryType.CLASS -> {
            val classReader = ClassReader(fileSource.readFile(path))
            val classWriter = StandaloneClassWriter(
              classReader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, grip.classRegistry
            )
            val classVisitor = Patcher(classWriter, colonyTypeToColoniesMap)
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

  private fun checkErrors() {
    if (errorReporter.hasErrors()) {
      throw ProcessingException(composeErrorMessage())
    }
  }

  private fun composeErrorMessage(): String {
    return errorReporter.getErrors().joinToString("\n") { it.message.orEmpty() }
  }

  private fun annotated() = { _: Grip, mirror: Annotated -> mirror.annotations.isNotEmpty() }

  companion object {
    fun process(parameters: ColonistParameters) {
      ColonistProcessor(parameters).use {
        it.processClasses()
      }
    }
  }
}
