package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.FileResponse;
import com.app4.project.timelapse.model.GlobalState;
import com.app4.project.timelapse.model.User;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;
import com.tambapps.http.restclient.util.ISSupplier;

import java.io.File;

//ugly class but it works (hopefully)
public class TimelapseBasicClient {
  //just use a sync client and returns result
  private final TimelapseSyncClient client;

  public TimelapseBasicClient(String baseUrl, User user) {
    this.client = new TimelapseSyncClient(baseUrl, user);
  }

  TimelapseResponse<Command> postCommand(Command command) {
    final ResponseRef<Command> responseRef = new ResponseRef<>();
    client.postCommand(command, callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<CameraState> putCameraState(CameraState cameraState) {
    final ResponseRef<CameraState> responseRef = new ResponseRef<>();
    client.putCameraState(cameraState, callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<Execution> postExecution(Execution execution) {
    final ResponseRef<Execution> responseRef = new ResponseRef<>();
    client.postExecution(execution, callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<Command> consumeCommand() {
    final ResponseRef<Command> responseRef = new ResponseRef<>();
    client.consumeCommand(callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<Execution> getExecution(int executionId) {
    final ResponseRef<Execution> responseRef = new ResponseRef<>();
    client.getExecution(executionId, callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<GlobalState> getGlobalState() {
    final ResponseRef<GlobalState> responseRef = new ResponseRef<>();
    client.getGlobalState(callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<CameraState> getCameraState() {
    final ResponseRef<CameraState> responseRef = new ResponseRef<>();
    client.getCameraState(callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<FileResponse> putImage(ISSupplier isSupplier, int executionId) {
    final ResponseRef<FileResponse> responseRef = new ResponseRef<>();
    client.putImage(isSupplier, callback(responseRef), executionId);
    return responseRef.response;
  }

  TimelapseResponse<FileResponse> putImage(File file, int executionId) {
    final ResponseRef<FileResponse> responseRef = new ResponseRef<>();
    client.putImage(file, callback(responseRef), executionId);
    return responseRef.response;
  }

  <T> TimelapseResponse<T> getImage(ResponseHandler<T> responseHandler, int executionId, int fileId) {
    final ResponseRef<T> responseRef = new ResponseRef<>();
    client.getImage(responseHandler, callback(responseRef), executionId, fileId);
    return responseRef.response;
  }

  TimelapseResponse<Integer> getImagesCount(int executionId) {
    final ResponseRef<Integer> responseRef = new ResponseRef<>();
    client.getImagesCount(executionId, callback(responseRef));
    return responseRef.response;
  }

  TimelapseResponse<Boolean> deleteExecution(int executionId) {
    final ResponseRef<Boolean> responseRef = new ResponseRef<>();
    client.deleteExecution(executionId, callback(responseRef));
    return responseRef.response;
  }

  private <T> Callback<T> callback(final ResponseRef<T> responseRef) {
    return new Callback<T>() {
      @Override
      public void onSuccess(int responseCode, T data) {
        responseRef.response = new TimelapseResponse<>(data, responseCode);
      }

      @Override
      public void onError(int responseCode, ErrorResponse response) {
        responseRef.response = new TimelapseResponse<>(response, responseCode);
      }
    };
  }

  private static class ResponseRef<T> {
    private TimelapseResponse<T> response;
  }
}
