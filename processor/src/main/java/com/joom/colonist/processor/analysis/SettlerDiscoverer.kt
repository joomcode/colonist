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

package com.joom.colonist.processor.analysis

import com.joom.colonist.processor.commons.Types
import com.joom.colonist.processor.model.Settler
import com.joom.colonist.processor.model.SettlerSelector
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.classpath
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.isInterface

interface SettlerDiscoverer {
  fun discoverSettlers(settlerSelector: SettlerSelector): Collection<Settler>
}

class SettlerDiscovererImpl(
  private val grip: Grip,
  private val settlerParser: SettlerParser
) : SettlerDiscoverer {

  override fun discoverSettlers(settlerSelector: SettlerSelector): Collection<Settler> {
    return when (settlerSelector) {
      is SettlerSelector.Annotation -> selectSettlersByAnnotation(settlerSelector.annotationType)
      is SettlerSelector.SuperType -> selectSettlersBySuperType(settlerSelector.superType)
      is SettlerSelector.Registered -> TODO("Registered selectors aren't supported yet")
    }
  }

  private fun selectSettlersByAnnotation(annotationType: Type.Object): Collection<Settler> {
    val query = grip select classes from classpath where annotatedWith(annotationType)
    return query.execute().classes.map {
      settlerParser.parseSettler(it.type)
    }
  }

  private fun selectSettlersBySuperType(superType: Type.Object): Collection<Settler> {
    val query = grip select classes from classpath where isSubtypeOf(superType)
    return query.execute().classes.map {
      settlerParser.parseSettler(it.type)
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
