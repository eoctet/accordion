package pro.octet.accordion.core.condition;

import com.google.common.base.Preconditions;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Feature;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.lexer.token.OperatorType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ConditionBuilder {
    private static volatile ConditionBuilder builder;
    private static final AviatorEvaluatorInstance EVALUATOR;

    static {
        EVALUATOR = AviatorEvaluator.newInstance();
        EVALUATOR.setOption(Options.FEATURE_SET, Feature.asSet(Feature.Assignment, Feature.Lambda));
        EVALUATOR.setOption(Options.SERIALIZABLE, true);
        EVALUATOR.aliasOperator(OperatorType.AND, "and");
        EVALUATOR.aliasOperator(OperatorType.OR, "or");
    }

    private ConditionBuilder() {
    }

    public static ConditionBuilder getInstance() {
        if (builder == null) {
            synchronized (ConditionBuilder.class) {
                if (builder == null) {
                    builder = new ConditionBuilder();
                }
            }
        }
        return builder;
    }

    public boolean test(Map<String, Object> params, String expression, boolean debug) {
        Preconditions.checkNotNull(params, "Condition parameters cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(expression), "Expression cannot be empty.");
        EVALUATOR.setOption(Options.TRACE_EVAL, debug);
        Object result = EVALUATOR.compile(expression, true).execute(params);
        return (boolean) Optional.of(result).orElse(false);
    }

    public boolean test(Map<String, Object> params, Condition condition, boolean debug) {
        String expression = build(condition);
        return test(params, expression, debug);
    }

    public boolean test(Map<String, Object> params, String expression) {
        return test(params, expression, false);
    }

    public boolean test(Map<String, Object> params, Condition condition) {
        return test(params, condition, false);
    }

    @SuppressWarnings("unchecked")
    private String build(List<Condition.ExpressionGroup> expressionGroups) {
        StringBuilder snippet = new StringBuilder();
        for (int i = 0; i < expressionGroups.size(); i++) {
            Condition.ExpressionGroup group = expressionGroups.get(i);
            if (i > 0) {
                snippet.append(StringUtils.SPACE).append(group.getType().name().toLowerCase()).append(StringUtils.SPACE);
            }
            String expression = "";
            if (group.isExpressionGroup()) {
                expression = build((List<Condition.ExpressionGroup>) group.getExpression());
            }
            if (group.isExpression()) {
                Condition.Expression expr = (Condition.Expression) group.getExpression();
                expression = expr.toExpression();
            }
            String flag = Optional.ofNullable(group.getNegation()).orElse(true) ? "" : "!";
            if (StringUtils.countMatches(expression, "(") > 1) {
                expression = StringUtils.join(flag, "(", expression, ")");
            } else {
                expression = StringUtils.join(flag, expression);
            }
            snippet.append(expression);
        }
        return snippet.toString();
    }

    public String build(Condition condition) {
        Preconditions.checkNotNull(condition, "Condition cannot be null.");
        return build(condition.getExpressionGroups());
    }

}
