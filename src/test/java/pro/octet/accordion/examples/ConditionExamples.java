package pro.octet.accordion.examples;


import com.google.common.collect.Maps;
import pro.octet.accordion.core.condition.Condition;
import pro.octet.accordion.core.condition.ConditionBuilder;
import pro.octet.accordion.core.enums.ConditionOperator;

import java.util.Map;

public class ConditionExamples {

    public static void main(String[] args) {
        //create a condition
        Condition condition = new Condition("vars", ConditionOperator.GT, 10)
                .and("vars", ConditionOperator.LT, 99);

        //set the condition params
        Map<String, Object> params = Maps.newLinkedHashMap();
        params.put("vars", 515);

        //print condition expression
        String expression = ConditionBuilder.getInstance().build(condition);
        System.out.println("Condition expression:\n" + expression);

        //print condition result
        System.out.println("Condition calc:\n" + ConditionBuilder.getInstance().test(params, expression));
    }
}
