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

import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.Component
import org.gradle.api.tasks.TaskProvider

internal object AllClassesTransformRegistrar : TransformTaskRegistrar {
  override fun register(component: Component, taskProvider: TaskProvider<ColonistTransformClassesTask>) {
    @Suppress("DEPRECATION")
    component.artifacts.use(taskProvider)
      .wiredWith(ColonistTransformClassesTask::inputDirectories, ColonistTransformClassesTask::outputDirectory)
      .toTransform(MultipleArtifact.ALL_CLASSES_DIRS)
  }
}
