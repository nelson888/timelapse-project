package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.SeekableByteChannel;
import org.slf4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class AbstractStorage implements StorageService {

  private final Path tempDirRoot;

  protected AbstractStorage(Path tempDirRoot) {
    this.tempDirRoot = tempDirRoot;
  }

  @Override
  public JpgSequenceEncoder newEncoderForExecution(int executionId, int fps) throws IOException {
    Path path = tempDirRoot.resolve(String.format("execution_%d.mp4", executionId));
    SeekableByteChannel channel = getChannel(path); // the JpgSequenceEncoder will close it
    return new JpgSequenceEncoder(channel, fps, () -> uploadVideo(executionId, path));
  }

  private void uploadVideo(int executionId, Path path) {
    try (InputStream inputStream = Files.newInputStream(path)) {
      uploadVideo(executionId, inputStream);
    } catch (IOException e) {
      getLogger().error("Couldn't save video for execution {}", executionId, e);
    }
  }

  abstract void uploadVideo(int executionId, InputStream inputStream);

  private SeekableByteChannel getChannel(Path path) throws IOException {
    return new FileChannelWrapper(new FileOutputStream(path.toFile()).getChannel());
  }

  abstract Logger getLogger();
}
