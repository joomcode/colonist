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

import java.nio.file.Path
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MultipleSinkOutputTest {

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @Test
  fun `getFileSink returns same instance per input`() {
    val firstInput = temporaryFolder.newFile().toPath()
    val secondInput = temporaryFolder.newFile().toPath()

    createOutput(
      inputs = listOf(firstInput, secondInput),
    ).use { output ->
      val firstSink = output.getFileSink(firstInput)
      val secondSink = output.getFileSink(secondInput)

      Assert.assertNotSame(firstSink, secondSink)
      Assert.assertSame(firstSink, output.getFileSink(firstInput))
      Assert.assertSame(secondSink, output.getFileSink(secondInput))
    }
  }

  @Test
  fun `getGenerationSink returns the same instance`() {
    val path = temporaryFolder.newFolder().toPath()
    SingleSinkOutput(path).use { output ->
      val inputSink = output.getFileSink(temporaryFolder.newFile().toPath())
      val generationSink = output.getGenerationSink()

      Assert.assertSame(inputSink, generationSink)
    }
  }

  private fun createOutput(inputs: List<Path>): MultipleSinkOutput {
    return MultipleSinkOutput(
      inputs = inputs,
      outputs = List(inputs.size) { temporaryFolder.newFolder().toPath() },
      generationPath = temporaryFolder.newFolder().toPath()
    )
  }
}
