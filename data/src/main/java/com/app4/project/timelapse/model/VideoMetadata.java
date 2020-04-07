package com.app4.project.timelapse.model;

import lombok.Data;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
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
