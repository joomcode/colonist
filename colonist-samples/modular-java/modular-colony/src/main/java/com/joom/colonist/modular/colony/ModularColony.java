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

package com.joom.colonist.modular.colony;

import com.joom.colonist.Colonist;
import com.joom.colonist.OnAcceptSettler;
import com.joom.colonist.OnProduceSettler;

@ModularColonyAnnotation
@ModularColonyProduceAnnotation
@ModularColonyProduceAndForgetAnnotation
@ModularColonyProduceStaticAnnotation
public class ModularColony {

  public void settle() {
    Colonist.settle(this);
    System.out.println("Colony settled");
  }

  @OnAcceptSettler(colonyAnnotation = ModularColonyAnnotation.class)
  private void onAcceptSettler(Object settler) {
    System.out.println("Accepted " + settler.getClass().getName());
  }

  @OnProduceSettler(colonyAnnotation = ModularColonyProduceAnnotation.class)
  private Object onProduceSettler(Class<?> settler) {
    System.out.println("Producing " + settler.getName());

    try {
      return settler.newInstance();
    } catch (Throwable throwable) {
      throw new RuntimeException("Failed to create settler " + settler.getName(), throwable);
    }
  }

  @OnProduceSettler(colonyAnnotation = ModularColonyProduceAndForgetAnnotation.class)
  private Object onProduceAndForgetSettler(Class<?> settler) {
    System.out.println("Producing and forgetting " + settler.getName());

    try {
      return settler.newInstance();
    } catch (Throwable throwable) {
      throw new RuntimeException("Failed to create settler " + settler.getName(), throwable);
    }
  }

  @OnAcceptSettler(colonyAnnotation = ModularColonyProduceAnnotation.class)
  private void onAcceptProducedSettler(Object settler) {
    System.out.println("Accepted produced settler " + settler.getClass().getName());
  }

  @OnProduceSettler(colonyAnnotation = ModularColonyProduceStaticAnnotation.class)
  private static Object onProduceSettlerStatic(Class<?> settler) {
    System.out.println("Producing via static " + settler.getName());

    try {
      return settler.newInstance();
    } catch (Throwable throwable) {
      throw new RuntimeException("Failed to create settler " + settler.getName(), throwable);
    }
  }

  @OnAcceptSettler(colonyAnnotation = ModularColonyProduceStaticAnnotation.class)
  private static void onAcceptProducedSettlerStatic(Object settler) {
    System.out.println("Accepted produced settler via static " + settler.getClass().getName());
  }
}
