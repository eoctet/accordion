package pro.octet.accordion.action.base;


import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.parameters.ConditionParameter;
import pro.octet.accordion.core.condition.ConditionBuilder;
import pro.octet.accordion.exceptions.ActionException;

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
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        boolean flag = false;

        try {
            String expression = params.getExpression();
            flag = ConditionBuilder.getInstance().test(getInputParameter(), expression, params.isDebug());
            log.debug("Condition action execution result: " + flag);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        actionResult.put(ACTION_CONDITION_STATE, flag);
        return actionResult;
    }
}
