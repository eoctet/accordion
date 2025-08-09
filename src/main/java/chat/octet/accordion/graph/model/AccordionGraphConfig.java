package chat.octet.accordion.graph.model;


import chat.octet.accordion.action.model.ActionConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccordionGraphConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ActionConfig> actions;

    private List<EdgeConfig> edges;

    public AccordionGraphConfig(final List<ActionConfig> actions, final List<EdgeConfig> edges) {
        // Create defensive copies to prevent external mutation
        this.actions = actions != null ? Lists.newArrayList(actions) : Lists.newArrayList();
        this.edges = edges != null ? Lists.newArrayList(edges) : Lists.newArrayList();
    }

    public AccordionGraphConfig() {
    }

    /**
     * Adds an edge configuration to the graph.
     *
     * @param edge the edge configuration to add
     */
    public void addEdge(final EdgeConfig edge) {
        this.edges.add(edge);
    }

    /**
     * Adds an action configuration to the graph.
     *
     * @param action the action configuration to add
     */
    public void addAction(final ActionConfig action) {
        this.actions.add(action);
    }
}
