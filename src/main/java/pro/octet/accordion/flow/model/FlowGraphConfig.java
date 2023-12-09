package pro.octet.accordion.flow.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pro.octet.accordion.action.model.ActionConfig;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowGraphConfig implements Serializable {

    @JsonProperty("actions")
    private List<ActionConfig> actions;

    @JsonProperty("edges")
    private List<EdgeConfig> edges;

    public FlowGraphConfig(List<ActionConfig> actions, List<EdgeConfig> edges) {
        this.actions = actions;
        this.edges = edges;
    }

    public FlowGraphConfig() {
    }
}
