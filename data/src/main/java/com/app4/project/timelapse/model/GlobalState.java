package com.app4.project.timelapse.model;

public class GlobalState {

  private CameraState state;
  private Execution[] executions;
  private Command[] commandsPending;

  public GlobalState(CameraState state, Execution[] executions,
      Command[] commandsPending) {
    this.state = state;
    this.executions = executions;
    this.commandsPending = commandsPending;
  }

  public GlobalState() {
  }

  public CameraState getState() {
    return state;
  }

  public Execution[] getExecutions() {
    return executions;
  }

  public Command[] getCommandsPending() {
    return commandsPending;
  }
}
