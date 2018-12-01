package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseClient;

public class AsyncAPIClientTest extends APIClientTest {

  @Override
  TimelapseClient newClient(String baseUrl) {
    return new TimelapseAsyncClient(baseUrl);
  }

}
