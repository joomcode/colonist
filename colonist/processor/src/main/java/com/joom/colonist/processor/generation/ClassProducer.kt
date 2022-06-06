/*
 * Copyright 2022 SIA Joom
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

package com.joom.colonist.processor.generation

import com.joom.colonist.processor.ErrorReporter
import com.joom.colonist.processor.logging.getLogger
import com.joom.grip.io.FileSink
import java.io.IOException

class ClassProducer(
  private val fileSink: FileSink,
  private val errorReporter: ErrorReporter
) {

  private val logger = getLogger()

  fun produceClass(internalName: String, classData: ByteArray) {
    logger.debug("Producing class {}", internalName)
    val classFileName = "$internalName.class"
    try {
      fileSink.createFile(classFileName, classData)
    } catch (exception: IOException) {
      errorReporter.reportError("Failed to produce class with ${classData.size} bytes: $classFileName", exception)
    }
  }
}
