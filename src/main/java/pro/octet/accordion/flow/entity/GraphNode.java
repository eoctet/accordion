package pro.octet.accordion.flow.entity;

import com.google.common.collect.Sets;
import lombok.Data;
import pro.octet.accordion.action.ActionService;
import pro.octet.accordion.core.enums.GraphNodeStatus;

import java.util.Objects;
import java.util.Set;


@Data
public class GraphNode {
    private String actionId;
    private String actionName;
    private Set<GraphEdge> edges;
    private ActionService actionService;
    private GraphNodeStatus status;

    public GraphNode(ActionService actionService) {
        this.actionId = actionService.getConfig().getId();
        this.actionName = actionService.getConfig().getActionName();
        this.actionService = actionService;
        this.edges = Sets.newConcurrentHashSet();
        this.status = GraphNodeStatus.NORMAL;
    }

    public void addEdge(GraphEdge edge) {
        edges.add(edge);
    }

    public boolean isSuccess() {
        return status == GraphNodeStatus.SUCCESS;
    }

    public void reset() {
        status = GraphNodeStatus.NORMAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphNode)) {
            return false;
        }
        GraphNode graphNode = (GraphNode) o;
        return actionId.equals(graphNode.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }
}
