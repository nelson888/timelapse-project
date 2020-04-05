package com.app4.project.timelapse.model.request;

import com.app4.project.timelapse.model.Command;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommandPostRequest {

  private String command;

  public CommandPostRequest(Command command) {
    this.command = command.name();
  }

}
