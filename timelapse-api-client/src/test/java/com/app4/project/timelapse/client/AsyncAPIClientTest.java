package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.User;

public class AsyncAPIClientTest extends APIClientTest {

  @Override
  TimelapseClient newClient(String baseUrl) {
    return new TimelapseAsyncClient(baseUrl);
  }

}
