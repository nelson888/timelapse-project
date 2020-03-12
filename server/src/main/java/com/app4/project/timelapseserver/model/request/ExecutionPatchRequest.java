package com.app4.project.timelapseserver.model.request;

import lombok.Data;

// all fields are nullable
@Data
public class ExecutionPatchRequest {
  private String title;
  private Long startTime;
  private Long endTime;
  private Long period;
}
