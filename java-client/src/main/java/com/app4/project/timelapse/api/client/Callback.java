package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.ErrorResponse;

public interface Callback<T> {
  void onSuccess(int responseCode, T data);
  void onError(int responseCode, ErrorResponse response);
}