package com.app4.project.timelapseserver.service.task;

import com.app4.project.timelapse.model.VideoTaskProgress;
import com.app4.project.timelapse.model.VideoMetadata;
import com.app4.project.timelapseserver.codec.JpgSequenceEncoder;
import com.app4.project.timelapseserver.exception.SavingException;
import com.app4.project.timelapseserver.repository.VideoMetadataRepository;
import com.app4.project.timelapseserver.util.FileChannelWrapper;
import com.app4.project.timelapseserver.storage.StorageService;
import com.app4.project.timelapseserver.util.IOSupplier;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@AllArgsConstructor
public class SaveToVideoTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaveToVideoTask.class);

  private final AtomicLong framesProcessed = new AtomicLong(0L);

  private final int taskId;
  private final StorageService storageService;
  private final Consumer<VideoTaskProgress> progressUpdater;
  private final VideoMetadataRepository videoMetadataRepository;
  private final int executionId;
  private final int fps;
  private final long fromTimestamp;
  private final long toTimestamp;
  private final long framesCount;

  @Override
  public void run() {
    LOGGER.info("Starting saving video for execution {} with fps {}", executionId, fps);
    progressUpdater.accept(VideoTaskProgress.onGoing(taskId, 0));
    long startTime = System.currentTimeMillis();
    try (FileChannelWrapper channelWrapper = storageService.createTempChannel(taskId);
      JpgSequenceEncoder encoder = new JpgSequenceEncoder(channelWrapper, fps)) {
      save(encoder, channelWrapper.getTempFilePath());
      LOGGER.info("Finished saving video for execution {} (it took {}s)", executionId,
        (System.currentTimeMillis() - startTime) / 1000L);
    } catch (IOException | SavingException e) {
      LOGGER.error("Error while saving video for execution {} (fps {})", e, fps, e);
      progressUpdater.accept(VideoTaskProgress.error(taskId, e.getMessage()));
    }
  }

  private void save(JpgSequenceEncoder encoder, Path tempFilePath) throws IOException {
    storageService.executionFiles(executionId, fromTimestamp, toTimestamp)
      .forEach(supplier -> addFrame(encoder, supplier));
    LOGGER.debug("[Task {}] Encoded all frames", taskId);
    int videoId = storageService.uploadVideo(tempFilePath);
    videoMetadataRepository.add(new VideoMetadata(executionId, videoId, fps, fromTimestamp, toTimestamp, framesCount));
    progressUpdater.accept(VideoTaskProgress.finished(taskId, videoId));
    LOGGER.info("[Task {}] Uploaded new video with id {}", taskId, videoId);
  }

  private void addFrame(JpgSequenceEncoder encoder, IOSupplier<byte[]> bytesSupplier) {
    try {
      LOGGER.debug("[Task {}] Encoding frame {} (out of {})", taskId, framesProcessed.get(), framesCount);
      byte[] bytes = bytesSupplier.get();
      encoder.addFrame(bytes);
      updateProgress();
    } catch (IOException e) {
      LOGGER.debug("[Task {}] Error while encoding frame", taskId, e);
      throw new SavingException("Error while encoding frame", e);
    }
  }

  private void updateProgress() {
    int percentage = (int) (100L * framesProcessed.incrementAndGet() / framesCount);
    progressUpdater.accept(VideoTaskProgress.onGoing(taskId, percentage));
  }
}
