package com.app4.project.timelapseserver.model;

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
}
