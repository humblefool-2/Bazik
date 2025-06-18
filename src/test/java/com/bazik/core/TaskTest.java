package com.bazik.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskTest {

    @Test
    void testTaskExecutionWithSingleInput() {
        Function<List<Integer>, String> executor = inputs -> String.valueOf(inputs.get(0));
        Task<Integer, String> task = new Task<>("test", executor, Collections.emptyList());

        assertEquals("42", task.execute(Collections.singletonList(42)));
        assertTrue(task.isExecuted());
    }

    @Test
    void testTaskExecutionWithMultipleInputs() {
        Function<List<String>, Integer> executor = inputs -> inputs.stream()
                .mapToInt(String::length)
                .sum();
        Task<String, Integer> task = new Task<>("sum-lengths", executor, Arrays.asList("dep1", "dep2"));

        assertEquals(10, task.execute(Arrays.asList("hello", "world")));
    }

    @Test
    void testTaskCachesResult() {
        int[] counter = {0};
        Function<List<String>, Integer> executor = inputs -> {
            counter[0]++;
            return counter[0];
        };
        Task<String, Integer> task = new Task<>("counter", executor, Collections.emptyList());

        assertFalse(task.isExecuted());
        assertEquals(1, task.execute(Collections.emptyList()));
        assertTrue(task.isExecuted());
        assertEquals(1, task.execute(Collections.emptyList()));
        assertTrue(task.isExecuted());
        assertEquals(1, counter[0]);
    }

    @Test
    void testTaskAttributes() {
        List<String> dependencies = Arrays.asList("dep1", "dep2");
        Task<String, Integer> task = new Task<>("test-task", inputs -> 0, dependencies);

        assertEquals("test-task", task.getName());
        assertEquals(dependencies, task.getDependencies());
        assertFalse(task.isExecuted());
    }

    @Test
    void testTaskWithNullInputs() {
        Function<List<Object>, String> executor = inputs -> "result";
        Task<Object, String> task = new Task<>("null-test", executor, null);

        assertEquals("result", task.execute(Collections.emptyList()));
        assertTrue(task.isExecuted());
    }
}
