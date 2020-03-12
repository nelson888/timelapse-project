package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.SeekableByteChannel;

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
    SeekableByteChannel channel = getChannel(path); // the JpgSequenceEncoder will close this channel
    return new JpgSequenceEncoder(channel, fps, () -> uploadVideo(executionId, path));
  }

  private void uploadVideo(int executionId, Path path) throws IOException {
    try (InputStream inputStream = Files.newInputStream(path)) {
      uploadVideo(executionId, inputStream);
    }
  }

  abstract void uploadVideo(int executionId, InputStream inputStream) throws IOException;

  private SeekableByteChannel getChannel(Path path) throws IOException {
    return new FileChannelWrapper(new FileOutputStream(path.toFile()).getChannel());
  }

  @Override
  public long executionFilesCount(int executionId, long fromTimestamp, long toTimestamp) {
    return executionFiles(executionId, fromTimestamp, toTimestamp).count();
  }

}
