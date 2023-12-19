package pro.octet.accordion.action.base;


import com.google.common.base.Preconditions;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.Condition;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.parameters.SwitchParameter;
import pro.octet.accordion.exceptions.ActionException;
import pro.octet.accordion.graph.entity.SwitchController;
import pro.octet.accordion.utils.CommonUtils;

public class SwitchAction extends AbstractAction {
    public static final String ACTION_SWITCH_CONTROL = "ACTION_SWITCH_CONTROL";
    private final SwitchParameter params;

    public SwitchAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(SwitchParameter.class, "Switch parameter cannot be null.");
        Preconditions.checkArgument(!CommonUtils.isEmpty(params.getBranches()), "Switch branches cannot be empty.");
    }

    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        SwitchController controller = new SwitchController();
        try {
            for (SwitchParameter.Branch branch : params.getBranches()) {
                String expression = branch.getCondition().getExpression();
                boolean flag = branch.isNegation() != Condition.test(getInputParameter(), expression);
                controller.put(branch.getActionId(), flag);
            }
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        actionResult.put(ACTION_SWITCH_CONTROL, controller);
        return actionResult;
    }
}
