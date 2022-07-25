package com.joom.colonist.processor.analysis.settlerdiscoverer.public_by_supertype

import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colony
import com.joom.colonist.OnAcceptSettler
import com.joom.colonist.ProduceSettlersAsClasses
import com.joom.colonist.SelectSettlersBySuperType

internal class InternalSettler : TestSettler

class PublicSettler : TestSettler

abstract class AbstractPublicClass {

  protected class ProtectedClass : TestSettler
}

interface TestSettler

@Colony
@SelectSettlersBySuperType(TestSettler::class)
@ProduceSettlersAsClasses
@AcceptSettlersViaCallback
annotation class TestColony

@TestColony
class TestColonyImpl {

  @OnAcceptSettler(colonyAnnotation = TestColony::class)
  fun onAcceptSettler(clazz: Class<*>) {

  }
}

