package chat.octet.accordion.action.llama;

import chat.octet.model.parameters.GenerateParameter;
import chat.octet.model.parameters.ModelParameter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import javax.annotation.Nullable;

/**
 * Llama action parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlamaParameter {

    /**
     * Llama model parameters.
     *
     * @see ModelParameter
     */
    @JsonProperty("model_parameters")
    private ModelParameter modelParameter;

    /**
     * Llama generate parameters. Use default values when not specified.
     *
     * @see GenerateParameter
     */
    @Nullable
    @JsonProperty("generate_parameters")
    private GenerateParameter generateParameter;

    /**
     * Use chat mode (default: true), Otherwise it will be in completion mode.
     */
    @Builder.Default
    @JsonProperty("chat_mode")
    private boolean chatMode = true;

    /**
     * System prompt text.
     */
    @Nullable
    private String system;

    /**
     * Use chat memory (default: false), Only available in chat mode.
     */
    private boolean memory;
}
