package chat.octet.accordion.graph.entity;

import lombok.Data;

@Data
public class GraphView {
    private GraphNode graphNode;
    private int level;
    private boolean end;

    public GraphView(final GraphNode graphNode, final int level, final boolean end) {
        // Store reference - GraphNode is managed by the framework
        this.graphNode = graphNode;
        this.level = level;
        this.end = end;
    }
}
