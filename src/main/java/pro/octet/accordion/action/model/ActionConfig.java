package pro.octet.accordion.action.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Data;
import pro.octet.accordion.core.enums.ActionType;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionConfig {

    @JsonProperty("id")
    private String id;

    @JsonProperty("action_type")
    private ActionType actionType;

    @JsonProperty("action_name")
    private String actionName;

    @JsonProperty("action_desc")
    private String actionDesc;

    @JsonProperty("action_params")
    private ActionParams actionParams;

    @JsonProperty("action_output")
    private List<OutputParameter> actionOutput = Lists.newArrayList();

    public ActionConfig(String id, ActionType actionType, String actionName, String actionDesc) {
        this.id = id;
        this.actionType = actionType;
        this.actionName = actionName;
        this.actionDesc = actionDesc;
    }

    public ActionConfig() {
    }

}
