package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.FileData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

//TODO
public class FirebaseStorageService  implements StorageService {


  @Override
  public FileData store(int executionId, MultipartFile multipartFile) {
    return null;
  }

  @Override
  public Resource loadAsResource(int executionId, int fileId) {
    return null;
  }

  @Override
  public int nbFiles(int executionId) {
    return 0;
  }

  @Override
  public FileData getFileData(int executionId, int fileId) {
    return null;
  }
}
