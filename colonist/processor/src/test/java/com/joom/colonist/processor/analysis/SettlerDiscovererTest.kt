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

import com.joom.colonist.processor.ErrorReporter
import com.joom.colonist.processor.integration.IntegrationTestRule
import com.joom.colonist.processor.integration.JvmRuntimeUtil
import com.joom.colonist.processor.integration.shouldNotHaveErrors
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.colonist.processor.model.SettlerSelector
import com.joom.grip.GripFactory
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.getObjectTypeByInternalName
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.Rule
import org.junit.Test

class SettlerDiscovererTest {

  @get:Rule
  val rule = IntegrationTestRule(PACKAGE)

  @Test
  fun `public settlers selected by annotation do not raise errors`() {
    rule.assertValidProject("public_by_annotation")
  }

  @Test
  fun `public settlers selected by super type do not raise errors`() {
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

  @Test
  fun `does not return abstract settlers selected by super type and produced via constructor`() {
    val settlers = discoverSettlers(
      sourceCodeDir = "abstract_by_supertype",
      selector = SettlerSelector.SuperType(computeType("abstract_by_supertype", "TestSettler")),
      producer = SettlerProducer.Constructor
    )

    settlers.shouldHaveSingleElement { it.type.className.endsWith("ConcreteSettler") }
  }

  @Test
  fun `returns all settlers selected by super type and produced as classes`() {
    val settlers = discoverSettlers(
      sourceCodeDir = "abstract_by_supertype",
      selector = SettlerSelector.SuperType(computeType("abstract_by_supertype", "TestSettler")),
      producer = SettlerProducer.Class
    )

    settlers.shouldHaveSize(4)
    settlers.shouldExist { it.type.className.endsWith("TestSettler") }
    settlers.shouldExist { it.type.className.endsWith("AbstractSettler") }
    settlers.shouldExist { it.type.className.endsWith("InterfaceSettler") }
    settlers.shouldExist { it.type.className.endsWith("ConcreteSettler") }
  }

  @Test
  fun `returns all settlers selected by super type and produced as callback`() {
    val settlers = discoverSettlers(
      sourceCodeDir = "abstract_by_supertype",
      selector = SettlerSelector.SuperType(computeType("abstract_by_supertype", "TestSettler")),
      producer = SettlerProducer.Callback
    )

    settlers.shouldHaveSize(4)
    settlers.shouldExist { it.type.className.endsWith("TestSettler") }
    settlers.shouldExist { it.type.className.endsWith("AbstractSettler") }
    settlers.shouldExist { it.type.className.endsWith("InterfaceSettler") }
    settlers.shouldExist { it.type.className.endsWith("ConcreteSettler") }
  }

  @Test
  fun `does not return abstract settlers selected by annotation and produced via constructor`() {
    val settlers = discoverSettlers(
      sourceCodeDir = "abstract_by_annotation",
      selector = SettlerSelector.Annotation(computeType("abstract_by_annotation", "TestSettler")),
      producer = SettlerProducer.Constructor
    )

    settlers.shouldHaveSingleElement { it.type.className.endsWith("ConcreteSettler") }
  }

  @Test
  fun `returns all settlers selected by annotation and produced as classes`() {
    val settlers = discoverSettlers(
      sourceCodeDir = "abstract_by_annotation",
      selector = SettlerSelector.Annotation(computeType("abstract_by_annotation", "TestSettler")),
      producer = SettlerProducer.Class
    )

    settlers.shouldHaveSize(3)
    settlers.shouldExist { it.type.className.endsWith("AbstractSettler") }
    settlers.shouldExist { it.type.className.endsWith("InterfaceSettler") }
    settlers.shouldExist { it.type.className.endsWith("ConcreteSettler") }
  }

  @Test
  fun `returns all settlers selected by annotation and produced as callback`() {
    val settlers = discoverSettlers(
      sourceCodeDir = "abstract_by_annotation",
      selector = SettlerSelector.Annotation(computeType("abstract_by_annotation", "TestSettler")),
      producer = SettlerProducer.Callback
    )

    settlers.shouldHaveSize(3)
    settlers.shouldExist { it.type.className.endsWith("AbstractSettler") }
    settlers.shouldExist { it.type.className.endsWith("InterfaceSettler") }
    settlers.shouldExist { it.type.className.endsWith("ConcreteSettler") }
  }

  private fun discoverSettlers(sourceCodeDir: String, selector: SettlerSelector, producer: SettlerProducer): Collection<Settler> {
    val reporter = ErrorReporter()
    val discoverer = createDiscoverer(sourceCodeDir, reporter)
    reporter.shouldNotHaveErrors()

    return discoverer.discoverSettlers(selector, producer)
  }

  private fun computeType(sourceCodeDir: String, name: String): Type.Object {
    val fullClassName = "$PACKAGE.$sourceCodeDir.$name"

    return getObjectTypeByInternalName(fullClassName.replace(".", "/"))
  }

  private fun createDiscoverer(sourceCodeDir: String, errorReporter: ErrorReporter): SettlerDiscoverer {
    val path = rule.compileProject(sourceCodeDir).normalize()
    val grip = GripFactory.INSTANCE.create(listOf(path) + JvmRuntimeUtil.computeRuntimeClasspath())
    val settlerParser = SettlerParserImpl(grip, SettlerProducerParserImpl, SettlerAcceptorParserImpl)

    return SettlerDiscovererImpl(grip, inputs = listOf(path), settlerParser = settlerParser, errorReporter = errorReporter)
  }

  private companion object {
    private const val PACKAGE = "com.joom.colonist.processor.analysis.settlerdiscoverer"
  }
}
