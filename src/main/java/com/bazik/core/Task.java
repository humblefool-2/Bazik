package com.bazik.core;

import java.util.List;
import java.util.function.Function;

public class Task<I, O> {
  private final String name;
  private final Function<List<I>, O> executor;
  private final List<String> dependencies;
  private boolean executed;
  private O result;

  public Task(String name,
              Function<List<I>, O> executor,
              List<String> dependencies) {
    this.name = name;
    this.executor = executor;
    this.dependencies = dependencies;
    this.executed = false;
  }

  public String getName() {
    return name;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public boolean isExecuted() {
    return executed;
  }

  public O execute(List<I> inputs) {
    if (!executed) {
      result = executor.apply(inputs);
      executed = true;
    }
    return result;
  }
}