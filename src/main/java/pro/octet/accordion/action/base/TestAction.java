package pro.octet.accordion.action.base;


import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.exceptions.ActionException;

public class TestAction extends AbstractAction {

    private final String name;

    public TestAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.name = actionConfig.getActionName();
    }

    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            System.out.println("Current execute action is " + name);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }
}
