package pro.octet.accordion;


import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.action.ActionFactory;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.GraphNodeStatus;
import pro.octet.accordion.flow.entity.GraphEdge;
import pro.octet.accordion.flow.entity.GraphNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class AccordionPlan {

    private final List<GraphNode> graphNodes;
    private final List<GraphEdge> graphEdges;
    private final StringBuffer graphSimpleView;
    private GraphNode rootGraphNode;
    private final List<GraphNode> graphNodesFilter;

    private static volatile AccordionPlan instance;

    private AccordionPlan() {
        this.graphNodes = Lists.newArrayList();
        this.graphEdges = Lists.newArrayList();
        this.graphNodesFilter = Lists.newArrayList();
        this.graphSimpleView = new StringBuffer();
    }

    public static AccordionPlan of() {
        if (instance == null) {
            synchronized (AccordionPlan.class) {
                if (instance == null) {
                    instance = new AccordionPlan();
                }
            }
        }
        return instance;
    }

    private GraphNode createGraphNode(ActionConfig action) {
        return new GraphNode(ActionFactory.getInstance().build(action.getActionType(), action));
    }

    private GraphNode findGraphNode(String actionId) {
        for (GraphNode node : graphNodes) {
            if (node.getActionId().equals(actionId)) {
                return node;
            }
        }
        return null;
    }


    public AccordionPlan start(ActionConfig actionConfig) {
        if (!graphNodes.isEmpty()) {
            throw new IllegalArgumentException("Not allowed to add because the list is not empty.");
        }
        rootGraphNode = createGraphNode(actionConfig);
        graphNodes.add(rootGraphNode);
        return this;
    }

    public AccordionPlan next(ActionConfig previousAction, ActionConfig nextAction) {
        return next(previousAction.getId(), nextAction);
    }

    private AccordionPlan next(String previousActionId, ActionConfig nextAction) {
        if (graphNodes.isEmpty()) {
            start(nextAction);
            return this;
        }
        GraphNode previousNode = findGraphNode(previousActionId);
        if (previousNode == null) {
            throw new IllegalArgumentException("Unable to find the previous action, please check your parameter.");
        }
        GraphNode nextNode = findGraphNode(nextAction.getId());
        if (nextNode == null) {
            nextNode = createGraphNode(nextAction);
            graphNodes.add(nextNode);
        }
        GraphEdge edge = new GraphEdge(previousNode, nextNode);
        previousNode.addEdge(edge);
        nextNode.addEdge(edge);
        graphEdges.add(edge);
        return this;
    }

    public AccordionPlan next(ActionConfig previousAction, ActionConfig... nextActions) {
        for (ActionConfig config : nextActions) {
            next(previousAction, config);
        }
        return this;
    }

    public void reset() {
        graphNodes.clear();
        graphEdges.clear();
        graphSimpleView.setLength(0);
        graphNodesFilter.clear();
    }

    protected void updateGraphNodeStatus(GraphNode graphNode, GraphNodeStatus status) {
        graphNode.setStatus(status);
    }

    protected GraphNode getRootGraphNode() {
        return rootGraphNode;
    }

    public String getPlanGraphView() {
        if (graphNodes.isEmpty()) {
            throw new IllegalArgumentException("Accordion plan is empty.");
        }
        graphViewUpdate(rootGraphNode, StringUtils.EMPTY);
        String view = graphSimpleView.toString();
        graphSimpleView.setLength(0);
        graphNodesFilter.clear();
        return view;
    }

    private void graphViewUpdate(GraphNode node, String depth) {
        if (graphNodesFilter.contains(node)) {
            return;
        }
        graphSimpleView.append(depth).append("⎣____ ").append(node.getStatus().getFlag()).append(StringUtils.SPACE).append(node.getActionName()).append("\n");
        List<GraphEdge> edges = node.getEdges().stream()
                .filter(edge -> Objects.equals(edge.getPreviousNode().getActionId(), node.getActionId()))
                .collect(Collectors.toList());
        for (GraphEdge edge : edges) {
            graphViewUpdate(edge.getNextNode(), depth + "\t");
        }
        graphNodesFilter.add(node);
    }

}
