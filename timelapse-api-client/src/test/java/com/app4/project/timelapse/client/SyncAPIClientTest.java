package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseSyncClient;
import com.app4.project.timelapse.model.User;

public class SyncAPIClientTest extends APIClientTest {

  @Override
  TimelapseClient newClient(String baseUrl, User user) {
    return new TimelapseSyncClient(baseUrl, user);
  }

}
