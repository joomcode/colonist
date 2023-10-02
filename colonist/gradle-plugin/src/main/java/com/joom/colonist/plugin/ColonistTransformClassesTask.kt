/*
 * Copyright 2022 SIA Joom
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

package com.joom.colonist.plugin

import com.joom.colonist.processor.ColonistOutputFactory
import com.joom.colonist.processor.ColonistParameters
import com.joom.colonist.processor.ColonistProcessor
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ColonistTransformClassesTask : DefaultTask() {
  @get:InputFiles
  @get:Classpath
  abstract val inputClasses: ListProperty<RegularFile>

  @get:InputFiles
  @get:Classpath
  abstract val inputDirectories: ListProperty<Directory>

  @get:InputFiles
  @get:CompileClasspath
  abstract val discoveryClasspath: ConfigurableFileCollection

  @get:InputFiles
  @get:CompileClasspath
  abstract val classpath: ConfigurableFileCollection

  @get:InputFiles
  @get:CompileClasspath
  abstract val bootClasspath: ConfigurableFileCollection

  @get:OutputFile
  @get:Optional
  abstract val output: RegularFileProperty

  @get:OutputDirectory
  @get:Optional
  abstract val outputDirectory: DirectoryProperty

  @Input
  var discoverSettlers: Boolean = false

  init {
    logging.captureStandardOutput(LogLevel.INFO)
  }

  @TaskAction
  fun process() {
    clean()

    val output = computeOutput().get().toPath()

    val parameters = ColonistParameters(
      inputs = inputClasses.get().map { it.asFile.toPath() } + inputDirectories.get().map { it.asFile.toPath() },
      outputFactory = ColonistOutputFactory.create(output),
      discoveryClasspath = discoveryClasspath.map { it.toPath() },
      classpath = classpath.map { it.toPath() },
      bootClasspath = bootClasspath.map { it.toPath() },
      discoverSettlers = discoverSettlers,
    )

    logger.info("Starting Colonist processor: {}", parameters)
    try {
      ColonistProcessor.process(parameters)
    } catch (exception: Exception) {
      throw GradleScriptException("Colonist processor failed to process files", exception)
    }
  }

  private fun computeOutput(): Provider<File> {
    return when {
      output.isPresent -> output.asFile
      outputDirectory.isPresent -> outputDirectory.asFile
      else -> error("output or outputDirectory is not set")
    }
  }

  private fun clean() {
    val output = computeOutput()

    if (output.get().exists()) {
      output.get().deleteRecursively()
    }
  }
}
