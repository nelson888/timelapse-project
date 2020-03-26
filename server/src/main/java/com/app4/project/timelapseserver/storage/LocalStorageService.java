package com.app4.project.timelapseserver.storage;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.FileNotFoundException;
import com.app4.project.timelapseserver.exception.FileStorageException;
import com.app4.project.timelapseserver.util.IOSupplier;
import com.google.cloud.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class LocalStorageService extends AbstractStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageService.class);

  private final Path rootPath;
  private final Map<Integer, AtomicInteger> executionFileCount = new ConcurrentHashMap<>();

  public LocalStorageService(Path tempDirRoot, Path rootPath) {
    super(tempDirRoot);
    this.rootPath = rootPath;
    LOGGER.info("Starting Local Storage Service...");
    File root = rootPath.toFile();
    if (!Files.exists(rootPath) && !root.mkdir()) {
      LOGGER.error("Couldn't find or create root directory");
      throw new RuntimeException(rootPath + " doesn't exists and couldn't be created");
    }
    LOGGER.info("Checking/Creating executions directories...");
    for (int i = 0; i < ApplicationConfiguration.MAX_EXECUTIONS; i++) {
      File execDir = new File(root, FOLDER_PREFIX + i);
      if (!execDir.exists() && !execDir.mkdir()) {
        LOGGER.error("Couldn't create needed directory");
        throw new RuntimeException(rootPath + "couldn't create directory " + execDir.getName());
      }
    }
    LOGGER.info("Local Storage Service was successfully instantiated");
  }

  private static String nDigitsNumber(int number, int n) {
    StringBuilder sNumber = new StringBuilder().append(number);
    while (sNumber.length() < n) {
      sNumber.insert(0, '0');
    }
    return sNumber.toString();
  }

  @Override
  public FileData store(int executionId, MultipartFile multipartFile) {
    LOGGER.info("attempting to store {} for executionId {}...", multipartFile.getOriginalFilename(), executionId);

    Path executionPath = rootPath.resolve(FOLDER_PREFIX + executionId);

    try (InputStream inputStream = multipartFile.getInputStream()) {
      return writeInputStream(inputStream, executionId, executionPath);
    } catch (IOException e) {
      LOGGER.error("Error while writing file", e);
      throw new FileStorageException(e.getMessage(), e);
    }
  }

  @Override
  public FileData store(int executionId, InputStream inputStream) {
    LOGGER.info("attempting to store file for executionId {}...", executionId);
    Path executionPath = rootPath.resolve(FOLDER_PREFIX + executionId);
    return writeInputStream(inputStream, executionId, executionPath);
  }

  private FileData writeInputStream(InputStream inputStream, int executionId, Path executionPath) {
    int fileId = getFileCount(executionId).get();
    try {
      Path filePath = executionPath.resolve(getFileName(fileId));
      LOGGER.info("Creating file in path {}", filePath);
      File file = filePath.toFile();
      if (!file.createNewFile()) {
        LOGGER.error("Couldn't create new file (unknown error)");
        throw new FileStorageException("Error while creating new file (unknown error)");
      }
      Files.copy(inputStream, filePath,
        StandardCopyOption.REPLACE_EXISTING);
      FileData fileData = new FileData(file.length(), file.getName(), System.currentTimeMillis(), executionId, fileId);
      LOGGER.info("Saved file successfully");
      getFileCount(executionId).getAndIncrement();
      return fileData;
    } catch (IOException e) {
      LOGGER.error("Error while writing file", e);
      throw new FileStorageException(e.getMessage(), e);
    }
  }

  @Override
  public Resource loadVideoAsResource(int executionId) {
    File file = rootPath.resolve(FOLDER_PREFIX + executionId + "video.mp4").toFile();
    if (!file.exists()) {
      throw new FileNotFoundException(String.format("There is no video for for execution %d", executionId));
    }
    try {
      return new UrlResource(file.toURI());
    } catch (MalformedURLException e) {
      throw new FileStorageException("Couldn't read video for execution " + executionId);
    }
  }

  private String getFileName(int fileId) {
    return nDigitsNumber(fileId, 5) + IMAGE_EXTENSION;
  }

  @Override
  public Resource loadAsResource(int executionId, int fileId) {
    try {
      Path executionPath = rootPath.resolve(FOLDER_PREFIX + executionId);
      File file = executionPath.resolve(getFileName(fileId)).toFile();
      if (!file.exists()) {
        throw new FileNotFoundException(String.format("The file with id %d for execution %d doesn't exists", fileId, executionId));
      }
      Resource resource = new UrlResource(file.toURI());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        LOGGER.error("Couldn't read file: {}", file.getName());
        throw new FileStorageException("Couldn't read file: " + file.getName());
      }
    } catch (MalformedURLException e) {
      throw new FileStorageException("Couldn't read url of requested file", e);
    }
  }

  @Override
  public int nbFiles(int executionId) {
    return Objects.requireNonNull(rootPath.resolve(FOLDER_PREFIX + executionId).toFile().list()).length;
  }

  @Override
  public Stream<IOSupplier<byte[]>> executionFiles(int executionId, long fromTimestamp, long toTimestamp) {
    return Arrays.stream(rootPath.resolve(FOLDER_PREFIX + executionId).toFile().listFiles())
      .filter(f -> f.getName().endsWith(IMAGE_EXTENSION) && isTimestampInRange(f, fromTimestamp, toTimestamp))
      .map(f -> (() -> Files.readAllBytes(f.toPath())));
  }

  private boolean isTimestampInRange(File f, long fromTimestamp, long toTimestamp) {
    long timestamp = creationTime(f);
    return timestamp >= fromTimestamp && timestamp<= toTimestamp;
  }

  private long creationTime(File f) {
    try {
      return ((FileTime) Files.getAttribute(f.toPath(), "creationTime")).toMillis();
    } catch (IOException e) {
      return Long.MIN_VALUE;
    }
  }

  @Override
  public FileData getFileData(int executionId, int fileId) {
    Path executionPath = rootPath.resolve(FOLDER_PREFIX + executionId);
    Path filePath = executionPath.resolve(getFileName(fileId));
    File file = filePath.toFile();
    if (file.exists()) {
      return new FileData(file.length(), file.getName(), file.lastModified(), executionId, fileId);
    }
    throw new FileNotFoundException(
      String.format("The file with id %d for execution %d doesn't exists", fileId, executionId));
  }

  @Override
  public void deleteForExecution(final int executionId) {
    File eDir = rootPath.resolve(FOLDER_PREFIX + executionId).toFile();
    File[] files = eDir.listFiles();
    if (files == null) {
      return;
    }
    Stream.of(files).forEach(File::delete);
  }

  private AtomicInteger getFileCount(int executionId) {
    return executionFileCount.computeIfAbsent(executionId, e -> new AtomicInteger(0));
  }

  @Override
  protected int getVideoCount() {
    try {
      return (int) Files.list(rootPath).map(Path::toFile)
        .filter(f -> f.getName().startsWith(VIDEO_FILE_PREFIX))
        .count();
    } catch (IOException e) {
      LOGGER.error("Couldn't get number of files", e);
      throw new StorageException(e);
    }
  }

  @Override
  void uploadVideo(int videoId, InputStream inputStream) throws IOException {
    File file = rootPath.resolve(VIDEO_FILE_PREFIX + videoId + VIDEO_FILE_EXTENSION).toFile();
    try (OutputStream os = new FileOutputStream(file)) {
      inputStream.transferTo(os);
    }
  }
}
