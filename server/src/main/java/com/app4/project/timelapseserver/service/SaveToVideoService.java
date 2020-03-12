package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.SavingState;
import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import com.app4.project.timelapseserver.exception.SavingException;
import com.app4.project.timelapseserver.service.storage.StorageService;
import com.app4.project.timelapseserver.util.IOSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Service
public class SaveToVideoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaveToVideoService.class);

  private final ExecutorService executor;
  private final StorageService storageService;
  private final ConcurrentMap<Integer, SavingState> executionSavingStateMap;

  public SaveToVideoService(ExecutorService executor, StorageService storageService, ConcurrentMap<Integer, SavingState> executionSavingStateMap) {
    this.executor = executor;
    this.storageService = storageService;
    this.executionSavingStateMap = executionSavingStateMap;
  }

  // TODO add toTimestamp?
  public SavingState startVideoSaving(Execution execution, int fps, long fromTimestamp) {
    if (getSavingState(execution.getId()) != SavingState.ON_GOING) {
      LOGGER.info("Starting saving video for execution {} with fps {}", execution.getId(), fps);
      executor.submit(() -> save(execution.getId(), fps, fromTimestamp));
    }
    return SavingState.ON_GOING;
  }

  public SavingState getSavingState(int executionId) {
    return executionSavingStateMap.getOrDefault(executionId, SavingState.NOT_STARTED);
  }

  private void save(int executionId, int fps, long fromTimestamp) {
    long startTime = System.currentTimeMillis();
    try (JpgSequenceEncoder encoder = storageService.newEncoderForExecution(executionId, fps)) {
      storageService.executionFiles(executionId, fromTimestamp)
        .forEach(supplier -> addFrame(encoder, supplier));
      executionSavingStateMap.put(executionId, SavingState.FINISHED);
      LOGGER.info("Finished saving video for execution {} (it took {}s)", executionId,
        (System.currentTimeMillis() - startTime) / 1000L);
    } catch (IOException | SavingException e) {
      LOGGER.error("Error while saving video for execution {} (fps {})", e, fps, e);
      executionSavingStateMap.put(executionId, SavingState.ERROR);
    }
  }

  private void addFrame(JpgSequenceEncoder encoder, IOSupplier<byte[]> bytesSupplier) {
    try {
      byte[] bytes = bytesSupplier.get();
      encoder.addFrame(bytes);
    } catch (IOException e) {
      throw new SavingException("Error while adding frame", e);
    }
  }
}
