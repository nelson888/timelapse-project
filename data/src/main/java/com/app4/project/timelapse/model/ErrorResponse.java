package com.app4.project.timelapse.model;

public class ErrorResponse {
  private String title;
  private String message;

  public ErrorResponse(String title, String message) {
    this.message = message;
    this.title = title;
  }

  public ErrorResponse() {
  }

  public String getMessage() {
    return message;
  }

  public String getTitle() {
    return title;
  }
}
