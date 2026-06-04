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
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Component
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.ScopedArtifacts
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

    val androidComponents = project.androidComponents
      ?: throw GradleException(
        "Colonist Android plugin requires Android Gradle Plugin $MIN_AGP_VERSION or newer " +
          "(androidComponents extension is missing)"
      )

    if (androidComponents.pluginVersion < MIN_AGP_VERSION) {
      throw GradleException(
        "Colonist Android plugin requires Android Gradle Plugin $MIN_AGP_VERSION or newer, " +
          "but ${androidComponents.pluginVersion} is used"
      )
    }

    addDependencies(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

    val extension = project.extensions.create("colonist", AndroidVariantColonistExtension::class.java)

    configureVariants(
      components = project.applicationAndroidComponents,
      extension = extension,
      discoverSettlers = true,
    )

    configureVariants(
      components = project.libraryAndroidComponents,
      extension = extension,
      discoverSettlers = false,
    )
  }

  private fun configureVariants(
    components: AndroidComponentsExtension<*, *, *>?,
    extension: AndroidVariantColonistExtension,
    discoverSettlers: Boolean,
  ) {
    components?.onVariants(components.selector().all()) { variant ->
      variant.registerColonistTasks(
        extension = extension,
        discoverSettlers = discoverSettlers,
      )
    }
  }

  private fun Variant.registerColonistTasks(
    extension: AndroidVariantColonistExtension,
    discoverSettlers: Boolean,
  ) {
    val runtimeClasspath = runtimeClasspathConfiguration()

    registerColonistTask(
      discoverSettlers = discoverSettlers,
      classpathProvider = classpathProvider(runtimeClasspath),
      discoveryClasspathProvider = discoveryClasspathProvider(runtimeClasspath),
      cacheable = extension.cacheable,
    )

    if (this is HasAndroidTest) {
      val androidTestComponent = androidTest ?: return
      androidTestComponent.registerColonistTask(
        discoverSettlers = discoverSettlers,
        classpathProvider = classpathProvider(androidTestComponent.runtimeClasspathConfiguration()),
        discoveryClasspathProvider = discoveryClasspathProvider(androidTestComponent.runtimeClasspathConfiguration()) -
          discoveryClasspathProvider(runtimeClasspath),
        cacheable = extension.cacheable,
      )
    }

    if (extension.processTest) {
      val unitTestComponent = unitTest ?: return
      val unitTestRuntimeClasspath = unitTestComponent.runtimeClasspathConfiguration()

      unitTestComponent.registerColonistTask(
        discoverSettlers = discoverSettlers,
        classpathProvider = classpathProvider(runtimeClasspath),
        discoveryClasspathProvider = discoveryClasspathProvider(unitTestRuntimeClasspath),
        cacheable = extension.cacheable,
      )
    }
  }

  private fun Component.registerColonistTask(
    discoverSettlers: Boolean,
    classpathProvider: Provider<FileCollection>,
    discoveryClasspathProvider: Provider<FileCollection>,
    cacheable: Boolean,
  ) {
    val taskProvider = project.registerTask<ColonistTransformClassesTask>(
      TASK_PREFIX + name.replaceFirstChar { it.uppercaseChar() }
    )

    artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
      .use(taskProvider)
      .toTransform(
        ScopedArtifact.CLASSES,
        ColonistTransformClassesTask::allJars,
        ColonistTransformClassesTask::allDirectories,
        ColonistTransformClassesTask::output,
      )

    taskProvider.configure { task ->
      task.discoverSettlers = discoverSettlers
      task.discoveryClasspath.setFrom(discoveryClasspathProvider)
      task.classpath.setFrom(classpathProvider)

      @Suppress("UnstableApiUsage")
      task.bootClasspath.from(project.androidComponents!!.sdkComponents.bootClasspath)

      if (!cacheable) {
        task.outputs.doNotCacheIf("colonist.cacheable is false") { true }
      }
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

  private companion object {
    private val MIN_AGP_VERSION = AndroidPluginVersion(major = 7, minor = 4, micro = 0)
    private const val TASK_PREFIX = "colonistTransformClasses"
  }
}
