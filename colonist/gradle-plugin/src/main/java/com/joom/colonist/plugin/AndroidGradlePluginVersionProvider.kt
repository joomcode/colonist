/*
 * Copyright 2022 SIA Joom
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
