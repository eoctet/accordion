package pro.octet.accordion.action.shell;


import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.exceptions.ActionException;

public class ShellAction extends AbstractAction {

    public ShellAction(ActionConfig actionConfig) {
        super(actionConfig);
    }

    @Override
    public ActionResult execute() throws ActionException {
        return null;
    }
}
