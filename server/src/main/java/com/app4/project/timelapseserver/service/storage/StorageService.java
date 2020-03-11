package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import com.app4.project.timelapseserver.util.IOSupplier;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public interface StorageService {

  String FOLDER_PREFIX = "execution_";
  String IMAGE_EXTENSION = ".jpg";

  FileData store(int executionId, MultipartFile multipartFile) throws IOException;

  FileData store(int executionId, InputStream inputStream) throws IOException;

  JpgSequenceEncoder newEncoderForExecution(int executionId, int fps) throws IOException;

  Resource loadAsResource(int executionId, int fileId);

  Resource loadVideoAsResource(int executionId);

  int nbFiles(int executionId);

  Stream<IOSupplier<byte[]>> executionFiles(int executionId, long fromTimestamp);

  FileData getFileData(int executionId, int fileId);

  void deleteForExecution(int executionId);

}
