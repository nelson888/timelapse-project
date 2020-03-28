package com.app4.project.timelapse.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraState {
  private boolean cameraWorking;
  private Execution currentExecution;
  private boolean sleeping;
  private boolean turnedOn;
  private long lastHeartBeat; //last time the camera communicated with the server
  private long batteryPercentage;

}
