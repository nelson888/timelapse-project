package com.app4.project.timelapseserver.service;

import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.SavingState;
import org.springframework.stereotype.Service;

@Service
public class SaveToVideoService {



  public SavingState startVideoSaving(Execution execution) {
    return SavingState.ON_GOING;
  }
}
