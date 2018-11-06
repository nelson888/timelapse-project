package com.app4.project.timelapseserver.model;

public class Execution {

  private String title;
  private long startTime;
  private long endTime;
  private long id;
  private long frequency;

  public Execution(String title, long startTime, long endTime, long id, long frequency) {
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.id = id;
    this.frequency = frequency;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getFrequency() {
    return frequency;
  }

  public void setFrequency(long frequency) {
    this.frequency = frequency;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
