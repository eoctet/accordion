package chat.octet.accordion.action.base;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.exceptions.ActionException;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * ConditionAction is similar to the "if" in Java, used to control the execution process.
 * When the conditional judgment is not true, the execution will be interrupted.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ConditionParameter
 */
@Slf4j
public class ConditionAction extends AbstractAction {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final String ACTION_CONDITION_STATE = "ACTION_CONDITION_STATE";
    private final transient ConditionParameter params;

    public ConditionAction(final ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ConditionParameter.class, "Condition parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getExpression()), "Condition expression cannot be empty.");
    }

    /**
     * Executes the condition action by evaluating the condition expression.
     *
     * @return ExecuteResult containing the condition evaluation result
     * @throws ActionException if condition evaluation fails
     */
    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        boolean flag = false;

        try {
            String expression = params.getExpression();
            if (StringUtils.isBlank(expression)) {
                throw new ActionException("Condition expression cannot be empty");
            }

            log.debug("({}) -> Evaluating condition expression: {}", getConfig().getId(), expression);
            flag = ConditionBuilder.getInstance().test(getInputParameter(), expression, params.isDebug());
            log.debug("({}) -> Condition evaluation result: {}", getConfig().getId(), flag);

        } catch (ActionException e) {
            setExecuteThrowable(e);
            throw e;
        } catch (Exception e) {
            ActionException actionException = new ActionException(
                    "Failed to evaluate condition expression '" + params.getExpression() + "': " + e.getMessage(), e);
            setExecuteThrowable(actionException);
            throw actionException;
        }

        executeResult.add(ACTION_CONDITION_STATE, flag);
        // The break flag is automatically determined by isBreak() method based on ACTION_CONDITION_STATE
        return executeResult;
    }

}
