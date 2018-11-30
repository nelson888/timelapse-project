package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.ErrorResponse;

public class TimelapseResponse<T> {

  private final Object data;
  private final boolean isError;
  private final int responseCode;

  public TimelapseResponse(T data, int responseCode) {
    this.data = data;
    this.isError = false;
    this.responseCode = responseCode;
  }

  public TimelapseResponse(ErrorResponse response, int responseCode) {
    this.data = response;
    this.isError = false;
    this.responseCode = responseCode;
  }

  public boolean isError() {
    return isError;
  }

  public T getData() {
    return (T) data;
  }

  public ErrorResponse getError() {
    return (ErrorResponse) data;
  }

  public int getResponseCode() {
    return responseCode;
  }
}
