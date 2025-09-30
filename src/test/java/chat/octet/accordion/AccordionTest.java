package chat.octet.accordion;

import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.test.AccordionTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for the main Accordion execution engine.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@DisplayName("Accordion Engine Tests")
class AccordionTest extends AccordionTestBase {

    @Nested
    @DisplayName("Basic Execution Tests")
    class BasicExecutionTests {

        @Test
        @DisplayName("Should execute simple script action successfully")
        void shouldExecuteSimpleScriptActionSuccessfully() {
            // Given
            ActionConfig scriptAction = ActionConfig.builder()
                    .id(createTestActionId("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Simple Script Test")
                    .actionDesc("Test script execution")
                    .actionParams(ScriptParameter.builder()
                            .script("1 + 1")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(scriptAction);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                var result = accordion.play(true);

                assertThat(result).isNotNull();
                assertThat(accordion.verbose()).isNotEmpty();
                logger.info("Execution result: {}", accordion.verbose());
            }
        }

        @Test
        @DisplayName("Should handle empty plan gracefully")
        void shouldHandleEmptyPlanGracefully() {
            // Given
            ActionConfig emptyAction = ActionConfig.builder()
                    .id(createTestActionId("EMPTY"))
                    .actionType(ActionType.TEST.name())
                    .actionName("Empty Test Action")
                    .actionDesc("Test empty action")
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(emptyAction);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                assertThatCode(accordion::play)
                        .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Plan Configuration Tests")
    class PlanConfigurationTests {

        @ParameterizedTest
        @ValueSource(strings = {"println('Hello')", "2 * 3", "math.abs(-10)"})
        @DisplayName("Should execute various script expressions")
        void shouldExecuteVariousScriptExpressions(final String script) {
            // Given
            ActionConfig scriptAction = ActionConfig.builder()
                    .id(createTestActionId("SCRIPT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Parameterized Script Test")
                    .actionDesc("Test various script expressions")
                    .actionParams(ScriptParameter.builder()
                            .script(script)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(scriptAction);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                assertThatCode(accordion::play)
                        .doesNotThrowAnyException();

                logger.info("Executed script: {} - Result: {}", script, accordion.verbose());
            }
        }

        @Test
        @DisplayName("Should export plan to JSON successfully")
        void shouldExportPlanToJsonSuccessfully() {
            // Given
            ActionConfig action = ActionConfig.builder()
                    .id(createTestActionId("JSON"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("JSON Export Test")
                    .actionDesc("Test JSON export functionality")
                    .actionParams(ScriptParameter.builder()
                            .script("'test'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            // When
            String jsonConfig = plan.exportToJsonConfig();

            // Then
            assertThat(jsonConfig)
                    .isNotNull()
                    .isNotEmpty()
                    .contains("JSON Export Test")
                    .contains("actions")
                    .contains("edges");

            logger.info("Exported JSON config: {}", jsonConfig);
        }
    }

    @Nested
    @DisplayName("Resource Management Tests")
    class ResourceManagementTests {

        @Test
        @DisplayName("Should properly close resources with try-with-resources")
        void shouldProperlyCloseResourcesWithTryWithResources() {
            // Given
            ActionConfig action = ActionConfig.builder()
                    .id(createTestActionId("RESOURCE"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Resource Management Test")
                    .actionDesc("Test resource cleanup")
                    .actionParams(ScriptParameter.builder()
                            .script("'cleanup test'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            // When & Then - Should not throw any exceptions during resource cleanup
            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play();
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle multiple executions with reset")
        void shouldHandleMultipleExecutionsWithReset() {
            // Given
            ActionConfig action = ActionConfig.builder()
                    .id(createTestActionId("MULTI"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Multiple Execution Test")
                    .actionDesc("Test multiple executions")
                    .actionParams(ScriptParameter.builder()
                            .script("rand(100)")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            // When & Then
            try (Accordion accordion = new Accordion(plan)) {
                // First execution
                accordion.play(true);
                String firstResult = accordion.verbose();
                assertThat(firstResult).isNotEmpty();

                // Second execution (should reset automatically)
                accordion.play(true);
                String secondResult = accordion.verbose();
                assertThat(secondResult).isNotEmpty();

                logger.info("First execution: {}", firstResult);
                logger.info("Second execution: {}", secondResult);
            }
        }
    }
}