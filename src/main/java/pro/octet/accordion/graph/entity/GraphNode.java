package pro.octet.accordion.graph.entity;

import com.google.common.collect.Lists;
import lombok.Data;
import pro.octet.accordion.action.ActionService;
import pro.octet.accordion.core.enums.GraphNodeStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@Data
public class GraphNode {
    private String actionId;
    private String actionName;
    private List<GraphEdge> leftEdges;
    private List<GraphEdge> rightEdges;
    private ActionService actionService;
    private GraphNodeStatus status;

    public GraphNode() {
    }

    public GraphNode(ActionService actionService) {
        this.actionId = actionService.getConfig().getId();
        this.actionName = actionService.getConfig().getActionName();
        this.actionService = actionService;
        this.leftEdges = Lists.newArrayList();
        this.rightEdges = Lists.newArrayList();
        this.status = GraphNodeStatus.NORMAL;
    }

    public void addEdge(GraphEdge edge) {
        if (edge.getPreviousNode().getActionId().equals(actionId)) {
            rightEdges.add(edge);
        } else {
            leftEdges.add(edge);
        }
    }

    public boolean preNodesAllExecuted() {
        return checkStatus(false, GraphNodeStatus.NORMAL);
    }

    public boolean preNodesHasErrorOrSkipped() {
        return checkStatus(true, GraphNodeStatus.ERROR) || checkStatus(true, GraphNodeStatus.SKIP);
    }

    private boolean checkStatus(boolean anyMatch, GraphNodeStatus status) {
        Stream<GraphNode> stream = leftEdges.stream().map(GraphEdge::getPreviousNode);
        return anyMatch ?
                stream.anyMatch(previousNode -> previousNode.getStatus() == status) :
                stream.noneMatch(previousNode -> previousNode.getStatus() == status);
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

    @Override
    public String toString() {
        return "GraphNode{" +
                "actionId='" + actionId + '\'' +
                ", actionName='" + actionName + '\'' +
                ", leftEdges=" + leftEdges +
                ", rightEdges=" + rightEdges +
                ", actionService=" + actionService +
                ", status=" + status +
                '}';
    }
}
