package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapseserver.exception.NotFoundException;
import com.app4.project.timelapseserver.repository.VideoMetadataRepository;
import com.app4.project.timelapseserver.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage/videos")
public class VideoStorageController {

  private final StorageService storageService;
  private final VideoMetadataRepository videoMetadataRepository;

  public VideoStorageController(StorageService storageService, VideoMetadataRepository videoMetadataRepository) {
    this.storageService = storageService;
    this.videoMetadataRepository = videoMetadataRepository;
  }

  private ResponseEntity multipartResponse(Resource resource) {
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
      "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
  }

  @GetMapping("/{videoId}")
  public ResponseEntity serveVideo(@PathVariable int videoId) {
    if (videoMetadataRepository.getByVideoId(videoId).isEmpty()) {
      throw new NotFoundException("Video with id " + videoId + " was not found");
    }
    Resource file = storageService.getVideoAsResource(videoId);
    return multipartResponse(file);
  }

}
