package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.base.ConditionParameter;
import chat.octet.accordion.core.condition.Condition;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.core.enums.ConditionOperator;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.RandomUtils;

import java.util.Map;

public final class MessageExample {

    private static final int MAX_RANDOM_VALUE = 100;
    private static final int MAX_FLOAT_VALUE = 10;
    private static final int THRESHOLD_VALUE = 50;

    private MessageExample() {
        // Utility class - prevent instantiation
    }

    public static void main(final String[] args) {
        Map<String, Object> global = Maps.newLinkedHashMap();
        global.put("flag", true);

        Message message = new Message();
        message.put("num", RandomUtils.nextInt(1, MAX_RANDOM_VALUE));
        message.put("div", RandomUtils.nextFloat(0, MAX_FLOAT_VALUE));

        Condition condition = new Condition("num*div", ConditionOperator.GT, THRESHOLD_VALUE).and("flag", ConditionOperator.EQ, true);
        ActionConfig conditionAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase(java.util.Locale.ROOT))
                .actionType(ActionType.CONDITION.name())
                .actionName("Condition action")
                .actionDesc("Condition action example")
                .actionParams(ConditionParameter.builder().expression(ConditionBuilder.getInstance().build(condition)).debug(true).build())
                .build();

        //Create an Accordion plan and set actions
        AccordionPlan plan = AccordionPlan.of().start(conditionAction);
        //
        try (Accordion accordion = new Accordion(plan)) {
            accordion.play(global, message, true);
            System.out.println("Accordion plan:\n" + accordion.verbose());
        }
    }
}
