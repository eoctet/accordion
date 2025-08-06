package chat.octet.accordion;

import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.test.AccordionTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AccordionPlan functionality.
 * 
 * @author <a href="https://github.com/eoctet">William</a>
 */
@DisplayName("AccordionPlan Tests")
class AccordionPlanTest extends AccordionTestBase {

    @Nested
    @DisplayName("Plan Creation Tests")
    class PlanCreationTests {

        @Test
        @DisplayName("Should create plan with single action")
        void should_create_plan_with_single_action() {
            // Given
            ActionConfig action = createTestAction("Single Action Test");

            // When
            AccordionPlan plan = AccordionPlan.of().start(action);

            // Then
            assertThat(plan).isNotNull();
            assertThat(plan.getGraphNodes()).hasSize(1);
            assertThat(plan.getRootGraphNode()).isNotNull();
            assertThat(plan.getRootGraphNode().getActionName()).isEqualTo("Single Action Test");
        }

        @Test
        @DisplayName("Should create plan with chained actions")
        void should_create_plan_with_chained_actions() {
            // Given
            ActionConfig firstAction = createTestAction("First Action");
            ActionConfig secondAction = createTestAction("Second Action");
            ActionConfig thirdAction = createTestAction("Third Action");

            // When
            AccordionPlan plan = AccordionPlan.of()
                    .start(firstAction)
                    .next(firstAction, secondAction)
                    .next(secondAction, thirdAction);

            // Then
            assertThat(plan.getGraphNodes()).hasSize(3);
            assertThat(plan.getRootGraphNode().getActionName()).isEqualTo("First Action");
        }

        @Test
        @DisplayName("Should create plan with multiple next actions")
        void should_create_plan_with_multiple_next_actions() {
            // Given
            ActionConfig rootAction = createTestAction("Root Action");
            ActionConfig branchAction1 = createTestAction("Branch Action 1");
            ActionConfig branchAction2 = createTestAction("Branch Action 2");

            // When
            AccordionPlan plan = AccordionPlan.of()
                    .start(rootAction)
                    .next(rootAction, branchAction1, branchAction2);

            // Then
            assertThat(plan.getGraphNodes()).hasSize(3);
            assertThat(plan.getRootGraphNode().getEdges()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("JSON Import/Export Tests")
    class JsonImportExportTests {

        @Test
        @DisplayName("Should export and import plan configuration")
        void should_export_and_import_plan_configuration() {
            // Given
            ActionConfig action1 = createTestAction("Export Test Action 1");
            ActionConfig action2 = createTestAction("Export Test Action 2");
            
            AccordionPlan originalPlan = AccordionPlan.of()
                    .start(action1)
                    .next(action1, action2);

            // When
            String jsonConfig = originalPlan.exportToJsonConfig();
            AccordionPlan importedPlan = AccordionPlan.of().importConfig(jsonConfig);

            // Then
            assertThat(jsonConfig).isNotEmpty();
            assertThat(importedPlan.getGraphNodes()).hasSize(2);
            assertThat(importedPlan.getRootGraphNode()).isNotNull();
            
            logger.info("Exported JSON: {}", jsonConfig);
        }

        @ParameterizedTest
        @CsvSource({
            "Action A, Action B",
            "测试动作1, 测试动作2",
            "Action with spaces, Another action"
        })
        @DisplayName("Should handle various action names in JSON export")
        void should_handle_various_action_names_in_json_export(String name1, String name2) {
            // Given
            ActionConfig action1 = createTestActionWithName(name1);
            ActionConfig action2 = createTestActionWithName(name2);
            
            AccordionPlan plan = AccordionPlan.of()
                    .start(action1)
                    .next(action1, action2);

            // When
            String jsonConfig = plan.exportToJsonConfig();

            // Then
            assertThat(jsonConfig)
                    .contains(name1)
                    .contains(name2);
        }
    }

    @Nested
    @DisplayName("Plan Reset Tests")
    class PlanResetTests {

        @Test
        @DisplayName("Should reset plan state successfully")
        void should_reset_plan_state_successfully() {
            // Given
            ActionConfig action = createTestAction("Reset Test Action");
            AccordionPlan plan = AccordionPlan.of().start(action);

            // When
            plan.reset();

            // Then
            assertThatCode(() -> plan.reset()).doesNotThrowAnyException();
            assertThat(plan.getGraphNodes()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception when starting with non-empty plan")
        void should_throw_exception_when_starting_with_non_empty_plan() {
            // Given
            ActionConfig action1 = createTestAction("First Action");
            ActionConfig action2 = createTestAction("Second Action");
            AccordionPlan plan = AccordionPlan.of().start(action1);

            // When & Then
            assertThatThrownBy(() -> plan.start(action2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not allowed to add because the list is not empty");
        }

        @Test
        @DisplayName("Should handle null action config gracefully")
        void should_handle_null_action_config_gracefully() {
            // When & Then
            assertThatThrownBy(() -> AccordionPlan.of().start(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // Helper methods
    private ActionConfig createTestAction(String name) {
        return ActionConfig.builder()
                .id(createTestActionId("TEST"))
                .actionType(ActionType.SCRIPT.name())
                .actionName(name)
                .actionDesc("Test action: " + name)
                .actionParams(ScriptParameter.builder()
                        .script("'test'")
                        .build())
                .build();
    }

    private ActionConfig createTestActionWithName(String name) {
        return ActionConfig.builder()
                .id(createTestActionId("NAME"))
                .actionType(ActionType.SCRIPT.name())
                .actionName(name)
                .actionDesc("Test action with custom name")
                .actionParams(ScriptParameter.builder()
                        .script("'" + name + "'")
                        .build())
                .build();
    }
}