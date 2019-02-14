package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.FileData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

  FileData store(int executionId, MultipartFile multipartFile) throws IOException;

  FileData store(int executionId, InputStream inputStream) throws IOException;

  Resource loadAsResource(int executionId, int fileId);

  int nbFiles(int executionId);

  FileData getFileData(int executionId, int fileId);

  void deleteForExecution(int executionId);

}
