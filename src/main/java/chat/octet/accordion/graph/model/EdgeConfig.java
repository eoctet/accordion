package chat.octet.accordion.graph.model;

import chat.octet.accordion.action.model.ActionConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeConfig implements Serializable {

    @JsonProperty("previous_action")
    private String previousAction;

    @JsonProperty("next_action")
    private String nextAction;

    public EdgeConfig(String previousActionId, String nextActionId) {
        this.previousAction = previousActionId;
        this.nextAction = nextActionId;
    }

    public EdgeConfig(ActionConfig previousAction, ActionConfig nextAction) {
        this(previousAction.getId(), nextAction.getId());
    }

    public EdgeConfig() {
    }

}
