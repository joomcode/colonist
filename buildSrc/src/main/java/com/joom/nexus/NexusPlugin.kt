package com.joom.nexus

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.PublishingExtension
import java.net.URI

class NexusPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val properties = project.rootProject.properties
    val username = properties["nexus.username"]?.toString() ?: ""
    val password = properties["nexus.password"]?.toString() ?: ""
    if (username.isEmpty() || password.isEmpty()) {
      return
    }

    project.afterEvaluate {
      maybeAddNexusRepository(project.buildscript.repositories, username, password)
      maybeAddNexusRepository(project.repositories, username, password)

      project.extensions.findByType(PublishingExtension::class.java)?.also { publishing ->
        maybeAddNexusRepository(publishing.repositories, username, password)
      }
    }
  }

  private fun maybeAddNexusRepository(repositories: RepositoryHandler, username: String, password: String) {
    repositories.maven { repository ->
      repository.name = "Nexus"
      repository.url = URI.create("https://repo.joom.it/repository/joom-maven")
      repository.credentials { credentials ->
        credentials.username = username
        credentials.password = password
      }
    }
  }
}
