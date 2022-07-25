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

package com.joom.colonist.processor.analysis

import com.joom.colonist.processor.integration.IntegrationTestRule
import org.junit.Rule
import org.junit.Test

class SettlerDiscovererTest {

  @get:Rule
  val rule = IntegrationTestRule("com.joom.colonist.processor.analysis.settlerdiscoverer")

  @Test
  fun `public settlers selected by annotation do not raise an error`() {
    rule.assertValidProject("public_by_annotation")
  }

  @Test
  fun `public settlers selected by super type do not raise an error`() {
    rule.assertValidProject("public_by_supertype")
  }

  @Test
  fun `private settlers selected by annotation raise an error`() {
    rule.assertInvalidProject(
      "private_by_annotation", "Settler selected by " +
          "@com.joom.colonist.processor.analysis.settlerdiscoverer.private_by_annotation.TestSettler should be a " +
          "public class [com.joom.colonist.processor.analysis.settlerdiscoverer.private_by_annotation.PrivateSettler]"
    )
  }

  @Test
  fun `private settlers selected by super type raise an error`() {
    rule.assertInvalidProject(
      "private_by_supertype", "Settler selected by " +
          "com.joom.colonist.processor.analysis.settlerdiscoverer.private_by_supertype.TestSettler should be a " +
          "public class [com.joom.colonist.processor.analysis.settlerdiscoverer.private_by_supertype.PrivateSettler]"
    )
  }

  @Test
  fun `abstract settlers selected by annotation and produced via constructor do not raise an error`() {
    rule.assertValidProject("abstract_by_annotation")
  }

  @Test
  fun `settlers selected by annotation without no arg constructor produced via constructor raise an error`() {
    rule.assertInvalidProject(
      "non_instantiable_by_annotation", "Settler selected by " +
          "@com.joom.colonist.processor.analysis.settlerdiscoverer.non_instantiable_by_annotation.TestSettler and produced via constructor " +
          "does not have public default constructor " +
          "[com.joom.colonist.processor.analysis.settlerdiscoverer.non_instantiable_by_annotation.PrivateConstructorSettler]"
    )
  }

  @Test
  fun `abstract settlers selected by super type and produced via constructor do not raise an error`() {
    rule.assertValidProject("abstract_by_supertype")
  }

  @Test
  fun `settlers selected by super type without no arg constructor produced via constructor raise an error`() {
    rule.assertInvalidProject(
      "non_instantiable_by_supertype", "Settler selected by " +
          "com.joom.colonist.processor.analysis.settlerdiscoverer.non_instantiable_by_supertype.TestSettler and produced via constructor " +
          "does not have public default constructor " +
          "[com.joom.colonist.processor.analysis.settlerdiscoverer.non_instantiable_by_supertype.PrivateConstructorSettler]"
    )
  }
}
