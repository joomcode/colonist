package com.joom.colonist.processor.analysis

import com.joom.colonist.AcceptSettlersAndForget
import com.joom.colonist.ProduceSettlersViaConstructor
import com.joom.colonist.processor.integration.JvmRuntimeUtil
import com.joom.colonist.processor.model.SettlerAcceptor
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.grip.GripFactory
import com.joom.grip.mirrors.getObjectType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Test

class SettlerParserTest {

  private val grip = GripFactory.INSTANCE.create(runtimeWithCurrentLocation())
  private val parser = SettlerParserImpl(grip, SettlerProducerParserImpl, SettlerAcceptorParserImpl)

  @Test
  fun `parseSettler returns settler without overridden producer and acceptors`() {
    val settler = parser.parseSettler(getObjectType<SettlerWithoutOverrides>())

    settler.overriddenSettlerProducer.shouldBeNull()
    settler.overriddenSettlerAcceptor.shouldBeNull()
  }

  @Test
  fun `parseSettler returns annotated settler with overridden settler producer settler`() {
    val settler = parser.parseSettler(getObjectType<OverriddenProducerSettler>())

    settler.overriddenSettlerProducer.shouldBeInstanceOf<SettlerProducer.Constructor>()
  }

  @Test
  fun `parseSettler returns annotated settler with overridden settler acceptor settler is annotated with`() {
    val settler = parser.parseSettler(getObjectType<OverriddenAcceptorSettler>())

    settler.overriddenSettlerAcceptor.shouldBeInstanceOf<SettlerAcceptor.None>()
  }

  @Test
  fun `parseSettler returns kotlin object settler`() {
    val settler = parser.parseSettler(getObjectType<KotlinObjectSettler>())

    settler.isKotlinObject shouldBe true
  }

  private fun runtimeWithCurrentLocation(): Iterable<Path> {
    return JvmRuntimeUtil.computeRuntimeClasspath() + listOf(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()))
  }

  @ProduceSettlersViaConstructor
  private class OverriddenProducerSettler

  @AcceptSettlersAndForget
  private class OverriddenAcceptorSettler

  private class SettlerWithoutOverrides

  private object KotlinObjectSettler
}
