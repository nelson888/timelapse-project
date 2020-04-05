package com.app4.project.timelapse.model;

import lombok.Data;
import lombok.Value;
import org.springframework.data.annotation.Id;

@Data
@Value
public class VideoMetadata {

  @Id
  int videoId;
  int executionId;
  int fps;
  long fromTimestamp;
  long toTimestamp;
  long framesCount;

}
