package pro.octet.accordion;


import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.ActionFactory;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.GraphNodeStatus;
import pro.octet.accordion.flow.entity.GraphEdge;
import pro.octet.accordion.flow.entity.GraphNode;

import java.util.List;

@Getter
@Slf4j
public class AccordionPlan {
    private final List<GraphNode> graphNodes;
    private final List<GraphEdge> graphEdges;
    private GraphNode rootGraphNode;

    private static volatile AccordionPlan instance;

    private AccordionPlan() {
        this.graphNodes = Lists.newArrayList();
        this.graphEdges = Lists.newArrayList();
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
        return graphNodes.stream().filter(node -> node.getActionId().equals(actionId)).findFirst().orElse(null);
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
    }

    protected void updateGraphNodeStatus(GraphNode graphNode, GraphNodeStatus status) {
        graphNode.setStatus(status);
    }

}
