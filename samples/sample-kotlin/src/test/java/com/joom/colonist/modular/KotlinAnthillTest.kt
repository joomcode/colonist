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

package com.joom.colonist.modular

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class KotlinAnthillTest {
  private val stream = ByteArrayOutputStream()
  private val out = System.out

  @Before
  fun replaceOutput() {
    System.setOut(PrintStream(stream))
  }

  @After
  fun restoreOutput() {
    System.setOut(out)
  }

  @Test
  fun testAnthillIsProcessed() {
    val ants = arrayOf("Bala", "Chip", "Muffy", "Weaver", "Z")
    Anthill()

    val output = stream.toByteArray().toString(Charsets.UTF_8)
    val strings = output.split('\n')
    for (ant in ants) {
      val prefix = "Ant $ant "
      assertTrue("${prefix}is expected but not found in output:\n$output", strings.any { it.startsWith(prefix) })
    }
  }
}

