package chat.octet.accordion.performance;

import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.test.AccordionTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

/**
 * Performance tests for Accordion framework.
 * These tests are disabled by default and can be enabled with system property: accordion.performance.tests=true
 * 
 * @author <a href="https://github.com/eoctet">William</a>
 */
@DisplayName("Accordion Performance Tests")
@EnabledIfSystemProperty(named = "accordion.performance.tests", matches = "true")
class AccordionPerformanceTest extends AccordionTestBase {

    @Nested
    @DisplayName("Execution Performance Tests")
    class ExecutionPerformanceTests {

        @Test
        @DisplayName("Should execute simple action within performance threshold")
        void should_execute_simple_action_within_performance_threshold() {
            // Given
            ActionConfig action = createSimpleScriptAction("Performance Test", "'test result'");
            AccordionPlan plan = AccordionPlan.of().start(action);

            // When
            Instant start = Instant.now();
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play();
                assertThat(result).isNotNull();
            }
            Duration executionTime = Duration.between(start, Instant.now());

            // Then
            assertThat(executionTime).isLessThan(Duration.ofSeconds(1));
            logger.info("Simple action execution time: {} ms", executionTime.toMillis());
        }

        @Test
        @DisplayName("Should handle high-frequency executions efficiently")
        void should_handle_high_frequency_executions_efficiently() {
            // Given
            ActionConfig action = createSimpleScriptAction("High Frequency Test", "rand(1000)");
            AccordionPlan plan = AccordionPlan.of().start(action);
            int executionCount = 1000;

            // When
            Instant start = Instant.now();
            try (Accordion accordion = new Accordion(plan)) {
                for (int i = 0; i < executionCount; i++) {
                    ExecuteResult result = accordion.play();
                    assertThat(result).isNotNull();
                }
            }
            Duration totalTime = Duration.between(start, Instant.now());

            // Then
            double avgExecutionTime = (double) totalTime.toMillis() / executionCount;
            assertThat(avgExecutionTime).isLessThan(10.0); // Less than 10ms per execution on average
            
            logger.info("High frequency test - Total time: {} ms, Average per execution: {} ms", 
                totalTime.toMillis(), avgExecutionTime);
        }

        @Test
        @DisplayName("Should execute complex plan within reasonable time")
        void should_execute_complex_plan_within_reasonable_time() {
            // Given
            AccordionPlan plan = createComplexPlan();

            // When
            Instant start = Instant.now();
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play(true);
                assertThat(result).isNotNull();
            }
            Duration executionTime = Duration.between(start, Instant.now());

            // Then
            assertThat(executionTime).isLessThan(Duration.ofSeconds(5));
            logger.info("Complex plan execution time: {} ms", executionTime.toMillis());
        }
    }

    @Nested
    @DisplayName("Concurrency Performance Tests")
    class ConcurrencyPerformanceTests {

        @Test
        @DisplayName("Should handle concurrent executions efficiently")
        void should_handle_concurrent_executions_efficiently() throws InterruptedException {
            // Given
            ActionConfig action = createSimpleScriptAction("Concurrent Test", "'concurrent-' + rand(100)");
            AccordionPlan plan = AccordionPlan.of().start(action);
            
            int threadCount = 10;
            int executionsPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            AtomicLong totalExecutionTime = new AtomicLong(0);

            // When
            Instant start = Instant.now();
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        Instant threadStart = Instant.now();
                        for (int j = 0; j < executionsPerThread; j++) {
                            try (Accordion accordion = new Accordion(plan)) {
                                ExecuteResult result = accordion.play();
                                if (result != null) {
                                    successCount.incrementAndGet();
                                } else {
                                    failureCount.incrementAndGet();
                                }
                            } catch (Exception e) {
                                failureCount.incrementAndGet();
                                logger.error("Execution failed", e);
                            }
                        }
                        Duration threadTime = Duration.between(threadStart, Instant.now());
                        totalExecutionTime.addAndGet(threadTime.toMillis());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();
            
            Duration totalTime = Duration.between(start, Instant.now());
            int totalExecutions = threadCount * executionsPerThread;
            double successRate = (double) successCount.get() / totalExecutions;
            double avgExecutionTime = (double) totalExecutionTime.get() / threadCount;

            assertThat(successRate).isGreaterThan(0.95); // 95% success rate
            assertThat(totalTime).isLessThan(Duration.ofSeconds(30));
            
            logger.info("Concurrent test - Threads: {}, Executions per thread: {}, Success rate: {:.2%}, " +
                "Total time: {} ms, Avg thread time: {} ms", 
                threadCount, executionsPerThread, successRate, totalTime.toMillis(), avgExecutionTime);
        }

        @Test
        @DisplayName("Should maintain performance under load")
        void should_maintain_performance_under_load() throws InterruptedException {
            // Given
            ActionConfig action = createSimpleScriptAction("Load Test", "rand(1000)");
            AccordionPlan plan = AccordionPlan.of().start(action);
            
            int threadCount = 20;
            int executionsPerThread = 25;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            ConcurrentLinkedQueue<Long> executionTimes = new ConcurrentLinkedQueue<>();

            // When
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < executionsPerThread; j++) {
                            Instant start = Instant.now();
                            try (Accordion accordion = new Accordion(plan)) {
                                accordion.play();
                            }
                            long executionTime = Duration.between(start, Instant.now()).toMillis();
                            executionTimes.offer(executionTime);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();

            // Analyze execution times
            long[] times = executionTimes.stream().mapToLong(Long::longValue).sorted().toArray();
            long median = times[times.length / 2];
            long p95 = times[(int) (times.length * 0.95)];
            double average = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0);

            assertThat(median).isLessThan(100); // Median execution time < 100ms
            assertThat(p95).isLessThan(500);    // 95th percentile < 500ms
            
            logger.info("Load test results - Executions: {}, Median: {} ms, 95th percentile: {} ms, Average: {:.2f} ms",
                times.length, median, p95, average);
        }
    }

    @Nested
    @DisplayName("Memory Performance Tests")
    class MemoryPerformanceTests {

        @Test
        @DisplayName("Should not leak memory during repeated executions")
        void should_not_leak_memory_during_repeated_executions() {
            // Given
            ActionConfig action = createSimpleScriptAction("Memory Test", "'memory-test-' + rand(1000)");
            AccordionPlan plan = AccordionPlan.of().start(action);
            
            Runtime runtime = Runtime.getRuntime();
            
            // Force initial GC
            System.gc();
            waitFor(100);
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            // When - Execute many times to detect memory leaks
            for (int i = 0; i < 500; i++) {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play();
                }
                
                // Periodic GC to help detect leaks
                if (i % 50 == 0) {
                    System.gc();
                    waitFor(10);
                }
            }

            // Force final GC
            System.gc();
            waitFor(200);
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();

            // Then
            long memoryIncrease = finalMemory - initialMemory;
            double memoryIncreasePercent = (double) memoryIncrease / initialMemory * 100;
            
            // Memory increase should be reasonable (less than 50% of initial memory)
            assertThat(memoryIncreasePercent).isLessThan(50.0);
            
            logger.info("Memory test - Initial: {} bytes, Final: {} bytes, Increase: {} bytes ({:.2f}%)",
                initialMemory, finalMemory, memoryIncrease, memoryIncreasePercent);
        }

        @Test
        @DisplayName("Should handle large data processing efficiently")
        void should_handle_large_data_processing_efficiently() {
            // Given
            ActionConfig action = createSimpleScriptAction("Large Data Test", 
                "let sum = 0; for(let i = 0; i < 10000; i++) { sum = sum + i; } sum");
            AccordionPlan plan = AccordionPlan.of().start(action);

            Runtime runtime = Runtime.getRuntime();
            long maxMemoryBefore = runtime.maxMemory();
            long freeMemoryBefore = runtime.freeMemory();

            // When
            Instant start = Instant.now();
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play();
                assertThat(result).isNotNull();
            }
            Duration executionTime = Duration.between(start, Instant.now());

            // Then
            long freeMemoryAfter = runtime.freeMemory();
            long memoryUsed = freeMemoryBefore - freeMemoryAfter;
            
            assertThat(executionTime).isLessThan(Duration.ofSeconds(2));
            assertThat(memoryUsed).isLessThan(maxMemoryBefore / 4); // Should not use more than 25% of max memory
            
            logger.info("Large data test - Execution time: {} ms, Memory used: {} bytes", 
                executionTime.toMillis(), memoryUsed);
        }
    }

    // Helper methods
    private ActionConfig createSimpleScriptAction(String name, String script) {
        return ActionConfig.builder()
                .id(createTestActionId("PERF"))
                .actionType(ActionType.SCRIPT.name())
                .actionName(name)
                .actionDesc("Performance test action")
                .actionParams(ScriptParameter.builder()
                        .script(script)
                        .build())
                .build();
    }

    private AccordionPlan createComplexPlan() {
        ActionConfig action1 = createSimpleScriptAction("Complex Action 1", "let x = 1; x");
        ActionConfig action2 = createSimpleScriptAction("Complex Action 2", "let y = 2; y");
        ActionConfig action3 = createSimpleScriptAction("Complex Action 3", "let z = 3; z");
        ActionConfig action4 = createSimpleScriptAction("Complex Action 4", "let w = 4; w");
        ActionConfig action5 = createSimpleScriptAction("Complex Action 5", "let v = 5; v");

        return AccordionPlan.of()
                .start(action1)
                .next(action1, action2, action3)
                .next(action2, action4)
                .next(action3, action5)
                .next(action4, action5);
    }
}