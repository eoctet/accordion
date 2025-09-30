package chat.octet.accordion;


import chat.octet.accordion.action.ActionRegister;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.core.enums.GraphNodeStatus;
import chat.octet.accordion.exceptions.AccordionException;
import chat.octet.accordion.exceptions.ActionException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Execution plan builder for defining and managing automation workflows.
 *
 * <p>AccordionPlan is a fluent API builder that allows you to construct directed acyclic graphs (DAG)
 * of actions for automation workflows. It provides both programmatic construction through method chaining
 * and configuration-based construction through JSON import/export.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Fluent API</strong>: Chain method calls to build complex workflows</li>
 *   <li><strong>DAG Structure</strong>: Ensures no circular dependencies in execution flow</li>
 *   <li><strong>JSON Support</strong>: Import/export plans as JSON for persistence and sharing</li>
 *   <li><strong>Validation</strong>: Automatic validation of plan structure and dependencies</li>
 *   <li><strong>Cycle Detection</strong>: Prevents creation of circular dependencies</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Simple Sequential Plan:</strong></p>
 * <pre>{@code
 * AccordionPlan plan = AccordionPlan.of()
 *     .start(fetchDataAction)
 *     .next(fetchDataAction, processDataAction)
 *     .next(processDataAction, sendEmailAction);
 * }</pre>
 *
 * <p><strong>Parallel Execution Plan:</strong></p>
 * <pre>{@code
 * AccordionPlan plan = AccordionPlan.of()
 *     .start(triggerAction)
 *     .next(triggerAction, apiAction1, apiAction2)  // Parallel execution
 *     .next(apiAction1, mergeAction)
 *     .next(apiAction2, mergeAction);
 * }</pre>
 *
 * <p><strong>JSON Configuration:</strong></p>
 * <pre>{@code
 * String jsonConfig = plan.exportToJsonConfig();
 * AccordionPlan importedPlan = AccordionPlan.of().importConfig(jsonConfig);
 * }</pre>
 *
 * <p><strong>Constraints and Rules:</strong></p>
 * <ul>
 *   <li>Plans must form a directed acyclic graph (no cycles allowed)</li>
 *   <li>Each action must have a unique ID within the plan</li>
 *   <li>Plans with multiple actions must have exactly one root action</li>
 *   <li>Actions depend on successful completion of their predecessors</li>
 * </ul>
 *
 * <p>Thread Safety: This class is not thread-safe. Plan construction should be done
 * in a single thread or with external synchronization.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ActionConfig
 * @see GraphNode
 * @see GraphEdge
 * @see Accordion
 * @since 1.0.0
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
     * Creates a new empty AccordionPlan instance.
     *
     * <p>This is the entry point for building execution plans using the fluent API.
     * The returned plan is empty and ready for action configuration.</p>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * AccordionPlan plan = AccordionPlan.of()
     *     .start(firstAction)
     *     .next(firstAction, secondAction);
     * }</pre>
     *
     * @return a new empty AccordionPlan instance ready for configuration
     * @since 1.0.0
     */
    public static AccordionPlan of() {
        return new AccordionPlan();
    }

    /**
     * Creates a new GraphNode from an ActionConfig.
     *
     * <p>This method wraps an ActionConfig in a GraphNode, which is the internal
     * representation used by the execution engine. The GraphNode contains the
     * action service and manages execution state.</p>
     *
     * @param action the action configuration to wrap in a graph node
     * @return a new GraphNode containing the specified action
     * @throws ActionException if the action cannot be built from the configuration
     */
    private GraphNode createGraphNode(ActionConfig action) {
        return new GraphNode(ActionRegister.getInstance().build(action));
    }

    /**
     * Finds a GraphNode by its action ID.
     *
     * <p>Searches through all registered graph nodes to find one with the
     * specified action ID. This is used internally for plan construction
     * and validation.</p>
     *
     * @param actionId the unique identifier of the action to find
     * @return the GraphNode with the specified ID, or null if not found
     */
    private GraphNode findGraphNode(final String actionId) {
        return graphNodes.stream().filter(node -> node.getActionId().equals(actionId)).findFirst().orElse(null);
    }

    /**
     * Finds and returns the root GraphNode of the execution plan.
     *
     * <p>The root node is the starting point of execution and is determined by:</p>
     * <ul>
     *   <li>For single-action plans: the only action is the root</li>
     *   <li>For multi-action plans: the action that is not a target of any edge</li>
     * </ul>
     *
     * <p>This method validates the plan structure and ensures there is exactly
     * one root node, preventing ambiguous execution flows.</p>
     *
     * @return the root GraphNode where execution should begin
     * @throws AccordionException if no actions exist, multiple roots found, or circular dependencies detected
     */
    private GraphNode findRootGraphNode() {
        if (graphNodes.isEmpty()) {
            throw new AccordionException("No actions found in the plan.");
        }

        // If there are no edges and only one node, that node is the root
        if (graphEdges.isEmpty() && graphNodes.size() == 1) {
            return graphNodes.get(0);
        }

        // If there are no edges but multiple nodes, throw an exception
        if (graphEdges.isEmpty()) {
            throw new AccordionException("Multiple actions found without edges. Please specify the starting action using start() method.");
        }

        // Standard case: find node that is not a target of any edge
        Set<GraphNode> targetNodes = graphEdges.stream()
                .map(GraphEdge::getNextNode)
                .collect(Collectors.toSet());

        List<GraphNode> rootCandidates = graphNodes.stream()
                .filter(node -> !targetNodes.contains(node))
                .toList();

        if (rootCandidates.isEmpty()) {
            throw new AccordionException("Circular dependency detected. No starting action found.");
        }

        if (rootCandidates.size() > 1) {
            throw new AccordionException("Multiple root actions found: " +
                    rootCandidates.stream().map(GraphNode::getActionId).collect(Collectors.joining(", ")) +
                    ". Please ensure there is only one starting action.");
        }

        return rootCandidates.get(0);
    }

    /**
     * Adds the starting action to the execution plan.
     *
     * <p>This method explicitly defines the first action in the execution sequence.
     * It can only be called on an empty plan and sets the root node for execution.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * AccordionPlan plan = AccordionPlan.of()
     *     .start(ActionConfig.builder()
     *         .id("init")
     *         .actionType("API")
     *         .actionName("Initialize System")
     *         .build());
     * }</pre>
     *
     * @param actionConfig the configuration for the starting action
     * @return this AccordionPlan instance for method chaining
     * @throws IllegalArgumentException if the plan already contains actions
     * @throws ActionException          if the action configuration is invalid
     * @since 1.0.0
     */
    public AccordionPlan start(final ActionConfig actionConfig) {
        if (!graphNodes.isEmpty()) {
            throw new IllegalArgumentException("Not allowed to add because the list is not empty.");
        }
        rootGraphNode = createGraphNode(actionConfig);
        graphNodes.add(rootGraphNode);
        return this;
    }

    /**
     * Adds a dependency relationship between two actions.
     *
     * <p>Creates an execution dependency where the next action will only execute
     * after the previous action completes successfully. This method automatically
     * handles action registration and cycle detection.</p>
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>If previous action doesn't exist, it's added to the plan</li>
     *   <li>If next action doesn't exist, it's added to the plan</li>
     *   <li>Creates a directed edge from previous to next action</li>
     *   <li>Validates that no cycles are created</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * plan.next(fetchDataAction, processDataAction)
     *     .next(processDataAction, sendEmailAction);
     * }</pre>
     *
     * @param previousAction the action that must complete before nextAction executes
     * @param nextAction     the action that depends on previousAction
     * @return this AccordionPlan instance for method chaining
     * @throws IllegalArgumentException if either action is null or if actions have the same ID
     * @throws AccordionException       if adding the dependency would create a cycle
     * @since 1.0.0
     */
    public AccordionPlan next(final ActionConfig previousAction, final ActionConfig nextAction) {
        if (previousAction == null || nextAction == null) {
            throw new IllegalArgumentException("Previous action and next action cannot be null");
        }

        if (previousAction.getId().equals(nextAction.getId())) {
            throw new IllegalArgumentException("Self-referencing action detected: " + previousAction.getId());
        }

        GraphNode previousNode = findGraphNode(previousAction.getId());
        if (previousNode == null) {
            if (!graphNodes.isEmpty()) {
                throw new IllegalArgumentException("Unable to find the previous action: "
                        + previousAction.getId()
                        + ". Please ensure the action is added to the plan first.");
            }
            previousNode = createGraphNode(previousAction);
            graphNodes.add(previousNode);
        }

        GraphNode nextNode = findGraphNode(nextAction.getId());
        if (nextNode == null) {
            nextNode = createGraphNode(nextAction);
            graphNodes.add(nextNode);
        }

        // Check for direct cycle
        if (wouldCreateCycle(previousNode, nextNode)) {
            throw new AccordionException("Adding edge from " + previousAction.getId()
                    + " to " + nextAction.getId() + " would create a cycle");
        }

        GraphEdge edge = new GraphEdge(previousNode, nextNode);
        graphEdges.add(edge);
        previousNode.addEdge(edge);
        return this;
    }

    /**
     * Checks if adding an edge would create a cycle using depth-first search.
     *
     * <p>This method prevents the creation of circular dependencies by checking
     * if the target node can reach the source node through existing edges.
     * If such a path exists, adding the new edge would create a cycle.</p>
     *
     * @param from the source node of the proposed edge
     * @param to   the target node of the proposed edge
     * @return true if adding the edge would create a cycle, false otherwise
     */
    private boolean wouldCreateCycle(final GraphNode from, final GraphNode to) {
        Set<GraphNode> visited = new HashSet<>();
        return hasCycleDFS(to, from, visited);
    }

    /**
     * Performs depth-first search to detect cycles in the graph.
     *
     * <p>Recursively traverses the graph from the current node to determine
     * if the target node can be reached, which would indicate a cycle if
     * an edge were added from target to current.</p>
     *
     * @param current the current node being visited
     * @param target  the target node we're trying to reach
     * @param visited set of already visited nodes to prevent infinite loops
     * @return true if target can be reached from current, false otherwise
     */
    private boolean hasCycleDFS(final GraphNode current, final GraphNode target, final Set<GraphNode> visited) {
        if (current.equals(target)) {
            return true;
        }

        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);

        for (GraphEdge edge : current.getEdges()) {
            if (hasCycleDFS(edge.getNextNode(), target, visited)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds multiple dependency relationships from one action to several next actions.
     *
     * <p>This is a convenience method for creating parallel execution paths where
     * multiple actions depend on the same previous action. All next actions will
     * execute in parallel once the previous action completes successfully.</p>
     *
     * <p>Example for parallel processing:</p>
     * <pre>{@code
     * plan.next(dataFetchAction,
     *          processAction1,
     *          processAction2,
     *          processAction3);
     * }</pre>
     *
     * @param previousAction the action that must complete before any next actions execute
     * @param nextActions    variable number of actions that depend on the previous action
     * @return this AccordionPlan instance for method chaining
     * @throws IllegalArgumentException if previousAction is null or nextActions is empty
     * @throws AccordionException       if any dependency would create a cycle
     * @since 1.0.0
     */
    public AccordionPlan next(final ActionConfig previousAction, final ActionConfig... nextActions) {
        for (ActionConfig config : nextActions) {
            next(previousAction, config);
        }
        return this;
    }

    /**
     * Exports the execution plan to JSON format for persistence or sharing.
     *
     * <p>The exported JSON contains complete plan configuration including:</p>
     * <ul>
     *   <li>Plan metadata (ID, name, description, creation time)</li>
     *   <li>All action configurations with parameters</li>
     *   <li>Dependency relationships between actions</li>
     * </ul>
     *
     * <p>The JSON format is compatible with {@link #importConfig(String)} and
     * can be used to recreate the exact same execution plan.</p>
     *
     * <p>Example output structure:</p>
     * <pre>{@code
     * {
     *   "id": "ACR_12345",
     *   "name": "Data Processing Pipeline",
     *   "description": "Automated data processing workflow",
     *   "graphConfig": {
     *     "actions": [...],
     *     "edges": [...]
     *   },
     *   "createTime": "2024-01-01T10:00:00"
     * }
     * }</pre>
     *
     * @return JSON string representation of the execution plan
     * @throws AccordionException if plan structure is invalid or JSON serialization fails
     * @since 1.0.0
     */
    public String exportToJsonConfig() {
        if (accordionConfig == null) {
            AccordionGraphConfig graphConfig = new AccordionGraphConfig(
                    Lists.newArrayList(), Lists.newArrayList());
            graphNodes.stream()
                    .map(graphNode -> graphNode.getActionService().getConfig())
                    .forEach(graphConfig::addAction);
            graphEdges.stream()
                    .map(graphEdge -> new EdgeConfig(
                            graphEdge.getPreviousNode().getActionId(),
                            graphEdge.getNextNode().getActionId()))
                    .forEach(graphConfig::addEdge);
            findRootGraphNode();
            AccordionConfig config = new AccordionConfig(
                    CommonUtils.randomString("ACR").toUpperCase(java.util.Locale.ROOT),
                    "Default accordion name",
                    "Default accordion desc",
                    graphConfig, LocalDateTime.now()
            );
            return JsonUtils.toJson(config);
        }
        return JsonUtils.toJson(accordionConfig);
    }

    /**
     * Imports an execution plan from JSON configuration.
     *
     * <p>This method parses a JSON string (typically created by {@link #exportToJsonConfig()})
     * and reconstructs the complete execution plan including all actions and dependencies.</p>
     *
     * <p>The import process:</p>
     * <ol>
     *   <li>Parses JSON to AccordionConfig object</li>
     *   <li>Clears current plan state</li>
     *   <li>Recreates all actions and dependencies</li>
     *   <li>Validates plan structure</li>
     * </ol>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * String savedPlan = "{ ... }"; // JSON from exportToJsonConfig()
     * AccordionPlan plan = AccordionPlan.of().importConfig(savedPlan);
     * }</pre>
     *
     * @param accordionConfigJson JSON string containing the plan configuration
     * @return this AccordionPlan instance for method chaining
     * @throws AccordionException   if JSON is invalid or plan structure is malformed
     * @throws NullPointerException if accordionConfigJson is null
     * @since 1.0.0
     */
    public AccordionPlan importConfig(final String accordionConfigJson) {
        AccordionConfig config = Objects.requireNonNull(
                JsonUtils.parseToObject(accordionConfigJson, AccordionConfig.class));
        return importConfig(config);
    }

    /**
     * Imports an execution plan from an AccordionConfig object.
     *
     * <p>This method reconstructs the execution plan from a structured configuration
     * object. It handles both single-action plans (no edges) and complex multi-action
     * plans with dependencies.</p>
     *
     * <p>Special cases handled:</p>
     * <ul>
     *   <li>Single action with no edges: treated as standalone execution</li>
     *   <li>Multiple actions with no edges: throws exception (ambiguous start)</li>
     *   <li>Complex dependency graphs: validates and constructs full DAG</li>
     * </ul>
     *
     * @param accordionConfig the configuration object containing plan definition
     * @return this AccordionPlan instance for method chaining
     * @throws AccordionException if configuration is invalid or actions cannot be found
     * @since 1.0.0
     */
    public AccordionPlan importConfig(final AccordionConfig accordionConfig) {
        // Create defensive copy to avoid external mutation
        this.accordionConfig = new AccordionConfig(
                accordionConfig.getId(),
                accordionConfig.getName(),
                accordionConfig.getDesc(),
                accordionConfig.getGraphConfig(),
                accordionConfig.getUpdatetime()
        );
        this.graphNodes.clear();
        this.graphEdges.clear();
        this.rootGraphNode = null;

        AccordionGraphConfig graphConfig = accordionConfig.getGraphConfig();
        List<ActionConfig> actionConfigs = graphConfig.getActions();

        // Handle case with no edges (single action)
        if (graphConfig.getEdges().isEmpty() && !actionConfigs.isEmpty()) {
            // If there are no edges but there are actions, treat the first action as the start action
            ActionConfig singleAction = actionConfigs.get(0);
            start(singleAction);
            return this;
        }

        String message = "Unable to find the action config, please check your parameter.";
        for (EdgeConfig edgeConfig : graphConfig.getEdges()) {
            ActionConfig previousAction = actionConfigs.stream()
                    .filter(action -> action.getId().equals(edgeConfig.getPreviousAction()))
                    .findFirst().orElseThrow(() -> new AccordionException(message));
            ActionConfig nextAction = actionConfigs.stream()
                    .filter(action -> action.getId().equals(edgeConfig.getNextAction()))
                    .findFirst().orElseThrow(() -> new AccordionException(message));
            next(previousAction, nextAction);
        }
        this.rootGraphNode = findRootGraphNode();
        return this;
    }

    /**
     * Resets the execution status of all actions in the plan.
     *
     * <p>This method clears the execution state of all graph nodes, allowing
     * the plan to be executed again with fresh state. It's automatically called
     * by the Accordion engine when needed.</p>
     *
     * <p>Reset operations include:</p>
     * <ul>
     *   <li>Clearing action execution status</li>
     *   <li>Resetting error states</li>
     *   <li>Clearing temporary execution data</li>
     * </ul>
     *
     * @since 1.0.0
     */
    public void reset() {
        graphNodes.forEach(GraphNode::reset);
    }

    /**
     * Checks if all prerequisite actions for a given node have completed successfully.
     *
     * <p>This method is used by the execution engine to determine if an action
     * is ready to execute. An action can only execute when all its dependencies
     * have completed with SUCCESS status.</p>
     *
     * <p>Logic:</p>
     * <ul>
     *   <li>Root node: always ready (returns true)</li>
     *   <li>Other nodes: ready only if all predecessor nodes have SUCCESS status</li>
     * </ul>
     *
     * @param graphNode the node to check for execution readiness
     * @return true if all prerequisite actions have completed successfully, false otherwise
     * @since 1.0.0
     */
    protected boolean prevGraphNodesFinished(final GraphNode graphNode) {
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
     * Updates the execution status of a graph node.
     *
     * <p>This method is called by the execution engine to track the progress
     * and status of individual actions during plan execution.</p>
     *
     * @param graphNode the node whose status should be updated
     * @param status    the new execution status for the node
     * @see GraphNodeStatus
     * @since 1.0.0
     */
    protected void updateGraphNodeStatus(final GraphNode graphNode, final GraphNodeStatus status) {
        graphNode.setStatus(status);
    }

    /**
     * Returns the root GraphNode for execution.
     *
     * <p>This method is used by the execution engine to determine the starting
     * point for plan execution. The root node is cached after first determination
     * for performance.</p>
     *
     * @return the root GraphNode where execution should begin
     * @throws AccordionException if no root node can be determined
     * @since 1.0.0
     */
    protected GraphNode getRootGraphNode() {
        if (rootGraphNode == null) {
            rootGraphNode = findRootGraphNode();
        }
        return rootGraphNode;
    }

    /**
     * Returns all graph nodes in the execution plan.
     *
     * <p>This method provides access to the complete list of actions in the plan.
     * It's primarily used by the execution engine for resource management and cleanup.</p>
     *
     * @return immutable view of all graph nodes in the plan
     * @since 1.0.0
     */
    protected List<GraphNode> getGraphNodes() {
        return graphNodes;
    }
}
