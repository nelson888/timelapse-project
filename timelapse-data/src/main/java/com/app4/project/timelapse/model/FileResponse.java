package com.app4.project.timelapse.model;

public class FileResponse {

  private Long size;
  private String name;

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

  @Override public String toString() {
    return "FileResponse{" +
        "size=" + size +
        ", name='" + name + '\'' +
        '}';
  }
}
