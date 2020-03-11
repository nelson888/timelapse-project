package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.FileData;
import com.app4.project.timelapseserver.configuration.ApplicationConfiguration;
import com.app4.project.timelapseserver.exception.BadRequestException;
import com.app4.project.timelapseserver.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/storage")
public class StorageController {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageController.class);
  private final StorageService storageService;

  public StorageController(StorageService storageService) {
    this.storageService = storageService;
  }

  @PostMapping("/{executionId}")
  public ResponseEntity uploadImage(@PathVariable int executionId,
                                    @RequestParam("image") MultipartFile multipartFile) throws IOException {
    idCheck(executionId);
    FileData fileData = storageService.store(executionId, multipartFile);
    LOGGER.info("Uploaded new image: {}", fileData);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(fileData);
  }

  @GetMapping("/{executionId}/count")
  public ResponseEntity nbImages(@PathVariable int executionId) {
    idCheck(executionId);
    return ResponseEntity.ok().body(storageService.nbFiles(executionId));
  }

  @GetMapping("/{executionId}/video") // TODO add on swagger
  public ResponseEntity saveVideo(@PathVariable int executionId) {
    idCheck(executionId);
    Resource file = storageService.loadVideoAsResource(executionId);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
      "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }

  @GetMapping("/{executionId}/{fileId}")
  @ResponseBody
  public ResponseEntity serveFile(@PathVariable int executionId, @PathVariable int fileId) {
    idCheck(executionId);
    Resource file = storageService.loadAsResource(executionId, fileId);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
      "attachment; filename=\"" + fileId + ".jpg\"").body(file);
  }

  @GetMapping("/{executionId}/{fileId}/data")
  @ResponseBody
  public ResponseEntity getFileData(@PathVariable int executionId, @PathVariable int fileId) {
    idCheck(executionId);
    FileData fileData = storageService.getFileData(executionId, fileId);
    return ResponseEntity
      .ok()
      .body(fileData);
  }

  private void idCheck(int executionId) {
    if (executionId < 0 || executionId >= ApplicationConfiguration.MAX_EXECUTIONS) {
      throw new BadRequestException("Execution with id " + executionId + " cannot exist");
    }
  }
}
