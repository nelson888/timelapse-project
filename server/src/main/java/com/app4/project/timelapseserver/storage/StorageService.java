package com.app4.project.timelapseserver.storage;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.util.FileChannelWrapper;
import com.app4.project.timelapseserver.util.IOSupplier;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

  String FOLDER_PREFIX = "execution_";
  String IMAGE_EXTENSION = ".jpg";

  FileData store(int executionId, MultipartFile multipartFile) throws IOException;

  FileData store(int executionId, InputStream inputStream) throws IOException;

  FileChannelWrapper createTempChannel(int taskId) throws IOException;

  int uploadVideo(Path tempVideoPath) throws IOException;

  Resource getImageAsResource(int executionId, int fileId);

  Resource getVideoAsResource(int videoId);

  int nbFiles(int executionId);

  Stream<IOSupplier<byte[]>> executionFiles(int executionId, long fromTimestamp, long toTimestamp);

  FileData getFileData(int executionId, int fileId);

  void deleteForExecution(int executionId);

  long executionFilesCount(int executionId, long fromTimestamp, long toTimestamp);

  void deleteVideo(int videoId);
}
