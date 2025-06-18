package com.bazik.cli;

import com.bazik.core.BuildGraph;
import com.bazik.core.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class BuildGraphVisualizerTest {

        private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        private final PrintStream originalOut = System.out;
        private BuildGraph buildGraph;
        private BuildGraphVisualizer visualizer;

        @BeforeEach
        void setUp() {
            System.setOut(new PrintStream(outContent));
            buildGraph = new BuildGraph();
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        private String getOutput() {
            return outContent.toString();
        }

        private void assertOutputContains(String... expectedStrings) {
            String output = getOutput();
            for (String expected : expectedStrings) {
                assertTrue(output.contains(expected),
                        "Expected output to contain: " + expected);
            }
        }

        private void assertOutputDoesNotContain(String... unexpectedStrings) {
            String output = getOutput();
            for (String unexpected : unexpectedStrings) {
                assertFalse(output.contains(unexpected),
                        "Output should not contain: " + unexpected);
            }
        }

        @Test
        @DisplayName("Visualize a simple graph with two tasks")
        void testVisualizeSimpleGraph() {
            // Create a simple graph with two tasks
            Task<String, String> compileTask = new Task<>(
                    "compile",
                    inputs -> "Compiled",
                    Collections.emptyList()
            );

            Task<String, String> linkTask = new Task<>(
                    "link",
                    inputs -> "Linked",
                    Collections.singletonList("compile")
            );

            buildGraph.addTask(compileTask);
            buildGraph.addTask(linkTask);

            visualizer = new BuildGraphVisualizer(buildGraph);
            visualizer.visualize("link");

            assertOutputContains(
                    "Build Graph for target: link",
                    "Level 0:",
                    "* link",
                    "Level 1:",
                    "* compile",
                    "Graph Representation:",
                    "└── link",
                    "    └── compile"
            );
        }

        @Test
        @DisplayName("Visualize a complex graph with multiple dependencies")
        void testVisualizeComplexGraph() {
            // Create tasks
            Task<String, String> compileTask = createTask("compile", Collections.emptyList());
            Task<String, String> resourcesTask = createTask("resources", Collections.emptyList());
            Task<String, String> linkTask = createTask("link", Arrays.asList("compile", "resources"));
            Task<String, String> testTask = createTask("test", Collections.singletonList("link"));

            // Add tasks to graph
            buildGraph.addTask(compileTask);
            buildGraph.addTask(resourcesTask);
            buildGraph.addTask(linkTask);
            buildGraph.addTask(testTask);

            visualizer = new BuildGraphVisualizer(buildGraph);
            visualizer.visualize("test");

            assertOutputContains(
                    "Build Graph for target: test",
                    "Level 0:",
                    "* test",
                    "Level 1:",
                    "* link",
                    "Level 2:",
                    "└── test",
                    "    └── link"
            );

            // Verify both compile and resources are dependencies of link
            assertTrue(getOutput().contains("depends on: compile, resources") ||
                    getOutput().contains("depends on: resources, compile"),
                    "Output should show link depends on both compile and resources");
        }

        @Test
        @DisplayName("Visualize a graph with cyclic dependencies")
        void testVisualizeCyclicGraph() {
            // Create a graph with a cyclic dependency
            Task<String, String> aTask = createTask("a", Collections.singletonList("b"));
            Task<String, String> bTask = createTask("b", Collections.singletonList("c"));
            Task<String, String> cTask = createTask("c", Collections.singletonList("a"));

            buildGraph.addTask(aTask);
            buildGraph.addTask(bTask);
            buildGraph.addTask(cTask);

            visualizer = new BuildGraphVisualizer(buildGraph);
            visualizer.visualize("a");

            assertOutputContains(
                    "Build Graph for target: a",
                    "cyclic reference"
            );
        }

        @Test
        @DisplayName("Visualize a graph with a single task (no dependencies)")
        void testVisualizeEmptyGraph() {
            Task<String, String> soloTask = createTask("solo", Collections.emptyList());
            buildGraph.addTask(soloTask);

            visualizer = new BuildGraphVisualizer(buildGraph);
            visualizer.visualize("solo");

            assertOutputContains(
                    "Build Graph for target: solo",
                    "Level 0:",
                    "* solo",
                    "└── solo"
            );

            assertOutputDoesNotContain("depends on:");
        }

        @Test
        @DisplayName("Visualize a graph with diamond dependency pattern")
        void testVisualizeDiamondDependencyGraph() {
            // Create a diamond dependency pattern
            Task<String, String> aTask = createTask("a", Collections.emptyList());
            Task<String, String> bTask = createTask("b", Collections.singletonList("a"));
            Task<String, String> cTask = createTask("c", Collections.singletonList("a"));
            Task<String, String> dTask = createTask("d", Arrays.asList("b", "c"));

            buildGraph.addTask(aTask);
            buildGraph.addTask(bTask);
            buildGraph.addTask(cTask);
            buildGraph.addTask(dTask);

            visualizer = new BuildGraphVisualizer(buildGraph);
            visualizer.visualize("d");

            assertOutputContains(
                    "Build Graph for target: d",
                    "Level 0:",
                    "* d",
                    "Level 1:",
                    "Level 2:",
                    "* a"
            );

            // Verify both b and c are at level 1
            String output = getOutput();
            assertTrue(output.contains("* b") && output.contains("* c"),
                    "Both b and c should be at level 1");

            // Verify ASCII tree visualization shows the diamond pattern
            assertTrue(output.contains("└── d"), "Root node d should be present");

            // Check that both b and c are dependencies of d (order may vary)
            assertTrue((output.contains("    ├── b") || output.contains("    └── b")) &&
                       (output.contains("    └── c") || output.contains("    ├── c")),
                    "Both b and c should be shown as dependencies of d");
        }

        // Helper method to create tasks
        private Task<String, String> createTask(String name, java.util.List<String> dependencies) {
            return new Task<>(
                    name,
                    inputs -> name.toUpperCase(),
                    dependencies
            );
        }
}
