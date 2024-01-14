package chat.octet.accordion.graph.model;


import chat.octet.accordion.action.model.ActionConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccordionGraphConfig implements Serializable {

    private List<ActionConfig> actions;

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
