package com.app4.project.timelapse.api.client;

import com.app4.project.timelapse.model.User;

import com.tambapps.http.restclient.RestClient;
import com.tambapps.http.restclient.request.RestRequest;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;

public class TimelapseAsyncClient extends AbstractTimelapseClient {

  public TimelapseAsyncClient(String baseUrl, User user) {
    super(baseUrl, user);
  }

  @Override
  <T> void executeRequest(RestClient client, RestRequest request,
      ResponseHandler<T> responseHandler, RestClient.Callback<T, T> callback) {
    client.executeAsync(request, responseHandler, callback);
  }

}
