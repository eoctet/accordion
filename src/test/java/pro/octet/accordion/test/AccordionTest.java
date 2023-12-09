package pro.octet.accordion.test;

import pro.octet.accordion.Accordion;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.entity.Message;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.flow.model.EdgeConfig;

/**
 * @author WangJian
 * @version 1.0
 * @since 1.8
 */
public class AccordionTest {

    public static void main(String[] args) {

        ActionConfig a = new ActionConfig("a", ActionType.TEST, "A", "");
        ActionConfig b = new ActionConfig("b", ActionType.TEST, "B", "");
        ActionConfig c = new ActionConfig("c", ActionType.TEST, "C", "");
        ActionConfig d = new ActionConfig("d", ActionType.TEST, "D", "");
        ActionConfig e = new ActionConfig("e", ActionType.TEST, "E", "");
        ActionConfig f = new ActionConfig("f", ActionType.TEST, "F", "");
        ActionConfig g = new ActionConfig("g", ActionType.TEST, "G", "");

        EdgeConfig e1 = new EdgeConfig(a, b);
        EdgeConfig e2 = new EdgeConfig(a, c);
        EdgeConfig e3 = new EdgeConfig(b, d);
        EdgeConfig e4 = new EdgeConfig(c, d);
        EdgeConfig e5 = new EdgeConfig(d, e);
        EdgeConfig e6 = new EdgeConfig(d, f);
        EdgeConfig e7 = new EdgeConfig(e, g);
        EdgeConfig e8 = new EdgeConfig(f, g);

        Accordion accordion = new Accordion();
        accordion.addAction(a)
                .addAction(b)
                .addAction(c)
                .addAction(d)
                .addAction(e)
                .addAction(f)
                .addAction(g)
                .addEdge(e1)
                .addEdge(e2)
                .addEdge(e3)
                .addEdge(e4)
                .addEdge(e5)
                .addEdge(e6)
                .addEdge(e7)
                .addEdge(e8)
                .play(new Message());

        System.out.println(accordion.verbose());

        System.out.println(accordion.parseToJson());

    }
}
