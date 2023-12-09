package pro.octet.accordion.action.base;


import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.exceptions.ActionException;

public class SwitchAction extends AbstractAction {

    public SwitchAction(ActionConfig actionConfig) {
        super(actionConfig);
    }

    @Override
    public ActionResult execute() throws ActionException {
        return null;
    }
}
