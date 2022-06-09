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
import com.android.build.api.variant.Component
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.Variant
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider

class AndroidColonistPlugin : BaseColonistPlugin() {
  override fun apply(project: Project) {
    super.apply(project)

    if (!project.hasAndroid) {
      throw GradleException("Colonist plugin must be applied *AFTER* Android plugin")
    }

    addDependencies(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

    val androidComponents = project.androidComponents
    if (androidComponents != null && androidComponents.pluginVersion >= VARIANT_API_REQUIRED_VERSION) {
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

  private fun <T> T.registerColonistTask(discoverSettlers: Boolean) where T : Variant, T : HasAndroidTest {
    val runtimeClasspath = runtimeClasspathConfiguration()

    registerColonistTask(
      discoverSettlers = discoverSettlers,
      classpathProvider = classpathProvider(runtimeClasspath),
      discoveryClasspathProvider = discoveryClasspathProvider(runtimeClasspath),
    )

    androidTest?.let { androidTest ->
      val androidTestRuntimeClasspath = androidTest.runtimeClasspathConfiguration()

      androidTest.registerColonistTask(
        discoverSettlers = discoverSettlers,
        classpathProvider = classpathProvider(androidTestRuntimeClasspath),
        discoveryClasspathProvider = discoveryClasspathProvider(androidTestRuntimeClasspath) - discoveryClasspathProvider(runtimeClasspath)
      )
    }
  }

  private fun Component.runtimeClasspathConfiguration(): Provider<Configuration> {
    return project.configurations.named(name + "RuntimeClasspath")
  }

  private fun classpathProvider(configuration: Provider<Configuration>): Provider<FileCollection> {
    return configuration.map { it.incomingJarArtifacts().artifactFiles }
  }

  private fun discoveryClasspathProvider(configuration: Provider<Configuration>): Provider<FileCollection> {
    return configuration.map { it.incomingJarArtifacts { it is ProjectComponentIdentifier }.artifactFiles }
  }

  private operator fun Provider<FileCollection>.minus(other: Provider<FileCollection>): Provider<FileCollection> {
    return zip(other) { first, second -> first - second }
  }

  @Suppress("UnstableApiUsage")
  private fun Component.registerColonistTask(
    discoverSettlers: Boolean,
    classpathProvider: Provider<FileCollection>,
    discoveryClasspathProvider: Provider<FileCollection>,
  ) {
    val taskProvider = project.registerTask<ColonistTransformClassesTask>("colonistTransformClasses${name.replaceFirstChar { it.uppercaseChar() }}")
    artifacts.use(taskProvider)
      .wiredWith(ColonistTransformClassesTask::inputClasses, ColonistTransformClassesTask::output)
      .toTransform(MultipleArtifact.ALL_CLASSES_DIRS)

    taskProvider.configure { task ->
      task.discoverSettlers = discoverSettlers
      task.discoveryClasspath.setFrom(discoveryClasspathProvider)
      task.classpath.setFrom(classpathProvider)
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
    private val VARIANT_API_REQUIRED_VERSION = AndroidPluginVersion(major = 7, minor = 1, micro = 0)
  }
}
