package pro.octet.accordion.graph.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pro.octet.accordion.action.model.ActionConfig;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccordionGraphConfig implements Serializable {

    @JsonProperty("actions")
    private List<ActionConfig> actions;

    @JsonProperty("edges")
    private List<EdgeConfig> edges;

    public AccordionGraphConfig(List<ActionConfig> actions, List<EdgeConfig> edges) {
        this.actions = actions;
        this.edges = edges;
    }

    public AccordionGraphConfig() {
    }

    public void addEdge(EdgeConfig edge) {
        this.edges.add(edge);
    }

    public void addAction(ActionConfig action) {
        this.actions.add(action);
    }
}
