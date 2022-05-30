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

package com.joom.colonist.plugin

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.plugins.JavaPlugin

class AndroidColonistPlugin : BaseColonistPlugin() {
  override fun apply(project: Project) {
    super.apply(project)

    if (!project.hasAndroid) {
      throw GradleException("Colonist plugin must be applied *AFTER* Android plugin")
    }

    addDependencies(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
    if (AndroidGradlePluginVersionProvider.getCurrentPluginVersion() >= VARIANT_API_REQUIRED_VERSION) {
      logger.info("Registering colonist with variant API")

      registerColonistWithVariantApi()
    } else {
      logger.info("Registering colonist with transform API")

      registerColonistWithTransform()
    }
  }

  private fun registerColonistWithVariantApi() {
    project.applicationAndroidComponents?.apply {
      onVariants { variant ->
        variant.registerColonistTask(discoverSettlers = true)
      }
    }

    project.libraryAndroidComponents?.apply {
      onVariants { variant ->
        variant.registerColonistTask(discoverSettlers = false)
      }
    }
  }

  @Suppress("UnstableApiUsage")
  private fun Variant.registerColonistTask(discoverSettlers: Boolean) {
    val taskProvider = project.registerTask<ColonistTransformClassesTask>("${name}ColonistTransformClasses")
    artifacts.use(taskProvider)
      .wiredWith(ColonistTransformClassesTask::inputClasses, ColonistTransformClassesTask::output)
      .toTransform(MultipleArtifact.ALL_CLASSES_DIRS)

    taskProvider.configure { task ->
      task.discoverSettlers = discoverSettlers
      task.classpath.setFrom(
        project.configurations.getByName("${name}RuntimeClasspath")
          .incoming
          .artifactView {
            it.attributes { attributes ->
              attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, AndroidArtifacts.ArtifactType.CLASSES_JAR.type)
            }
          }
          .artifacts
          .artifactFiles
      )
      task.bootClasspath.setFrom(project.android.bootClasspath)
    }
  }

  private fun registerColonistWithTransform() {
    val extension = project.extensions.create("colonist", AndroidColonistExtension::class.java)

    @Suppress("DEPRECATION")
    project.android.registerTransform(ColonistTransform(extension))

    project.afterEvaluate {
      extension.bootClasspath = project.android.bootClasspath
    }
  }

  private companion object {
    private val VARIANT_API_REQUIRED_VERSION = AndroidPluginVersion(major = 7, minor = 2, micro = 0)
  }
}
