package chat.octet.accordion.graph.entity;

import chat.octet.accordion.action.ActionService;
import chat.octet.accordion.core.enums.GraphNodeStatus;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Objects;
import java.util.Set;


@Data
public class GraphNode {
    private String actionId;
    private String actionName;
    private Set<GraphEdge> edges;
    private ActionService actionService;
    private GraphNodeStatus status;

    public GraphNode() {
    }

    public GraphNode(final ActionService actionService) {
        this.actionId = actionService.getConfig().getId();
        this.actionName = actionService.getConfig().getActionName();
        this.actionService = actionService;
        this.edges = Sets.newHashSet();
        this.status = GraphNodeStatus.NORMAL;
    }

    /**
     * Adds an edge to this graph node.
     * The edge must have this node as its previous node.
     *
     * @param edge the edge to add to this node
     * @throws IllegalArgumentException if edge is null or has incorrect previous node
     */
    public void addEdge(final GraphEdge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null");
        }
        if (edge.getPreviousNode() != this) {
            throw new IllegalArgumentException("Edge previous node must be this node");
        }
        edges.add(edge);
    }

    /**
     * Resets the node status to NORMAL.
     * This method is typically called before starting a new execution.
     */
    public void reset() {
        status = GraphNodeStatus.NORMAL;
    }

    /**
     * Checks if this node has finished execution.
     * A node is considered finished if its status is not NORMAL.
     *
     * @return true if the node has finished execution, false otherwise
     */
    public boolean isFinished() {
        return status != GraphNodeStatus.NORMAL;
    }

    /**
     * Compares this GraphNode with another object for equality.
     * Two GraphNode objects are considered equal if they have the same actionId.
     *
     * @param o the object to compare with this GraphNode
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphNode graphNode)) {
            return false;
        }
        return actionId.equals(graphNode.actionId);
    }

    /**
     * Returns the hash code value for this GraphNode.
     * The hash code is based on the actionId field.
     *
     * @return the hash code value for this GraphNode
     */
    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }

    /**
     * Returns a string representation of this GraphNode.
     * The string includes actionId, actionName, edge count, and status.
     *
     * @return a string representation of this GraphNode
     */
    @Override
    public String toString() {
        return "GraphNode{"
                + "actionId='" + actionId + '\''
                + ", actionName='" + actionName + '\''
                + ", edgeCount=" + (edges != null ? edges.size() : 0)
                + ", status=" + status
                + '}';
    }
}
