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

package com.joom.colonist.processor.integration

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.spi.FileSystemProvider
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.streams.toList

object JvmRuntimeUtil {

  val JAVA_CLASS_PATH = "${System.getProperty("java.class.path")}${File.pathSeparator}${System.getProperty("java.home")}"

  fun computeRuntimeClasspath(): List<Path> {
    return JAVA_CLASS_PATH.convertToPathList().filterDirectoriesAndJars() + jrtClassesMaybe()
  }

  private fun jrtClassesMaybe(): List<Path> {
    val installedFilesystemSets = FileSystemProvider.installedProviders().map { it.scheme }.toSet()
    return if (installedFilesystemSets.contains("jrt")) {
      listOf(Paths.get(URI.create("jrt:/")).resolve("/modules/java.base"))
    } else {
      emptyList()
    }
  }

  private fun String.convertToPathList(): List<Path> {
    return this.split(File.pathSeparator)
      .map(Paths::get)
      .toMutableList()
  }

  private fun List<Path>.filterDirectoriesAndJars(): List<Path> {
    val result = mutableListOf<Path>()
    for (file in this) {
      when {
        file.isDirectory() -> result.addAll(file.allWithJarExtensions())
        file.exists() -> result.add(file)
      }
    }
    return result
  }

  private fun Path.allWithJarExtensions(): List<Path> {
    return getFilesThat(this) {
      it.extension.endsWith("jar", ignoreCase = true)
    }
  }

  private fun getFilesThat(path: Path, filter: (Path) -> Boolean): List<Path> {
    Files.walk(path).use { paths ->
      return paths
        .filter(filter)
        .toList()
    }
  }
}
