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
