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
 * Specify an accordion plan to be executed using the accordion.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Slf4j
public class Accordion {
    private final Session session;
    private final StringBuffer executeGraphView;
    private boolean verbose;
    private SwitchFilter switchFilter;
    private boolean breakUp;
    private final AccordionPlan plan;

    public Accordion(AccordionPlan accordionPlan) {
        Preconditions.checkNotNull(accordionPlan, "Accordion plan cannot be null");
        this.plan = accordionPlan;
        this.session = new Session();
        this.executeGraphView = new StringBuffer();
        this.switchFilter = new SwitchFilter();
    }

    /**
     * Play the accordion plan.
     *
     * @return ExecuteResult, last execution result.
     */
    public ExecuteResult play() {
        return play(false);
    }

    /**
     * Play the accordion plan.
     *
     * @param verbose Print the execution views.
     * @return ExecuteResult, last execution result.
     */
    public ExecuteResult play(boolean verbose) {
        return play(null, null, verbose);
    }

    /**
     * Play the accordion plan.
     *
     * @param message The message to be passed to the accordion.
     * @return ExecuteResult, last execution result.
     */
    public ExecuteResult play(Message message) {
        return play(null, message, false);
    }

    /**
     * Play the accordion plan.
     *
     * @param message The message to be passed to the accordion.
     * @param verbose Print the execution views.
     * @return ExecuteResult, last execution result.
     */
    public ExecuteResult play(Message message, boolean verbose) {
        return play(null, message, verbose);
    }

    /**
     * Play the accordion plan.
     *
     * @param globalParams The global parameters to be passed to the accordion.
     * @param message      The message to be passed to the accordion.
     * @param verbose      Print the execution views.
     * @return ExecuteResult, last execution result.
     */
    public ExecuteResult play(@Nullable Map<String, Object> globalParams, @Nullable Message message, boolean verbose) {
        if (executeGraphView.length() > 0) {
            log.debug("Reset the execution status of the accordion.");
            reset();
        }
        if (globalParams != null && !globalParams.isEmpty()) {
            globalParams.forEach((key, value) -> this.session.add(key, value, true));
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

    private ExecuteResult execute(GraphNode node) {
        ExecuteResult result = new ExecuteResult();
        boolean filter = Optional.ofNullable(switchFilter.get(node.getActionId())).orElse(true);
        if (!breakUp && filter && plan.prevGraphNodesFinished(node)) {
            ActionService actionService = node.getActionService();
            result = actionService.prepare(session).execute();
            actionService.updateOutput(result);
            GraphNodeStatus status = actionService.checkError() ? GraphNodeStatus.ERROR : GraphNodeStatus.SUCCESS;
            plan.updateGraphNodeStatus(node, status);
            if (ActionType.CONDITION == ActionType.valueOf(actionService.getConfig().getActionType())) {
                breakUp = result.isBreak();
            }
            if (ActionType.SWITCH == ActionType.valueOf(actionService.getConfig().getActionType())) {
                switchFilter = result.getSwitchFilter();
            }
        } else {
            plan.updateGraphNodeStatus(node, GraphNodeStatus.SKIP);
        }
        return result;
    }

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
     * Return the execution views.
     *
     * @return String
     */
    public String verbose() {
        return executeGraphView.toString();
    }

    /**
     * Reset the accordion status.
     */
    public void reset() {
        this.executeGraphView.setLength(0);
        this.breakUp = false;
        this.switchFilter.clear();
        this.session.clear();
        if (this.plan != null) {
            this.plan.reset();
        }
    }

}
