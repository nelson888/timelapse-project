package com.app4.project.timelapse.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class SavingProgress {

  private static final int NO_PERCENTAGE = -1;

  private int taskId;
  private int percentage;
  private SavingState state;
  private String message;

  public SavingProgress(int taskId, int percentage, SavingState state) {
    this(taskId, percentage, state, null);
  }

  public static SavingProgress error(int taskId, String message) {
    return new SavingProgress(taskId, NO_PERCENTAGE, SavingState.ERROR, message);
  }

  public static SavingProgress notStarted(String message) {
    return new SavingProgress(-1, NO_PERCENTAGE, SavingState.NOT_STARTED, message);
  }

  public static SavingProgress onGoing(int taskId, int percentage) {
    return new SavingProgress(taskId, percentage, SavingState.ON_GOING);
  }

  public static SavingProgress finished(int taskId) {
    return new SavingProgress(taskId, 100, SavingState.FINISHED);
  }
}
