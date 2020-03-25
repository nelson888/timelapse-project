package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.SavingProgress;
import com.app4.project.timelapseserver.service.storage.StorageService;
import com.app4.project.timelapseserver.service.task.SaveToVideoTask;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class SaveToVideoService {

  private final ExecutorService executor;
  private final StorageService storageService;
  private final ConcurrentMap<Integer, SavingProgress> taskProgressMap; // map task id -> SavingProgress
  private final ConcurrentMap<Integer, Queue<Integer>> executionTasksMap; // map execution id -> task ids
  private final AtomicInteger idGenerator = new AtomicInteger();

  public SaveToVideoService(ExecutorService executor, StorageService storageService,
                            ConcurrentMap<Integer, SavingProgress> taskProgressMap,
                            ConcurrentMap<Integer, Queue<Integer>> executionTasksMap) {
    this.executor = executor;
    this.storageService = storageService;
    this.taskProgressMap = taskProgressMap;
    this.executionTasksMap = executionTasksMap;
  }

  public SavingProgress startVideoSaving(int  executionId, int fps, long fromTimestamp, long toTimestamp) {
    long framesCount = storageService.executionFilesCount(executionId, fromTimestamp, toTimestamp);
    if (framesCount == 0) {
      return SavingProgress.notStarted("There are no frames for between the timestamp(s)");
    }
    int taskId = idGenerator.getAndIncrement();
    Queue<Integer> executionTasks = executionTasksMap.computeIfAbsent(executionId, k -> new ConcurrentLinkedDeque<>());
    executionTasks.add(taskId);

    executor.submit(new SaveToVideoTask(taskId, storageService, (p) -> updateState(taskId, p),
      executionId, fps, fromTimestamp, toTimestamp, framesCount));
    return SavingProgress.onGoing(taskId, 0);
  }

  public Optional<SavingProgress> getOptionalSavingProgress(int taskId) {
    return Optional.ofNullable(taskProgressMap.get(taskId));
  }

  public List<Integer> getAllTasksForExecution(int executionId) {
    Queue<Integer> queue = executionTasksMap.get(executionId);
    return queue == null ? Collections.emptyList() : List.copyOf(queue);
  }

  public List<Integer> getAllTasks() {
    return executionTasksMap.values().stream()
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  private void updateState(int taskId, SavingProgress progress) {
    taskProgressMap.put(taskId, progress);
  }
}
