package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapse.model.VideoMetadata;

import java.util.List;
import java.util.Optional;

public interface VideoMetadataRepository {

  List<VideoMetadata> getAll();

  List<VideoMetadata> getAllByExecutionId(int executionId);

  Optional<VideoMetadata> getByVideoId(int videoId);

  boolean remove(int videoId);

  void add(VideoMetadata metadata);

  int count();

}
