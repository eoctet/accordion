package chat.octet.accordion;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.ActionService;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.entity.Session;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.core.enums.GraphNodeStatus;
import chat.octet.accordion.exceptions.AccordionException;
import chat.octet.accordion.graph.entity.GraphEdge;
import chat.octet.accordion.graph.entity.GraphNode;
import chat.octet.accordion.graph.entity.GraphView;
import chat.octet.accordion.graph.entity.SwitchFilter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The main execution engine for Accordion automation framework.
 *
 * <p>Accordion is the core execution engine that orchestrates the execution of action plans
 * defined by {@link AccordionPlan}. It manages the execution lifecycle, resource cleanup,
 * session state, and provides detailed execution visualization.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic resource management through {@link AutoCloseable}</li>
 *   <li>Session-based parameter passing between actions</li>
 *   <li>Execution state tracking and visualization</li>
 *   <li>Error handling and recovery mechanisms</li>
 *   <li>Support for conditional execution and switch filters</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * AccordionPlan plan = AccordionPlan.of()
 *     .start(apiAction)
 *     .next(apiAction, emailAction);
 *
 * try (Accordion accordion = new Accordion(plan)) {
 *     ExecuteResult result = accordion.play(true); // with verbose output
 *     System.out.println(accordion.verbose());
 * }
 * }</pre>
 *
 * <p>Thread Safety: This class is not thread-safe. Each thread should use its own instance.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see AccordionPlan
 * @see ExecuteResult
 * @see Session
 * @since 1.0.0
 */
@Slf4j
public class Accordion implements AutoCloseable {
    private final Session session;
    private final StringBuilder executeGraphView; // StringBuilder is more efficient than StringBuffer for single-threaded use
    private volatile boolean verbose; // Make volatile for thread safety
    private volatile SwitchFilter switchFilter; // Make volatile for thread safety
    private volatile boolean breakUp; // Make volatile for thread safety
    private final AccordionPlan plan;
    private volatile boolean closed = false; // Track close state

    /**
     * Constructs a new Accordion execution engine with the specified plan.
     *
     * <p>Initializes the execution environment including session management,
     * execution visualization, and switch filtering capabilities.</p>
     *
     * @param accordionPlan the execution plan to be executed, must not be null
     * @throws NullPointerException if accordionPlan is null
     * @since 1.0.0
     */
    public Accordion(AccordionPlan accordionPlan) {
        Preconditions.checkNotNull(accordionPlan, "Accordion plan cannot be null");
        this.plan = accordionPlan;
        this.session = new Session();
        this.executeGraphView = new StringBuilder();
        this.switchFilter = new SwitchFilter();
    }

    /**
     * Executes the accordion plan with default settings.
     *
     * <p>This is a convenience method equivalent to calling {@code play(false)}.</p>
     *
     * @return the execution result of the last action in the plan
     * @throws AccordionException    if execution fails or the accordion is closed
     * @throws IllegalStateException if the accordion has been closed
     * @since 1.0.0
     */
    public ExecuteResult play() {
        return play(false);
    }

    /**
     * Executes the accordion plan with optional verbose output.
     *
     * <p>When verbose is enabled, detailed execution visualization will be generated
     * and can be retrieved using {@link #verbose()}.</p>
     *
     * @param verbose if true, generates detailed execution visualization
     * @return the execution result of the last action in the plan
     * @throws AccordionException    if execution fails
     * @throws IllegalStateException if the accordion has been closed
     * @since 1.0.0
     */
    public ExecuteResult play(boolean verbose) {
        return play(null, null, verbose);
    }

    /**
     * Executes the accordion plan with an initial message.
     *
     * <p>The message will be available to all actions in the plan through the session
     * under the key {@link AbstractAction#ACCORDION_MESSAGE}.</p>
     *
     * @param message the initial message to pass to the execution plan, may be null
     * @return the execution result of the last action in the plan
     * @throws AccordionException    if execution fails
     * @throws IllegalStateException if the accordion has been closed
     * @since 1.0.0
     */
    public ExecuteResult play(Message message) {
        return play(null, message, false);
    }

    /**
     * Executes the accordion plan with an initial message and optional verbose output.
     *
     * <p>Combines message passing with execution visualization capabilities.</p>
     *
     * @param message the initial message to pass to the execution plan, may be null
     * @param verbose if true, generates detailed execution visualization
     * @return the execution result of the last action in the plan
     * @throws AccordionException    if execution fails
     * @throws IllegalStateException if the accordion has been closed
     * @since 1.0.0
     */
    public ExecuteResult play(Message message, boolean verbose) {
        return play(null, message, verbose);
    }

    /**
     * Executes the accordion plan with full configuration options.
     *
     * <p>This is the most comprehensive execution method, allowing you to specify
     * global parameters, initial message, and visualization options.</p>
     *
     * <p>Global parameters are available to all actions throughout the execution
     * and take precedence over action-specific parameters. The message is available
     * under the {@link AbstractAction#ACCORDION_MESSAGE} key.</p>
     *
     * <p>Execution Process:</p>
     * <ol>
     *   <li>Validates and adds global parameters to session</li>
     *   <li>Adds message to session if provided</li>
     *   <li>Executes actions in dependency order</li>
     *   <li>Handles conditional execution and switch filtering</li>
     *   <li>Generates execution visualization if verbose is enabled</li>
     * </ol>
     *
     * @param globalParams global parameters available to all actions, may be null
     * @param message      the initial message to pass to the execution plan, may be null
     * @param verbose      if true, generates detailed execution visualization
     * @return the execution result of the last action in the plan
     * @throws AccordionException       if execution fails
     * @throws IllegalStateException    if the accordion has been closed
     * @throws IllegalArgumentException if global parameter keys are null
     * @since 1.0.0
     */
    public ExecuteResult play(@Nullable Map<String, Object> globalParams, @Nullable Message message, boolean verbose) {
        if (closed) {
            throw new IllegalStateException("Accordion has been closed and cannot be reused");
        }

        if (executeGraphView.length() > 0) {
            log.debug("Reset the execution status of the accordion.");
            reset();
        }

        // Validate and add global parameters
        if (globalParams != null && !globalParams.isEmpty()) {
            globalParams.forEach((key, value) -> {
                if (key == null) {
                    throw new IllegalArgumentException("Global parameter key cannot be null");
                }
                this.session.add(key, value, true);
            });
        }

        if (message != null) {
            this.session.add(AbstractAction.ACCORDION_MESSAGE, message);
        }
        this.verbose = verbose;

        GraphNode node = plan.getRootGraphNode();
        Preconditions.checkNotNull(node, "Root graph node cannot be null.");
        Queue<GraphNode> queue = Lists.newLinkedList();

        int level = 0;
        long start;
        ExecuteResult lastResult;
        List<GraphView> executedGraphViews = Lists.newArrayList(new GraphView(node, level, false));

        try {
            do {
                //execute action service
                start = System.currentTimeMillis();
                lastResult = execute(node);
                log.debug("({}) -> Action execution time: {} ms.", node.getActionId(), System.currentTimeMillis() - start);
                //get next actions
                Set<GraphEdge> edges = node.getEdges();
                level += edges.isEmpty() ? 0 : 1;
                int count = 0;
                for (GraphEdge edge : edges) {
                    GraphNode nextNode = edge.getNextNode();
                    if (!queue.contains(nextNode) && !nextNode.isFinished()) {
                        queue.offer(nextNode);
                        executedGraphViews.add(new GraphView(nextNode, level, (count == edges.size() - 1)));
                    } else {
                        --level;
                    }
                    count++;
                }
                node = queue.poll();
            } while (node != null);
        } catch (Exception e) {
            throw new AccordionException(e.getMessage(), e);
        }
        generateExecuteGraphView(executedGraphViews);
        return lastResult;
    }

    /**
     * Executes a single graph node (action) within the execution plan.
     *
     * <p>This method handles the execution logic for individual actions, including:</p>
     * <ul>
     *   <li>Switch filtering to determine if action should execute</li>
     *   <li>Dependency checking to ensure prerequisites are met</li>
     *   <li>Action execution and result processing</li>
     *   <li>Status updates and error handling</li>
     *   <li>Special handling for CONDITION and SWITCH action types</li>
     * </ul>
     *
     * @param node the graph node containing the action to execute
     * @return the execution result of the action
     */
    private ExecuteResult execute(GraphNode node) {
        ExecuteResult result = new ExecuteResult();
        boolean filter = Optional.ofNullable(switchFilter.get(node.getActionId())).orElse(true);
        if (!breakUp && filter && plan.prevGraphNodesFinished(node)) {
            //
            ActionService actionService = node.getActionService();
            result = actionService.prepare(session).execute();
            actionService.output(result);
            //
            GraphNodeStatus status = actionService.checkError() ? GraphNodeStatus.ERROR : GraphNodeStatus.SUCCESS;
            plan.updateGraphNodeStatus(node, status);
            //
            ActionType actionType = ActionType.valueOf(actionService.getConfig().getActionType());
            if (ActionType.CONDITION == actionType) {
                breakUp = result.isBreak();
            }
            if (ActionType.SWITCH == actionType) {
                switchFilter = result.getSwitchFilter();
            }
        } else {
            plan.updateGraphNodeStatus(node, GraphNodeStatus.SKIP);
        }
        return result;
    }

    /**
     * Generates a visual representation of the execution plan.
     *
     * <p>Creates a tree-like visualization showing the execution flow,
     * status of each action, and hierarchical relationships. The visualization
     * uses Unicode characters to create a professional tree structure.</p>
     *
     * <p>Example output:</p>
     * <pre>
     * 🆎───⨀ ✓ Start Action (start-001)
     * 	├───⨀ ✓ API Call (api-001)
     * 	└───⨀ ✓ Send Email (email-001)
     * </pre>
     *
     * @param views list of graph views representing the execution sequence
     */
    private void generateExecuteGraphView(List<GraphView> views) {
        if (this.verbose) {
            GraphView view;
            String suffix;
            for (Iterator<GraphView> iterator = views.iterator();
                 iterator.hasNext();
                 this.executeGraphView.append(IntStream.range(0, view.getLevel())
                         .mapToObj((i) -> "\t")
                         .collect(Collectors.joining("", "", suffix
                                 + "───⨀ "
                                 + view.getGraphNode().getStatus().getFlag()
                                 + StringUtils.SPACE + view.getGraphNode().getActionName() + StringUtils.SPACE
                                 + "(" + view.getGraphNode().getActionId() + ")\n")))) {
                view = iterator.next();
                suffix = view.isEnd() ? "└" : "├";
                if (view.getLevel() == 0) {
                    suffix = "\ud83c\udd5e";
                }
            }
        }
    }


    /**
     * Returns the execution visualization as a formatted string.
     *
     * <p>This method returns the tree-like visualization of the execution plan
     * that was generated during execution when verbose mode was enabled.
     * If verbose mode was not enabled, returns an empty string.</p>
     *
     * <p>The visualization includes:</p>
     * <ul>
     *   <li>Execution order and hierarchy</li>
     *   <li>Action status indicators (✓ success, ✗ error, ⊘ skipped)</li>
     *   <li>Action names and IDs</li>
     *   <li>Tree structure showing dependencies</li>
     * </ul>
     *
     * @return formatted execution visualization string, empty if verbose was not enabled
     * @since 1.0.0
     */
    public String verbose() {
        return executeGraphView.toString();
    }

    /**
     * Resets the accordion to its initial state for reuse.
     *
     * <p>This method clears all execution state including:</p>
     * <ul>
     *   <li>Execution visualization buffer</li>
     *   <li>Break-up flags and switch filters</li>
     *   <li>Session parameters and context</li>
     *   <li>Graph node execution status</li>
     * </ul>
     *
     * <p>After calling this method, the accordion can be used to execute
     * the same plan again with fresh state.</p>
     *
     * @since 1.0.0
     */
    public void reset() {
        this.executeGraphView.setLength(0);
        this.breakUp = false;
        this.switchFilter.clear();
        this.session.clear();
        this.plan.reset();
    }

    /**
     * Closes the accordion and releases all associated resources.
     *
     * <p>This method implements {@link AutoCloseable} and ensures proper cleanup
     * of all resources including:</p>
     * <ul>
     *   <li>Action service resources</li>
     *   <li>Session data and parameters</li>
     *   <li>Execution visualization buffers</li>
     * </ul>
     *
     * <p>Once closed, the accordion cannot be reused and will throw
     * {@link IllegalStateException} if execution is attempted.</p>
     *
     * <p>This method is idempotent - calling it multiple times has no additional effect.</p>
     *
     * @since 1.0.0
     */
    @Override
    public void close() {
        if (closed) {
            return; // Already closed
        }

        try {
            // Close all action services
            for (GraphNode n : plan.getGraphNodes()) {
                try {
                    if (n.getActionService() != null) {
                        n.getActionService().close();
                    }
                } catch (Exception e) {
                    log.warn("Error closing action service for node {}: {}", n.getActionId(), e.getMessage());
                }
            }

            // Clear session data
            if (session != null) {
                session.clear();
            }

            // Clear execution view
            if (executeGraphView != null) {
                executeGraphView.setLength(0);
            }

        } finally {
            closed = true;
        }
    }
}
