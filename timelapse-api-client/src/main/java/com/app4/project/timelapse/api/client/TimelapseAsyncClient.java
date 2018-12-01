package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.User;

import com.tambapps.http.restclient.RestClient;
import com.tambapps.http.restclient.request.RestRequest;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;

public class TimelapseAsyncClient extends AbstractTimelapseClient {

  public TimelapseAsyncClient(String baseUrl) {
    super(baseUrl);
  }

  @Override
  <T> void executeRequest(RestClient client, RestRequest request,
      ResponseHandler<T> responseHandler, RestClient.Callback<T, T> callback) {
    client.executeAsync(request, responseHandler, callback);
  }

  @Override
  <T1, T2> void executeRequest(RestClient client, RestRequest request,
      ResponseHandler<T1> responseHandler, ResponseHandler<T2> errorHandler,
      RestClient.Callback<T1, T2> callback) {
    client.executeAsync(request, responseHandler, errorHandler, callback);
  }
}
