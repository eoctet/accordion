package chat.octet.accordion.action.base;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.accordion.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * TestAction is a special actions used for testing,
 * which do not contain specific execution logic and only output parameters and status through logs.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Slf4j
public class TestAction extends AbstractAction {

    private final String name;

    public TestAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.name = actionConfig.getActionName();
        log.info("Create new action, action id: {}, name: {}, parameters: {}.",
                actionConfig.getId(), actionConfig.getActionName(), JsonUtils.toJson(actionConfig.getActionParams())
        );
    }

    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            List<OutputParameter> outputParameter = getActionOutput();
            if (!CommonUtils.isEmpty(outputParameter)) {
                executeResult.findAndAddParameters(outputParameter, getInputParameter().toMap());
            }
            log.info("Current execute action: {}, action input parameters: {}, action result: {}.", name, getInputParameter(), executeResult);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }
}
