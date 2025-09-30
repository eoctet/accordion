package chat.octet.accordion.action;

import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.base.ConditionParameter;
import chat.octet.accordion.action.base.SwitchParameter;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.action.shell.ShellParameter;
import chat.octet.accordion.core.condition.Condition;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.core.enums.ConditionOperator;
import chat.octet.accordion.core.enums.DataType;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Comprehensive integration tests for all Action types.
 * This test class covers ScriptAction, ShellAction, ConditionAction, and SwitchAction
 * in real workflow scenarios to improve test coverage.
 */
@Slf4j
@DisplayName("Action Integration Tests")
class ActionIntegrationTest {

    @Nested
    @DisplayName("ScriptAction Integration Tests")
    class ScriptActionIntegrationTests {

        @Test
        @DisplayName("Should execute simple arithmetic script")
        void shouldExecuteSimpleArithmeticScript() {
            log.info("Testing ScriptAction with arithmetic operation");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Arithmetic Script")
                    .actionDesc("Simple arithmetic calculation")
                    .actionParams(ScriptParameter.builder().script("5 + 3").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("result", DataType.LONG, "Calculation result")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    ExecuteResult result = accordion.play(true);
                    assertThat(result).isNotNull();
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute script with string manipulation")
        void shouldExecuteScriptWithStringManipulation() {
            log.info("Testing ScriptAction with string operations");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("String Script")
                    .actionDesc("String manipulation")
                    .actionParams(ScriptParameter.builder()
                            .script("'Hello ' + 'World'")
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("message", DataType.STRING, "String result")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute script with variable substitution")
        void shouldExecuteScriptWithVariableSubstitution() {
            log.info("Testing ScriptAction with variable substitution");

            Message message = new Message();
            message.put("num1", 10);
            message.put("num2", 20);

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Variable Script")
                    .actionDesc("Script with variables")
                    .actionParams(ScriptParameter.builder()
                            .script("num1 + num2")
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("sum", DataType.LONG, "Sum result")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute complex script with multiple operations")
        void shouldExecuteComplexScript() {
            log.info("Testing ScriptAction with complex operations");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Complex Script")
                    .actionDesc("Complex calculation")
                    .actionParams(ScriptParameter.builder()
                            .script("let result = (10 + 5) * 2 - 8; result")
                            .debug(false)
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("result", DataType.LONG, "Complex result")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute script with boolean logic")
        void shouldExecuteScriptWithBooleanLogic() {
            log.info("Testing ScriptAction with boolean operations");

            Message message = new Message();
            message.put("flag", true);
            message.put("value", 100);

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Boolean Script")
                    .actionDesc("Boolean logic test")
                    .actionParams(ScriptParameter.builder()
                            .script("flag && (value > 50)")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, false);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ShellAction Integration Tests")
    @DisabledOnOs(OS.WINDOWS)
    class ShellActionIntegrationTests {

        @Test
        @DisplayName("Should execute simple shell command")
        void shouldExecuteSimpleShellCommand() {
            log.info("Testing ShellAction with echo command");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SHELL.name())
                    .actionName("Echo Command")
                    .actionDesc("Simple echo test")
                    .actionParams(ShellParameter.builder()
                            .type(ShellParameter.ShellType.BASH)
                            .shell("echo 'Test Message'")
                            .timeout(5000)
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("output", DataType.STRING, "Command output")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute shell command with variable substitution")
        void shouldExecuteShellCommandWithVariables() {
            log.info("Testing ShellAction with variable substitution");

            Message message = new Message();
            message.put("text", "Hello Accordion");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SHELL.name())
                    .actionName("Echo with Variable")
                    .actionDesc("Echo with parameter")
                    .actionParams(ShellParameter.builder()
                            .type(ShellParameter.ShellType.BASH)
                            .shell("echo '${text}'")
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("output", DataType.STRING, "Echo result")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute shell command with piping")
        void shouldExecuteShellCommandWithPiping() {
            log.info("Testing ShellAction with pipe operations");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SHELL.name())
                    .actionName("Pipe Command")
                    .actionDesc("Test pipe operations")
                    .actionParams(ShellParameter.builder()
                            .type(ShellParameter.ShellType.BASH)
                            .shell("echo 'one two three' | wc -w")
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("count", DataType.STRING, "Word count")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute shell command with environment variable")
        void shouldExecuteShellCommandWithEnvVar() {
            log.info("Testing ShellAction with environment variables");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SHELL.name())
                    .actionName("Env Variable Command")
                    .actionDesc("Test environment variable")
                    .actionParams(ShellParameter.builder()
                            .type(ShellParameter.ShellType.BASH)
                            .shell("echo $USER")
                            .build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("user", DataType.STRING, "User name")))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("ConditionAction Integration Tests")
    class ConditionActionIntegrationTests {

        @Test
        @DisplayName("Should evaluate simple equality condition")
        void shouldEvaluateSimpleEqualityCondition() {
            log.info("Testing ConditionAction with equality check");

            // First action: calculate
            ActionConfig calcAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Calculate")
                    .actionParams(ScriptParameter.builder().script("1 + 1").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("number", DataType.LONG, "Result")))
                    .build();

            // Second action: check if equals 2
            Condition condition = new Condition("number", ConditionOperator.EQ, 2);
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Check Result")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(calcAction)
                    .next(calcAction, conditionAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should evaluate greater than condition")
        void shouldEvaluateGreaterThanCondition() {
            log.info("Testing ConditionAction with greater than comparison");

            Message message = new Message();
            message.put("value", 100);

            Condition condition = new Condition("value", ConditionOperator.GT, 50);
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Check Greater Than")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .debug(true)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(conditionAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should evaluate complex AND condition")
        void shouldEvaluateComplexAndCondition() {
            log.info("Testing ConditionAction with AND logic");

            Message message = new Message();
            message.put("age", 25);
            message.put("status", "active");

            Condition condition = new Condition("age", ConditionOperator.GT, 18)
                    .and("age", ConditionOperator.LT, 65)
                    .and("status", ConditionOperator.EQ, "active");

            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Complex Condition")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(conditionAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, false);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should evaluate condition with arithmetic expression")
        void shouldEvaluateConditionWithArithmetic() {
            log.info("Testing ConditionAction with arithmetic in expression");

            Message message = new Message();
            message.put("num", 20);
            message.put("div", 2.5);

            Condition condition = new Condition("num*div", ConditionOperator.GT, 40);
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Arithmetic Condition")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .debug(true)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(conditionAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle condition breaking workflow")
        void shouldHandleConditionBreakingWorkflow() {
            log.info("Testing ConditionAction breaking execution flow");

            Message message = new Message();
            message.put("value", 10);

            // Condition that should fail (10 is not > 50)
            Condition condition = new Condition("value", ConditionOperator.GT, 50);
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Break Condition")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .build())
                    .build();

            // This should not execute due to condition failure
            ActionConfig afterAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("After Condition")
                    .actionParams(ScriptParameter.builder()
                            .script("println('Should not execute')")
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(conditionAction)
                    .next(conditionAction, afterAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("SwitchAction Integration Tests")
    class SwitchActionIntegrationTests {

        @Test
        @DisplayName("Should execute switch with multiple branches")
        void shouldExecuteSwitchWithMultipleBranches() {
            log.info("Testing SwitchAction with multiple branches");

            Message message = new Message();
            message.put("type", "A");

            String branchAId = CommonUtils.randomString("ACT");
            String branchBId = CommonUtils.randomString("ACT");
            String branchCId = CommonUtils.randomString("ACT");

            // Create branch actions
            ActionConfig branchA = ActionConfig.builder()
                    .id(branchAId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch A")
                    .actionParams(ScriptParameter.builder().script("println('Branch A executed')").build())
                    .build();

            ActionConfig branchB = ActionConfig.builder()
                    .id(branchBId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch B")
                    .actionParams(ScriptParameter.builder().script("println('Branch B executed')").build())
                    .build();

            ActionConfig branchC = ActionConfig.builder()
                    .id(branchCId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Branch C")
                    .actionParams(ScriptParameter.builder().script("println('Branch C executed')").build())
                    .build();

            // Create switch action
            SwitchParameter.Branch switchBranchA = SwitchParameter.Branch.builder()
                    .name("Type A")
                    .actionId(branchAId)
                    .expression("type == 'A'")
                    .negation(false)
                    .build();

            SwitchParameter.Branch switchBranchB = SwitchParameter.Branch.builder()
                    .name("Type B")
                    .actionId(branchBId)
                    .expression("type == 'B'")
                    .negation(false)
                    .build();

            SwitchParameter.Branch switchBranchC = SwitchParameter.Branch.builder()
                    .name("Type C")
                    .actionId(branchCId)
                    .expression("type == 'C'")
                    .negation(false)
                    .build();

            ActionConfig switchAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SWITCH.name())
                    .actionName("Type Switch")
                    .actionParams(SwitchParameter.builder()
                            .branches(Lists.newArrayList(switchBranchA, switchBranchB, switchBranchC))
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(switchAction)
                    .next(switchAction, branchA)
                    .next(switchAction, branchB)
                    .next(switchAction, branchC);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute switch with numeric condition")
        void shouldExecuteSwitchWithNumericCondition() {
            log.info("Testing SwitchAction with numeric conditions");

            Message message = new Message();
            message.put("score", 85);

            String highScoreId = CommonUtils.randomString("ACT");
            String midScoreId = CommonUtils.randomString("ACT");
            String lowScoreId = CommonUtils.randomString("ACT");

            ActionConfig highScore = ActionConfig.builder()
                    .id(highScoreId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("High Score")
                    .actionParams(ScriptParameter.builder().script("println('Excellent!')").build())
                    .build();

            ActionConfig midScore = ActionConfig.builder()
                    .id(midScoreId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Mid Score")
                    .actionParams(ScriptParameter.builder().script("println('Good!')").build())
                    .build();

            ActionConfig lowScore = ActionConfig.builder()
                    .id(lowScoreId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Low Score")
                    .actionParams(ScriptParameter.builder().script("println('Need improvement')").build())
                    .build();

            SwitchParameter switchParam = SwitchParameter.builder().build();
            switchParam.addBranch(
                    SwitchParameter.Branch.builder()
                            .name("High")
                            .actionId(highScoreId)
                            .expression("score >= 80")
                            .negation(false)
                            .build(),
                    SwitchParameter.Branch.builder()
                            .name("Mid")
                            .actionId(midScoreId)
                            .expression("score >= 60 && score < 80")
                            .negation(false)
                            .build(),
                    SwitchParameter.Branch.builder()
                            .name("Low")
                            .actionId(lowScoreId)
                            .expression("score < 60")
                            .negation(false)
                            .build()
            );

            ActionConfig switchAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SWITCH.name())
                    .actionName("Score Switch")
                    .actionParams(switchParam)
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(switchAction)
                    .next(switchAction, highScore)
                    .next(switchAction, midScore)
                    .next(switchAction, lowScore);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute switch with negation")
        void shouldExecuteSwitchWithNegation() {
            log.info("Testing SwitchAction with negated conditions");

            Message message = new Message();
            message.put("enabled", false);

            String enabledBranchId = CommonUtils.randomString("ACT");
            String disabledBranchId = CommonUtils.randomString("ACT");

            ActionConfig enabledBranch = ActionConfig.builder()
                    .id(enabledBranchId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Enabled Branch")
                    .actionParams(ScriptParameter.builder().script("println('Enabled')").build())
                    .build();

            ActionConfig disabledBranch = ActionConfig.builder()
                    .id(disabledBranchId)
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Disabled Branch")
                    .actionParams(ScriptParameter.builder().script("println('Disabled')").build())
                    .build();

            ActionConfig switchAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SWITCH.name())
                    .actionName("Enabled Switch")
                    .actionParams(SwitchParameter.builder()
                            .branches(Lists.newArrayList(
                                    SwitchParameter.Branch.builder()
                                            .name("Enabled")
                                            .actionId(enabledBranchId)
                                            .expression("enabled == true")
                                            .negation(false)
                                            .build(),
                                    SwitchParameter.Branch.builder()
                                            .name("Disabled")
                                            .actionId(disabledBranchId)
                                            .expression("enabled == true")
                                            .negation(true)  // Negated condition
                                            .build()
                            ))
                            .debug(true)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(switchAction)
                    .next(switchAction, enabledBranch)
                    .next(switchAction, disabledBranch);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Complex Workflow Integration Tests")
    class ComplexWorkflowTests {

        @Test
        @DisplayName("Should execute workflow with script and condition chaining")
        void shouldExecuteWorkflowWithScriptAndCondition() {
            log.info("Testing complex workflow with script and condition");

            // Step 1: Calculate value
            ActionConfig calcAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Calculate")
                    .actionParams(ScriptParameter.builder().script("10 * 5").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("result", DataType.LONG, "Result")))
                    .build();

            // Step 2: Check if result > 40
            Condition condition = new Condition("result", ConditionOperator.GT, 40);
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Check Result")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .build())
                    .build();

            // Step 3: Print success message
            ActionConfig successAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Success")
                    .actionParams(ScriptParameter.builder().script("println('Success: result > 40')").build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(calcAction)
                    .next(calcAction, conditionAction)
                    .next(conditionAction, successAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute workflow with global and message parameters")
        void shouldExecuteWorkflowWithGlobalAndMessageParams() {
            log.info("Testing workflow with global and message parameters");

            Map<String, Object> global = Maps.newLinkedHashMap();
            global.put("multiplier", 3);

            Message message = new Message();
            message.put("base", 5);

            ActionConfig calcAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Multiply")
                    .actionParams(ScriptParameter.builder().script("base * multiplier").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("product", DataType.LONG, "Product")))
                    .build();

            Condition condition = new Condition("product", ConditionOperator.EQ, 15);
            ActionConfig conditionAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.CONDITION.name())
                    .actionName("Verify")
                    .actionParams(ConditionParameter.builder()
                            .expression(ConditionBuilder.getInstance().build(condition))
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(calcAction)
                    .next(calcAction, conditionAction);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(global, message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute complex workflow with switch and multiple branches")
        @DisabledOnOs(OS.WINDOWS)
        void shouldExecuteComplexWorkflowWithSwitch() {
            log.info("Testing complex workflow with switch, shell, and script actions");

            Message message = new Message();
            message.put("environment", "development");

            String devBranchId = CommonUtils.randomString("ACT");
            String prodBranchId = CommonUtils.randomString("ACT");

            // Development branch
            ActionConfig devBranch = ActionConfig.builder()
                    .id(devBranchId)
                    .actionType(ActionType.SHELL.name())
                    .actionName("Dev Deployment")
                    .actionParams(ShellParameter.builder()
                            .shell("echo 'Deploying to development'")
                            .build())
                    .build();

            // Production branch
            ActionConfig prodBranch = ActionConfig.builder()
                    .id(prodBranchId)
                    .actionType(ActionType.SHELL.name())
                    .actionName("Prod Deployment")
                    .actionParams(ShellParameter.builder()
                            .shell("echo 'Deploying to production'")
                            .build())
                    .build();

            // Switch action
            ActionConfig switchAction = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SWITCH.name())
                    .actionName("Environment Switch")
                    .actionParams(SwitchParameter.builder()
                            .branches(Lists.newArrayList(
                                    SwitchParameter.Branch.builder()
                                            .name("Development")
                                            .actionId(devBranchId)
                                            .expression("environment == 'development'")
                                            .build(),
                                    SwitchParameter.Branch.builder()
                                            .name("Production")
                                            .actionId(prodBranchId)
                                            .expression("environment == 'production'")
                                            .build()
                            ))
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(switchAction)
                    .next(switchAction, devBranch)
                    .next(switchAction, prodBranch);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, true);
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should execute workflow with sequential script actions")
        void shouldExecuteWorkflowWithSequentialScripts() {
            log.info("Testing workflow with sequential script actions");

            ActionConfig action1 = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Step 1")
                    .actionParams(ScriptParameter.builder().script("5").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("step1", DataType.LONG, "Step 1 result")))
                    .build();

            ActionConfig action2 = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Step 2")
                    .actionParams(ScriptParameter.builder().script("step1 * 2").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("step2", DataType.LONG, "Step 2 result")))
                    .build();

            ActionConfig action3 = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.SCRIPT.name())
                    .actionName("Step 3")
                    .actionParams(ScriptParameter.builder().script("step2 + 5").build())
                    .actionOutput(Lists.newArrayList(new OutputParameter("final", DataType.LONG, "Final result")))
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(action1)
                    .next(action1, action2)
                    .next(action2, action3);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(true);
                }
            }).doesNotThrowAnyException();
        }
    }
}