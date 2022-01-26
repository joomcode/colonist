package com.joom.colonist.modular.ants;

import com.joom.colonist.modular.Ant;
import com.joom.colonist.modular.AntSettler;

public class QueenAntLair {
  @AntSettler
  private static class Queen implements Ant {
    @Override
    public String toString() {
      return "Queen";
    }
  }
}
