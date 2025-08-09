package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.shell.ShellParameter;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.utils.CommonUtils;

public final class ShellActionExample {

    private ShellActionExample() {
        // Utility class - prevent instantiation
    }

    public static void main(final String[] args) {
        Message message = new Message();
        message.put("text", "Hello world");

        ActionConfig action = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase(java.util.Locale.ROOT))
                .actionType(ActionType.SHELL.name())
                .actionName("Shell action")
                .actionParams(
                        ShellParameter.builder().shell("echo ${text}").build()
                )
                .build();

        AccordionPlan plan = AccordionPlan.of().start(action);
        //
        try (Accordion accordion = new Accordion(plan)) {
            accordion.play(message, true);
            System.out.println("Accordion plan:\n" + accordion.verbose());
        }
    }
}
