package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.FileNotFoundException;
import com.app4.project.timelapseserver.exception.FileStorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
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

@Service
public class StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
  private static final String FOLDER_PREFIX = "execution_";
  private static final String IMAGE_FORMAT = ".png";

  private final Path rootPath;
  private final Map<Integer, Path> fileMap = new ConcurrentHashMap<>();
  private final Map<Integer, FileData> fileDataMap = new ConcurrentHashMap<>();

  public StorageService(Path rootPath) {
    this.rootPath = rootPath;
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
    LOGGER.info("Storage Service was successfully instantiated");
  }

  public FileData store(int executionId, MultipartFile multipartFile) {
    LOGGER.info("attempting to store {} for executionId {}...", multipartFile.getOriginalFilename(), executionId);

    Path executionPath = rootPath.resolve(FOLDER_PREFIX + executionId);

    try (InputStream inputStream = multipartFile.getInputStream()) {
      int key = hash(executionId, fileMap.size());
      Path filePath = executionPath.resolve("image_" + key + IMAGE_FORMAT);
      LOGGER.info("Creating file in path {}", filePath);
      File file = filePath.toFile();
      if (!file.createNewFile()) {
        LOGGER.error("Couldn't create new file (unknown error)");
        throw new FileStorageException("Error while creating new file (unknown error)");
      }
      Files.copy(inputStream, filePath,
          StandardCopyOption.REPLACE_EXISTING);
      fileMap.put(key, filePath);
      FileData fileData = new FileData(file.length(), file.getName(), System.currentTimeMillis(), executionId, key);
      fileDataMap.put(key, fileData);
      LOGGER.info("Saved file successfully");
      return fileData;
    } catch (IOException e) {
      LOGGER.error("Error while writing file", e);
      throw new FileStorageException(e.getMessage(), e);
    }
  }

  private int hash(int executionId, int fileId) {
    return Objects.hash(executionId, fileId);
  }

  public Resource loadAsResource(int executionId, int fileId) {
    try {
      Path file = fileMap.get(hash(executionId, fileId));
      if (file == null) {
        throw new FileNotFoundException(String.format("The file with id %d for execution %d doesn't exists", fileId, executionId));
      }
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      }
      else {
        LOGGER.error("Couldn't read file: " + file.getFileName().toString());
        throw new FileStorageException("Couldn't read file: " + file.getFileName().toString());
      }
    }
    catch (MalformedURLException e) {
      throw new FileStorageException("Couldn't read url of requested file", e);
    }
  }

  public int nbFiles(int executionId) {
    return Objects.requireNonNull(rootPath.resolve(FOLDER_PREFIX + executionId).toFile().list()).length;
  }

  public FileData getFileData(int executionId, int fileId) {
    FileData fileData =  fileDataMap.get(hash(executionId, fileId));
    if (fileData == null) {
      throw new FileNotFoundException(
        String.format("The file with id %d for execution %d doesn't exists", fileId, executionId));
    }
    return fileData;
  }
}
