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
import com.tambapps.http.restclient.util.ISSupplier;

import java.io.File;

//TODO add overides
abstract class AbstractTimelapseClient implements TimelapseClient {

  private static final String API_ENDPOINT = "api/";
  private static final String FILE_STORAGE_ENDPOINT = "files/";
  private final RestClient client;
  private final Gson gson = new Gson();

  //TODO handle authentication with server and jwt
  public AbstractTimelapseClient(String baseUrl, User user) {
    client = new RestClient(baseUrl);
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

  public void postCommand(Command command, Callback<Command> callback) {
    postObject(API_ENDPOINT + "command", command, callback);
  }

  public void putCameraState(CameraState cameraState, Callback<CameraState> callback) {
    putObject(API_ENDPOINT + "state", cameraState, callback);
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

  public void putImage(ISSupplier isSupplier, Callback<FileResponse> callback, int executionId) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId)
        .PUT()
        .output(BodyHandlers.multipartStream(isSupplier, "image"))
        .build();
    executeRequest(client, request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, FileResponse.class));
  }

  public void putImage(File file, Callback<FileResponse> callback, int executionId) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId)
        .PUT()
        .output(BodyHandlers.multipartFile(file))
        .build();
    executeRequest(client, request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, FileResponse.class));
  }

  //RestResponseHandler: InputStream -> Bitmap avec BitmapFactory.decodeStream(is)
  public <T> void getImage(ResponseHandler<T> responseHandler, final Callback<T> callback,
      int executionId, int fileId) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId + "/" + fileId)
        .GET()
        .build();
    executeRequest(client, request, responseHandler, ResponseHandlers.stringHandler(),
        new RestClient.Callback<T, String>() {
          @Override
          public void call(RestResponse<T, String> response) {
            if (response.isSuccessful() && !response.isErrorResponse()) {
              callback.onSuccess(response.getResponseCode(), response.getSuccessData());
            } else {
              callback.onError(response.getResponseCode(),
                  gson.fromJson(response.getErrorData(), ErrorResponse.class));
            }
          }
        });
  }

  public void getImagesCount(int executionId, final Callback<Integer> callback) {
    RestRequest request = RestRequest.builder(FILE_STORAGE_ENDPOINT + executionId + "/count")
        .GET()
        .build();
    executeRequest(client, request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, Integer.class));
  }

  public void shutdown() {
    client.shutDown();
  }

  private <T> void getObject(String endpoint, Class<T> clazz, Callback<T> callback) {
    RestRequest request = RestRequest.builder(endpoint)
        .GET()
        .build();
    executeRequest(client, request, ResponseHandlers.stringHandler(),
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
    executeRequest(client, request, ResponseHandlers.stringHandler(),
        generateRestCallback(callback, clazz));
  }

  abstract <T> void executeRequest(RestClient client,
      RestRequest request, ResponseHandler<T> responseHandler, RestClient.Callback<T, T> callback);

  abstract <T1, T2> void executeRequest(RestClient client,
      RestRequest request, ResponseHandler<T1> responseHandler,
      ResponseHandler<T2> errorHandler, RestClient.Callback<T1, T2> callback);

  private <T> RestClient.Callback<String, String> generateRestCallback(final Callback<T> callback,
      final Class<T> clazz) {
    return new RestClient.Callback<String, String>() {
      @Override
      public void call(RestResponse<String, String> response) {
        if (response.isSuccessful()) {
          callback.onSuccess(response.getResponseCode(),
              gson.fromJson(response.getSuccessData(), clazz));
        } else if (response.isErrorResponse()) {
          callback.onError(response.getResponseCode(), gson.fromJson(response.getErrorData(),
              ErrorResponse.class));
        } else { //has exception
          callback.onError(RestResponse.REQUEST_NOT_SENT,
              new ErrorResponse("Request failed to be sent", response.getException().getMessage()));
        }
      }
    };
  }

}