package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.FileData;

import com.app4.project.timelapseserver.exception.FileNotFoundException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * All files are stored in the following architecture:
 *
 * execution_executionId/fileId.jpg
 * with 'executionId' and 'fileId' numbers.
 * For example:
 * execution_0/5.jpg
 */
public class FirebaseStorageService implements StorageService {

  private static final String EXECUTION_FILENAME_TEMPLATE = "execution_%d/%d.jpg";
  private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseStorageService.class);
  private final Bucket bucket;
  private final Map<Integer, AtomicInteger> executionFileCount = new ConcurrentHashMap<>();

  public FirebaseStorageService(Bucket bucket) {
    this.bucket = bucket;
    LOGGER.info("Starting Firebase Storage Service...");
    //looks if there are already some files in the cloud storage
    StreamSupport.stream(bucket.list().getValues().spliterator(), false)
      .map(BlobInfo::getName)
      .filter(name -> name.endsWith(".jpg")) //get only images
      .map(name -> name.replace(".jpg", "")) //to only get the number
      .forEach(blobName -> {
        int executionId = extractExecutionId(blobName);
        int fileId = Integer.parseInt(blobName.substring(blobName.indexOf("/") + 1));
        AtomicInteger fileCount = getFileCount(executionId);
        fileCount.updateAndGet(value -> Math.max(value, fileId + 1));
      });

    for (Map.Entry<Integer, AtomicInteger> entry : executionFileCount.entrySet()) {
      LOGGER.info("Execution {} has {} images", entry.getKey(), entry.getValue().get());
    }
    LOGGER.info("Started Firebase Storage Service successfully");
  }

  private AtomicInteger getFileCount(int executionId) {
    return executionFileCount.computeIfAbsent(executionId, e -> new AtomicInteger(0));
  }

  @Override
  public FileData store(int executionId, MultipartFile multipartFile) throws IOException {
    int fileId = getFileCount(executionId).getAndIncrement();
    Blob blob = bucket.create(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId),
      multipartFile.getBytes());
    return new FileData(blob.getSize(), blob.getName(), blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public FileData store(int executionId, InputStream inputStream) {
    int fileId = getFileCount(executionId).getAndIncrement();
    Blob blob = bucket.create(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId), inputStream);
    return new FileData(blob.getSize(), blob.getName(), blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public Resource loadAsResource(int executionId, int fileId) {
    Blob blob = bucket.get(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId));
    if (!blob.exists()) {
      throw new FileNotFoundException(String.format("The file with id %d for execution %d doesn't exists",
        fileId, executionId));
    }
    return new ByteArrayResource(blob.getContent(),
      String.format("File for execution %d with id %d", executionId, fileId));
  }

  @Override
  public int nbFiles(int executionId) {
    return getFileCount(executionId).get();
  }

  @Override
  public FileData getFileData(int executionId, int fileId) {
    Blob blob = bucket.get(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId));
    return new FileData(blob.getSize(), blob.getName(), blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public void deleteForExecution(int executionId) {
    int nbFiles = getFileCount(executionId).get();
    for (int i = 0; i < nbFiles; i++) {
      Blob blob = bucket.get("execution_" + executionId + "/" + i + ".jpg");
      if (blob != null) {
        blob.delete();
      }
    }
    executionFileCount.remove(executionId);
  }

  private int extractExecutionId(String blobName) {
    int beginning = blobName.indexOf("_") + 1;
    return Integer.parseInt(blobName.substring(beginning, blobName.indexOf("/")));
  }
}
