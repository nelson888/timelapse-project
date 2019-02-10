package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseSyncClient;

public class SyncAPIClientTest extends APIClientTest {

  @Override
  TimelapseClient newClient(String baseUrl) {
    return new TimelapseSyncClient(baseUrl);
  }

}
