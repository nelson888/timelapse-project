package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapseserver.util.FileChannelWrapper;

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
  public FileChannelWrapper createTempChannel(int taskId) throws IOException {
    Path path = tempDirRoot.resolve(String.format("video_%d.mp4", taskId));
    return new FileChannelWrapper(new FileOutputStream(path.toFile()).getChannel(), path);
  }

  @Override
  public int uploadVideo(int taskId, Path tempVideoPath) throws IOException {
    int videoId = generateVideoId(taskId);
    try (InputStream inputStream = Files.newInputStream(tempVideoPath)) {
      uploadVideo(videoId, inputStream);
    }
    return videoId;
  }

  protected int generateVideoId(int taskId) {
    return taskId + 250; // random operation to differentiate taskId from videoId
  }

  abstract void uploadVideo(int videoId, InputStream inputStream) throws IOException;

  @Override
  public long executionFilesCount(int executionId, long fromTimestamp, long toTimestamp) {
    return executionFiles(executionId, fromTimestamp, toTimestamp).count();
  }

}
