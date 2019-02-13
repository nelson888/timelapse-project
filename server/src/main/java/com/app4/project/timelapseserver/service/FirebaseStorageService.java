package com.app4.project.timelapseserver.service;

import static com.app4.project.timelapseserver.configuration.ApplicationConfiguration.MAX_EXECUTIONS;

import com.app4.project.timelapse.model.FileData;

import com.app4.project.timelapseserver.exception.FileNotFoundException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FirebaseStorageService implements StorageService {

  private final Bucket bucket;
  private final Map<Integer, Integer> executionFileCount = new ConcurrentHashMap<>();

  public FirebaseStorageService(Bucket bucket) {
    this.bucket = bucket;

    for (int i = 0; i < MAX_EXECUTIONS; i++) {
      executionFileCount.put(i, 0);
    }
    //looks if there are already some files in the cloud storage
    List<Integer> blobIds = StreamSupport.stream(bucket.list().getValues().spliterator(), false)
      .map(BlobInfo::getName)
      .map(Integer::parseInt)
      .collect(Collectors.toList());
    for (int executionId = 0; executionId < MAX_EXECUTIONS; executionId++) {
      int fileId = 0;
      int fileHash;
      while (blobIds.contains(fileHash = hash(executionId, fileId))) {
        executionFileCount.compute(fileHash, (key, nb) -> nb + 1);
        fileId++;
      }
    }
  }

  @Override
  public FileData store(int executionId, MultipartFile multipartFile) throws IOException {
    int fileId = executionFileCount.get(executionId);
    String fileName = "" + hash(executionId, fileId);
    Blob blob = bucket.create("executions/" + executionId + "/" + fileName,
      multipartFile.getBytes());
    return new FileData(blob.getSize(), fileName, blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public Resource loadAsResource(int executionId, int fileId) {
    Blob blob = bucket.get(String.valueOf(hash(executionId, fileId)));
    if (!blob.exists()) {
      throw new FileNotFoundException(String.format("The file with id %d for execution %d doesn't exists",
        fileId, executionId));
    }
    return new ByteArrayResource(blob.getContent(),
      String.format("File for execution %d with id %d", executionId, fileId));
  }

  @Override
  public int nbFiles(int executionId) {
    return executionFileCount.get(executionId);
  }

  @Override
  public FileData getFileData(int executionId, int fileId) {
    String h = String.valueOf(hash(executionId, fileId));
    Blob blob = bucket.get(h);
    return new FileData(blob.getSize(), h, blob.getCreateTime(), executionId, fileId);
  }

  private int hash(int executionId, int fileId) {
    return Objects.hash(executionId, fileId) - 961; //so that it starts at 0
  }
}
