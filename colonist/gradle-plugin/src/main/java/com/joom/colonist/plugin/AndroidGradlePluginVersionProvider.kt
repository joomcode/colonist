package com.joom.colonist.plugin

import com.android.build.api.AndroidPluginVersion

internal object AndroidGradlePluginVersionProvider {
  fun getCurrentPluginVersion(): AndroidPluginVersion {
    val version = readVersionReflective()

    return parseVersion(version)
  }

  private fun readVersionReflective(): String {
    val clazz = findClassOrNull("com.android.Version") ?: findClassOrNull("com.android.builder.model.Version")
    if (clazz != null) {
      return clazz.getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null) as String
    }
    error(
      "Failed to find Android Gradle Plugin version"
    )
  }

  private fun parseVersion(version: String): AndroidPluginVersion {
    val parts = version.split('.')
    if (parts.size < 3) {
      error("Version '$version' should have at least three parts, got ${parts.size}")
    }

    return AndroidPluginVersion(major = parts[0].toInt(), minor = parts[1].toInt(), micro = parts[2].toInt())
  }

  private fun findClassOrNull(className: String): Class<*>? {
    return try {
      Class.forName(className)
    } catch (_: ClassNotFoundException) {
      return null
    }
  }
}
