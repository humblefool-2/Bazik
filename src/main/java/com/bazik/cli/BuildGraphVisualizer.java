package com.bazik.cli;

import com.bazik.core.BuildGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A utility class to visualize the build graph as a text diagram in the console.
 */
public class BuildGraphVisualizer {
    // ASCII art constants
    private static final String INDENT_PIPE = "│   ";
    private static final String INDENT_SPACE = "    ";
    private static final String BRANCH_LAST = "└── ";
    private static final String BRANCH_MIDDLE = "├── ";
    private final BuildGraph buildGraph;
    private final Map<String, List<String>> dependencyRegistry;

    /**
     * Creates a new BuildGraphVisualizer with the given BuildGraph.
     *
     * @param buildGraph The build graph to visualize
     */
    public BuildGraphVisualizer(BuildGraph buildGraph) {
        this.buildGraph = buildGraph;
        this.dependencyRegistry = buildGraph.getDependencyRegistry();
    }

    /**
     * Prints the header for the visualization.
     */
    private void printHeader(String targetTaskName) {
        System.out.println("Build Graph for target: " + targetTaskName);
        System.out.println();
    }

    /**
     * Gets all tasks involved in the build graph for the target task.
     */
    private Set<String> getAllTasks(String targetTaskName) {
        Set<String> allTasks = buildGraph.resolveDependencies(targetTaskName);
        allTasks.add(targetTaskName);
        return allTasks;
    }

    /**
     * Prints a level-based visualization of the build graph.
     */
    private void printLevelBasedVisualization(String targetTaskName, Set<String> allTasks) {
        // Calculate task levels
        Map<String, Integer> taskLevels = calculateTaskLevels(targetTaskName, allTasks);
        int maxLevel = findMaxLevel(taskLevels);

        // Print each level
        for (int level = 0; level <= maxLevel; level++) {
            printTasksAtLevel(level, taskLevels, targetTaskName);
        }
    }

    /**
     * Prints all tasks at a specific level in the graph.
     */
    private void printTasksAtLevel(int level, Map<String, Integer> taskLevels, String targetTaskName) {
        List<String> tasksAtLevel = getTasksAtLevel(level, taskLevels);

        if (!tasksAtLevel.isEmpty()) {
            System.out.println("Level " + level + ":");

            for (String task : tasksAtLevel) {
                printTaskInfo(task, targetTaskName);
            }

            System.out.println();
        }
    }

    /**
     * Gets all tasks at a specific level in the graph.
     */
    private List<String> getTasksAtLevel(int level, Map<String, Integer> taskLevels) {
        return taskLevels.entrySet().stream()
                .filter(entry -> entry.getValue() == level)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Prints information about a specific task.
     */
    private void printTaskInfo(String task, String targetTaskName) {
        if (task.equals(targetTaskName)) {
            System.out.println("  * " + task + " (TARGET)");
        } else {
            System.out.println("  * " + task);
        }

        // Print dependencies
        List<String> deps = dependencyRegistry.getOrDefault(task, Collections.emptyList());
        if (!deps.isEmpty()) {
            System.out.println("      depends on: " + String.join(", ", deps));
        }
    }

    /**
     * Finds the maximum level in the task levels map.
     */
    private int findMaxLevel(Map<String, Integer> taskLevels) {
        return taskLevels.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    /**
     * Prints an ASCII tree visualization of the build graph.
     */
    private void printAsciiTreeVisualization(String targetTaskName) {
        System.out.println("Graph Representation:");
        printAsciiGraph(targetTaskName, new HashSet<>(), "", true);
        System.out.println();
    }

    /**
     * Recursively prints an ASCII representation of the graph.
     *
     * @param taskName The current task to print
     * @param visited  Set of already visited tasks to detect cycles
     * @param indent   Current indentation string
     * @param isLast   Whether this is the last item in its branch
     */
    private void printAsciiGraph(String taskName, Set<String> visited, String indent, boolean isLast) {
        // Check for cyclic references
        if (!visited.add(taskName)) {
            System.out.println(indent + (isLast ? BRANCH_LAST : BRANCH_MIDDLE) + taskName + " (cyclic reference)");
            return;
        }

        // Print the current task
        System.out.println(indent + (isLast ? BRANCH_LAST : BRANCH_MIDDLE) + taskName);

        // Get dependencies and return if none
        List<String> dependencies = dependencyRegistry.getOrDefault(taskName, Collections.emptyList());
        if (dependencies.isEmpty()) {
            visited.remove(taskName);
            return;
        }

        // Calculate new indent for child items
        String newIndent = indent + (isLast ? INDENT_SPACE : INDENT_PIPE);

        // Recursively print dependencies
        for (int i = 0; i < dependencies.size(); i++) {
            boolean last = i == dependencies.size() - 1;
            printAsciiGraph(dependencies.get(i), new HashSet<>(visited), newIndent, last);
        }

        visited.remove(taskName);
    }

    /**
     * Calculate the level of each task in the graph.
     * The target task is at level 0, its direct dependencies at level 1, and so on.
     *
     * @param targetTaskName The target task name
     * @param allTasks       All tasks in the graph
     * @return A map of task names to their levels
     */
    private Map<String, Integer> calculateTaskLevels(String targetTaskName, Set<String> allTasks) {
        Map<String, Integer> levels = new HashMap<>();
        levels.put(targetTaskName, 0);

        boolean changed;
        do {
            changed = false;
            for (String task : allTasks) {
                if (!levels.containsKey(task)) {
                    // Try to assign a level to this task
                    if (tryAssignLevel(task, levels, allTasks)) {
                        changed = true;
                    }
                }
            }
        } while (changed);

        // Handle any remaining tasks (could be due to cycles)
        handleUnresolvedTasks(allTasks, levels);

        return levels;
    }

    /**
     * Tries to assign a level to a task based on its dependents.
     */
    private boolean tryAssignLevel(String task, Map<String, Integer> levels, Set<String> allTasks) {
        boolean allDependentsHaveLevels = true;
        int minDependentLevel = Integer.MAX_VALUE;

        for (String otherTask : allTasks) {
            List<String> deps = dependencyRegistry.getOrDefault(otherTask, Collections.emptyList());
            if (deps.contains(task)) {
                if (!levels.containsKey(otherTask)) {
                    allDependentsHaveLevels = false;
                    break;
                } else {
                    minDependentLevel = Math.min(minDependentLevel, levels.get(otherTask));
                }
            }
        }

        if (allDependentsHaveLevels && minDependentLevel != Integer.MAX_VALUE) {
            levels.put(task, minDependentLevel + 1);
            return true;
        }

        return false;
    }

    /**
     * Assigns a high level to any tasks that couldn't be resolved normally.
     */
    private void handleUnresolvedTasks(Set<String> allTasks, Map<String, Integer> levels) {
        for (String task : allTasks) {
            if (!levels.containsKey(task)) {
                levels.put(task, 999); // Assign a high level to unresolved tasks
            }
        }
    }

    /**
     * Generates a text-based representation of the build graph for the specified target task
     * and prints it to the console.
     *
     * @param targetTaskName The name of the target task
     */
    public void visualize(String targetTaskName) {
        printHeader(targetTaskName);

        // Get all tasks in the graph
        Set<String> allTasks = getAllTasks(targetTaskName);

        // Calculate and print the level-based representation
        printLevelBasedVisualization(targetTaskName, allTasks);

        // Print the ASCII tree representation
        printAsciiTreeVisualization(targetTaskName);
    }
}
