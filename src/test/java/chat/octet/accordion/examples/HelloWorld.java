package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.script.ScriptParameter;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.utils.CommonUtils;

public final class HelloWorld {

    private HelloWorld() {
        // Utility class - prevent instantiation
    }

    public static void main(final String[] args) {
        ActionConfig myAction = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase(java.util.Locale.ROOT))
                .actionType(ActionType.SCRIPT.name())
                .actionName("My action")
                .actionDesc("My first action example")
                .actionParams(ScriptParameter.builder().script("println('Hello world')").build())
                .build();

        AccordionPlan plan = AccordionPlan.of().start(myAction);
        try (Accordion accordion = new Accordion(plan)) {
            accordion.play(true);
            System.out.println("Accordion plan:\n" + accordion.verbose());
        }
    }
}
