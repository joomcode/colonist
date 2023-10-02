/*
 * Copyright 2023 SIA Joom
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

import com.joom.grip.io.DirectoryFileSink
import com.joom.grip.io.EmptyFileSink
import com.joom.grip.io.FileSink
import com.joom.grip.io.JarFileSink
import java.nio.file.Path
import java.util.Collections
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

internal fun createFileSink(outputFile: Path): FileSink {
  return when (outputFile.sourceType) {
    FileType.EMPTY -> EmptyFileSink
    FileType.DIRECTORY -> DirectoryFileSink(outputFile)
    FileType.JAR -> PatchedJarFileSink(JarFileSink(outputFile)).synchronized()
  }
}

private class PatchedJarFileSink(private val delegate: FileSink) : FileSink by delegate {
  private val directories = Collections.synchronizedSet(HashSet<String>())

  override fun createDirectory(path: String) {
    if (directories.add(path)) {
      delegate.createDirectory(path)
    }
  }
}

private val Path.sourceType: FileType
  get() = when {
    extension.endsWith("jar", ignoreCase = true) -> FileType.JAR
    !exists() || isDirectory() -> FileType.DIRECTORY
    else -> error("Unknown source type for file $this")
  }

private enum class FileType {
  EMPTY,
  DIRECTORY,
  JAR,
}

private fun FileSink.synchronized(): FileSink {
  return if (this !is SynchronizedFileSink) SynchronizedFileSink(this) else this
}

private class SynchronizedFileSink(private val delegate: FileSink) : FileSink by delegate {
  private val lock = Any()
  override fun createFile(path: String, data: ByteArray) {
    synchronized(lock) {
      delegate.createFile(path, data)
    }
  }

  override fun createDirectory(path: String) {
    synchronized(lock) {
      delegate.createDirectory(path)
    }
  }

  override fun close() {
    synchronized(lock) {
      delegate.close()
    }
  }

  override fun flush() {
    synchronized(lock) {
      delegate.flush()
    }
  }
}
