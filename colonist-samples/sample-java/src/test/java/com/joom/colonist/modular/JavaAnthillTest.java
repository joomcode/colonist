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

package com.joom.colonist.modular;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class JavaAnthillTest {
  private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
  private final PrintStream out = System.out;

  @Before
  public void replaceOutput() {
    System.setOut(new PrintStream(stream));
  }

  @After
  public void restoreOutput() {
    System.setOut(out);
  }

  @Test
  public void testAnthillIsProcessed() {
    final String[] ants = { "Bala", "Chip", "Muffy", "Weaver", "Z", "Queen" };
    new Anthill();

    final String output = new String(stream.toByteArray(), Charset.forName("UTF-8"));
    final List<String> strings = Arrays.asList(output.split("\n"));
    for (final String ant : ants) {
      final String prefix = "Ant " + ant + " ";
      assertTrue(prefix + "is expected but not found in output:\n" + output, strings.stream().anyMatch(string -> string.startsWith(prefix)));
    }
  }
}
