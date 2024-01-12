package chat.octet.accordion.action.llama;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.JsonUtils;
import chat.octet.model.Model;
import chat.octet.model.beans.CompletionResult;
import chat.octet.model.parameters.GenerateParameter;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.Optional;

/**
 * Implemented using llama-Java, supports automatic inference in chat mode and completion mode.
 * <p>
 * For more information, please refer to
 * <a href="https://github.com/eoctet/llama-java">Llama-java</a>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see LlamaParameter
 */
@Slf4j
public class LlamaAction extends AbstractAction {
    public static final String LLAMA_INPUT = "LLAMA_INPUT";
    public static final String LLAMA_OUTPUT = "LLAMA_OUTPUT";
    private final LlamaParameter params;
    private final GenerateParameter generateParams;
    private final Model model;

    public LlamaAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(LlamaParameter.class, "Llama parameter cannot be null.");
        Preconditions.checkArgument(this.params.getModelParameter() != null, "Llama model parameter cannot be null.");
        this.generateParams = Optional.ofNullable(this.params.getGenerateParameter()).orElse(GenerateParameter.builder().build());
        this.model = new Model(this.params.getModelParameter());
    }

    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            String text = getInputParameter().getString(LLAMA_INPUT);
            if (StringUtils.isEmpty(text)) {
                log.warn("Llama input text is empty, skipping inference.");
            } else {
                String prompt = null;
                if (StringUtils.isNotEmpty(params.getSystem())) {
                    //format prompt text and inject dynamic variables
                    prompt = StringSubstitutor.replace(params.getSystem(), getInputParameter());
                }
                CompletionResult result;
                if (params.isChatMode()) {
                    if (!params.isMemory()) {
                        //always clear status in chat mode
                        model.removeChatStatus(generateParams.getUser());
                    }
                    result = model.chatCompletions(generateParams, prompt, text);
                } else {
                    result = model.completions(generateParams, text);
                }
                executeResult.add(LLAMA_OUTPUT, result.getContent());
                log.debug("Llama action execution finished, result: " + JsonUtils.toJson(result));
            }
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }

    @Override
    public void close() {
        if (model != null) {
            model.close();
            log.debug("Close llama model and release all resources.");
        }
    }
}
