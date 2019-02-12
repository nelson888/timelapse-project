package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.FileData;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FirebaseStorageService  implements StorageService {

  private final Bucket bucket;

  public FirebaseStorageService(Bucket bucket) {
    this.bucket = bucket;
  }

  @Override
  public FileData store(int executionId, MultipartFile multipartFile) throws IOException {
    String fileName = multipartFile.getOriginalFilename();
    Blob blob = bucket.create("executions/" + executionId + "/" + fileName,
      multipartFile.getBytes());
    return new FileData(blob.getSize(), fileName, System.currentTimeMillis(), executionId, 0); //TODO find a way to determine ID
  }

  @Override
  public Resource loadAsResource(int executionId, int fileId) {
    return null;
  }

  @Override
  public int nbFiles(int executionId) {
    return 0;
  }

  @Override
  public FileData getFileData(int executionId, int fileId) {
    return null;
  }
}
