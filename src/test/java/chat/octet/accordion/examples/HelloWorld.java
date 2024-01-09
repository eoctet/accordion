package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.parameters.ScriptParameter;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.utils.CommonUtils;

public class HelloWorld {

    public static void main(String[] args) {
        ActionConfig myAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase())
                .actionType(ActionType.SCRIPT.name())
                .actionName("My Action")
                .actionDesc("My first action example")
                .actionParams(ScriptParameter.builder().script("println('Hello world')").build())
                .build();

        AccordionPlan plan = AccordionPlan.of().start(myAction);
        Accordion accordion = new Accordion();
        accordion.play(plan);
        System.out.println("Accordion plan:\n" + accordion.verbose());
    }
}
