package pro.octet.accordion;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.ActionService;
import pro.octet.accordion.action.base.ConditionAction;
import pro.octet.accordion.action.base.SwitchAction;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.core.entity.Message;
import pro.octet.accordion.core.entity.Session;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.core.enums.GraphNodeStatus;
import pro.octet.accordion.exceptions.AccordionException;
import pro.octet.accordion.graph.entity.GraphEdge;
import pro.octet.accordion.graph.entity.GraphNode;
import pro.octet.accordion.graph.entity.GraphView;
import pro.octet.accordion.graph.entity.SwitchFilter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class Accordion {

    private final Session session;
    private final StringBuffer executeGraphView;
    private boolean verbose;
    private SwitchFilter switchFilter;
    private boolean breakUp;
    private AccordionPlan plan;

    public Accordion() {
        this.session = new Session();
        this.executeGraphView = new StringBuffer();
        this.switchFilter = new SwitchFilter();
    }

    public void play(AccordionPlan plan) {
        play(null, plan, false);
    }

    public void play(AccordionPlan plan, boolean verbose) {
        play(null, plan, verbose);
    }

    public void play(@Nullable Message message, AccordionPlan accordionPlan, boolean verbose) {
        Preconditions.checkNotNull(accordionPlan, "Accordion plan cannot be null");
        this.verbose = verbose;
        this.plan = accordionPlan;

        reset();
        if (message != null) {
            this.session.put(AbstractAction.ACCORDION_MESSAGE, message);
        }

        GraphNode node = plan.getRootGraphNode();
        Preconditions.checkNotNull(node, "Root graph node cannot be null.");
        Queue<GraphNode> queue = Lists.newLinkedList();

        int level = 0;
        List<GraphView> executedGraphViews = Lists.newArrayList(new GraphView(node, level, false));

        try {
            do {
                //execute action service
                execute(node);
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
    }

    private void execute(GraphNode node) {
        boolean filter = Optional.ofNullable(switchFilter.get(node.getActionId())).orElse(true);
        if (!breakUp && filter && plan.prevGraphNodesFinished(node)) {
            ActionService actionService = node.getActionService();
            ActionResult result = actionService.prepare(session).execute();
            actionService.updateOutput(result);
            GraphNodeStatus status = actionService.checkError() ? GraphNodeStatus.ERROR : GraphNodeStatus.SUCCESS;
            plan.updateGraphNodeStatus(node, status);
            if (ActionType.CONDITION.name().equalsIgnoreCase(actionService.getConfig().getActionType())) {
                breakUp = !result.getBoolean(ConditionAction.ACTION_CONDITION_STATE);
            }
            if (ActionType.SWITCH.name().equalsIgnoreCase(actionService.getConfig().getActionType())) {
                switchFilter = (SwitchFilter) result.get(SwitchAction.ACTION_SWITCH_CONTROL);
            }
        } else {
            plan.updateGraphNodeStatus(node, GraphNodeStatus.SKIP);
        }
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

    public String verbose() {
        return executeGraphView.toString();
    }

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
