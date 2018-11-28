package com.app4.project.timelapse.model;

public class Command {
  private String commandType; //commandType
  private Object[] arguments;

  public Command(String commandType, Object[] arguments) {
    this.commandType = commandType;
    this.arguments = arguments;
  }

}
