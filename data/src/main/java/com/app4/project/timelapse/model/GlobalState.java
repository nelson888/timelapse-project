package com.app4.project.timelapse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalState {

  private CameraState state;
  private Execution[] executions;
  private Command[] commandsPending;

}
