package pro.octet.accordion;


import com.google.common.base.Preconditions;
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
import pro.octet.accordion.exceptions.AccordionExecuteException;
import pro.octet.accordion.graph.entity.GraphEdge;
import pro.octet.accordion.graph.entity.GraphNode;
import pro.octet.accordion.graph.entity.SwitchController;

import javax.annotation.Nullable;
import java.util.Optional;

@Slf4j
public class Accordion {

    private final Session session;
    private AccordionPlan plan;
    private final StringBuffer executeGraphView;
    private boolean debug;
    private boolean breakUp;
    private SwitchController controller;

    public Accordion() {
        this.session = new Session();
        this.executeGraphView = new StringBuffer();
        this.controller = new SwitchController();
    }

    public void play(AccordionPlan plan) {
        play(null, plan, false);
    }

    public void play(AccordionPlan plan, boolean isDebug) {
        play(null, plan, isDebug);
    }

    public void play(@Nullable Message message, AccordionPlan accordionPlan, boolean isDebug) {
        Preconditions.checkNotNull(accordionPlan, "Accordion plan cannot be null");
        this.plan = accordionPlan;
        this.debug = isDebug;

        try {
            if (message != null) {
                session.put(AbstractAction.ACCORDION_MESSAGE, message);
            }
            executeGraphView.setLength(0);
            breakUp = false;
            controller.clear();
            execute(plan.getRootGraphNode(), StringUtils.EMPTY);
        } catch (Exception e) {
            throw new AccordionExecuteException(e.getMessage(), e);
        }
    }

    private void execute(GraphNode nextNode, String depth) {
        if (nextNode.preNodesAllExecuted()) {
            boolean hasControl = Optional.ofNullable(controller.get(nextNode.getActionId())).orElse(true);
            if (debug) {
                plan.updateGraphNodeStatus(nextNode, GraphNodeStatus.SUCCESS);
            } else if (!nextNode.preNodesHasErrorOrSkipped() && !breakUp && hasControl) {
                ActionService actionService = nextNode.getActionService();
                ActionResult result = actionService.prepare(session).execute();
                actionService.updateOutput(result);
                GraphNodeStatus status = actionService.checkError() ? GraphNodeStatus.ERROR : GraphNodeStatus.SUCCESS;
                plan.updateGraphNodeStatus(nextNode, status);
                if (ActionType.CONDITION.name().equalsIgnoreCase(actionService.getConfig().getActionType())) {
                    breakUp = !result.getBoolean(ConditionAction.ACTION_CONDITION_STATE);
                }
                if (ActionType.SWITCH.name().equalsIgnoreCase(actionService.getConfig().getActionType())) {
                    controller = (SwitchController) result.get(SwitchAction.ACTION_SWITCH_CONTROL);
                }
            } else {
                plan.updateGraphNodeStatus(nextNode, GraphNodeStatus.SKIP);
            }
            executeGraphView.append(depth)
                    .append("⎣____ ")
                    .append(nextNode.getStatus().getFlag()).append(StringUtils.SPACE).append(nextNode.getActionName())
                    .append(" (").append(nextNode.getActionId()).append(")")
                    .append("\n");

            for (GraphEdge edge : nextNode.getRightEdges()) {
                execute(edge.getNextNode(), depth + "\t");
            }
        }
    }

    public String verbose() {
        return executeGraphView.toString();
    }

}
