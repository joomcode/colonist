package com.joom.colonist.processor.analysis.settlerdiscoverer.privatesettler

import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colony
import com.joom.colonist.OnAcceptSettler
import com.joom.colonist.ProduceSettlersAsClasses
import com.joom.colonist.SelectSettlersByAnnotation

@TestSettler
private class PrivateSettler

annotation class TestSettler

@Colony
@SelectSettlersByAnnotation(TestSettler::class)
@ProduceSettlersAsClasses
@AcceptSettlersViaCallback
annotation class TestColony

@TestColony
class TestColonyImpl {

  @OnAcceptSettler(colonyAnnotation = TestColony::class)
  fun onAcceptSettler(clazz: Class<*>) {

  }
}
