package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.*;

import com.tambapps.http.restclient.request.handler.response.ResponseHandler;
import com.tambapps.http.restclient.util.ISSupplier;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;

public class TimelapseFakeClient implements TimelapseClient {

  private static final int RESPONSE_SUCCESS = 200;

  private final Queue<Execution> executions = new PriorityQueue<>(10);
  private final Queue<Command> commands = new PriorityQueue<>();
  private CameraState cameraState = new CameraState();

  public TimelapseFakeClient() {
    long now = new Date().getTime();
    long eightHours = 8 * 60 * 60 * 1000;
    executions.add(new Execution("Pop corne qui explose", now, now + eightHours, 250));
    executions.add(new Execution("Eclosion d'une fleur", now + eightHours * 4, now + eightHours * 6, 500));
    executions.add(new Execution("Execution 3", now + eightHours * 25, now + eightHours * 26, 150));


    commands.add(new Command("SLEEP", null));
    cameraState.setCameraWorking(true);
  }

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
    if (commands.isEmpty()) {
      callback.onError(23423, new ErrorResponse("Bad Request", "There isn't any command to get"));
    } else {
      callback.onSuccess(RESPONSE_SUCCESS, commands.remove());
    }
  }

  public void getExecution(int executionId, Callback<Execution> callback) {
    Execution e = findExecution(executionId);
    if (e == null) {
      callback.onError(324234,
          new ErrorResponse("Bad Request", "Execution with id " + executionId + "doesn't exists"));
    } else {
      callback.onSuccess(RESPONSE_SUCCESS, e);
    }
  }

  @Override
  public void getSoonestExecution(Callback<Execution> callback) {
    callback.onSuccess(200, executions.peek());
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
  public void putImage(ISSupplier isSupplier, Callback<FileData> callback, int executionId) {
    putImage((File)null, callback, 0);
  }

  @Override
  public void putImage(File file, Callback<FileData> callback, int executionId) {
    callback.onError(23424, new ErrorResponse("Fake client", "You cannot put any images"));
  }

  @Override
  public <T> void getImage(ResponseHandler<T> responseHandler, Callback<T> callback,
      int executionId, int fileId) {
    callback.onError(23424, new ErrorResponse("Fake client", "There isn't any images"));
  }

  @Override
  public void getImagesCount(int executionId, Callback<Integer> callback) {
    callback.onSuccess(RESPONSE_SUCCESS, 0);
  }

  @Override
  public void deleteExecution(int executionId, Callback<Boolean> callback) {
    Execution e = findExecution(executionId);
    callback.onSuccess(200, e != null && executions.remove(e));
  }


  @Override
  public void authenticate(User user, Callback<Boolean> callback) {
    callback.onSuccess(RESPONSE_SUCCESS, true);
  }

  public void shutdown() {

  }

  private Execution findExecution(int id) {
    for (Execution e : executions) {
      if (e.getId() == id) {
        return e;
      }
    }
    return null;
  }
}
