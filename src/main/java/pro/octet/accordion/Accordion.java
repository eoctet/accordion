package pro.octet.accordion;


import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.action.ActionFactory;
import pro.octet.accordion.action.ActionService;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.core.entity.Message;
import pro.octet.accordion.core.entity.Session;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.core.enums.Constant;
import pro.octet.accordion.core.enums.GraphNodeStatus;
import pro.octet.accordion.exceptions.AccordionExecuteException;
import pro.octet.accordion.flow.entity.FlowGraph;
import pro.octet.accordion.flow.entity.GraphEdge;
import pro.octet.accordion.flow.entity.GraphNode;
import pro.octet.accordion.flow.model.EdgeConfig;
import pro.octet.accordion.flow.model.FlowConfig;
import pro.octet.accordion.flow.model.FlowGraphConfig;
import pro.octet.accordion.utils.JsonUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Slf4j
public class Accordion {

    private final List<ActionConfig> actionConfigs;
    private final List<EdgeConfig> edgeConfigs;
    private final Session session;
    private FlowGraph flowGraph;
    private FlowConfig flowConfig;

    public Accordion() {
        this.actionConfigs = Lists.newArrayList();
        this.edgeConfigs = Lists.newArrayList();
        this.session = new Session();
    }

    public Accordion addAction(ActionConfig action) {
        actionConfigs.add(action);
        return this;
    }

    public Accordion addEdge(EdgeConfig edge) {
        edgeConfigs.add(edge);
        return this;
    }

    public String parseToJson() {
        return JsonUtils.toJson(loadFlowConfig());
    }

    public void play(@Nullable Message message) {
        if (message != null) {
            session.put(Constant.ACCORDION_SESSION, message);
        }
        buildFlowGraph();
        execute();
    }

    public String verbose() {
        if (flowGraph.getGraphSimpleView().length() == 0) {
            return "Nothing";
        }
        return flowGraph.getGraphSimpleView().toString();
    }

    private boolean previousNodesAllExecution(GraphNode graphNode) {
        Set<GraphEdge> edges = graphNode.getEdges();
        for (GraphEdge edge : edges) {
            GraphNode node = edge.getNextAction();
            return flowGraph.isSuccess(node);
        }
        return true;
    }

    private FlowConfig loadFlowConfig() {
        if (flowConfig == null) {
            flowConfig = new FlowConfig();
            flowConfig.setFlowGraphConfig(new FlowGraphConfig(actionConfigs, edgeConfigs));
        }
        return flowConfig;
    }

    private List<ActionService> getExecuteActions() {
        List<ActionService> actionServices = Lists.newArrayList();
        for (GraphNode graphNode : flowGraph.getGraphNodes()) {
            if (!flowGraph.isSuccess(graphNode) && previousNodesAllExecution(graphNode)) {
                actionServices.add(graphNode.getActionService());
            }
        }
        return actionServices;
    }

    private List<GraphNode> createGraphNodes(FlowGraphConfig graphConfig) {
        List<GraphNode> graphNodes = Lists.newLinkedList();
        graphConfig.getActions().forEach(c -> {
            GraphNode graphNode = new GraphNode(ActionFactory.getInstance().build(c.getActionType(), c));
            graphNodes.add(graphNode);
        });
        return graphNodes;
    }

    private GraphNode getGraphNode(List<GraphNode> graphNodes, String actionId) {
        for (GraphNode graphNode : graphNodes) {
            if (graphNode.getActionId().equals(actionId)) {
                return graphNode;
            }
        }
        return null;
    }

    private void buildFlowGraph() {
        FlowGraphConfig graphConfig = loadFlowConfig().getFlowGraphConfig();
        List<GraphNode> graphNodes = createGraphNodes(graphConfig);
        List<GraphEdge> graphEdges = Lists.newArrayList();
        graphConfig.getEdges().forEach(e -> {
            GraphEdge edge = new GraphEdge();
            if (StringUtils.isNotBlank(e.getSwitchCondition())) {
                edge.setName(e.getSwitchCondition());
            }
            if (StringUtils.isNotBlank(e.getSwitchLogic())) {
                edge.setSwitcher(Boolean.parseBoolean(e.getSwitchLogic()));
            }
            GraphNode previousNode = getGraphNode(graphNodes, e.getNextAction());
            if (previousNode != null) {
                previousNode.addEdge(edge);
                edge.setPreviousAction(previousNode);
            }
            GraphNode nextNode = getGraphNode(graphNodes, e.getPreviousAction());
            if (nextNode != null) {
                edge.setNextAction(nextNode);
            }
            graphEdges.add(edge);
        });
        flowGraph = new FlowGraph(graphNodes, graphEdges);
        log.info("Create flow graph success, graph node size: {}, graph edge size: {}.", flowGraph.getGraphNodes().size(), flowGraph.getGraphEdges().size());
    }


    private void execute() {
        try {
            boolean breakUp = false;

            do {
                List<ActionService> actionServices = getExecuteActions();
                if (actionServices.isEmpty()) {
                    log.info("Execute action list is empty, break execution.");
                    break;
                }
                for (ActionService action : actionServices) {
                    ActionResult result = action.prepare(session).execute();
                    action.updateOutput(result);
                    GraphNodeStatus status = action.checkError() ? GraphNodeStatus.ERROR : GraphNodeStatus.SUCCESS;
                    flowGraph.updateGraphNodeStatus(action.getConfig().getId(), action.getConfig().getActionName(), status);

                    if (action.getConfig().getActionType() == ActionType.CONDITION) {
                        breakUp = !result.getBoolean(Constant.ACTION_CONDITION_STATE);
                    }
                }
                if (breakUp) {
                    log.info("Execute condition action, filter status is TRUE, break execution.");
                    break;
                }
                flowGraph.updateGraphSimpleView();
            } while (!flowGraph.isExecuteDone());
        } catch (Exception e) {
            throw new AccordionExecuteException(e.getMessage(), e);
        }
    }


}
