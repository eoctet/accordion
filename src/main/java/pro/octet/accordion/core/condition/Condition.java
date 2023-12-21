package pro.octet.accordion.core.condition;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import pro.octet.accordion.core.enums.ConditionOperator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * There are two ways to use conditional expressions.
 * The first is to write the expression directly, and the second is to create it using Condition.
 * <p><p>
 * example 1:
 * <code>Condition condition = new Condition("vars", ConditionOperator.GREATER_THAN, 10).or("vars", ConditionOperator.LESS_THAN, 99);</code>
 * <p><p>
 * example 2:
 * <code>(vars > 10) or (vars < 99)</code>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Slf4j
public class Condition implements Serializable {

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

    private Condition join(ConditionOperator join, Object param, ConditionOperator operator, Object value, boolean negation) {
        Preconditions.checkNotNull(param, "Parameter cannot be null.");
        Preconditions.checkNotNull(operator, "Condition operator cannot be null.");
        Preconditions.checkNotNull(value, "Value cannot be null.");
        Preconditions.checkArgument(ConditionOperator.isLogicalOperator(operator), "Condition operator cannot be AND / OR.");

        ExpressionGroup group = expressionGroups.stream().findFirst().orElse(null);
        if (group == null) {
            group = new ExpressionGroup();
            expressionGroups.add(group);
        }
        group.addExpression(new Expression(join, param, operator, value, negation));
        return this;
    }

    private Condition join(ConditionOperator join, boolean negation, Condition... conditions) {
        Preconditions.checkNotNull(conditions, "Conditions cannot be null.");

        for (Condition c : conditions) {
            List<ExpressionGroup> groups = Lists.newArrayList();
            for (ExpressionGroup g : c.getExpressionGroups()) {
                List<Expression> expressions = g.getExpressions().stream().map(Expression::clone).collect(Collectors.toList());
                groups.add(new ExpressionGroup(expressions, join, negation));
            }
            this.expressionGroups.addAll(groups);
        }
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

    @Getter
    protected static class ExpressionGroup implements Serializable {
        private final List<Expression> expressions;
        private ConditionOperator join;
        private Boolean negation;

        public ExpressionGroup(List<Expression> expressions, ConditionOperator join, boolean negation) {
            this.expressions = expressions;
            this.join = join;
            this.negation = negation;
        }

        public ExpressionGroup() {
            this.expressions = Lists.newArrayList();
        }

        public void addExpression(Expression expr) {
            this.expressions.add(expr);
        }

        public String toExpression() {
            StringBuilder snippet = new StringBuilder();
            for (int i = 0; i < expressions.size(); i++) {
                Condition.Expression expression = expressions.get(i);
                if (i > 0) {
                    snippet.append(StringUtils.SPACE).append(expression.getJoin().getOperator()).append(StringUtils.SPACE);
                }
                snippet.append(expression.toExpression());
            }
            return snippet.toString();
        }

        @Override
        public String toString() {
            return toExpression();
        }
    }

    @Getter
    protected static class Expression implements Cloneable, Serializable {
        private static final String EXPRESSION_TEMP = "{negation}({param} {operator} {value})";

        private final ConditionOperator join;
        private final Object parameter;
        private final ConditionOperator operator;
        private final Object value;
        private final Boolean negation;

        public Expression(ConditionOperator join, Object parameter, ConditionOperator operator, Object value, boolean negation) {
            this.join = join;
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

        @Override
        public Expression clone() {
            try {
                return (Expression) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }
}
