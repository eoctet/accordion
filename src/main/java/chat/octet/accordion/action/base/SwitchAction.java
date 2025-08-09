package chat.octet.accordion.action.base;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.condition.ConditionBuilder;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.graph.entity.SwitchFilter;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

/**
 * SwitchAction is a combination of multiple sets of conditions used to control multiple different execution chains,
 * suitable for execution scenarios of multiple independent tasks.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see SwitchParameter
 */
@Slf4j
public class SwitchAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public static final String ACTION_SWITCH_CONTROL = "ACTION_SWITCH_CONTROL";
    private final transient SwitchParameter params;

    public SwitchAction(final ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(SwitchParameter.class, "Switch parameter cannot be null.");
        Preconditions.checkArgument(!CommonUtils.isEmpty(params.getBranches()), "Switch branches cannot be empty.");
    }

    /**
     * Executes the switch action by evaluating all branch conditions.
     *
     * @return ExecuteResult containing the switch control filter
     * @throws ActionException if condition evaluation fails
     */
    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        SwitchFilter controller = new SwitchFilter();
        try {
            for (SwitchParameter.Branch branch : params.getBranches()) {
                boolean flag = branch.isNegation() != ConditionBuilder.getInstance().test(getInputParameter(), branch.getExpression(), params.isDebug());
                controller.put(branch.getActionId(), flag);
            }
            log.debug("Switch action execution result: " + controller);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        executeResult.add(ACTION_SWITCH_CONTROL, controller);
        return executeResult;
    }

}
