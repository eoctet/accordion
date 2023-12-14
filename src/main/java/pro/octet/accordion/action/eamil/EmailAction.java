package pro.octet.accordion.action.eamil;


import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.exceptions.ActionException;

public class EmailAction extends AbstractAction {

    public EmailAction(ActionConfig actionConfig) {
        super(actionConfig);
    }

    @Override
    public ActionResult execute() throws ActionException {
        return null;
    }
}
