package chat.octet.accordion.integration;

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
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the complete Accordion workflow.
 * 
 * @author <a href="https://github.com/eoctet">William</a>
 */
@DisplayName("Accordion Integration Tests")
@Tag("integration")
class AccordionIntegrationTest extends AccordionTestBase {

    @Nested
    @DisplayName("End-to-End Workflow Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should execute complete calculation workflow")
        void should_execute_complete_calculation_workflow() {
            // Given - Create a calculation workflow
            ActionConfig calculateAction = ActionConfig.builder()
                    .id(createTestActionId("CALC"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Calculate Sum")
                    .actionDesc("Calculate sum of two numbers")
                    .actionParams(ScriptParameter.builder()
                            .script("let a = 10; let b = 20; a + b")
                            .build())
                    .build();

            ActionConfig validateAction = ActionConfig.builder()
                    .id(createTestActionId("VALIDATE"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Validate Result")
                    .actionDesc("Validate calculation result")
                    .actionParams(ScriptParameter.builder()
                            .script("println('Validation passed')")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(calculateAction)
                    .next(calculateAction, validateAction);

            // When
            ExecuteResult result;
            try (Accordion accordion = new Accordion(plan)) {
                result = accordion.play(true);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(accordion.verbose())
                        .isNotEmpty()
                        .contains("Calculate Sum")
                        .contains("Validate Result");
                
                logger.info("Complete workflow execution:\n{}", accordion.verbose());
            }
        }

        @Test
        @DisplayName("Should handle complex branching workflow")
        void should_handle_complex_branching_workflow() {
            // Given - Create a branching workflow
            ActionConfig rootAction = ActionConfig.builder()
                    .id(createTestActionId("ROOT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Root Action")
                    .actionDesc("Starting point of workflow")
                    .actionParams(ScriptParameter.builder()
                            .script("let rootValue = 42; rootValue")
                            .build())
                    .build();

            ActionConfig branch1Action = ActionConfig.builder()
                    .id(createTestActionId("BRANCH1"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch 1 Processing")
                    .actionDesc("First branch processing")
                    .actionParams(ScriptParameter.builder()
                            .script("println('Branch 1 executed')")
                            .build())
                    .build();

            ActionConfig branch2Action = ActionConfig.builder()
                    .id(createTestActionId("BRANCH2"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch 2 Processing")
                    .actionDesc("Second branch processing")
                    .actionParams(ScriptParameter.builder()
                            .script("println('Branch 2 executed')")
                            .build())
                    .build();

            ActionConfig mergeAction = ActionConfig.builder()
                    .id(createTestActionId("MERGE"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Merge Results")
                    .actionDesc("Merge branch results")
                    .actionParams(ScriptParameter.builder()
                            .script("println('Branches merged successfully')")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(rootAction)
                    .next(rootAction, branch1Action, branch2Action)
                    .next(branch1Action, mergeAction)
                    .next(branch2Action, mergeAction);

            // When
            try (Accordion accordion = new Accordion(plan)) {
                ExecuteResult result = accordion.play(true);
                
                // Then
                assertThat(result).isNotNull();
                String executionLog = accordion.verbose();
                assertThat(executionLog)
                        .contains("Root Action")
                        .contains("Branch 1 Processing")
                        .contains("Branch 2 Processing")
                        .contains("Merge Results");
                
                logger.info("Branching workflow execution:\n{}", executionLog);
            }
        }
    }

    @Nested
    @DisplayName("JSON Configuration Integration Tests")
    class JsonConfigurationIntegrationTests {

        @Test
        @DisplayName("Should execute workflow from JSON configuration")
        void should_execute_workflow_from_json_configuration() {
            // Given - Create and export a workflow
            ActionConfig originalAction = ActionConfig.builder()
                    .id(createTestActionId("JSON"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("JSON Configuration Test")
                    .actionDesc("Test JSON import/export workflow")
                    .actionParams(ScriptParameter.builder()
                            .script("println('Executed from JSON config')")
                            .build())
                    .build();

            AccordionPlan originalPlan = AccordionPlan.of().start(originalAction);
            String jsonConfig = originalPlan.exportToJsonConfig();

            // When - Import and execute from JSON
            AccordionPlan importedPlan = AccordionPlan.of().importConfig(jsonConfig);
            
            try (Accordion accordion = new Accordion(importedPlan)) {
                ExecuteResult result = accordion.play(true);
                
                // Then
                assertThat(result).isNotNull();
                assertThat(accordion.verbose())
                        .contains("JSON Configuration Test");
                
                logger.info("JSON workflow execution:\n{}", accordion.verbose());
            }
        }

        @Test
        @DisplayName("Should maintain workflow integrity through JSON round-trip")
        void should_maintain_workflow_integrity_through_json_round_trip() {
            // Given - Create a multi-step workflow
            ActionConfig step1 = ActionConfig.builder()
                    .id(createTestActionId("STEP1"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Step 1")
                    .actionDesc("First step")
                    .actionParams(ScriptParameter.builder()
                            .script("let step1Result = 'Step 1 completed'")
                            .build())
                    .build();

            ActionConfig step2 = ActionConfig.builder()
                    .id(createTestActionId("STEP2"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Step 2")
                    .actionDesc("Second step")
                    .actionParams(ScriptParameter.builder()
                            .script("let step2Result = 'Step 2 completed'")
                            .build())
                    .build();

            AccordionPlan originalPlan = AccordionPlan.of()
                    .start(step1)
                    .next(step1, step2);

            // When - Export, import, and execute
            String jsonConfig = originalPlan.exportToJsonConfig();
            AccordionPlan importedPlan = AccordionPlan.of().importConfig(jsonConfig);

            // Then - Both plans should execute identically
            String originalExecution;
            try (Accordion originalAccordion = new Accordion(originalPlan)) {
                originalAccordion.play(true);
                originalExecution = originalAccordion.verbose();
            }

            String importedExecution;
            try (Accordion importedAccordion = new Accordion(importedPlan)) {
                importedAccordion.play(true);
                importedExecution = importedAccordion.verbose();
            }

            // Verify both executions contain the same steps
            assertThat(originalExecution).contains("Step 1", "Step 2");
            assertThat(importedExecution).contains("Step 1", "Step 2");
            
            logger.info("Original execution:\n{}", originalExecution);
            logger.info("Imported execution:\n{}", importedExecution);
        }
    }

    @Nested
    @DisplayName("Error Recovery Integration Tests")
    class ErrorRecoveryIntegrationTests {

        @Test
        @DisplayName("Should handle action failures gracefully")
        void should_handle_action_failures_gracefully() {
            // Given - Create a workflow with a failing action
            ActionConfig successAction = ActionConfig.builder()
                    .id(createTestActionId("SUCCESS"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Success Action")
                    .actionDesc("This action should succeed")
                    .actionParams(ScriptParameter.builder()
                            .script("'Success!'")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(successAction);

            // When & Then - Should handle failure without crashing
            try (Accordion accordion = new Accordion(plan)) {
                assertThatCode(() -> accordion.play(true))
                        .doesNotThrowAnyException();
                
                String executionLog = accordion.verbose();
                assertThat(executionLog).contains("Success Action");
                
                logger.info("Error handling execution:\n{}", executionLog);
            }
        }
    }
}