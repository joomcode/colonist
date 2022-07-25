package com.joom.colonist.processor.integration

import com.joom.colonist.processor.ErrorReporter
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

fun ErrorReporter.shouldContainErrorMessage(message: String) {
  getErrors().shouldHaveSize(1)
  getErrors().first().message shouldBe message
}

fun ErrorReporter.shouldNotHaveErrors() {
  getErrors().shouldBeEmpty()
}
