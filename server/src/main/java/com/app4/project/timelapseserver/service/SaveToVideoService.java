package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.SavingProgress;
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
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SaveToVideoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaveToVideoService.class);

  private final ExecutorService executor;
  private final StorageService storageService;
  private final ConcurrentMap<Integer, SavingProgress> executionSavingStateMap;

  public SaveToVideoService(ExecutorService executor, StorageService storageService,
                            ConcurrentMap<Integer, SavingProgress> executionSavingStateMap) {
    this.executor = executor;
    this.storageService = storageService;
    this.executionSavingStateMap = executionSavingStateMap;
  }

  // TODO add toTimestamp?
  public SavingState startVideoSaving(Execution execution, int fps, long fromTimestamp) {
    if (getSavingProgress(execution.getId()).getState() != SavingState.ON_GOING) {
      LOGGER.info("Starting saving video for execution {} with fps {}", execution.getId(), fps);
      executionSavingStateMap.put(execution.getId(), SavingProgress.onGoing(0));
      long framesCount = storageService.executionFilesCount(execution.getId(), fromTimestamp);
      executor.submit(() -> save(execution.getId(), fps, fromTimestamp, framesCount));
    }
    return SavingState.ON_GOING;
  }

  public SavingProgress getSavingProgress(int executionId) {
    return executionSavingStateMap.getOrDefault(executionId, SavingProgress.notStarted());
  }

  private void save(int executionId, int fps, long fromTimestamp, long framesCount) {
    long startTime = System.currentTimeMillis();
    AtomicLong framesProcessed = new AtomicLong(0L);
    try (JpgSequenceEncoder encoder = storageService.newEncoderForExecution(executionId, fps)) {
      storageService.executionFiles(executionId, fromTimestamp)
        .forEach(supplier ->
          addFrame(encoder, supplier,() -> updateProgress(executionId, framesProcessed, framesCount)));
      executionSavingStateMap.put(executionId, SavingProgress.finished());
      LOGGER.info("Finished saving  x video for execution {} (it took {}s)", executionId,
        (System.currentTimeMillis() - startTime) / 1000L);
    } catch (IOException | SavingException e) {
      LOGGER.error("Error while saving video for execution {} (fps {})", e, fps, e);
      executionSavingStateMap.put(executionId, SavingProgress.error());
    }
  }

  private void updateProgress(int executionId, AtomicLong framesProcessed, long framesCount) {
    int percentage = (int) (100L * framesProcessed.incrementAndGet() / framesCount);
    executionSavingStateMap.put(executionId, SavingProgress.onGoing(percentage));
  }
  private void addFrame(JpgSequenceEncoder encoder, IOSupplier<byte[]> bytesSupplier,
                        Runnable progressUpdater) {
    try {
      LOGGER.debug("Encoding frame");
      byte[] bytes = bytesSupplier.get();
      encoder.addFrame(bytes);
      progressUpdater.run();
    } catch (IOException e) {
      throw new SavingException("Error while adding frame", e);
    }
  }
}
