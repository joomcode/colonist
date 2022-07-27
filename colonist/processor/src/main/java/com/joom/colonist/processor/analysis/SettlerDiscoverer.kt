/*
 * Copyright 2019 SIA Joom
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
import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerProducer
import com.joom.colonist.processor.model.SettlerSelector
import com.joom.grip.Grip
import com.joom.grip.annotatedWith
import com.joom.grip.classes
import com.joom.grip.mirrors.ClassMirror
import com.joom.grip.mirrors.Type
import com.joom.grip.mirrors.isAbstract
import com.joom.grip.mirrors.isInterface
import com.joom.grip.mirrors.isPublic
import java.nio.file.Path

interface SettlerDiscoverer {
  fun discoverSettlers(settlerSelector: SettlerSelector, settlerProducer: SettlerProducer): Collection<Settler>
}

class SettlerDiscovererImpl(
  private val grip: Grip,
  private val inputs: List<Path>,
  private val settlerParser: SettlerParser,
  private val errorReporter: ErrorReporter,
) : SettlerDiscoverer {

  override fun discoverSettlers(settlerSelector: SettlerSelector, settlerProducer: SettlerProducer): Collection<Settler> {
    return when (settlerSelector) {
      is SettlerSelector.Annotation -> selectSettlersByAnnotation(settlerSelector)
      is SettlerSelector.SuperType -> selectSettlersBySuperType(settlerSelector)
      is SettlerSelector.Registered -> TODO("Registered selectors aren't supported yet")
    }.let { filterProducibleSettlers(it, settlerSelector, settlerProducer) }
  }

  private fun selectSettlersByAnnotation(selector: SettlerSelector.Annotation): Collection<Settler> {
    val query = grip select classes from inputs where annotatedWith(selector.annotationType)
    return query.execute().classes.mapNotNull {
      if (!it.isPublic) {
        errorReporter.reportError("Settler selected by ${selector.describe()} should be a public class [${it.type.className}]")
        return@mapNotNull null
      }

      settlerParser.parseSettler(it.type)
    }
  }

  private fun selectSettlersBySuperType(selector: SettlerSelector.SuperType): Collection<Settler> {
    val query = grip select classes from inputs where isSubtypeOf(selector.superType)
    return query.execute().classes.mapNotNull {
      if (!it.isPublic) {
        errorReporter.reportError("Settler selected by ${selector.describe()} should be a public class [${it.type.className}]")
        return@mapNotNull null
      }

      settlerParser.parseSettler(it.type)
    }
  }

  private fun filterProducibleSettlers(settlers: Collection<Settler>, selector: SettlerSelector, producer: SettlerProducer): Collection<Settler> {
    return settlers.filter { canBeProducedByProducer(it, selector, it.overriddenSettlerProducer ?: producer) }
  }

  private fun canBeProducedByProducer(settler: Settler, selector: SettlerSelector, producer: SettlerProducer): Boolean {
    return when (producer) {
      SettlerProducer.Callback,
      SettlerProducer.Class -> true
      SettlerProducer.Constructor -> {
        val mirror = grip.classRegistry.getClassMirror(settler.type)

        if (mirror.isInterface || mirror.isAbstract) {
          return false
        }

        if (mirror.constructors.find { it.isPublic && it.parameters.isEmpty() } == null && !settler.isKotlinObject) {
          errorReporter.reportError(
            "Settler selected by ${selector.describe()} and produced " +
                "via constructor does not have public default constructor [${settler.type.className}]"
          )
          return false
        }

        return true
      }
    }
  }

  private fun SettlerSelector.describe(): String {
    return when (this) {
      is SettlerSelector.Annotation -> "@${annotationType.className}"
      SettlerSelector.Registered -> "registered selector"
      is SettlerSelector.SuperType -> superType.className
    }
  }

  private fun isSubtypeOf(baseType: Type.Object): (Grip, ClassMirror) -> Boolean {
    if (baseType == Types.OBJECT_TYPE) {
      return { _, _ -> true }
    }

    val resolver = SubtypeResolver(grip, baseType)
    return { _, mirror -> resolver.isSubtype(mirror) }
  }

  private class SubtypeResolver(
    private val grip: Grip,
    private val baseType: Type.Object
  ) {

    private val isInterface = grip.classRegistry.getClassMirror(baseType).isInterface

    private val cache = HashMap<Type.Object, Boolean>().apply {
      put(baseType, true)
    }

    fun isSubtype(mirror: ClassMirror): Boolean {
      return when {
        isInterface -> isSubtypeOfInterface(mirror.type)
        !mirror.isInterface -> isSubtypeOfClass(mirror.type)
        else -> false
      }
    }

    private fun isSubtypeOfClass(type: Type.Object): Boolean {
      return cache.getOrPut(type) {
        if (type == baseType) {
          true
        } else {
          val mirror = grip.classRegistry.getClassMirror(type)
          mirror.superType?.let { isSubtypeOfClass(it) } ?: false
        }
      }
    }

    private fun isSubtypeOfInterface(type: Type.Object): Boolean {
      return cache.getOrPut(type) {
        if (type == baseType) {
          true
        } else {
          val mirror = grip.classRegistry.getClassMirror(type)
          if (mirror.interfaces.any { isSubtypeOfInterface(it) }) {
            true
          } else {
            mirror.superType?.let { isSubtypeOfInterface(it) } ?: false
          }
        }
      }
    }
  }
}
