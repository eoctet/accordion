package chat.octet.accordion.examples;


import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.llama.LlamaAction;
import chat.octet.accordion.action.llama.LlamaParameter;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.model.parameters.GenerateParameter;
import chat.octet.model.parameters.ModelParameter;

public class LlamaActionExample {

    public static void main(String[] args) {
        Message message = new Message();
        message.put(LlamaAction.LLAMA_INPUT, "Who are you?");

        ActionConfig action = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT").toUpperCase())
                .actionType(ActionType.LLAMA.name())
                .actionName("llama action")
                .actionParams(
                        LlamaParameter.builder().modelParameter(
                                        ModelParameter.builder().modelPath("/llama-java/models/model-7b-q6_k.gguf").build()
                                ).generateParameter(
                                        GenerateParameter.builder().verbosePrompt(true).build()
                                ).system("You're Kitty Jamie, and your answers and actions are the same as those of a kitten.")
                                .build()
                )
                .build();

        AccordionPlan plan = AccordionPlan.of().start(action);
        //
        try (Accordion accordion = new Accordion(plan)) {
            ExecuteResult result = accordion.play(message, true);
            System.out.println("Accordion plan:\n" + accordion.verbose());
            System.out.println(result);
        }
    }
}
