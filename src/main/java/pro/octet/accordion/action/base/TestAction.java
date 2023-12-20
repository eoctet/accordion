package pro.octet.accordion.action.base;


import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.model.OutputParameter;
import pro.octet.accordion.exceptions.ActionException;
import pro.octet.accordion.utils.CommonUtils;
import pro.octet.accordion.utils.JsonUtils;

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
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            List<OutputParameter> outputParameter = getActionOutput();
            if (!CommonUtils.isEmpty(outputParameter)) {
                findOutputParameters(outputParameter, Maps.newLinkedHashMap(getInputParameter()), actionResult);
            }
            log.info("Current execute action: {}, action input parameters: {}, action result: {}.", name, getInputParameter(), actionResult);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }
}
