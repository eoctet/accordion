package chat.octet.accordion.graph.entity;

import chat.octet.accordion.utils.CommonUtils;
import lombok.Data;

import java.util.Objects;
import java.util.Optional;

@Data
public class GraphEdge {
    private String id;
    private GraphNode previousNode;
    private GraphNode nextNode;

    public GraphEdge() {
        this.id = CommonUtils.randomString("edge");
    }

    public GraphEdge(final GraphNode previousNode, final GraphNode nextNode) {
        this.id = CommonUtils.randomString("edge");
        // Store references - GraphNodes are managed by the framework
        // These are not mutable objects that need defensive copying
        this.previousNode = previousNode;
        this.nextNode = nextNode;
    }

    /**
     * Compares this GraphEdge with another object for equality.
     * Two GraphEdge objects are considered equal if they have the same id.
     *
     * @param o the object to compare with this GraphEdge
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphEdge edge)) {
            return false;
        }
        return Objects.equals(id, edge.id);
    }

    /**
     * Returns the hash code value for this GraphEdge.
     * The hash code is based on the id field.
     *
     * @return the hash code value for this GraphEdge
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this GraphEdge.
     * The string includes the id and the action IDs of the connected nodes.
     *
     * @return a string representation of this GraphEdge
     */
    @Override
    public String toString() {
        return "GraphEdge{"
                + "id='" + id + '\''
                + ", previousNode=" + Optional.ofNullable(previousNode).orElse(new GraphNode()).getActionId()
                + ", nextNode=" + Optional.ofNullable(nextNode).orElse(new GraphNode()).getActionId()
                + '}';
    }
}
