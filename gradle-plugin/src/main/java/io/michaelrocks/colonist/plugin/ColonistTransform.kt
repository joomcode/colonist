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

package io.michaelrocks.colonist.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import io.michaelrocks.colonist.processor.ColonistParameters
import io.michaelrocks.colonist.processor.ColonistProcessor
import io.michaelrocks.colonist.processor.logging.getLogger
import org.gradle.api.Project
import java.io.IOException
import java.util.EnumSet

class ColonistTransform(private val project: Project) : Transform() {
  private val logger = getLogger()

  override fun transform(invocation: TransformInvocation) {
    if (!invocation.isIncremental) {
      invocation.outputProvider.deleteAll()
    }

    val inputs = invocation.inputs.flatMap { it.jarInputs + it.directoryInputs }
    val outputs = inputs.map { input ->
      val format = if (input is JarInput) Format.JAR else Format.DIRECTORY
      invocation.outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, format)
    }

    val parameters = ColonistParameters(
      inputs = inputs.map { it.file },
      outputs = outputs,
      classpath = invocation.referencedInputs.flatMap { input ->
        input.jarInputs.map { it.file } + input.directoryInputs.map { it.file }
      },
      bootClasspath = project.android.bootClasspath,
      projectName = invocation.context.variantName,
      debug = logger.isDebugEnabled,
      info = logger.isInfoEnabled
    )
    logger.info("Starting Colonist processor: {}", parameters)
    try {
      ColonistProcessor.process(parameters)
      logger.info("Colonist finished processing")
    } catch (exception: IOException) {
      logger.error("Colonist failed", exception)
      throw exception
    } catch (exception: Exception) {
      logger.error("Colonist failed", exception)
      throw TransformException(exception)
    }
  }

  override fun getName(): String {
    return "colonist"
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return EnumSet.of(
      QualifiedContent.Scope.PROJECT,
      QualifiedContent.Scope.SUB_PROJECTS
    )
  }

  override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
    return EnumSet.of(
      QualifiedContent.Scope.EXTERNAL_LIBRARIES,
      QualifiedContent.Scope.PROVIDED_ONLY
    )
  }

  override fun isIncremental(): Boolean {
    return false
  }
}
