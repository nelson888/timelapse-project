package com.app4.project.timelapse.model;

public class FileData {

  private Long size;
  private String name;
  private Long uploadTimestamp;
  private int executionId;
  private int fileId;

  public FileData(Long size, String name, Long uploadTimestamp, int executionId, int fileId) {
    this.size = size;
    this.name = name;
    this.uploadTimestamp = uploadTimestamp;
    this.executionId = executionId;
    this.fileId = fileId;
  }

}
