package com.joom.colonist.processor.analysis.settlerdiscoverer.kotlin_object_by_supertype

import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colony
import com.joom.colonist.OnAcceptSettler
import com.joom.colonist.ProduceSettlersViaConstructor
import com.joom.colonist.SelectSettlersBySuperType

object ObjectSettler : TestSettler

interface TestSettler

@Colony
@SelectSettlersBySuperType(TestSettler::class)
@ProduceSettlersViaConstructor
@AcceptSettlersViaCallback
annotation class TestColony

@TestColony
class TestColonyImpl {

  @OnAcceptSettler(colonyAnnotation = TestColony::class)
  fun onAcceptSettler(any: Any) {

  }
}
