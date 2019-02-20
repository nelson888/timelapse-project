package com.app4.project.timelapseserver.utils;

import java.util.Arrays;

public class IdPool {

  private final boolean[] took;

  public IdPool(int max) {
    took = new boolean[max];
    Arrays.fill(took, false);
  }


  public int get() {
    int i = 0;
    while (took[i]) {
      i++;
      if (i >= took.length) {
        throw new RuntimeException("There isn't any free id");
      }
    }
    took[i] = true;
    return i;
  }

  public void free(int i) {
    if (i < 0 || i >= took.length) {
      return;
    }
    took[i] = false;
  }
}
