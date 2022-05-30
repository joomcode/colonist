package com.joom.colonist.plugin

import com.joom.colonist.processor.ColonistParameters
import com.joom.colonist.processor.ColonistProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ColonistTransformClassesTask : DefaultTask() {
  @get:InputFiles
  @get:Classpath
  abstract val inputClasses: ListProperty<Directory>

  @get:InputFiles
  @get:Classpath
  abstract val discoveryClasspath: ConfigurableFileCollection

  @get:InputFiles
  @get:Classpath
  abstract val classpath: ConfigurableFileCollection

  @get:InputFiles
  @get:Classpath
  abstract val bootClasspath: ConfigurableFileCollection

  @get:OutputDirectory
  abstract val output: DirectoryProperty

  @Input
  var discoverSettlers: Boolean = false

  init {
    logging.captureStandardOutput(LogLevel.INFO)
  }

  @TaskAction
  fun process() {
    validate()

    cleanOutput()

    val parameters = ColonistParameters(
      inputs = inputClasses.get().map { it.asFile },
      outputs = List(inputClasses.get().size) { output.get().asFile },
      generationOutput = output.get().asFile,
      discoveryClasspath = discoveryClasspath.toList(),
      classpath = classpath.toList(),
      bootClasspath = bootClasspath.toList(),
      projectName = name.orEmpty().replace(":colonistProcess", ":").replace(':', '$'),
      discoverSettlers = discoverSettlers,
      debug = logger.isDebugEnabled,
      info = logger.isInfoEnabled
    )

    logger.info("Starting Colonist processor: {}", parameters)
    try {
      ColonistProcessor.process(parameters)
    } catch (exception: Exception) {
      throw GradleScriptException("Colonist processor failed to process files", exception)
    }
  }

  private fun validate() {
    require(inputClasses.get().isNotEmpty()) { "inputClasses is not set" }
    require(output.isPresent) { "output is not set" }
  }

  private fun cleanOutput() {
    output.get().asFile.deleteRecursively()
  }
}
