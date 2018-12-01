package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.FileData;
import com.tambapps.http.restclient.request.handler.response.ResponseHandlers;
import com.tambapps.http.restclient.util.ISSupplier;
import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.*;

public class BasicClientTest {

  private TimelapseBasicClient client = new TimelapseBasicClient(APIClientTest.BASE_URL);

  @Test
  public void getTest() {
    TimelapseResponse<CameraState> response = client.getCameraState();
    assertTrue(response.isSuccessful());
    assertNotNull("Shouldn't be null", response.getData());
  }

  @Test
  public void getImagesCountTest() {
    TimelapseResponse<Integer> response = client.getImagesCount(0);

    assertTrue(response.isSuccessful());
    assertNotNull("Shouldn't be null", response.getData());
  }

  @Test
  public void postTest() {
    final CameraState state = new CameraState();
    state.setCameraWorking(true);
    state.setSleeping(true);
    TimelapseResponse<CameraState> response = client.putCameraState(state);

    CameraState data = response.getData();
    assertTrue(response.isSuccessful());
    assertNotNull("Shouldn't be null", data);
    assertEquals("Should be equal", state, data);
  }

  private InputStream getResourceStream() {
    return AsyncAPIClientTest.class.getResourceAsStream("/file.txt");
  }

  @Test
  public void putFile() {
    TimelapseResponse<FileData> response = client.putImage(
      new ISSupplier() {
        @Override
        public InputStream get() {
          return getResourceStream();
        }
      }, 0);

    assertTrue(response.isSuccessful());
    assertNotNull("Shouldn't be null", response.getData());
  }

  @Test
  public void getFile() {
    final String fileData = new Scanner(getResourceStream()).useDelimiter("\\A").next();
    TimelapseResponse<String> response = client.getImage(ResponseHandlers.stringHandler(), 0, 0);

    String data = response.getData();
    assertNotNull("Shouldn't be null", data);
    assertEquals("Should be equal", fileData, data);
  }
}
