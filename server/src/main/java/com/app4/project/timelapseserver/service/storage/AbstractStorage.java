package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.SeekableByteChannel;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

abstract class AbstractStorage implements StorageService {

  private final FileSystem inMemoryFileSystem;

  protected AbstractStorage(FileSystem inMemoryFileSystem) {
    this.inMemoryFileSystem = inMemoryFileSystem;
  }

  @Override
  public JpgSequenceEncoder newEncoderForExecution(int executionId, int fps) throws IOException {
    Path path = inMemoryFileSystem.getPath(String.format("execution_%d.mp4", executionId));
    SeekableByteChannel channel = getChannel(path); // the JpgSequenceEncoder will close it
    return new JpgSequenceEncoder(channel, fps, () -> uploadVideo(executionId, path));
  }

  private void uploadVideo(int executionId, Path path) {
    try (InputStream inputStream = inMemoryFileSystem.provider().newInputStream(path)) {
      uploadVideo(executionId, inputStream);
    } catch (IOException e) {
      getLogger().error("Couldn't save video for execution {}", executionId, e);
    }
  }

  abstract void uploadVideo(int executionId, InputStream inputStream);

  private SeekableByteChannel getChannel(Path path) throws IOException {
    return new FileChannelWrapper(
      inMemoryFileSystem.provider().newFileChannel(path, Set.of(StandardOpenOption.CREATE)));
  }

  abstract Logger getLogger();
}
