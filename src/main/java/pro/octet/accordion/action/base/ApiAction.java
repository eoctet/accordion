package pro.octet.accordion.action.base;


import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.exceptions.ActionException;

@Slf4j
public class ApiAction extends AbstractAction {

    public ApiAction(ActionConfig actionConfig) {
        super(actionConfig);
    }

    @Override
    public ActionResult execute() throws ActionException {
        return null;
    }
}
