package com.app4.project.timelapseserver.util;

import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class FileChannelWrapper extends org.jcodec.common.io.FileChannelWrapper {

  private final Path path;
  public FileChannelWrapper(FileChannel ch, Path path) throws FileNotFoundException {
    super(ch);
    this.path = path;
  }

  public Path getPath() {
    return path;
  }
}
