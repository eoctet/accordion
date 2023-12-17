package pro.octet.accordion.action;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.core.enums.ConditionOperator;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import java.io.Serializable;
import java.util.Map;

@Slf4j
public class Condition implements Serializable {

    private static final String EXPRESSION_BEGIN = "${";
    private static final String EXPRESSION_END = "}";
    private final StringBuffer expression;
    private static final ExpressionFactory EXPRESSION_FACTORY;

    static {
        EXPRESSION_FACTORY = new ExpressionFactoryImpl();
    }

    public Condition() {
        this.expression = new StringBuffer();
    }

    public Condition(String expression) {
        this.expression = new StringBuffer();
        this.expression.append(expression);
    }

    public Condition(Object param, ConditionOperator operator, Object value, boolean negation) {
        this.expression = new StringBuffer();
        this.expression.append(snippet(param, operator, value, negation)).append(StringUtils.SPACE);
    }

    public Condition(Object param, ConditionOperator operator, Object value) {
        this(param, operator, value, true);
    }

    private String snippet(Object param, ConditionOperator operator, Object value, boolean negation) {
        String flag = negation ? StringUtils.EMPTY : "!";
        return StringUtils.join(flag, "(", param, StringUtils.SPACE, operator.getOperator(), StringUtils.SPACE, value, ")");
    }

    private Condition join(ConditionOperator joinOperator, Object param, ConditionOperator operator, Object value, boolean negation) {
        String snippet = snippet(param, operator, value, negation);
        if (expression.length() > 0) {
            expression.append(joinOperator.getOperator()).append(StringUtils.SPACE);
        }
        expression.append(snippet).append(StringUtils.SPACE);
        return this;
    }

    public Condition and(Object param, ConditionOperator operator, Object value, boolean negation) {
        return join(ConditionOperator.AND, param, operator, value, negation);
    }

    public Condition and(Object param, ConditionOperator operator, Object value) {
        return and(param, operator, value, true);
    }

    public Condition or(Object param, ConditionOperator operator, Object value, boolean negation) {
        return join(ConditionOperator.OR, param, operator, value, negation);
    }

    public Condition or(Object param, ConditionOperator operator, Object value) {
        return or(param, operator, value, true);
    }

    private Condition join(ConditionOperator operator, boolean negation, Condition... conditions) {
        StringBuilder snippets = new StringBuilder(expression.toString().trim());
        if (snippets.length() > 0 && StringUtils.countMatches(snippets.toString(), "(") > 1) {
            snippets.insert(0, "(").insert(snippets.length(), ")").append(StringUtils.SPACE);
        }
        for (Condition condition : conditions) {
            StringBuilder buffer = new StringBuilder(condition.getExpression());
            if (StringUtils.countMatches(buffer.toString(), "(") > 1) {
                buffer.insert(0, "(").insert(buffer.length(), ")");
            }
            if (snippets.length() > 0) {
                snippets.append(operator.getOperator()).append(StringUtils.SPACE);
            }
            if (!negation) {
                buffer.insert(0, "!");
            }
            snippets.append(buffer).append(StringUtils.SPACE);
        }
        expression.setLength(0);
        expression.append(snippets);
        return this;
    }

    public Condition or(boolean negation, Condition... conditions) {
        return join(ConditionOperator.OR, negation, conditions);
    }

    public Condition or(Condition... conditions) {
        return or(true, conditions);
    }

    public Condition and(boolean negation, Condition... conditions) {
        return join(ConditionOperator.AND, negation, conditions);
    }

    public Condition and(Condition... conditions) {
        return and(true, conditions);
    }

    public static boolean test(Map<String, Object> params, String expression) {
        SimpleContext context = new SimpleContext();
        params.forEach((key, value) -> context.setVariable(key, EXPRESSION_FACTORY.createValueExpression(value, value.getClass())));
        ValueExpression valueExpression = EXPRESSION_FACTORY.createValueExpression(context, expression, Boolean.class);
        return (boolean) valueExpression.getValue(context);
    }

    public String getExpression() {
        return expression.toString().trim();
    }

    public String build() {
        String finalExpression = getExpression();
        if (!StringUtils.startsWith(finalExpression, EXPRESSION_BEGIN) && !StringUtils.endsWith(finalExpression, EXPRESSION_END)) {
            finalExpression = StringUtils.join(EXPRESSION_BEGIN, finalExpression, EXPRESSION_END);
        }
        return finalExpression;
    }

    @Override
    public String toString() {
        return build();
    }
}
