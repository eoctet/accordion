package pro.octet.accordion.action.eamil;


import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.parameters.EmailParameter;
import pro.octet.accordion.exceptions.ActionException;
import pro.octet.accordion.utils.CommonUtils;
import pro.octet.accordion.utils.EmailUtils;

@Slf4j
public class EmailAction extends AbstractAction {

    private final EmailParameter params;

    public EmailAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(EmailParameter.class);
    }

    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            String content = CommonUtils.parameterFormat(getInputParameter(), params.getContent());
            String status = EmailUtils.send(params, content);
            log.debug("The email has been sent, status: " + status);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }
}
