package com.app4.project.timelapseserver.repository;

import com.app4.project.timelapse.model.Execution;

import java.util.List;
import java.util.Optional;

public interface ExecutionRepository {

  int count();

  List<Execution> getAll();

  void add(Execution execution);

  Optional<Execution> getById(int id);

  boolean remove(int id);

  Optional<Execution> getSoonest();

}
