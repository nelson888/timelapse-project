package com.app4.project.timelapse.client;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.FileResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Pour ces tests, il faut faire tourner le serveur localement
 */
public class AsyncAPIClientTest {

  private static final int TIMEOUT = 4;
  private TimelapseClient client;
  private CountDownLatch latch; //allows to wait until async code is executed

  @Before
  public void init() {
    client = new TimelapseClient(null);
     latch = new CountDownLatch(1);
  }

  @After
  public void dispose() {
    client.shutdown();
  }

  @Test
  public void getTest() throws InterruptedException {
    client.getCameraState(new Callback<CameraState>() {
      @Override
      public void onSuccess(int responseCode, CameraState data) {
        assertNotNull("Shouldn't be null", data);
        latch.countDown();
      }

      @Override
      public void onError(int responseCode, ErrorResponse response) {

      }
    });
    assertTrue("Should be true", latch.await(TIMEOUT, TimeUnit.SECONDS));
  }

  @Test
  public void getImagesCountTest() throws InterruptedException {
    client.getImagesCount(0, new Callback<Integer>() {
      @Override
      public void onSuccess(int responseCode, Integer data) {
        assertNotNull("Shouldn't be null", data);
        latch.countDown();
      }

      @Override
      public void onError(int responseCode, ErrorResponse response) {

      }
    });
    assertTrue("Should be true", latch.await(TIMEOUT, TimeUnit.SECONDS));
  }

  @Test
  public void putTest() throws InterruptedException {
    final CameraState state = new CameraState();
    state.setCameraWorking(true);
    state.setSleeping(true);
    client.putCameraState(state, new Callback<CameraState>() {
      @Override
      public void onSuccess(int responseCode, CameraState data) {
        assertNotNull("Shouldn't be null", data);
        assertEquals("Should be equal", state, data);
        System.err.println(data);
        latch.countDown();
      }

      @Override
      public void onError(int responseCode, ErrorResponse response) {

      }
    });
    assertTrue("Should be true", latch.await(TIMEOUT, TimeUnit.SECONDS));
  }

  @Test
  public void putFile() throws InterruptedException {
    String filePath = "/home/nelson/test.txt";
    client.putImage(new File(filePath), new Callback<FileResponse>() {
      @Override
      public void onSuccess(int responseCode, FileResponse data) {

      }

      @Override
      public void onError(int responseCode, ErrorResponse response) {

      }
    }, 0);

    assertTrue("Should be true", latch.await(TIMEOUT, TimeUnit.SECONDS));
  }

  @Test
  public void getFile() throws InterruptedException {
    /*
    client.getFile("0/0", new APIClient.Callback<InputStream>() {
      @Override
      public void onSuccess(int responseCode, InputStream data) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(data));
        String line;
        try {
          while((line = reader.readLine()) != null) {
            System.out.println(line);
          }
        } catch (Exception e) {
          System.out.println(e);
        }
        latch.countDown();
      }

      @Override
      public void onError(int responseCode, ErrorResponse response) {

      }
    });

    assertTrue("Should be true", latch.await(TIMEOUT, TimeUnit.SECONDS));
    */
  }
}
