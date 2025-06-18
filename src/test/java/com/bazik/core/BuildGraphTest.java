package com.bazik.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class BuildGraphTest {

    private BuildGraph graph;

    private Task<String, String> getCompileTask() {
        return new Task<>(
                "compile",
                inputs -> "Compiled",
                Collections.emptyList()
        );
    }

    private Task<String, String> getLinkTask(String taskName, String inputName) {
        return new Task<>(taskName, inputs -> inputName,
                Collections.singletonList("compile")
        );
    }

    private Task<String, String> getLinkTask() {
        return getLinkTask("link", "Linked Compiled");
    }

    private Task<String, String> getTestTask() {
        return new Task<>(
                "test",
                inputs -> "Tested Linked Compiled",
                Collections.singletonList("link")
        );
    }

    private Task<String, String> getMultipleDependencyTask() {
        return new Task<>(
                "test",
                inputs -> "Tested SecondLinked Linked Compiled",
                List.of("link", "secondLink", "compile")
        );
    }

    @BeforeEach
    void setUp() {
        graph = new BuildGraph();
    }

    @AfterEach
    void tearDown() {
        graph = null;
    }

    @Nested
    class ExecuteTargetTests {
        @Test
        void testOneLevel() {
            graph.addTask(getCompileTask());
            assertEquals("Compiled", graph.executeTarget("compile"));
        }

        @Test
        void testTwoLevel() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());

            assertEquals("Linked Compiled", graph.executeTarget("link"));
        }

        @Test
        void testThreeLevel() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getTestTask());

            assertEquals("Tested Linked Compiled", graph.executeTarget("test"));
        }

        @Test
        void testLinkedDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getLinkTask("secondLink", "SecondLinked Compiled"));
            graph.addTask(getMultipleDependencyTask());

            assertEquals("Tested SecondLinked Linked Compiled", graph.executeTarget("test"));
        }

        @Test
        void testSubGraphExecution() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getLinkTask("secondLink", "SecondLinked "));
            graph.addTask(getMultipleDependencyTask());

            assertEquals("Linked Compiled", graph.executeTarget("link"));
        }
    }

    @Nested
    class ExecutionOrderTests {
        @Test
        void testCreateExecutionOrder() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getTestTask());

            List<String> executionOrder = graph.createExecutionOrder("test");

            assertEquals(3, executionOrder.size());
            assertEquals("compile", executionOrder.get(0));
            assertEquals("link", executionOrder.get(1));
            assertEquals("test", executionOrder.get(2));
        }

        @Test
        void testCreateExecutionOrderWithMultipleDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getLinkTask("secondLink", "SecondLinked Compiled"));
            graph.addTask(getMultipleDependencyTask());

            List<String> executionOrder = graph.createExecutionOrder("test");

            assertEquals(4, executionOrder.size());
            assertTrue(executionOrder.contains("compile"));
            assertTrue(executionOrder.contains("link"));
            assertTrue(executionOrder.contains("secondLink"));
            assertEquals("test", executionOrder.get(executionOrder.size() - 1));

            int compileIndex = executionOrder.indexOf("compile");
            int linkIndex = executionOrder.indexOf("link");
            int secondLinkIndex = executionOrder.indexOf("secondLink");
            int testIndex = executionOrder.indexOf("test");

            assertTrue(compileIndex < linkIndex);
            assertTrue(compileIndex < secondLinkIndex);
            assertTrue(linkIndex < testIndex);
            assertTrue(secondLinkIndex < testIndex);
        }
    }

    @Nested
    class DependencyResolutionTests {
        @Test
        void testResolveDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getTestTask());

            Set<String> dependencies = graph.resolveDependencies("test");

            assertEquals(2, dependencies.size());
            assertTrue(dependencies.contains("compile"));
            assertTrue(dependencies.contains("link"));
        }

        @Test
        void testResolveDependenciesWithMultipleDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getLinkTask("secondLink", "SecondLinked Compiled"));
            graph.addTask(getMultipleDependencyTask());

            Set<String> dependencies = graph.resolveDependencies("test");

            assertEquals(3, dependencies.size());
            assertTrue(dependencies.contains("compile"));
            assertTrue(dependencies.contains("link"));
            assertTrue(dependencies.contains("secondLink"));
        }
    }

    @Nested
    class DependencyTraversalTests {
        @Test
        void testTraverseDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getTestTask());

            Set<String> visited = new HashSet<>();
            List<String> order = new ArrayList<>();

            graph.traverseDependencies("test", visited, order);

            assertEquals(3, order.size());
            assertEquals("compile", order.get(0));
            assertEquals("link", order.get(1));
            assertEquals("test", order.get(2));
        }

        @Test
        void testTraverseDependenciesWithMultipleDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getLinkTask("secondLink", "SecondLinked Compiled"));
            graph.addTask(getMultipleDependencyTask());

            Set<String> visited = new HashSet<>();
            List<String> order = new ArrayList<>();

            graph.traverseDependencies("test", visited, order);

            assertEquals(4, order.size());
            assertTrue(order.contains("compile"));
            assertTrue(order.contains("link"));
            assertTrue(order.contains("secondLink"));
            assertEquals("test", order.get(order.size() - 1));
        }
    }

    @Nested
    class DependencyRegistryTests {
        @Test
        void testGetDependencyRegistry() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getTestTask());

            Map<String, List<String>> registry = graph.getDependencyRegistry();

            assertNotNull(registry);
            assertEquals(3, registry.size());

            assertTrue(registry.containsKey("compile"));
            assertTrue(registry.get("compile").isEmpty());

            assertTrue(registry.containsKey("link"));
            assertEquals(1, registry.get("link").size());
            assertEquals("compile", registry.get("link").get(0));

            assertTrue(registry.containsKey("test"));
            assertEquals(1, registry.get("test").size());
            assertEquals("link", registry.get("test").get(0));
        }

        @Test
        void testGetDependencyRegistryWithMultipleDependencies() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());
            graph.addTask(getLinkTask("secondLink", "SecondLinked Compiled"));
            graph.addTask(getMultipleDependencyTask());

            Map<String, List<String>> registry = graph.getDependencyRegistry();

            assertNotNull(registry);
            assertEquals(4, registry.size());

            assertTrue(registry.containsKey("test"));
            assertEquals(3, registry.get("test").size());
            assertTrue(registry.get("test").contains("link"));
            assertTrue(registry.get("test").contains("secondLink"));
            assertTrue(registry.get("test").contains("compile"));
        }

        @Test
        void testDependencyRegistryIsUnmodifiable() {
            graph.addTask(getCompileTask());
            graph.addTask(getLinkTask());

            Map<String, List<String>> registry = graph.getDependencyRegistry();

            assertThrows(UnsupportedOperationException.class, () -> {
                registry.put("newTask", Collections.emptyList());
            });
        }
    }
}