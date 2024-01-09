package chat.octet.accordion.core.condition;


import chat.octet.accordion.core.enums.ConditionOperator;
import chat.octet.accordion.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * There are two ways to use conditional expressions.
 * The first is to write the expression directly, and the second is to create it using Condition.
 * <p><p>
 * example 1:
 * <code>Condition condition = new Condition("vars", ConditionOperator.GREATER_THAN, 10).and("vars", ConditionOperator.LESS_THAN, 99);</code>
 * <p><p>
 * example 2:
 * <code>(vars > 10) and (vars < 99)</code>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Slf4j
public class Condition implements Serializable {

    @JsonProperty("expressions")
    private final List<ExpressionGroup> expressionGroups;

    public Condition() {
        this.expressionGroups = Lists.newArrayList();
    }

    public Condition(Object param, ConditionOperator operator, Object value) {
        this(param, operator, value, true);
    }

    public Condition(Object param, ConditionOperator operator, Object value, boolean negation) {
        this();
        and(param, operator, value, negation);
    }

    private Condition join(ConditionType type, Object param, ConditionOperator operator, Object value, boolean negation) {
        Preconditions.checkNotNull(param, "Parameter cannot be null.");
        Preconditions.checkNotNull(operator, "Condition operator cannot be null.");
        Preconditions.checkNotNull(value, "Value cannot be null.");

        Expression expression = new Expression(param, operator, value, negation);
        ExpressionGroup group = new ExpressionGroup(type, expression, null);
        expressionGroups.add(group);
        return this;
    }

    private Condition join(ConditionType type, boolean negation, Condition... conditions) {
        Preconditions.checkNotNull(conditions, "Conditions cannot be null.");

        for (Condition c : conditions) {
            ExpressionGroup group = new ExpressionGroup(type, c.getExpressionGroups(), negation);
            expressionGroups.add(group);
        }
        return this;
    }

    public Condition and(Object param, ConditionOperator operator, Object value, boolean negation) {
        return join(ConditionType.AND, param, operator, value, negation);
    }

    public Condition and(Object param, ConditionOperator operator, Object value) {
        return and(param, operator, value, true);
    }

    public Condition or(Object param, ConditionOperator operator, Object value, boolean negation) {
        return join(ConditionType.OR, param, operator, value, negation);
    }

    public Condition or(Object param, ConditionOperator operator, Object value) {
        return or(param, operator, value, true);
    }

    public Condition or(boolean negation, Condition... conditions) {
        return join(ConditionType.OR, negation, conditions);
    }

    public Condition or(Condition... conditions) {
        return or(true, conditions);
    }

    public Condition and(boolean negation, Condition... conditions) {
        return join(ConditionType.AND, negation, conditions);
    }

    public Condition and(Condition... conditions) {
        return and(true, conditions);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static class ExpressionGroup implements Serializable {
        private final ConditionType type;
        private final Object expression;
        private final Boolean negation;

        @JsonCreator
        public ExpressionGroup(@JsonProperty("type") ConditionType type, @JsonProperty("expression") Object expression, @JsonProperty("negation") Boolean negation) {
            this.type = type;
            this.negation = negation;

            if (expression instanceof Map) {
                this.expression = JsonUtils.parseToObject(JsonUtils.toJson(expression), Expression.class);
            } else if (expression instanceof List) {
                this.expression = JsonUtils.parseJsonToList(JsonUtils.toJson(expression), ExpressionGroup.class);
            } else {
                this.expression = expression;
            }
        }

        @JsonIgnore
        public boolean isExpressionGroup() {
            return expression instanceof List;
        }

        @JsonIgnore
        public boolean isExpression() {
            return expression instanceof Expression;
        }
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static class Expression implements Serializable {
        private static final String EXPRESSION_TEMP = "{negation}({param} {operator} {value})";

        private final Object parameter;
        private final ConditionOperator operator;
        private final Object value;
        private final Boolean negation;

        @JsonCreator
        public Expression(@JsonProperty("parameter") Object parameter, @JsonProperty("operator") ConditionOperator operator, @JsonProperty("value") Object value, @JsonProperty("negation") boolean negation) {
            this.parameter = parameter;
            this.operator = operator;
            this.value = value;
            this.negation = negation;
        }

        public String toExpression() {
            Map<String, Object> maps = Maps.newHashMap();
            maps.put("negation", negation ? "" : "!");
            maps.put("param", parameter);
            maps.put("operator", operator.getOperator());
            maps.put("value", value);
            return StringSubstitutor.replace(EXPRESSION_TEMP, maps, "{", "}");
        }

        @Override
        public String toString() {
            return toExpression();
        }

    }

    protected enum ConditionType {
        AND, OR
    }
}
