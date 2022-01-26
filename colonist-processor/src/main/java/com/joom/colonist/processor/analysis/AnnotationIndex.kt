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

import com.joom.grip.mirrors.Type

interface AnnotationIndex {
  fun findClassesWithAnnotation(annotationType: Type.Object): Collection<Type.Object>

  class Builder {
    private val annotationTypeToTypeCollectionMap = hashMapOf<Type.Object, MutableCollection<Type.Object>>()

    fun addAnnotatedType(type: Type.Object, annotationType: Type.Object) = apply {
      val types = annotationTypeToTypeCollectionMap.getOrPut(annotationType, ::LinkedHashSet)
      types += type
    }

    fun build(): AnnotationIndex {
      return SimpleAnnotationIndex(annotationTypeToTypeCollectionMap.toMap())
    }
  }

  companion object {
    inline fun build(body: Builder.() -> Unit): AnnotationIndex {
      return Builder().also(body).build()
    }
  }
}
