package chat.octet.accordion;


import chat.octet.accordion.action.ActionRegister;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.core.enums.GraphNodeStatus;
import chat.octet.accordion.exceptions.AccordionException;
import chat.octet.accordion.graph.entity.GraphEdge;
import chat.octet.accordion.graph.entity.GraphNode;
import chat.octet.accordion.graph.model.AccordionConfig;
import chat.octet.accordion.graph.model.AccordionGraphConfig;
import chat.octet.accordion.graph.model.EdgeConfig;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.accordion.utils.JsonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an execution plan used to specify the execution of one or more actions.
 * In addition to specifying the connection order of each action through the API,
 * you can quickly create an execution plan using the method of importing and exporting JSON.
 * <p><p>
 * The execution plan is a directed acyclic graph, therefore, a start action must be specified.
 * If you specify an execution plan that is end-to-end, it will result in the plan being unable to execute.
 * The execution of each action depends on the preceding action.
 * If the preceding action is not executed correctly, subsequent actions will terminate execution.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Slf4j
public class AccordionPlan {
    private final List<GraphNode> graphNodes;
    private final List<GraphEdge> graphEdges;
    private AccordionConfig accordionConfig;
    private GraphNode rootGraphNode;

    public AccordionPlan() {
        this.graphNodes = Lists.newArrayList();
        this.graphEdges = Lists.newArrayList();
    }

    /**
     * Create a new instance of the Accordion plan.
     *
     * @return AccordionPlan
     */
    public static AccordionPlan of() {
        return new AccordionPlan();
    }

    /**
     * Create a new GraphNode.
     *
     * @param action Action config
     * @return GraphNode
     */
    private GraphNode createGraphNode(ActionConfig action) {
        return new GraphNode(ActionRegister.getInstance().build(action));
    }

    /**
     * Find a GraphNode by actionId.
     *
     * @param actionId Action id
     * @return GraphNode
     */
    private GraphNode findGraphNode(String actionId) {
        return graphNodes.stream().filter(node -> node.getActionId().equals(actionId)).findFirst().orElse(null);
    }

    /**
     * Find the root GraphNode.
     *
     * @return GraphNode
     */
    private GraphNode findRootGraphNode() {
        Set<GraphNode> nextGraphNodes = graphEdges.stream().map(GraphEdge::getNextNode).collect(Collectors.toSet());
        return graphNodes.stream().filter(node -> !nextGraphNodes.contains(node)).findFirst()
                .orElseThrow(() -> new AccordionException("No starting action found."));
    }

    /**
     * Add a start action.
     *
     * @param actionConfig Action config
     * @return AccordionPlan
     */
    public AccordionPlan start(ActionConfig actionConfig) {
        if (!graphNodes.isEmpty()) {
            throw new IllegalArgumentException("Not allowed to add because the list is not empty.");
        }
        rootGraphNode = createGraphNode(actionConfig);
        graphNodes.add(rootGraphNode);
        return this;
    }

    /**
     * Add a next action.
     *
     * @param previousAction Previous action
     * @param nextAction     Next action
     * @return AccordionPlan
     */
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

    /**
     * Add one or more next actions.
     *
     * @param previousAction Previous action
     * @param nextActions    One or more next actions
     * @return AccordionPlan
     */
    public AccordionPlan next(ActionConfig previousAction, ActionConfig... nextActions) {
        for (ActionConfig config : nextActions) {
            next(previousAction, config);
        }
        return this;
    }

    /**
     * Export the plan to JSON.
     *
     * @return String
     */
    public String exportToJsonConfig() {
        if (accordionConfig == null) {
            AccordionGraphConfig graphConfig = new AccordionGraphConfig(Lists.newArrayList(), Lists.newArrayList());
            graphNodes.stream().map(graphNode -> graphNode.getActionService().getConfig()).forEach(graphConfig::addAction);
            graphEdges.stream().map(graphEdge -> new EdgeConfig(graphEdge.getPreviousNode().getActionId(), graphEdge.getNextNode().getActionId())).forEach(graphConfig::addEdge);
            findRootGraphNode();
            AccordionConfig config = new AccordionConfig(
                    CommonUtils.randomString("ACR").toUpperCase(),
                    "Default accordion name",
                    "Default accordion desc",
                    graphConfig, LocalDateTime.now()
            );
            return JsonUtils.toJson(config);
        }
        return JsonUtils.toJson(accordionConfig);
    }

    /**
     * Import the plan from JSON.
     *
     * @param accordionConfigJson Accordion config JSON.
     * @return AccordionPlan
     */
    public AccordionPlan importConfig(String accordionConfigJson) {
        AccordionConfig config = Objects.requireNonNull(JsonUtils.parseToObject(accordionConfigJson, AccordionConfig.class));
        return importConfig(config);
    }

    /**
     * Import the plan from AccordionConfig.
     *
     * @param accordionConfig Accordion config.
     * @return AccordionPlan
     */
    public AccordionPlan importConfig(AccordionConfig accordionConfig) {
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
        return this;
    }

    /**
     * Reset the plan status.
     */
    public void reset() {
        graphNodes.forEach(GraphNode::reset);
    }

    /**
     * Check if all previous nodes are finished.
     *
     * @param graphNode Dependent graph node.
     * @return boolean, Returns true if all previous nodes have been completed, otherwise false is returned.
     */
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

    /**
     * Update the status of the graph node.
     *
     * @param graphNode Graph node.
     * @param status    Graph node status.
     */
    protected void updateGraphNodeStatus(GraphNode graphNode, GraphNodeStatus status) {
        graphNode.setStatus(status);
    }

    /**
     * Get the root GraphNode.
     *
     * @return GraphNode
     */
    protected GraphNode getRootGraphNode() {
        if (rootGraphNode == null) {
            rootGraphNode = findRootGraphNode();
        }
        return rootGraphNode;
    }

    /**
     * Get the graph nodes.
     *
     * @return List, graph nodes.
     */
    protected List<GraphNode> getGraphNodes() {
        return graphNodes;
    }
}
