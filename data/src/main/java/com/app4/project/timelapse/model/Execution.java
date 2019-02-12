package com.app4.project.timelapse.model;

import java.util.Objects;

public class Execution implements Comparable<Execution> {

  private String title;
  private long startTime;
  private long endTime;
  private int id;
  private long frequency;

  public Execution(String title, long startTime, long endTime, int id, long frequency) {
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.id = id;
    this.frequency = frequency;
  }

  public Execution(String title, long startTime, long endTime, long frequency) {
    this(title, startTime, endTime, 0, frequency);
  }

  public Execution() { }

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

  public int getId() {
    return id;
  }

  public void setId(int id) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Execution execution = (Execution) o;
    return id == execution.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public int compareTo(Execution execution) {
    return (int) (execution.startTime - startTime);
  }
}
