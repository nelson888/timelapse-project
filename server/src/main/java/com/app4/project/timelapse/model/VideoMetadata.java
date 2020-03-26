package com.app4.project.timelapse.model;

import lombok.Value;

@Value
public class VideoMetadata {

  int videoId;
  int executionId;
  int fps;
  long fromTimestamp;
  long toTimestamp;
  long framesCount;

}
