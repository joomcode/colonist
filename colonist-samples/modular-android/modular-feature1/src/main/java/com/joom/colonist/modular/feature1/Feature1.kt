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

package com.joom.colonist.modular.feature1

import android.util.Log
import com.joom.colonist.modular.api.Feature
import com.joom.colonist.modular.api.FeatureSettler

@FeatureSettler
class Feature1 : Feature {

  override fun activate() {
    Log.i("Modular", "Standalone feature activated")
  }
}
