package com.app4.project.timelapseserver.service.task;

import com.app4.project.timelapse.model.SavingProgress;
import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import com.app4.project.timelapseserver.exception.SavingException;
import com.app4.project.timelapseserver.util.FileChannelWrapper;
import com.app4.project.timelapseserver.service.storage.StorageService;
import com.app4.project.timelapseserver.util.IOSupplier;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@AllArgsConstructor
public class SaveToVideoTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaveToVideoTask.class);

  private final StorageService storageService;
  private final Map<Integer, SavingProgress> executionSavingStateMap;
  private final int executionId;
  private final int fps;
  private final long fromTimestamp;
  private final long toTimestamp;
  private final long framesCount;
  private final AtomicLong framesProcessed = new AtomicLong(0L);

  @Override
  public void run() {
    LOGGER.info("Starting saving video for execution {} with fps {}", executionId, fps);
    executionSavingStateMap.put(executionId, SavingProgress.onGoing(0));
    long startTime = System.currentTimeMillis();
    try (FileChannelWrapper channelWrapper = storageService.createTempChannel(executionId);
      JpgSequenceEncoder encoder = new JpgSequenceEncoder(channelWrapper, fps)) {
      save(encoder, channelWrapper.getPath());
      LOGGER.info("Finished saving video for execution {} (it took {}s)", executionId,
        (System.currentTimeMillis() - startTime) / 1000L);
    } catch (IOException | SavingException e) {
      LOGGER.error("Error while saving video for execution {} (fps {})", e, fps, e);
      executionSavingStateMap.put(executionId, SavingProgress.error());
    }
  }

  private void save(JpgSequenceEncoder encoder, Path tempFilePath) throws IOException {
    storageService.executionFiles(executionId, fromTimestamp, toTimestamp)
      .forEach(supplier -> addFrame(encoder, supplier));
    storageService.uploadVideo(executionId, tempFilePath);
    executionSavingStateMap.put(executionId, SavingProgress.finished());
  }

  private void addFrame(JpgSequenceEncoder encoder, IOSupplier<byte[]> bytesSupplier) {
    try {
      LOGGER.debug("Encoding frame {} (out of {})", framesProcessed.get(), framesCount);
      byte[] bytes = bytesSupplier.get();
      encoder.addFrame(bytes);
      updateProgress();
    } catch (IOException e) {
      throw new SavingException("Error while adding frame", e);
    }
  }

  private void updateProgress() {
    int percentage = (int) (100L * framesProcessed.incrementAndGet() / framesCount);
    executionSavingStateMap.put(executionId, SavingProgress.onGoing(percentage));
  }
}
