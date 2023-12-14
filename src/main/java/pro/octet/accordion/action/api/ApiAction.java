package pro.octet.accordion.action.api;


import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.exceptions.ActionException;


@Slf4j
public class ApiAction extends AbstractAction {

    private final ApiParams apiParams;

    public ApiAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.apiParams = (ApiParams) actionConfig.getActionParams();
    }


    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {

        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }

}
