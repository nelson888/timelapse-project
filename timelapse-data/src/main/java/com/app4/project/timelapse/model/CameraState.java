package com.app4.project.timelapse.model;

import java.util.Objects;

public class CameraState {
  private boolean cameraWorking;
  private Execution currentExecution;
  private boolean sleeping;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CameraState that = (CameraState) o;
    return cameraWorking == that.cameraWorking &&
            sleeping == that.sleeping &&
            Objects.equals(currentExecution, that.currentExecution);
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
            '}';
  }
}
