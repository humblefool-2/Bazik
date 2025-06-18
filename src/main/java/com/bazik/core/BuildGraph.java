package com.bazik.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BuildGraph {
    private final Map<String, Task<?, ?>> taskRegistry = new HashMap<>();
    private final Map<String, List<String>> dependencyRegistry = new HashMap<>();

    public List<String> createExecutionOrder(String targetTask) {
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();

        Set<String> dependencies = resolveDependencies(targetTask);
        for (String task : dependencies) {
            if (!visited.contains(task)) {
                traverseDependencies(task, visited, order);
            }
        }

        order.add(targetTask);
        return order;
    }

    private <O> O executeTaskOrder(List<String> executionOrder, String targetTaskName) {
        Map<String, Object> resultCache = new HashMap<>();

        for (String taskName : executionOrder) {
            Task<?, ?> currentTask = taskRegistry.get(taskName);
            List<Object> dependencyResults = getDependencyResults(currentTask, resultCache);
            Object result = ((Task) currentTask).execute(dependencyResults);
            resultCache.put(taskName, result);
        }

        return (O) resultCache.get(targetTaskName);
    }

    private List<Object> getDependencyResults(Task<?, ?> task, Map<String, Object> resultCache) {
        return task.getDependencies().stream()
                .map(resultCache::get)
                .collect(Collectors.toList());
    }

    public void traverseDependencies(String task, Set<String> visited, List<String> order) {
        visited.add(task);

        for (String dependency : dependencyRegistry.getOrDefault(task, Collections.emptyList())) {
            if (!visited.contains(dependency)) {
                traverseDependencies(dependency, visited, order);
            }
        }

        order.add(task);
    }

    public Set<String> resolveDependencies(String targetTask) {
        Set<String> resolvedDependencies = new HashSet<>();
        Deque<String> processingQueue = new LinkedList<>(Collections.singletonList(targetTask));

        while (!processingQueue.isEmpty()) {
            String current = processingQueue.poll();
            for (String dependency : dependencyRegistry.getOrDefault(current, Collections.emptyList())) {
                if (resolvedDependencies.add(dependency)) {
                    processingQueue.add(dependency);
                }
            }
        }

        return resolvedDependencies;
    }

    public void addTask(Task<?, ?> task) {
        taskRegistry.put(task.getName(), task);
        dependencyRegistry.put(task.getName(), task.getDependencies());
    }

    public <I, O> O executeTarget(String targetTaskName) {
        List<String> executionOrder = createExecutionOrder(targetTaskName);
        return executeTaskOrder(executionOrder, targetTaskName);
    }

    public Map<String, List<String>> getDependencyRegistry() {
        return Collections.unmodifiableMap(dependencyRegistry);
    }
}