package pro.octet.accordion.test;

import pro.octet.accordion.Accordion;
import pro.octet.accordion.AccordionPlan;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.utils.CommonUtils;

public class AccordionTest {

    public static void main(String[] args) {
        ActionConfig a = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("A").build();
        ActionConfig b = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("B").build();
        ActionConfig c = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("C").build();
        ActionConfig d = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("D").build();
        ActionConfig e = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("E").build();
        ActionConfig f = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("F").build();
        ActionConfig g = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("G").build();
        ActionConfig h = ActionConfig.builder().id(CommonUtils.randomString("ACT").toUpperCase()).actionType(ActionType.TEST).actionName("H").build();

        AccordionPlan plan = AccordionPlan.of()
                .start(a)
                .next(a, b, c)
                .next(b, d)
                .next(c, d)
                .next(d, e, f, g)
                .next(e, h)
                .next(f, h)
                .next(g, h);

        System.out.println("Execute:");
        Accordion accordion = new Accordion();
        accordion.play(plan, true);
        System.out.println("Accordion plan:\n" + accordion.verbose());

    }
}
