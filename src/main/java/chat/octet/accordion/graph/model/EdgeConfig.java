package chat.octet.accordion.graph.model;

import chat.octet.accordion.action.model.ActionConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EdgeConfig implements Serializable {

    private String previousAction;

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
