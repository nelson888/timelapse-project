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

  public FileData() {
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getUploadTimestamp() {
    return uploadTimestamp;
  }

  public int getExecutionId() {
    return executionId;
  }

  public int getFileId() {
    return fileId;
  }

  @Override
  public String toString() {
    return "FileData{" +
      "size=" + size +
      ", name='" + name + '\'' +
      ", uploadTimestamp=" + uploadTimestamp +
      ", executionId=" + executionId +
      ", fileId=" + fileId +
      '}';
  }
}
