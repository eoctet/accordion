package pro.octet.accordion.core.condition;

import com.google.common.base.Preconditions;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Feature;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.lexer.token.OperatorType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class ConditionBuilder {
    private static volatile ConditionBuilder builder;
    private static final AviatorEvaluatorInstance EVALUATOR;

    static {
        EVALUATOR = AviatorEvaluator.newInstance();
        EVALUATOR.setOption(Options.FEATURE_SET, Feature.asSet(Feature.Assignment, Feature.Lambda));
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

    public boolean test(Map<String, Object> params, String expression) {
        Preconditions.checkNotNull(params, "Condition parameters cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(expression), "Expression cannot be empty.");
        Object result = EVALUATOR.compile(expression, true).execute(params);
        return (boolean) Optional.of(result).orElse(false);
    }

    public boolean test(Map<String, Object> params, Condition condition) {
        String expression = build(condition);
        return test(params, expression);
    }

    public String build(Condition condition) {
        Preconditions.checkNotNull(condition, "Condition cannot be null.");

        StringBuilder snippet = new StringBuilder();
        for (int i = 0; i < condition.getExpressionGroups().size(); i++) {
            Condition.ExpressionGroup group = condition.getExpressionGroups().get(i);
            if (i > 0) {
                snippet.append(StringUtils.SPACE).append(group.getJoin().getOperator()).append(StringUtils.SPACE);
            }
            String expression = group.toExpression();
            String flag = Optional.ofNullable(group.getNegation()).orElse(true) ? "" : "!";
            if (StringUtils.countMatches(expression, "(") > 1 && condition.getExpressionGroups().size() > 1) {
                expression = StringUtils.join(flag, "(", expression, ")");
            } else {
                expression = StringUtils.join(flag, expression);
            }
            snippet.append(expression);
        }
        return snippet.toString();
    }

}
