package chat.octet.accordion.integration;

import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.action.base.ConditionParameter;

import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.test.AccordionTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for the complete Accordion workflow.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@DisplayName("Accordion Integration Tests")
class AccordionIntegrationTest extends AccordionTestBase {

    @Nested
    @DisplayName("Complete Workflow Tests")
    class CompleteWorkflowTests {

        @Test
        @DisplayName("Should execute sequential actions successfully")
        void shouldExecuteSequentialActionsSuccessfully() {
            // Given
            ActionConfig firstAction = ActionConfig.builder()
                    .id(createTestActionId("FIRST"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("First Action")
                    .actionDesc("Initialize counter")
                    .actionParams(ScriptParameter.builder()
                            .script("let counter = 1; counter")
                            .build())
                    .build();

            ActionConfig secondAction = ActionConfig.builder()
                    .id(createTestActionId("SECOND"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Second Action")
                    .actionDesc("Process result")
                    .actionParams(ScriptParameter.builder()
                            .script("'second action result'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(firstAction)
                    .next(firstAction, secondAction);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play(true);

                assertThat(result).isNotNull();
                assertThat(accordion.verbose()).contains("First Action");
                assertThat(accordion.verbose()).contains("Second Action");

                logger.info("Sequential execution completed: {}", accordion.verbose());
            }
        }

        @Test
        @DisplayName("Should handle conditional branching correctly")
        void shouldHandleConditionalBranchingCorrectly() {
            // Given
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(createTestActionId("CONDITION"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Check Value")
                    .actionDesc("Check if value is greater than 5")
                    .actionParams(ConditionParameter.builder()
                            .expression("testValue > 5")
                            .debug(true)
                            .build())
                    .build();

            ActionConfig trueAction = ActionConfig.builder()
                    .id(createTestActionId("TRUE"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("True Branch")
                    .actionDesc("Execute when condition is true")
                    .actionParams(ScriptParameter.builder()
                            .script("'Condition was true'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(conditionAction)
                    .next(conditionAction, trueAction);

            Map<String, Object> globalParams = new HashMap<>();
            globalParams.put("testValue", 10);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play(globalParams, null, true);

                assertThat(result).isNotNull();
                assertThat(accordion.verbose()).contains("Check Value");

                logger.info("Conditional execution completed: {}", accordion.verbose());
            }
        }

        @Test
        @DisplayName("Should handle parallel branches correctly")
        void shouldHandleParallelBranchesCorrectly() {
            // Given
            ActionConfig rootAction = ActionConfig.builder()
                    .id(createTestActionId("ROOT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Root Action")
                    .actionDesc("Initialize data")
                    .actionParams(ScriptParameter.builder()
                            .script("let data = 'initialized'; data")
                            .build())
                    .build();

            ActionConfig branch1Action = ActionConfig.builder()
                    .id(createTestActionId("BRANCH1"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch 1")
                    .actionDesc("Process branch 1")
                    .actionParams(ScriptParameter.builder()
                            .script("'Branch 1 processed'")
                            .build())
                    .build();

            ActionConfig branch2Action = ActionConfig.builder()
                    .id(createTestActionId("BRANCH2"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch 2")
                    .actionDesc("Process branch 2")
                    .actionParams(ScriptParameter.builder()
                            .script("'Branch 2 processed'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(rootAction)
                    .next(rootAction, branch1Action, branch2Action);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play(true);

                assertThat(result).isNotNull();
                assertThat(accordion.verbose()).contains("Root Action");
                assertThat(accordion.verbose()).contains("Branch 1");
                assertThat(accordion.verbose()).contains("Branch 2");

                logger.info("Parallel execution completed: {}", accordion.verbose());
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle action execution errors gracefully")
        void shouldHandleActionExecutionErrorsGracefully() {
            // Given
            ActionConfig errorAction = ActionConfig.builder()
                    .id(createTestActionId("ERROR"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Error Action")
                    .actionDesc("Action that will fail")
                    .actionParams(ScriptParameter.builder()
                            .script("throw new RuntimeException('Test error')")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(errorAction);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                // Should not throw exception, but handle error gracefully
                assertThatCode(() -> accordion.play(true))
                        .doesNotThrowAnyException();

                String verbose = accordion.verbose();
                assertThat(verbose).contains("Error Action");

                logger.info("Error handling test completed: {}", verbose);
            }
        }

        @Test
        @DisplayName("Should handle invalid action configuration")
        void shouldHandleInvalidActionConfiguration() {
            // Given - Action with invalid script
            ActionConfig invalidAction = ActionConfig.builder()
                    .id(createTestActionId("INVALID"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Invalid Action")
                    .actionDesc("Action with invalid script")
                    .actionParams(ScriptParameter.builder()
                            .script("invalid syntax $$$ @@@")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(invalidAction);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                assertThatCode(() -> accordion.play(true))
                        .doesNotThrowAnyException();

                logger.info("Invalid configuration test completed: {}", accordion.verbose());
            }
        }
    }

    @Nested
    @DisplayName("JSON Configuration Tests")
    class JsonConfigurationTests {

        @Test
        @DisplayName("Should export and import complex plan successfully")
        void shouldExportAndImportComplexPlanSuccessfully() {
            // Given
            ActionConfig action1 = ActionConfig.builder()
                    .id(createTestActionId("JSON1"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("JSON Test Action 1")
                    .actionDesc("First action for JSON test")
                    .actionParams(ScriptParameter.builder()
                            .script("'First action result'")
                            .build())
                    .build();

            ActionConfig action2 = ActionConfig.builder()
                    .id(createTestActionId("JSON2"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("JSON Test Action 2")
                    .actionDesc("Second action for JSON test")
                    .actionParams(ScriptParameter.builder()
                            .script("'Second action result'")
                            .build())
                    .build();

            AccordionPlan originalPlan = AccordionPlan.of()
                    .start(action1)
                    .next(action1, action2);

            // When
            String jsonConfig = originalPlan.exportToJsonConfig();
            AccordionPlan importedPlan = AccordionPlan.of().importConfig(jsonConfig);

            // Then
            assertThat(jsonConfig).isNotEmpty();
            // Test that the imported plan can be executed successfully
            assertThat(importedPlan).isNotNull();

            // Test execution of imported plan
            try (Accordion accordion = new Accordion(importedPlan)) {
                ExecuteResult result = accordion.play(true);
                assertThat(result).isNotNull();

                String verbose = accordion.verbose();
                assertThat(verbose).contains("JSON Test Action 1");
                assertThat(verbose).contains("JSON Test Action 2");

                logger.info("JSON import/export test completed: {}", verbose);
            }
        }
    }

    @Nested
    @DisplayName("Resource Management Tests")
    class ResourceManagementTests {

        @Test
        @DisplayName("Should properly manage resources across multiple executions")
        void shouldProperlyManageResourcesAcrossMultipleExecutions() {
            // Given
            ActionConfig action = ActionConfig.builder()
                    .id(createTestActionId("RESOURCE"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Resource Test Action")
                    .actionDesc("Test resource management")
                    .actionParams(ScriptParameter.builder()
                            .script("'resource test completed'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                // Multiple executions should work without issues
                for (int i = 0; i < 5; i++) {
                    ExecuteResult result = accordion.play();
                    assertThat(result).isNotNull();

                    // Small delay between executions
                    waitFor(10);
                }

                logger.info("Resource management test completed successfully");
            }
        }

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() {
            // Given
            ActionConfig action = ActionConfig.builder()
                    .id(createTestActionId("CONCURRENT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Concurrent Test Action")
                    .actionDesc("Test concurrent access")
                    .actionParams(ScriptParameter.builder()
                            .script("'concurrent test'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            // When & Then - Each thread should have its own Accordion instance
            assertThatCode(() -> {
                Thread[] threads = new Thread[3];
                for (int i = 0; i < threads.length; i++) {
                    final int threadIndex = i;
                    threads[i] = new Thread(() -> {
                        try (Accordion accordion = new Accordion(plan)) {
                            ExecuteResult result = accordion.play();
                            assertThat(result).isNotNull();
                            logger.info("Thread {} completed execution", threadIndex);
                        }
                    });
                }

                for (Thread thread : threads) {
                    thread.start();
                }

                for (Thread thread : threads) {
                    thread.join(5000); // 5 second timeout
                }
            }).doesNotThrowAnyException();
        }
    }
}