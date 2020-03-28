package com.app4.project.timelapse.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class VideoTaskProgress {

  private static final int NO_PERCENTAGE = -1;

  private int taskId;
  private int percentage;
  private TaskState state;
  private String message;
  private Integer videoId;

  public VideoTaskProgress(int taskId, int percentage, TaskState state) {
    this(taskId, percentage, state, null, null);
  }

  public VideoTaskProgress(int taskId, int percentage, TaskState state, String message) {
    this(taskId, percentage, state, message, null);
  }

  public static VideoTaskProgress error(int taskId, String message) {
    return new VideoTaskProgress(taskId, NO_PERCENTAGE, TaskState.ERROR, message);
  }

  public static VideoTaskProgress notStarted(String message) {
    return new VideoTaskProgress(-1, NO_PERCENTAGE, TaskState.NOT_STARTED, message);
  }

  public static VideoTaskProgress onGoing(int taskId, int percentage) {
    return new VideoTaskProgress(taskId, percentage, TaskState.ON_GOING);
  }

  public static VideoTaskProgress finished(int taskId, int videoId) {
    return new VideoTaskProgress(taskId, 100, TaskState.FINISHED, null, videoId);
  }
}
