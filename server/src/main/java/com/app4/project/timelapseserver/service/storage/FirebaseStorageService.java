package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.exception.FileNotFoundException;
import com.app4.project.timelapseserver.util.IOSupplier;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.app4.project.timelapseserver.configuration.ApplicationConfiguration.MAX_EXECUTIONS;

/**
 * All files are stored in the following architecture:
 * <p>
 * execution_executionId/fileId.jpg
 * with 'executionId' and 'fileId' numbers.
 * For example:
 * execution_0/5.jpg
 */
public class FirebaseStorageService extends AbstractStorage {

  private static final String EXECUTION_FILENAME_TEMPLATE = "execution_%d/%d.jpg";
  private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseStorageService.class);
  private final Bucket bucket;
  private final Map<Integer, AtomicInteger> executionFileCount = new ConcurrentHashMap<>();

  public FirebaseStorageService(Path tempDirRoot, Bucket bucket) {
    super(tempDirRoot);
    this.bucket = bucket;
    LOGGER.info("Starting Firebase Storage Service...");
    //looks if there are already some files in the cloud storage
    for (int executionId = 0; executionId < MAX_EXECUTIONS; executionId++) {
      int nextFileId = 1 + StreamSupport.stream(bucket.list(Storage.BlobListOption.prefix(FOLDER_PREFIX + executionId),
        Storage.BlobListOption.fields(Storage.BlobField.NAME))
        .iterateAll().spliterator(), false)
        .map(BlobInfo::getName)
        .filter(name -> name.endsWith(".jpg"))
        .mapToInt(this::extractFileId)
        .max()
        .orElse(-1);
      executionFileCount.put(executionId, new AtomicInteger(nextFileId));
      LOGGER.info("Execution {} has {} images", executionId, nextFileId);
    }
    LOGGER.info("Started Firebase Storage Service successfully");
  }

  private int extractFileId(String blobName) {
    int startIndex = blobName.indexOf("/") + 1;
    return Integer.parseInt(blobName.replace(".jpg", "").substring(startIndex));
  }

  @Override
  public FileData store(int executionId, MultipartFile multipartFile) throws IOException {
    int fileId = executionFileCount.get(executionId).getAndIncrement();
    Blob blob = bucket.create(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId),
      multipartFile.getBytes());
    return new FileData(blob.getSize(), blob.getName(), blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public FileData store(int executionId, InputStream inputStream) {
    int fileId = executionFileCount.get(executionId).getAndIncrement();
    Blob blob = bucket.create(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId), inputStream);
    return new FileData(blob.getSize(), blob.getName(), blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public Resource loadAsResource(int executionId, int fileId) {
    Blob blob = bucket.get(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId));
    if (blob == null || !blob.exists()) {
      throw new FileNotFoundException(String.format("The file with id %d for execution %d doesn't exists",
        fileId, executionId));
    }
    return new ByteArrayResource(blob.getContent(),
      String.format("File for execution %d with id %d", executionId, fileId));
  }

  @Override
  public Resource loadVideoAsResource(int executionId) {
    Blob blob = bucket.get(String.format("execution_%d/video.mp4", executionId));
    if (!blob.exists()) {
      throw new FileNotFoundException("There is no video for execution with id " + executionId);
    }
    return new ByteArrayResource(blob.getContent(),
      String.format("Video for execution %d", executionId));
  }

  @Override
  public int nbFiles(int executionId) {
    return executionFileCount.get(executionId).get();
  }

  @Override
  public Stream<IOSupplier<byte[]>> executionFiles(int executionId, long fromTimestamp) {
    return StreamSupport.stream(
      bucket.list(Storage.BlobListOption.prefix(FOLDER_PREFIX + executionId),
        Storage.BlobListOption.fields(Storage.BlobField.NAME))
        .iterateAll().spliterator(), false)
      .filter(b -> b.getName().endsWith(IMAGE_EXTENSION) && b.getCreateTime() >= fromTimestamp)
      .map(b -> (() -> getContent(b)));
  }

  private byte[] getContent(Blob blob) throws IOException {
    try {
      return blob.getContent();
    } catch (StorageException e) {
      throw new IOException(e);
    }
  }

  @Override
  public FileData getFileData(int executionId, int fileId) {
    Blob blob = bucket.get(String.format(EXECUTION_FILENAME_TEMPLATE, executionId, fileId));
    return new FileData(blob.getSize(), blob.getName(), blob.getCreateTime(), executionId, fileId);
  }

  @Override
  public void deleteForExecution(int executionId) {
    int nbFiles = executionFileCount.get(executionId).get();
    for (int i = 0; i < nbFiles; i++) {
      Blob blob = bucket.get(FOLDER_PREFIX + executionId + "/" + i + ".jpg");
      if (blob != null) {
        blob.delete();
      }
    }
    executionFileCount.remove(executionId);
  }

  @Override
  void uploadVideo(int executionId, InputStream inputStream) {
    // TODO
  }

  @Override
  Logger getLogger() {
    return LOGGER;
  }
}
