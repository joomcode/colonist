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

import java.io.File
import java.util.Locale
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.api.tasks.compile.JavaCompile

class JavaColonistPlugin : BaseColonistPlugin() {
  override fun apply(project: Project) {
    super.apply(project)

    val colonist = project.extensions.create("colonist", JavaColonistPluginExtension::class.java)

    addDependencies()

    project.afterEvaluate {
      if (project.plugins.hasPlugin("java")) {
        setupColonistForJava(discoverSettlers = colonist.discoverSettlers)
        if (colonist.processTest) {
          setupColonistForJavaTest(discoverSettlers = colonist.discoverSettlers)
        }
      } else {
        throw GradleException("Project should use Java plugin")
      }
    }
  }

  private fun addDependencies() {
    addDependencies(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
    addDependencies(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME)
  }

  private fun setupColonistForJava(discoverSettlers: Boolean) {
    logger.info("Setting up Colonist task for Java project {}...", project.name)
    createTasks(project.sourceSets.main, project.tasks.compileJava, project.tasks.classes, discoverSettlers)
  }

  private fun setupColonistForJavaTest(discoverSettlers: Boolean) {
    logger.info("Setting up Colonist task for Java test project {}...", project.name)
    createTasks(project.sourceSets.test, project.tasks.compileTestJava, project.tasks.testClasses, discoverSettlers, "test")
  }

  private fun createTasks(sourceSet: SourceSet, compileTask: JavaCompile, classesTask: Task, discoverSettlers: Boolean, nameSuffix: String = "") {
    val suffix = nameSuffix.capitalized()
    val colonistDir = File(project.buildDir, getColonistRelativePath(nameSuffix))
    val classesDirs = getClassesDirs(sourceSet.output)
    val backupDirs = getBackupDirs(project.buildDir, colonistDir, classesDirs)
    val sourceDir = File(colonistDir, "src")
    val classpath = compileTask.classpath.toList() - classesDirs.toSet()
    val bootClasspath =
      compileTask.options.bootstrapClasspath?.toList()
        ?: System.getProperty("sun.boot.class.path")?.split(File.pathSeparator)?.map { File(it) }
        ?: emptyList()
    val colonistTask = createColonistProcessTask(
      taskName = "colonistProcess$suffix",
      classesDirs = classesDirs,
      backupDirs = backupDirs,
      sourceDir = sourceDir,
      classpath = classpath,
      bootClasspath = bootClasspath,
      discoverSettlers = discoverSettlers,
    )
    val backupTask = createBackupClassFilesTask(
      taskName = "colonistBackupClasses$suffix",
      classesDirs = classesDirs,
      backupDirs = backupDirs
    )
    configureTasks(colonistTask, backupTask, compileTask, classesTask)
  }

  private fun getColonistRelativePath(suffix: String): String {
    return if (suffix.isEmpty()) COLONIST_PATH else COLONIST_PATH + File.separatorChar + suffix
  }

  private fun getClassesDirs(output: SourceSetOutput): List<File> {
    return output.classesDirs.files.toList()
  }

  private fun getBackupDirs(buildDir: File, colonistDir: File, classesDirs: List<File>): List<File> {
    return classesDirs.map { classesDir ->
      val relativeFile = classesDir.relativeToOrSelf(buildDir)
      // XXX: What if relativeFile is rooted? Maybe we need to remove the root part from it.
      File(colonistDir, relativeFile.path)
    }
  }

  private fun configureTasks(colonistTask: ColonistTask, backupTask: BackupClassesTask, compileTask: Task, classesTask: Task) {
    backupTask.dependsOn(compileTask)
    colonistTask.mustRunAfter(compileTask)
    colonistTask.dependsOn(compileTask)
    colonistTask.dependsOn(backupTask)
    classesTask.dependsOn(colonistTask)

    val cleanBackupTask = project.tasks["clean${backupTask.name.capitalized()}"]!!
    val cleanColonistTask = project.tasks["clean${colonistTask.name.capitalized()}"]!!

    cleanBackupTask.doFirst {
      backupTask.clean()
    }

    cleanColonistTask.doFirst {
      colonistTask.clean()
    }

    cleanColonistTask.dependsOn(cleanBackupTask)
  }

  private fun createColonistProcessTask(
    taskName: String,
    classesDirs: List<File>,
    backupDirs: List<File>,
    sourceDir: File,
    classpath: List<File>,
    bootClasspath: List<File>,
    discoverSettlers: Boolean,
  ): ColonistTask {
    logger.info("Creating Colonist task {}...", taskName)
    logger.info("  Source classes directories: {}", backupDirs)
    logger.info("  Processed classes directories: {}", classesDirs)

    return project.tasks.create(taskName, ColonistTask::class.java) { task ->
      task.description = "Processes .class files with Colonist Processor."
      task.backupDirs = backupDirs
      task.classesDirs = classesDirs
      task.sourceDir = sourceDir
      task.classpath = classpath
      task.bootClasspath = bootClasspath
      task.discoverSettlers = discoverSettlers
    }
  }

  private fun createBackupClassFilesTask(
    taskName: String,
    classesDirs: List<File>,
    backupDirs: List<File>
  ): BackupClassesTask {
    return project.tasks.create(taskName, BackupClassesTask::class.java) { task ->
      task.description = "Back up original .class files."
      task.classesDirs = classesDirs
      task.backupDirs = backupDirs
    }
  }

  private fun String.capitalized(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
  }

  companion object {
    private const val COLONIST_PATH = "colonist"
  }
}
