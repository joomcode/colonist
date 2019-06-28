/*
 * Copyright 2019 Michael Rozumyanskiy
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

package com.joom.colonist;

import com.joom.colonist.internal.ColonyFounder;

import javax.annotation.Nonnull;

public class Colonist {
  private Colonist() {
  }

  public static void settle(@Nonnull final Object colony) {
    final ColonyFounder founder = requireColonyFounder(colony);
    founder.found(null);
  }

  public static void settle(@Nonnull final Object colony, @Nonnull final Class<?> colonyAnnotationClass) {
    final ColonyFounder founder = requireColonyFounder(colony);
    founder.found(colonyAnnotationClass);
  }

  private static ColonyFounder requireColonyFounder(@Nonnull final Object colony) {
    if (colony instanceof ColonyFounder) {
      return (ColonyFounder) colony;
    }

    throw new ColonistException("Colony " + colony + " doesn't have a founder. Maybe you forgot to apply the colonist plugin?");
  }
}
