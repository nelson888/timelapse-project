package com.app4.project.timelapse.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingProgress {

  private static final int NO_PERCENTAGE = -1;
  private static final SavingProgress FINISHED = new SavingProgress(100, SavingState.FINISHED);

  private int percentage;
  private SavingState state;
  private String message;

  public SavingProgress(int percentage, SavingState state) {
    this(percentage, state, null);
  }

  public static SavingProgress error(String message) {
    return new SavingProgress(NO_PERCENTAGE, SavingState.ERROR, message);
  }

  public static SavingProgress notStarted(String message) {
    return new SavingProgress(NO_PERCENTAGE, SavingState.NOT_STARTED, message);
  }

  public static SavingProgress onGoing(int percentage) {
    return new SavingProgress(percentage, SavingState.ON_GOING);
  }

  public static SavingProgress finished() {
    return FINISHED;
  }
}