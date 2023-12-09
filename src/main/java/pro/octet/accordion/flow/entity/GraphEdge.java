package pro.octet.accordion.flow.entity;

import lombok.Data;
import pro.octet.accordion.utils.CommonUtils;

import java.util.Objects;

@Data
public class GraphEdge {
    private String id;
    private String name;
    private boolean switcher;
    private GraphNode previousAction;
    private GraphNode nextAction;

    public GraphEdge() {
        this.id = CommonUtils.randomString("edge");
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
                ", name='" + name + '\'' +
                ", switcher=" + switcher +
                ", previousAction=" + previousAction +
                ", nextAction=" + nextAction +
                '}';
    }
}
