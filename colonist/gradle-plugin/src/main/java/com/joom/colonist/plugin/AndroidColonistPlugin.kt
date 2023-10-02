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
import com.android.build.api.variant.Component
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
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

    val componentsExtension = project.androidComponents

    when {
      componentsExtension != null && componentsExtension.pluginVersion >= SCOPED_ARTIFACTS_VERSION -> {
        logger.info("Registering colonist with scoped artifacts API")

        configureTransformWithArtifactsApi(ScopedArtifactsRegistrar)
      }

      componentsExtension != null && componentsExtension.pluginVersion >= ALL_CLASSES_TRANSFORM_API_VERSION -> {
        logger.info("Registering colonist with all classes transform API")

        configureTransformWithArtifactsApi(AllClassesTransformRegistrar)
      }

      else -> {
        logger.info("Registering colonist with transform API")

        configureTransform()
      }
    }
  }

  private fun configureTransformWithArtifactsApi(registrar: TransformTaskRegistrar) {
    val extension = project.extensions.create("colonist", AndroidVariantColonistExtension::class.java)

    project.applicationAndroidComponents?.apply {
      onVariants(selector().all()) { variant ->
        variant.registerColonistTask(registrar, discoverSettlers = true, processTest = extension.processTest)
      }
    }

    project.libraryAndroidComponents?.apply {
      onVariants(selector().all()) { variant ->
        variant.registerColonistTask(registrar, discoverSettlers = false, processTest = extension.processTest)
      }
    }
  }

  private fun <T> T.registerColonistTask(
    registrar: TransformTaskRegistrar,
    discoverSettlers: Boolean,
    processTest: Boolean
  ) where T : Variant, T : HasAndroidTest {
    val runtimeClasspath = runtimeClasspathConfiguration()

    registerColonistTask(
      registrar = registrar,
      discoverSettlers = discoverSettlers,
      classpathProvider = classpathProvider(runtimeClasspath),
      discoveryClasspathProvider = discoveryClasspathProvider(runtimeClasspath),
    )

    androidTest?.let { androidTest ->
      val androidTestRuntimeClasspath = androidTest.runtimeClasspathConfiguration()

      androidTest.registerColonistTask(
        registrar = registrar,
        discoverSettlers = discoverSettlers,
        classpathProvider = classpathProvider(androidTestRuntimeClasspath),
        discoveryClasspathProvider = discoveryClasspathProvider(androidTestRuntimeClasspath) - discoveryClasspathProvider(runtimeClasspath)
      )
    }

    unitTest.takeIf { processTest }?.let { unitTest ->
      val unitTestRuntimeClasspath = unitTest.runtimeClasspathConfiguration()

      unitTest.registerColonistTask(
        registrar = registrar,
        discoverSettlers = discoverSettlers,
        classpathProvider = classpathProvider(runtimeClasspath),
        discoveryClasspathProvider = discoveryClasspathProvider(unitTestRuntimeClasspath)
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
    registrar: TransformTaskRegistrar,
    discoverSettlers: Boolean,
    classpathProvider: Provider<FileCollection>,
    discoveryClasspathProvider: Provider<FileCollection>,
  ) {
    val taskProvider = project.registerTask<ColonistTransformClassesTask>("colonistTransformClasses${name.replaceFirstChar { it.uppercaseChar() }}")
    registrar.register(this, taskProvider)

    taskProvider.configure { task ->
      task.discoverSettlers = discoverSettlers
      task.discoveryClasspath.setFrom(discoveryClasspathProvider)
      task.classpath.setFrom(classpathProvider)
      task.bootClasspath.setFrom(project.android.bootClasspath)
    }
  }

  @Suppress("DEPRECATION")
  private fun configureTransform() {
    if (project.android !is AppExtension) {
      return
    }

    val extension = project.extensions.create("colonist", AndroidColonistExtension::class.java)

    @Suppress("DEPRECATION")
    project.android.registerTransform(ColonistTransform(extension))

    project.afterEvaluate {
      extension.bootClasspath = project.android.bootClasspath
    }
  }

  private companion object {
    private val SCOPED_ARTIFACTS_VERSION = AndroidPluginVersion(major = 7, minor = 4, micro = 0)
    private val ALL_CLASSES_TRANSFORM_API_VERSION = AndroidPluginVersion(major = 7, minor = 1, micro = 0)
  }
}
