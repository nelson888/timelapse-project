package com.app4.project.timelapse.model;

public class ErrorResponse {
  private String message;

  public ErrorResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
