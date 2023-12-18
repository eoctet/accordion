package pro.octet.accordion.action;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Feature;
import com.googlecode.aviator.Options;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.core.enums.ConditionOperator;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Condition implements Serializable {

    private final StringBuffer expression;
    private static final AviatorEvaluatorInstance EVALUATOR;

    static {
        EVALUATOR = AviatorEvaluator.newInstance();
        EVALUATOR.setOption(Options.FEATURE_SET, Feature.asSet(Feature.Assignment, Feature.Lambda));
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
        Object result = EVALUATOR.compile(expression, true).execute(params);
        return (boolean) Optional.of(result).orElse(false);
    }

    public String getExpression() {
        return expression.toString().trim();
    }

    public String build() {
        return getExpression();
    }

    @Override
    public String toString() {
        return build();
    }
}
