package com.app4.project.timelapse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

  private Long size;
  private String name;
  private Long uploadTimestamp;
  private int executionId;
  private int fileId;

}
