package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.User;

/**
 * Pour ces tests, il faut faire tourner le serveur localement
 */
public class AsyncAPIClientTest extends APIClientTest {



  @Override TimelapseClient newClient(String baseUrl, User user) {
    return new TimelapseAsyncClient(baseUrl, user);
  }
}
