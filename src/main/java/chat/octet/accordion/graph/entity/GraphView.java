package chat.octet.accordion.graph.entity;

import lombok.Data;

@Data
public class GraphView {
    private GraphNode graphNode;
    private int level;
    private boolean end;

    public GraphView(GraphNode graphNode, int level, boolean end) {
        this.graphNode = graphNode;
        this.level = level;
        this.end = end;
    }
}
