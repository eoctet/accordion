package pro.octet.accordion;


import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.ActionRegister;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.GraphNodeStatus;
import pro.octet.accordion.exceptions.AccordionException;
import pro.octet.accordion.graph.entity.GraphEdge;
import pro.octet.accordion.graph.entity.GraphNode;
import pro.octet.accordion.graph.model.AccordionConfig;
import pro.octet.accordion.graph.model.AccordionGraphConfig;
import pro.octet.accordion.graph.model.EdgeConfig;
import pro.octet.accordion.utils.CommonUtils;
import pro.octet.accordion.utils.JsonUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public class AccordionPlan {
    private final List<GraphNode> graphNodes;
    private final List<GraphEdge> graphEdges;
    private AccordionConfig accordionConfig;
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
        return new GraphNode(ActionRegister.getInstance().build(action));
    }

    private GraphNode findGraphNode(String actionId) {
        return graphNodes.stream().filter(node -> node.getActionId().equals(actionId)).findFirst().orElse(null);
    }

    private GraphNode findRootGraphNode() {
        Set<GraphNode> nextGraphNodes = graphEdges.stream().map(GraphEdge::getNextNode).collect(Collectors.toSet());
        return graphNodes.stream().filter(node -> !nextGraphNodes.contains(node)).findFirst()
                .orElseThrow(() -> new AccordionException("No starting action found."));
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
        GraphNode previousNode = findGraphNode(previousAction.getId());
        if (previousNode == null) {
            if (!graphNodes.isEmpty()) {
                throw new IllegalArgumentException("Unable to find the previous action, please check your parameter.");
            }
            previousNode = createGraphNode(previousAction);
            graphNodes.add(previousNode);
        }
        GraphNode nextNode = findGraphNode(nextAction.getId());
        if (nextNode == null) {
            nextNode = createGraphNode(nextAction);
            graphNodes.add(nextNode);
        }
        GraphEdge edge = new GraphEdge(previousNode, nextNode);
        graphEdges.add(edge);
        previousNode.addEdge(edge);
        return this;
    }

    public AccordionPlan next(ActionConfig previousAction, ActionConfig... nextActions) {
        for (ActionConfig config : nextActions) {
            next(previousAction, config);
        }
        return this;
    }

    public void reset() {
        graphNodes.forEach(GraphNode::reset);
    }

    protected boolean prevGraphNodesFinished(GraphNode graphNode) {
        if (graphNode.equals(rootGraphNode)) {
            return true;
        }
        for (GraphEdge edge : graphEdges) {
            GraphNode previousNode = edge.getPreviousNode();
            GraphNode nextNode = edge.getNextNode();
            if (nextNode.equals(graphNode) && previousNode.getStatus() != GraphNodeStatus.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    protected void updateGraphNodeStatus(GraphNode graphNode, GraphNodeStatus status) {
        graphNode.setStatus(status);
    }

    protected GraphNode getRootGraphNode() {
        if (rootGraphNode == null) {
            rootGraphNode = findRootGraphNode();
        }
        return rootGraphNode;
    }

    public String toAccordionConfigJson() {
        if (accordionConfig == null) {
            AccordionGraphConfig graphConfig = new AccordionGraphConfig(Lists.newArrayList(), Lists.newArrayList());
            graphNodes.stream().map(graphNode -> graphNode.getActionService().getConfig()).forEach(graphConfig::addAction);
            graphEdges.stream().map(graphEdge -> new EdgeConfig(graphEdge.getPreviousNode().getActionId(), graphEdge.getNextNode().getActionId())).forEach(graphConfig::addEdge);
            findRootGraphNode();
            AccordionConfig config = new AccordionConfig(
                    CommonUtils.randomString("ACR").toUpperCase(),
                    "Default accordion name",
                    "Default accordion desc",
                    graphConfig, new Date()
            );
            return JsonUtils.toJson(config);
        }
        return JsonUtils.toJson(accordionConfig);
    }

    public void fromAccordionConfig(String accordionConfigJson) {
        AccordionConfig config = Objects.requireNonNull(JsonUtils.parseToObject(accordionConfigJson, AccordionConfig.class));
        fromAccordionConfig(config);
    }

    public void fromAccordionConfig(AccordionConfig accordionConfig) {
        this.accordionConfig = accordionConfig;
        this.graphNodes.clear();
        this.graphEdges.clear();
        this.rootGraphNode = null;

        AccordionGraphConfig graphConfig = accordionConfig.getGraphConfig();
        List<ActionConfig> actionConfigs = graphConfig.getActions();

        String message = "Unable to find the action config, please check your parameter.";
        for (EdgeConfig edgeConfig : graphConfig.getEdges()) {
            ActionConfig previousAction = actionConfigs.stream().filter(action -> action.getId().equals(edgeConfig.getPreviousAction()))
                    .findFirst().orElseThrow(() -> new AccordionException(message));
            ActionConfig nextAction = actionConfigs.stream().filter(action -> action.getId().equals(edgeConfig.getNextAction()))
                    .findFirst().orElseThrow(() -> new AccordionException(message));
            next(previousAction, nextAction);
        }
        this.rootGraphNode = findRootGraphNode();
    }
}
