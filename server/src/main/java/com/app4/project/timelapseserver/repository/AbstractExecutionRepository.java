package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapseserver.model.request.ExecutionPatchRequest;

public abstract class AbstractExecutionRepository implements ExecutionRepository {

  protected void updateExecution(Execution ex, ExecutionPatchRequest request) {
    if (request.getEndTime() != null) {
      ex.setEndTime(request.getEndTime());
    }
    if (request.getPeriod() != null) {
      ex.setPeriod(request.getPeriod());
    }
    if (request.getStartTime() != null) {
      ex.setStartTime(request.getStartTime());
    }
    if (request.getTitle() != null) {
      ex.setTitle(request.getTitle());
    }
  }
}
