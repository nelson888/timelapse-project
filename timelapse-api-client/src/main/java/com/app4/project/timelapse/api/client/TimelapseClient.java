package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.FileResponse;
import com.app4.project.timelapse.model.GlobalState;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;
import com.tambapps.http.restclient.util.ISSupplier;

import java.io.File;

public interface TimelapseClient {

  void postCommand(Command command, Callback<Command> callback);

  void putCameraState(CameraState cameraState, Callback<CameraState> callback);

  void postExecution(Execution execution, Callback<Execution> callback);

  void consumeCommand(Callback<Command> callback);

  void getExecution(Callback<Execution> callback);

  void getGlobalState(Callback<GlobalState> callback);

  void getCameraState(Callback<CameraState> callback);

  void putImage(ISSupplier isSupplier, Callback<FileResponse> callback, int executionId);

  void putImage(File file, Callback<FileResponse> callback, int executionId);

  <T> void getImage(ResponseHandler<T> responseHandler, final Callback<T> callback, int executionId, int fileId);

  void getImagesCount(int executionId, final Callback<Integer> callback);

  void deleteExecution(int executionId, Callback<Boolean> callback);

  void shutdown();
}
