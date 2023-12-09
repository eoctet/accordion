package pro.octet.accordion.flow.entity;


import lombok.Getter;
import pro.octet.accordion.core.enums.GraphNodeStatus;

import java.io.Serializable;
import java.util.List;


@Getter
public class FlowGraph implements Serializable {
    private final List<GraphNode> graphNodes;
    private final List<GraphEdge> graphEdges;
    private int completeCount;
    private final StringBuffer graphSimpleView;

    public FlowGraph(List<GraphNode> graphNodes, List<GraphEdge> graphEdges) {
        this.graphNodes = graphNodes;
        this.graphEdges = graphEdges;
        this.graphSimpleView = new StringBuffer();
    }

    public void updateGraphNodeStatus(String actionId, String actionName, GraphNodeStatus status) {
        graphNodes.forEach(n -> {
            if (n.getActionId().equals(actionId)) {
                n.setStatus(status);
            }
        });
        completeCount++;
        graphSimpleView.append("(").append(actionName).append(":").append(status.name()).append(")");
    }

    public boolean isSuccess(GraphNode graphNode) {
        for (GraphNode node : graphNodes) {
            if (node.getActionId().equals(graphNode.getActionId())) {
                return graphNode.isSuccess();
            }
        }
        return false;
    }

    public void updateGraphSimpleView() {
        graphSimpleView.append("=>");
    }

    public boolean isExecuteDone() {
        boolean flag = completeCount == graphNodes.size();
        if (flag) {
            graphSimpleView.append("(END)");
        }
        return flag;
    }

}
