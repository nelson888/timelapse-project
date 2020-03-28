package com.app4.project.timelapse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Execution implements Comparable<Execution> {

  private int id;
  private String title;
  private long startTime;
  private long endTime;
  private long period; // in seconds

  public Execution(String title, long startTime, long endTime, long period) {
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.period = period;
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
    return (int) (startTime - execution.startTime);
  }

  public boolean isRunning() {
    long now = System.currentTimeMillis();
    return now >= startTime && now <= endTime;
  }

  public boolean overlaps(Execution e) {
    return startTime >= e.startTime && startTime < e.endTime ||
      endTime >= e.startTime && endTime < e.endTime ||
      startTime <= e.startTime && endTime >= e.endTime;
  }

}
