package com.app4.project.timelapse.model;

import lombok.Value;

@Value
public class SavingProgress {

  private static final int NO_PERCENTAGE = -1;
  private static final SavingProgress NOT_STARTED = new SavingProgress(NO_PERCENTAGE, SavingState.NOT_STARTED);
  private static final SavingProgress ERROR = new SavingProgress(NO_PERCENTAGE, SavingState.ERROR);
  private static final SavingProgress FINISHED = new SavingProgress(100, SavingState.FINISHED);

  private int percentage;
  private SavingState state;

  public static SavingProgress error() {
    return ERROR;
  }

  public static SavingProgress notStarted() {
    return NOT_STARTED;
  }

  public static SavingProgress onGoing(int percentage) {
    return new SavingProgress(percentage, SavingState.ON_GOING);
  }

  public static SavingProgress finished() {
    return FINISHED;
  }
}
