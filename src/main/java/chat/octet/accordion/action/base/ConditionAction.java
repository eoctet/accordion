package chat.octet.accordion.action.base;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.exceptions.ActionException;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ConditionAction is similar to the "if" in Java, used to control the execution process.
 * When the conditional judgment is not true, the execution will be interrupted.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ConditionParameter
 */
@Slf4j
public class ConditionAction extends AbstractAction {
    public static final String ACTION_CONDITION_STATE = "ACTION_CONDITION_STATE";
    private final ConditionParameter params;

    public ConditionAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ConditionParameter.class, "Condition parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getExpression()), "Condition expression cannot be empty.");
    }

    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        boolean flag = false;

        try {
            String expression = params.getExpression();
            flag = ConditionBuilder.getInstance().test(getInputParameter(), expression, params.isDebug());
            log.debug("Condition action execution result: " + flag);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        executeResult.add(ACTION_CONDITION_STATE, flag);
        return executeResult;
    }
}
