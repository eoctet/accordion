package pro.octet.accordion.action.base;


import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.Condition;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.parameters.ConditionParameter;
import pro.octet.accordion.core.enums.Constant;
import pro.octet.accordion.exceptions.ActionException;

public class ConditionAction extends AbstractAction {

    private final ConditionParameter params;

    public ConditionAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ConditionParameter.class);
    }

    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        boolean flag = false;

        try {
            String expression = params.getExpression();
            flag = Condition.test(getInputParameter(), expression);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        actionResult.put(Constant.ACTION_CONDITION_STATE, flag);
        return actionResult;
    }
}
