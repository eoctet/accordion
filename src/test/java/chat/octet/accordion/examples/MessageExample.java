package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.parameters.ConditionParameter;
import chat.octet.accordion.core.condition.Condition;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.core.enums.ConditionOperator;
import chat.octet.accordion.utils.CommonUtils;
import org.apache.commons.lang3.RandomUtils;

public class MessageExample {
    public static void main(String[] args) {
        Message message = new Message();
        message.put("num", RandomUtils.nextInt(1, 100));
        message.put("div", RandomUtils.nextFloat(0, 10));

        Condition condition = new Condition("num*div", ConditionOperator.GT, 50);
        ActionConfig conditionAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase())
                .actionType(ActionType.CONDITION.name())
                .actionName("Condition action")
                .actionDesc("Condition action example")
                .actionParams(ConditionParameter.builder().expression(ConditionBuilder.getInstance().build(condition)).debug(true).build())
                .build();

        //Create an Accordion plan and set actions
        AccordionPlan plan = AccordionPlan.of().start(conditionAction);
        //
        Accordion accordion = new Accordion(plan);
        accordion.play(message, true);
        System.out.println("Accordion plan:\n" + accordion.verbose());
    }
}
