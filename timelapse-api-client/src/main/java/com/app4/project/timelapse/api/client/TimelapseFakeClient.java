package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.FileResponse;
import com.app4.project.timelapse.model.GlobalState;
import com.google.gson.Gson;
import com.tambapps.http.restclient.RestClient;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;
import com.tambapps.http.restclient.util.ISSupplier;

import java.io.File;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class TimelapseFakeClient implements TimelapseClient {
  private static final String API_ENDPOINT = "api/";
  private static final String FILE_STORAGE_ENDPOINT = "files/";
  private static final int RESPONSE_SUCCESS = 200;
  private final Gson gson = new Gson();

  private final Queue<Execution> executions = new PriorityQueue<>(10);
  private final Queue<Command> commands = new ArrayDeque<>();
  private CameraState cameraState = new CameraState();


  public void putCommand(Command command, Callback<Command> callback) {
    commands.add(command);
    callback.onSuccess(RESPONSE_SUCCESS, command);
  }

  public void postCommand(Command command, Callback<Command> callback) {
    putCommand(command, callback);
  }

  public void putCameraState(CameraState cameraState, Callback<CameraState> callback) {
    this.cameraState = cameraState;
    callback.onSuccess(RESPONSE_SUCCESS, cameraState);
  }

  public void putExecution(Execution execution, Callback<Execution> callback) {
    executions.add(execution);
    callback.onSuccess(RESPONSE_SUCCESS, execution);
  }

  public void postExecution(Execution execution, Callback<Execution> callback) {
    putExecution(execution, callback);
  }

  public void consumeCommand(Callback<Command> callback) {
    //TODO
  }

  public void getExecution(Callback<Execution> callback) {
    //TODO
  }

  public void getGlobalState(Callback<GlobalState> callback) {
    callback.onSuccess(RESPONSE_SUCCESS,
        new GlobalState(cameraState, this.executions.toArray(new Execution[0]),
        commands.toArray(new Command[0])));
  }

  public void getCameraState(Callback<CameraState> callback) {
    callback.onSuccess(RESPONSE_SUCCESS, cameraState);
  }

  @Override
  public void putImage(ISSupplier isSupplier, Callback<FileResponse> callback, int executionId) {
    //do nothing
  }

  @Override
  public void putImage(File file, Callback<FileResponse> callback, int executionId) {
    //do nothing
  }

  @Override
  public <T> void getImage(ResponseHandler<T> responseHandler, Callback<T> callback,
      int executionId, int fileId) {
    //TODO store images locally
  }

  @Override
  public void getImagesCount(int executionId, Callback<Integer> callback) {
    //TODO
  }

  @Override
  public void deleteExecution(int executionId, Callback<Boolean> callback) {
    Execution toRemove = null;
    for (Execution e : executions) {
      if (e.getId() == executionId) {
        toRemove = e;
      }
    }

    callback.onSuccess(200, toRemove != null && executions.remove(toRemove));
  }



  public void shutdown() {

  }
}
