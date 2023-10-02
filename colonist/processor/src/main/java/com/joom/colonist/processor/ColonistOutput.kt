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

import com.joom.colonist.processor.commons.associateByIndexedTo
import com.joom.colonist.processor.commons.closeQuietly
import com.joom.grip.io.FileSink
import java.io.Closeable
import java.nio.file.Path

interface ColonistOutput : Closeable {
  fun getFileSink(input: Path): FileSink
  fun getGenerationSink(): FileSink
}

internal class MultipleSinkOutput(private val inputs: List<Path>, private val outputs: List<Path>, generationPath: Path) : ColonistOutput {
  init {
    require(inputs.size == outputs.size) { "Inputs size should be equal to outputs size" }
  }

  private val generationSink = createFileSink(generationPath)
  private val sinksByInputs = HashMap<Path, FileSink>().also {
    inputs.associateByIndexedTo(it, { _, input -> input }, { index, _ -> createFileSink(outputs[index]) })
  }

  override fun getFileSink(input: Path): FileSink {
    return sinksByInputs[input] ?: error("Failed to create file sink for input $input")
  }

  override fun getGenerationSink(): FileSink {
    return generationSink
  }

  override fun close() {
    generationSink.closeQuietly()
    sinksByInputs.forEach { (_, sink) ->
      sink.closeQuietly()
    }
  }
}

internal class SingleSinkOutput(path: Path) : ColonistOutput {
  private var sink = createFileSink(path)

  override fun getFileSink(input: Path): FileSink {
    return sink
  }

  override fun getGenerationSink(): FileSink {
    return sink
  }

  override fun close() {
    sink.closeQuietly()
  }
}
