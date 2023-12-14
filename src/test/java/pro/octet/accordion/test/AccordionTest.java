package pro.octet.accordion.test;

import pro.octet.accordion.Accordion;
import pro.octet.accordion.AccordionPlan;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.ActionType;

public class AccordionTest {

    public static void main(String[] args) {
        ActionConfig a = ActionConfig.builder().id("a").actionType(ActionType.TEST).actionName("A").build();
        ActionConfig b = ActionConfig.builder().id("b").actionType(ActionType.TEST).actionName("B").build();
        ActionConfig c = ActionConfig.builder().id("c").actionType(ActionType.TEST).actionName("C").build();
        ActionConfig d = ActionConfig.builder().id("d").actionType(ActionType.TEST).actionName("D").build();
        ActionConfig e = ActionConfig.builder().id("e").actionType(ActionType.TEST).actionName("E").build();
        ActionConfig f = ActionConfig.builder().id("f").actionType(ActionType.TEST).actionName("F").build();
        ActionConfig g = ActionConfig.builder().id("g").actionType(ActionType.TEST).actionName("G").build();

        AccordionPlan plan = AccordionPlan.of()
                .start(a)
                .next(a, b, c)
                .next(b, d)
                .next(c, d)
                .next(d, e, f)
                .next(e, g)
                .next(f, g);

        System.out.println(plan.getPlanGraphView());
        Accordion accordion = new Accordion();
        accordion.play(plan);
        System.out.println(accordion.verbose());

    }
}
