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

import com.joom.colonist.AcceptSettlersViaCallback
import com.joom.colonist.Colony
import com.joom.colonist.ProduceSettlersViaConstructor
import com.joom.colonist.SelectSettlersByAnnotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@Colony
@SelectSettlersByAnnotation(AntSettler::class)
@ProduceSettlersViaConstructor
@AcceptSettlersViaCallback
annotation class AntColony
