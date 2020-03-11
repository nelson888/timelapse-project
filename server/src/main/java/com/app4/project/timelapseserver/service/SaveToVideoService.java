package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.SavingState;
import org.springframework.stereotype.Service;

@Service
public class SaveToVideoService {



  public SavingState startVideoSaving(Execution execution, int fps) {
    return SavingState.ON_GOING;
  }
}
