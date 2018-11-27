package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.FileResponse;
import com.app4.project.timelapse.model.GlobalState;
import com.app4.project.timelapse.model.User;
import com.google.gson.Gson;
import com.tambapps.http.restclient.RestClient;
import com.tambapps.http.restclient.request.RestRequest;

import com.tambapps.http.restclient.request.handler.output.BodyHandlers;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;
import com.tambapps.http.restclient.request.handler.response.ResponseHandlers;
import com.tambapps.http.restclient.response.RestResponse;

import java.io.File;

public class TimelapseClient {

  private static final String BASE_URL = "http://localhost:8080/";
  private static final String API_ENDPOINT = "api/";
  private static final String FILE_STORAGE_ENDPOINT = "files/";
  private final RestClient client;
  private final Gson gson = new Gson();

  //TODO handle authentication with server and jwt
  public TimelapseClient(User user) {
    client = new RestClient(BASE_URL);
    //TODO authenticate handle jwt
    /*
    RestResponse<String, String> response = client.execute(RestRequest.builder()
            .POST()
            .endpoint(API_ENDPOINT + "/authenticate")
            .output(BodyHandlers.json(gson.toJson(user)))
            .build(),
        ResponseHandlers.stringHandler());
    if (!response.isSuccessful() || response.isErrorResponse()) {
      throw new RuntimeException("Couldn't authenticate");
    }

    String jwt = gson.fromJson(response.getSuccessData(), AuthResponse.class).getJwt();
    client.setJwt(jwt);*/
  }

  public void putCommand(Command command, Callback<Command> callback) {
    putObject(API_ENDPOINT + "command", command, callback);
  }

  public void postCommand(Command command, Callback<Command> callback) {
    postObject(API_ENDPOINT + "command", command, callback);
  }

  public void putCameraState(CameraState cameraState, Callback<CameraState> callback) {
    putObject(API_ENDPOINT + "state", cameraState, callback);
  }

  public void putExecution(Execution execution, Callback<Execution> callback) {
    putObject(API_ENDPOINT + "execution", execution, callback);
  }

  public void postExecution(Execution execution, Callback<Execution> callback) {
    postObject(API_ENDPOINT + "execution", execution, callback);
  }

  public void getCommand(Callback<Command> callback) {
    getObject(API_ENDPOINT + "command", Command.class, callback);
  }

  public void getExecution(Callback<Execution> callback) {
    getObject(API_ENDPOINT + "execution", Execution.class, callback);
  }

  public void getGlobalState(Callback<GlobalState> callback) {
    getObject(API_ENDPOINT + "globalState", GlobalState.class, callback);
  }

  public void getCameraState(Callback<CameraState> callback) {
    getObject(API_ENDPOINT + "state", CameraState.class, callback);
  }

  public void putImage(File file, Callback<FileResponse> callback, int executionId) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId)
        .PUT()
        .output(BodyHandlers.multipartFile(file))
        .build();
    if (true) {
      RestResponse<String, String> response = client.execute(request, ResponseHandlers.stringHandler());
      generateRestCallback(callback, FileResponse.class).call(response);
      return;
    }
    client.executeAsync(request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, FileResponse.class));
  }

  //RestResponseHandler: InputStream -> Bitmap avec BitmapFactory.decodeStream(is)
  public <T> void getImage(ResponseHandler<T> responseHandler, final Callback<T> callback, int executionId, int fileId) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId + "/" + fileId)
        .PUT()
        .build();
    client.executeAsync(request, responseHandler, ResponseHandlers.stringHandler(),
        new RestClient.Callback<T, String>() {
          @Override
          public void call(RestResponse<T, String> response) {
            if (response.isSuccessful() && !response.isErrorResponse()) {
              callback.onSuccess(response.getResponseCode(), response.getSuccessData());
            } else {
              callback.onError(response.getResponseCode(), new ErrorResponse(response.getErrorData()));
            }
          }
    });
  }

  public Integer getImagesCount(int executionId, final Callback<Integer> callback) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId + "/count")
        .GET()
        .build();
    client.executeAsync(request, ResponseHandlers.intHandler(), ResponseHandlers.stringHandler(), new RestClient.Callback<Integer, String>() {
      @Override
      public void call(RestResponse<Integer, String> restResponse) {
        if (restResponse.isSuccessful()) {
          callback.onSuccess(restResponse.getResponseCode(), restResponse.getSuccessData());
        } else {
          callback.onError(restResponse.getResponseCode(), gson.fromJson(restResponse.getErrorData(), ErrorResponse.class));
        }
      }
    });
    RestResponse<Integer, ?> response = client.execute(request, ResponseHandlers.intHandler());
    return response.isErrorResponse() ? null : response.getSuccessData();
  }

  private <T> void getObject(String endpoint, Class<T> clazz, Callback<T> callback) {
    RestRequest request = RestRequest.builder(endpoint)
        .GET()
        .build();
    client.executeAsync(request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, clazz));
  }

  private <T> void putObject(String endpoint, T object, Callback<T> callback) {
    requestObject(RestRequest.PUT, endpoint, object, callback);
  }

  private <T> void postObject(String endpoint, T object, Callback<T> callback) {
    requestObject(RestRequest.POST, endpoint, object, callback);
  }

  private <T> void requestObject(String method, String endpoint, T object, Callback<T> callback) {
    Class<T> clazz = (Class<T>) object.getClass();
    RestRequest request = RestRequest.builder(endpoint).method(method)
        .output(BodyHandlers.json(gson.toJson(object)))
        .build();
    client.executeAsync(request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, clazz));
  }

  private <T> RestClient.Callback<String, String> generateRestCallback(final Callback<T> callback, final Class<T> clazz) {
    return new RestClient.Callback<String, String>() {
      @Override
      public void call(RestResponse<String, String> response) {
        if (response.isSuccessful() && !response.isErrorResponse()) {
          callback.onSuccess(response.getResponseCode(), gson.fromJson(response.getSuccessData(), clazz));
        } else {
          callback.onError(response.getResponseCode(), gson.fromJson(response.getErrorData(), ErrorResponse.class));
        }
      }
    };
  }

  public void shutdown() {
    client.shutDown();
  }
}
