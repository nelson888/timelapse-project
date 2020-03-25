package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.SavingProgress;
import com.app4.project.timelapse.model.SavingState;
import com.app4.project.timelapseserver.service.storage.StorageService;
import com.app4.project.timelapseserver.service.task.SaveToVideoTask;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Service
public class SaveToVideoService {

  private final ExecutorService executor;
  private final StorageService storageService;
  private final ConcurrentMap<Integer, SavingProgress> executionSavingStateMap;

  public SaveToVideoService(ExecutorService executor, StorageService storageService,
                            ConcurrentMap<Integer, SavingProgress> executionSavingStateMap) {
    this.executor = executor;
    this.storageService = storageService;
    this.executionSavingStateMap = executionSavingStateMap;
  }

  public SavingProgress startVideoSaving(Execution execution, int fps, long fromTimestamp,
                                      long toTimestamp) {
    if (getSavingProgress(execution.getId()).getState() != SavingState.ON_GOING) {
      long framesCount = storageService.executionFilesCount(execution.getId(), fromTimestamp, toTimestamp);
      if (framesCount == 0) {
        return SavingProgress.notStarted();
      }
      executor.submit(new SaveToVideoTask(storageService, executionSavingStateMap, execution.getId(), fps, fromTimestamp, toTimestamp, framesCount));
    }
    return SavingProgress.onGoing(0);
  }

  public SavingProgress getSavingProgress(int executionId) {
    return executionSavingStateMap.getOrDefault(executionId, SavingProgress.notStarted());
  }

}
