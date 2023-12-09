package pro.octet.accordion.test;


import com.google.common.collect.Lists;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.flow.model.EdgeConfig;
import pro.octet.accordion.flow.model.FlowConfig;
import pro.octet.accordion.flow.model.FlowGraphConfig;

public class AgentTest {

    public static void main(String[] args) {
//        FlowConfig flowConfig = CommonUtils.parseToObject("", FlowConfig.class);
//
//        AgentRunner agentRunner = AgentRunner.getInstance();
//        Agent agent = agentRunner.createAgent(flowConfig);
//        ProcessStatus status = agentRunner.execute(agent);


        FlowGraphConfig config = new FlowGraphConfig();
        //component config
        ActionConfig t1 = new ActionConfig("a", ActionType.TEST, "A", "");
        ActionConfig t2 = new ActionConfig("b", ActionType.TEST, "B", "");
        ActionConfig t3 = new ActionConfig("c", ActionType.TEST, "C", "");
        ActionConfig t4 = new ActionConfig("d", ActionType.TEST, "D", "");
        ActionConfig t5 = new ActionConfig("e", ActionType.TEST, "E", "");
        ActionConfig t6 = new ActionConfig("f", ActionType.TEST, "F", "");
        ActionConfig t7 = new ActionConfig("g", ActionType.TEST, "G", "");
        config.setActions(Lists.newArrayList(t1, t2, t3, t4, t5, t6, t7));
        //edges config
        //(A)=>(BC)=>(D)=>(EF)=>(G)
        EdgeConfig e1 = new EdgeConfig("a", "b");
        EdgeConfig e2 = new EdgeConfig("a", "c");
        EdgeConfig e3 = new EdgeConfig("b", "d");
        EdgeConfig e4 = new EdgeConfig("c", "d");
        EdgeConfig e5 = new EdgeConfig("d", "e");
        EdgeConfig e6 = new EdgeConfig("d", "f");
        EdgeConfig e7 = new EdgeConfig("e", "g");
        EdgeConfig e8 = new EdgeConfig("f", "g");
        config.setEdges(Lists.newArrayList(e1, e2, e3, e4, e5, e6, e7, e8));
        //
        FlowConfig flow = new FlowConfig();
        flow.setFlowGraphConfig(config);
        //
//        AgentRunner agentRunner = AgentRunner.getInstance();
//        Agent agent = agentRunner.createAgent(flow);
//        ProcessStatus status = agentRunner.execute(agent);
//        System.out.println(status.formatProcessingTrack());
    }
}
