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

import com.joom.colonist.AcceptSettlersAndForget
import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colony
import com.joom.colonist.OnAcceptSettler
import com.joom.colonist.OnProduceSettler
import com.joom.colonist.ProduceSettlersAsClasses
import com.joom.colonist.ProduceSettlersViaCallback
import com.joom.colonist.SelectSettlersByAnnotation
import com.joom.colonist.processor.integration.JvmRuntimeUtil
import com.joom.grip.GripFactory
import com.joom.grip.mirrors.getObjectType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.throwable.shouldHaveMessage
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Test

class ColonyParserTest {

  private val grip = GripFactory.INSTANCE.create(runtimeWithCurrentLocation())

  private val colonyParser = createColonyParser()
  private val markerParser = createColonyMarkerParser()

  @Test
  fun `colony with multiple produce methods raises an error`() {
    shouldThrow<IllegalArgumentException> {
      colonyParser.parseColony(
        colonyType = getObjectType<MultipleProduceCallbacksColony>(),
        colonyMarker = markerParser.parseColonyMarker(getObjectType<ProduceColony>())
      )
    }.shouldHaveMessage(
      "Class com.joom.colonist.processor.analysis.ColonyParserTest\$MultipleProduceCallbacksColony contains multiple " +
          "methods annotated with @com.joom.colonist.OnProduceSettler} for colony " +
          "@com.joom.colonist.processor.analysis.ColonyParserTest\$ProduceColony:\n" +
          "  firstCallback\n" +
          "  secondCallback"
    )
  }

  @Test
  fun `colony with multiple arguments produce callback raises an error`() {
    shouldThrow<IllegalArgumentException> {
      colonyParser.parseColony(
        colonyType = getObjectType<MultipleArgumentsProduceCallbackColony>(),
        colonyMarker = markerParser.parseColonyMarker(getObjectType<ProduceColony>())
      )
    }.shouldHaveMessage(
      "Callback method callback in class com.joom.colonist.processor.analysis.ColonyParserTest\$MultipleArgumentsProduceCallbackColony " +
          "must have a single argument for a settler"
    )
  }

  @Test
  fun `colony with multiple accept methods raises an error`() {
    shouldThrow<IllegalArgumentException> {
      colonyParser.parseColony(
        colonyType = getObjectType<MultipleAcceptCallbacksColony>(),
        colonyMarker = markerParser.parseColonyMarker(getObjectType<AcceptColony>())
      )
    }.shouldHaveMessage(
      "Class com.joom.colonist.processor.analysis.ColonyParserTest\$MultipleAcceptCallbacksColony contains multiple " +
          "methods annotated with @com.joom.colonist.OnAcceptSettler} for colony " +
          "@com.joom.colonist.processor.analysis.ColonyParserTest\$AcceptColony:\n" +
          "  firstCallback\n" +
          "  secondCallback"
    )
  }

  @Test
  fun `colony with multiple arguments accept callback raises an error`() {
    shouldThrow<IllegalArgumentException> {
      colonyParser.parseColony(
        colonyType = getObjectType<MultipleArgumentsAcceptCallbackColony>(),
        colonyMarker = markerParser.parseColonyMarker(getObjectType<AcceptColony>())
      )
    }.shouldHaveMessage(
      "Callback method callback in class com.joom.colonist.processor.analysis.ColonyParserTest\$MultipleArgumentsAcceptCallbackColony " +
          "must have a single argument for a settler"
    )
  }

  private fun createColonyParser(): ColonyParser {
    return ColonyParserImpl(grip)
  }

  private fun createColonyMarkerParser(): ColonyMarkerParser {
    return ColonyMarkerParserImpl(grip, SettlerSelectorParserImpl, SettlerProducerParserImpl, SettlerAcceptorParserImpl)
  }

  private fun runtimeWithCurrentLocation(): Iterable<Path> {
    return JvmRuntimeUtil.computeRuntimeClasspath() + listOf(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()))
  }

  annotation class Annotation

  @Colony
  @SelectSettlersByAnnotation(Annotation::class)
  @AcceptSettlersAndForget
  @ProduceSettlersViaCallback
  annotation class ProduceColony

  @Suppress("UNUSED_PARAMETER")
  @ProduceColony
  class MultipleProduceCallbacksColony {

    @OnProduceSettler(colonyAnnotation = ProduceColony::class)
    fun firstCallback(clazz: Class<*>): Any {
      return Unit
    }

    @OnProduceSettler(colonyAnnotation = ProduceColony::class)
    fun secondCallback(clazz: Class<*>): Any {
      return Unit
    }
  }

  @Suppress("UNUSED_PARAMETER")
  @ProduceColony
  class MultipleArgumentsProduceCallbackColony {
    @OnProduceSettler(colonyAnnotation = ProduceColony::class)
    fun callback(first: Class<*>, second: Class<*>, third: Class<*>): Any {
      return Unit
    }
  }

  @Colony
  @SelectSettlersByAnnotation(Annotation::class)
  @AcceptSettlersViaCallback
  @ProduceSettlersAsClasses
  annotation class AcceptColony

  @Suppress("UNUSED_PARAMETER")
  @AcceptColony
  class MultipleAcceptCallbacksColony {

    @OnAcceptSettler(colonyAnnotation = AcceptColony::class)
    fun firstCallback(clazz: Class<*>) = Unit

    @OnAcceptSettler(colonyAnnotation = AcceptColony::class)
    fun secondCallback(clazz: Class<*>) = Unit
  }

  @Suppress("UNUSED_PARAMETER")
  @AcceptColony
  class MultipleArgumentsAcceptCallbackColony {
    @OnAcceptSettler(colonyAnnotation = AcceptColony::class)
    fun callback(first: Class<*>, second: Class<*>, third: Class<*>) = Unit
  }
}
