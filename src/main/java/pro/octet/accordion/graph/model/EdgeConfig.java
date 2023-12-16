package pro.octet.accordion.graph.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pro.octet.accordion.action.model.ActionConfig;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeConfig implements Serializable {

    @JsonProperty("switch_condition")
    private String switchCondition;

    @JsonProperty("switch_logic")
    private String switchLogic;

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

    public EdgeConfig(String switchCondition, String switchLogic, String previousAction, String nextAction) {
        this.switchCondition = switchCondition;
        this.switchLogic = switchLogic;
        this.previousAction = previousAction;
        this.nextAction = nextAction;
    }

    public EdgeConfig(String switchCondition, String switchLogic, ActionConfig previousAction, ActionConfig nextAction) {
        this(switchCondition, switchLogic, previousAction.getId(), nextAction.getId());
    }

    public EdgeConfig() {
    }

}
