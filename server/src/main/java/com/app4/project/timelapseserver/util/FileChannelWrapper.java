package com.app4.project.timelapseserver.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class FileChannelWrapper extends org.jcodec.common.io.FileChannelWrapper {

  private final Path tempFile;
  public FileChannelWrapper(FileChannel ch, Path tempFile) throws FileNotFoundException {
    super(ch);
    this.tempFile = tempFile;
  }

  public Path getTempFilePath() {
    return tempFile;
  }

  @Override
  public void close() throws IOException {
    super.close();
    tempFile.toFile().delete();
  }
}
