package com.joom.colonist.modular.colony;

import com.joom.colonist.Colonist;
import com.joom.colonist.OnAcceptSettler;

@ModularColonyAnnotation
public class ModularColony {

  public void settle() {
    Colonist.settle(this);
    System.out.println("Colony settled");
  }

  @OnAcceptSettler(colonyAnnotation = ModularColonyAnnotation.class)
  public void onAcceptSettler(Class<?> settler) {
    System.out.println("Accepted " + settler.getName());
  }
}
