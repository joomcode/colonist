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

package com.joom.colonist.plugin

import com.joom.colonist.processor.ColonistParameters
import com.joom.colonist.processor.ColonistProcessor
import com.joom.colonist.processor.watermark.WatermarkChecker
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

open class ColonistTask : DefaultTask() {
  @InputFiles
  var backupDirs: List<File> = emptyList()

  @OutputDirectories
  var classesDirs: List<File> = emptyList()

  @OutputDirectory
  var sourceDir: File? = null

  @InputFiles
  @Classpath
  var classpath: List<File> = emptyList()

  @InputFiles
  @Classpath
  var bootClasspath: List<File> = emptyList()

  @Input
  var discoverSettlers: Boolean = false

  init {
    logging.captureStandardOutput(LogLevel.INFO)
  }

  @TaskAction
  fun process() {
    validate()

    val parameters = ColonistParameters(
      inputs = backupDirs,
      outputs = classesDirs,
      classpath = classpath,
      discoveryClasspath = classpath,
      generationOutput = classesDirs.first(),
      bootClasspath = bootClasspath,
      projectName = name.orEmpty().replace(":colonistProcess", ":").replace(':', '$'),
      discoverSettlers = discoverSettlers,
      debug = logger.isDebugEnabled,
      info = logger.isInfoEnabled
    )

    logger.info("Starting Colonist processor: {}", parameters)
    try {
      ColonistProcessor.process(parameters)
    } catch (exception: Exception) {
      throw GradleScriptException("Colonist processor failed to process files", exception)
    }
  }

  fun clean() {
    validate()
    logger.info("Removing patched files from {}", classesDirs)

    for (classesDir in classesDirs) {
      if (!classesDir.exists()) {
        continue
      }

      classesDir.walkBottomUp().forEach { file ->
        if (file.isDirectory) {
          file.delete()
        } else {
          logger.debug("Checking {}...", file)
          if (WatermarkChecker.isColonistClass(file)) {
            logger.debug("File was patched - removing")
            file.delete()
          } else {
            logger.debug("File wasn't patched - skipping")
          }
        }
      }
    }

    sourceDir?.let { sourceDir ->
      logger.info("Removing a directory with generated source files: {}", sourceDir)
      sourceDir.deleteRecursively()
    }
  }

  private fun validate() {
    require(classesDirs.isNotEmpty()) { "classesDirs is not set" }
    require(backupDirs.isNotEmpty()) { "backupDirs is not set" }
    require(classesDirs.size == backupDirs.size) { "classesDirs and backupDirs must have equal size" }
    requireNotNull(sourceDir) { "sourceDir is not set" }
  }
}
