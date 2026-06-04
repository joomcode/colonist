/*
 * Copyright 2023 SIA Joom
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

import java.io.File
import java.net.URI
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
internal class AndroidColonistPluginTest(private val case: TestCase) {
  @get:Rule
  val temporaryFolder = TemporaryFolder()

  private companion object {
    @Parameters(name = "{0}")
    @JvmStatic
    fun parameters(): List<TestCase> {
      return listOf(
        TestCase(
          agpVersion = "7.4.2",
          gradleDistribution = GradleDistribution.GRADLE_8_14,
          projectType = AndroidProjectType.APPLICATION,
          expectedTaskName = ":colonistTransformClassesDebug",
        ),
        TestCase(
          agpVersion = "8.1.2",
          gradleDistribution = GradleDistribution.GRADLE_8_14,
          projectType = AndroidProjectType.APPLICATION,
          expectedTaskName = ":colonistTransformClassesDebug",
        ),
        TestCase(
          agpVersion = "8.12.0",
          gradleDistribution = GradleDistribution.GRADLE_9_5,
          projectType = AndroidProjectType.APPLICATION,
          expectedTaskName = ":colonistTransformClassesDebug",
        ),
        TestCase(
          agpVersion = "9.2.1",
          gradleDistribution = GradleDistribution.GRADLE_9_5,
          projectType = AndroidProjectType.APPLICATION,
          expectedTaskName = ":colonistTransformClassesDebug",
        ),
        TestCase(
          agpVersion = "9.2.1",
          gradleDistribution = GradleDistribution.GRADLE_9_5,
          projectType = AndroidProjectType.LIBRARY,
          expectedTaskName = ":colonistTransformClassesDebug",
        ),
      )
    }

    @Language("xml")
    private const val ANDROID_MANIFEST = """
<?xml version="1.0" encoding="utf-8"?>
<manifest />
"""
  }

  @Test
  fun test() {
    val projectRoot = createProjectDirectory(
      agpVersion = case.agpVersion,
      projectType = case.projectType,
    )

    val result = createGradleRunner(projectRoot, case.gradleDistribution).build()

    val tasks = result.parseDryRunExecution()
    Assert.assertTrue(tasks.any { it.path == case.expectedTaskName })
  }

  private fun createProjectDirectory(agpVersion: String, projectType: AndroidProjectType): File {
    val projectRoot = temporaryFolder.newFolder()
    writeText(createBuildGradle(agpVersion, projectType), File(projectRoot, "build.gradle"))
    writeText(ANDROID_MANIFEST, File(projectRoot, "src/main/AndroidManifest.xml"))
    return projectRoot
  }

  private fun createGradleRunner(projectDir: File, gradle: GradleDistribution): GradleRunner {
    return GradleRunner.create()
      .withGradleDistribution(URI.create(gradle.url))
      .forwardOutput()
      .withProjectDir(projectDir)
      .withArguments("assembleDebug", "--dry-run", "--stacktrace")
  }

  private fun BuildResult.parseDryRunExecution(): List<BuildTask> {
    val split = output.split('\n')
    return split.mapNotNull {
      if (it.startsWith(":")) {
        val (path, _) = it.split(" ")
        TestBuildTask(path = path, outcome = TaskOutcome.SKIPPED)
      } else {
        null
      }
    }
  }

  private fun writeText(content: String, destination: File) {
    if (!destination.parentFile.exists() && !destination.parentFile.mkdirs()) {
      error("Failed to create parent directory ${destination.parentFile}")
    }

    destination.writeText(content)
  }

  @Language("gradle")
  private fun createBuildGradle(
    agpVersion: String,
    projectType: AndroidProjectType,
    compileSdk: Int = 31,
  ): String {
    return """
      buildscript {
        repositories {
          google()
          mavenLocal()
          mavenCentral()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:$agpVersion"
          classpath "com.joom.colonist:colonist-gradle-plugin:+"
        }
      }

      apply plugin: "${projectType.pluginId}"
      apply plugin: "com.joom.colonist.android"

      repositories {
        google()
        mavenCentral()
      }

      android {
        compileSdk $compileSdk
        buildToolsVersion "34.0.0"

        namespace "com.joom.colonist.test"

        ${projectType.defaultConfig}
      }
    """.trimIndent()
  }

  private data class TestBuildTask(
    private val path: String,
    private val outcome: TaskOutcome,
  ) : BuildTask {
    override fun getPath(): String {
      return path
    }

    override fun getOutcome(): TaskOutcome {
      return outcome
    }
  }
}

internal data class TestCase(
  val agpVersion: String,
  val gradleDistribution: GradleDistribution,
  val projectType: AndroidProjectType,
  val expectedTaskName: String
)

internal enum class AndroidProjectType(
  val pluginId: String,
  val defaultConfig: String,
) {
  APPLICATION(
    pluginId = "com.android.application",
    defaultConfig = """
      defaultConfig {
        applicationId "com.joom.colonist.test"
        versionCode 1
        versionName "1"
      }
    """.trimIndent(),
  ),
  LIBRARY(
    pluginId = "com.android.library",
    defaultConfig = "",
  ),
}
