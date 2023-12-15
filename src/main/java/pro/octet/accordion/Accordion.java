package pro.octet.accordion;


import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.action.ActionService;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.core.entity.Message;
import pro.octet.accordion.core.entity.Session;
import pro.octet.accordion.core.enums.Constant;
import pro.octet.accordion.core.enums.GraphNodeStatus;
import pro.octet.accordion.exceptions.AccordionExecuteException;
import pro.octet.accordion.flow.entity.GraphEdge;
import pro.octet.accordion.flow.entity.GraphNode;

import javax.annotation.Nullable;

@Slf4j
public class Accordion {

    private final Session session;
    private AccordionPlan plan;
    private final StringBuffer executeGraphView;

    public Accordion() {
        this.session = new Session();
        this.executeGraphView = new StringBuffer();
    }

    public void play(AccordionPlan plan) {
        play(null, plan);
    }

    public void play(@Nullable Message message, AccordionPlan accordionPlan) {
        Preconditions.checkNotNull(accordionPlan, "Accordion plan cannot be null");
        this.plan = accordionPlan;

        try {
            if (message != null) {
                session.put(Constant.ACCORDION_MESSAGE, message);
            }
            execute(plan.getRootGraphNode(), StringUtils.EMPTY);
        } catch (Exception e) {
            throw new AccordionExecuteException(e.getMessage(), e);
        }
    }

    private void execute(GraphNode nextNode, String depth) {
        if (!nextNode.preNodesAllExecuted()) {
            return;
        }
        if (!nextNode.preNodesHasErrorOrSkipped()) {
            ActionService actionService = nextNode.getActionService();
            ActionResult result = actionService.prepare(session).execute();
            actionService.updateOutput(result);
            GraphNodeStatus status = actionService.checkError() ? GraphNodeStatus.ERROR : GraphNodeStatus.SUCCESS;
            plan.updateGraphNodeStatus(nextNode, status);
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

    public String verbose() {
        return executeGraphView.toString();
    }

}
