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

package com.joom.colonist.modular

import com.joom.colonist.modular.feature1.Feature1
import com.joom.colonist.modular.feature2.Feature2
import org.junit.Assert
import org.junit.Test

class FeatureManagerTest {
  @Test
  fun testFeatureManagerIsProcessed() {
    val features = ArrayList<Any>()
    FeatureManager { feature ->
      features += feature
    }

    Assert.assertEquals(2, features.size)
    Assert.assertTrue(features.any { it is Feature1 })
    Assert.assertTrue(features.any { it is Feature2 })
  }
}
