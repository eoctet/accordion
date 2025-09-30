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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * There are two ways to use conditional expressions.
 * The first is to write the expression directly, and the second is to create it using Condition.
 *
 * <p>example 1:
 * <code>Condition condition = new Condition("vars", ConditionOperator.GREATER_THAN, 10).and("vars", ConditionOperator.LESS_THAN, 99);</code>
 *
 * <p>example 2:
 * <code>(vars > 10) and (vars < 99)</code>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Slf4j
public class Condition implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("expressions")
    private final List<ExpressionGroup> expressionGroups;

    public Condition() {
        this.expressionGroups = Lists.newArrayList();
    }

    public Condition(final Object param, final ConditionOperator operator, final Object value) {
        this(param, operator, value, true);
    }

    public Condition(final Object param, final ConditionOperator operator, final Object value, final boolean negation) {
        this();
        and(param, operator, value, negation);
    }

    private Condition join(final ConditionType type, final Object param, final ConditionOperator operator, final Object value, final boolean negation) {
        Preconditions.checkNotNull(param, "Parameter cannot be null.");
        Preconditions.checkNotNull(operator, "Condition operator cannot be null.");
        Preconditions.checkNotNull(value, "Value cannot be null.");

        Expression expression = new Expression(param, operator, value, negation);
        ExpressionGroup group = new ExpressionGroup(type, expression, null);
        expressionGroups.add(group);
        return this;
    }

    private Condition join(final ConditionType type, final boolean negation, final Condition... conditions) {
        Preconditions.checkNotNull(conditions, "Conditions cannot be null.");

        for (Condition c : conditions) {
            ExpressionGroup group = new ExpressionGroup(type, c.getExpressionGroups(), negation);
            expressionGroups.add(group);
        }
        return this;
    }

    /**
     * Adds an AND condition with specified negation.
     *
     * @param param    the parameter to evaluate
     * @param operator the comparison operator
     * @param value    the value to compare against
     * @param negation whether to negate the condition
     * @return this condition instance for method chaining
     */
    public Condition and(final Object param, final ConditionOperator operator, final Object value, final boolean negation) {
        return join(ConditionType.AND, param, operator, value, negation);
    }

    /**
     * Adds an AND condition with default negation (true).
     *
     * @param param    the parameter to evaluate
     * @param operator the comparison operator
     * @param value    the value to compare against
     * @return this condition instance for method chaining
     */
    public Condition and(final Object param, final ConditionOperator operator, final Object value) {
        return and(param, operator, value, true);
    }

    /**
     * Adds an OR condition with specified negation.
     *
     * @param param    the parameter to evaluate
     * @param operator the comparison operator
     * @param value    the value to compare against
     * @param negation whether to negate the condition
     * @return this condition instance for method chaining
     */
    public Condition or(final Object param, final ConditionOperator operator, final Object value, final boolean negation) {
        return join(ConditionType.OR, param, operator, value, negation);
    }

    /**
     * Adds an OR condition with default negation (true).
     *
     * @param param    the parameter to evaluate
     * @param operator the comparison operator
     * @param value    the value to compare against
     * @return this condition instance for method chaining
     */
    public Condition or(final Object param, final ConditionOperator operator, final Object value) {
        return or(param, operator, value, true);
    }

    /**
     * Adds OR conditions from multiple condition objects with specified negation.
     *
     * @param negation   whether to negate the conditions
     * @param conditions the conditions to combine with OR
     * @return this condition instance for method chaining
     */
    public Condition or(final boolean negation, final Condition... conditions) {
        return join(ConditionType.OR, negation, conditions);
    }

    /**
     * Adds OR conditions from multiple condition objects with default negation (true).
     *
     * @param conditions the conditions to combine with OR
     * @return this condition instance for method chaining
     */
    public Condition or(final Condition... conditions) {
        return or(true, conditions);
    }

    /**
     * Adds AND conditions from multiple condition objects with specified negation.
     *
     * @param negation   whether to negate the conditions
     * @param conditions the conditions to combine with AND
     * @return this condition instance for method chaining
     */
    public Condition and(final boolean negation, final Condition... conditions) {
        return join(ConditionType.AND, negation, conditions);
    }

    /**
     * Adds AND conditions from multiple condition objects with default negation (true).
     *
     * @param conditions the conditions to combine with AND
     * @return this condition instance for method chaining
     */
    public Condition and(final Condition... conditions) {
        return and(true, conditions);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static class ExpressionGroup implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private final ConditionType type;
        private final Object expression;
        private final Boolean negation;

        @JsonCreator
        public ExpressionGroup(@JsonProperty("type") final ConditionType type, @JsonProperty("expression") final Object expression, @JsonProperty("negation") final Boolean negation) {
            this.type = type;
            this.negation = negation;
            //Special handling of type issues in JSON parsing
            if (expression instanceof Map) {
                this.expression = JsonUtils.parseToObject(JsonUtils.toJson(expression), Expression.class);
            } else if (expression instanceof List) {
                this.expression = JsonUtils.parseJsonToList(JsonUtils.toJson(expression), ExpressionGroup.class);
            } else {
                this.expression = expression;
            }
        }

        /**
         * Checks if this expression group contains a list of expression groups.
         *
         * @return true if expression is a List, false otherwise
         */
        @JsonIgnore
        public boolean isExpressionGroup() {
            return expression instanceof List;
        }

        /**
         * Checks if this expression group contains a single expression.
         *
         * @return true if expression is an Expression, false otherwise
         */
        @JsonIgnore
        public boolean isExpression() {
            return expression instanceof Expression;
        }
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected static class Expression implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final String EXPRESSION_TEMP = "{negation}({param} {operator} {value})";

        private final Object parameter;
        private final ConditionOperator operator;
        private final Object value;
        private final Boolean negation;

        @JsonCreator
        public Expression(@JsonProperty("parameter") final Object parameter, @JsonProperty("operator") final ConditionOperator operator, @JsonProperty("value") final Object value, @JsonProperty("negation") final boolean negation) {
            this.parameter = parameter;
            this.operator = operator;
            this.value = value;
            this.negation = negation;
        }

        /**
         * Converts this expression to a string representation.
         *
         * @return the expression as a formatted string
         */
        public String toExpression() {
            Map<String, Object> maps = Maps.newHashMap();
            maps.put("negation", negation ? "" : "!");
            maps.put("param", parameter);
            maps.put("operator", operator.getOperator());
            maps.put("value", value);
            return StringSubstitutor.replace(EXPRESSION_TEMP, maps, "{", "}");
        }

        /**
         * Returns a string representation of this expression.
         *
         * @return the expression as a formatted string
         */
        @Override
        public String toString() {
            return toExpression();
        }

    }

    protected enum ConditionType {
        AND, OR
    }
}
