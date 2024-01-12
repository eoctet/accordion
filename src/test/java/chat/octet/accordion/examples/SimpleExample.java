package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.base.ConditionParameter;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.core.condition.Condition;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.core.enums.ConditionOperator;
import chat.octet.accordion.core.enums.DataType;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.collect.Lists;

public class SimpleExample {

    public static void main(String[] args) {
        //Create the start action with script
        //NOTE: Script will execute a 1+1 addition calculation and outputs the calculation result.
        ActionConfig myAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase())
                .actionType(ActionType.SCRIPT.name())
                .actionName("My action")
                .actionDesc("My first action example")
                .actionParams(ScriptParameter.builder().script("1+1").build())
                .actionOutput(Lists.newArrayList(new OutputParameter("number", DataType.LONG, "Calc result")))
                .build();
        //Create the next action with condition
        //NOTE: Expression will determine whether the value is equal to 2.
        Condition condition = new Condition("number", ConditionOperator.EQ, 2);
        ActionConfig conditionAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase())
                .actionType(ActionType.CONDITION.name())
                .actionName("Condition action")
                .actionDesc("Condition action example")
                .actionParams(ConditionParameter.builder().expression(ConditionBuilder.getInstance().build(condition)).build())
                .build();
        //Create an Accordion plan and set actions
        AccordionPlan plan = AccordionPlan.of().next(myAction, conditionAction);
        System.out.println(plan.exportToJsonConfig());
        //
        try (Accordion accordion = new Accordion(plan)) {
            accordion.play(true);
            System.out.println("Accordion plan:\n" + accordion.verbose());
        }
    }
}
