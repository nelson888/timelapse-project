package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.FileNotFoundException;
import com.app4.project.timelapseserver.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class LocalStorageService implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageService.class);
  private static final String FOLDER_PREFIX = "execution_";
  private static final String IMAGE_FORMAT = ".png";

  private final Path rootPath;
  private final Map<Integer, AtomicInteger> executionFileCount = new ConcurrentHashMap<>();

  public LocalStorageService(Path rootPath) {
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
    StringBuilder sNumber = (new StringBuilder()).append(number);

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

  private String getFileName(int fileId) {
    return nDigitsNumber(fileId, 5) + IMAGE_FORMAT;
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

}
