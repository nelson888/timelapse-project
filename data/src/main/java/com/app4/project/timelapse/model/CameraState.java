package com.app4.project.timelapse.model;

import java.util.Objects;

public class CameraState {
  private boolean cameraWorking;
  private Execution currentExecution;
  private boolean sleeping;
  private boolean turnedOn;
  private long lastTimeAlive; //last time the camera communicated with the server
  private long batteryPercentage;

  public CameraState() {
  }

  public CameraState(boolean cameraWorking,
      Execution currentExecution, boolean sleeping, boolean turnedOn, long lastTimeAlive) {
    this.cameraWorking = cameraWorking;
    this.currentExecution = currentExecution;
    this.sleeping = sleeping;
    this.turnedOn = turnedOn;
    this.lastTimeAlive = lastTimeAlive;
  }

  public void setCurrentExecution(Execution currentExecution) {
    this.currentExecution = currentExecution;
  }

  public boolean isTurnedOn() {
    return turnedOn;
  }

  public void setTurnedOn(boolean turnedOn) {
    this.turnedOn = turnedOn;
  }

  public boolean isCameraWorking() {
    return cameraWorking;
  }

  public void setCameraWorking(boolean cameraWorking) {
    this.cameraWorking = cameraWorking;
  }

  public Execution getCurrentExecution() {
    return currentExecution;
  }

  public boolean isSleeping() {
    return sleeping;
  }

  public void setSleeping(boolean sleeping) {
    this.sleeping = sleeping;
  }

  public void setBatteryPercentage(long batteryPercentage) {
    this.batteryPercentage = batteryPercentage;
  }

  public void setLastTimeAlive(long lastTimeAlive) {
    this.lastTimeAlive = lastTimeAlive;
  }

  public long getLastTimeAlive() {
    return lastTimeAlive;
  }

  public long getBatteryPercentage() {
    return batteryPercentage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CameraState that = (CameraState) o;
    return cameraWorking == that.cameraWorking &&
            sleeping == that.sleeping &&
            Objects.equals(currentExecution, that.currentExecution) &&
      batteryPercentage == that.batteryPercentage;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cameraWorking, currentExecution, sleeping);
  }

  @Override
  public String toString() {
    return "CameraState{" +
            "cameraWorking=" + cameraWorking +
            ", currentExecution=" + currentExecution +
            ", sleeping=" + sleeping +
            ", batteryPercentage=" + batteryPercentage +
            '}';
  }
}
