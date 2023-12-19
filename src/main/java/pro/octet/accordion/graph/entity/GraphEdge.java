package pro.octet.accordion.graph.entity;

import lombok.Data;
import pro.octet.accordion.utils.CommonUtils;

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

    public GraphEdge(GraphNode previousNode, GraphNode nextNode) {
        this.id = CommonUtils.randomString("edge");
        this.previousNode = previousNode;
        this.nextNode = nextNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphEdge)) {
            return false;
        }
        GraphEdge edge = (GraphEdge) o;
        return Objects.equals(id, edge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GraphEdge{" +
                "id='" + id + '\'' +
                ", previousNode=" + Optional.ofNullable(previousNode).orElse(new GraphNode()).getActionId() +
                ", nextNode=" + Optional.ofNullable(nextNode).orElse(new GraphNode()).getActionId() +
                '}';
    }
}
