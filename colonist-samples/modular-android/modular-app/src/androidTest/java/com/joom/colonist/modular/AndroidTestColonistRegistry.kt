package com.joom.colonist.modular

import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colonist
import com.joom.colonist.Colony
import com.joom.colonist.OnAcceptSettler
import com.joom.colonist.ProduceSettlersAsClasses
import com.joom.colonist.SelectSettlersByAnnotation

@AndroidTestColony
class AndroidTestColonistRegistry {

  private val settlers = ArrayList<Class<*>>()

  init {
    Colonist.settle(this)
  }

  @OnAcceptSettler(colonyAnnotation = AndroidTestColony::class)
  fun onAcceptSettler(settler: Class<*>) {
    settlers += settler
  }

  fun getRegisteredSettlers(): List<Class<*>> {
    return ArrayList(settlers)
  }
}

@Colony
@SelectSettlersByAnnotation(AndroidTestColonist::class)
@ProduceSettlersAsClasses
@AcceptSettlersViaCallback
annotation class AndroidTestColony

annotation class AndroidTestColonist

@AndroidTestColonist
class FirstTestColonist

@AndroidTestColonist
class SecondTestColonist
