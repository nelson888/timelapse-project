package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.FileResponse;
import com.app4.project.timelapseserver.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/files")
public class FileController {

  private final StorageService storageService;

  public FileController(StorageService storageService) {
    this.storageService = storageService;
  }

  @PutMapping("/{executionId}")
  public ResponseEntity uploadImage(@PathVariable int executionId,
      @RequestParam("image") MultipartFile multipartFile) {
    File file = storageService.store(executionId, multipartFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new FileResponse(file.length(), file.getName()));
  }

  @GetMapping("/{executionId}/count")
  public ResponseEntity nbImages(@PathVariable int executionId) {
    return ResponseEntity.ok().body(storageService.nbFiles(executionId));
  }

  @GetMapping("/{executionId}/{fileId}")
  @ResponseBody
  public ResponseEntity serveFile(@PathVariable int executionId, @PathVariable int fileId) {
    Resource file = storageService.loadAsResource(executionId, fileId);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }

}
