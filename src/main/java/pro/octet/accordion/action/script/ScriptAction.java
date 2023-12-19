package pro.octet.accordion.action.script;


import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.googlecode.aviator.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.model.OutputParameter;
import pro.octet.accordion.action.parameters.ScriptParameter;
import pro.octet.accordion.core.enums.DataType;
import pro.octet.accordion.exceptions.ActionException;
import pro.octet.accordion.utils.CommonUtils;

import java.util.List;

@Slf4j
public class ScriptAction extends AbstractAction {
    public static final String ACTION_SCRIPT_RESULT = "ACTION_SCRIPT_RESULT";
    private final ScriptParameter params;
    private final AviatorEvaluatorInstance evaluator;

    public ScriptAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ScriptParameter.class, "Script parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getScript()), "Script cannot be empty.");
        //init AviatorEvaluator and config
        evaluator = AviatorEvaluator.newInstance();
        evaluator.setOption(Options.MAX_LOOP_COUNT, Integer.MAX_VALUE);
        evaluator.setOption(Options.SERIALIZABLE, true);
        evaluator.setOption(Options.ALLOWED_CLASS_SET, Sets.newHashSet());
        evaluator.disableFeature(Feature.NewInstance);
        evaluator.disableFeature(Feature.Module);
        evaluator.disableFeature(Feature.InternalVars);
        evaluator.setOption(Options.TRACE_EVAL, params.isDebug());
        //if no output parameters are set, the default value is used
        List<OutputParameter> outputConfig = getActionOutput();
        if (CommonUtils.isEmpty(outputConfig)) {
            outputConfig.add(new OutputParameter(ACTION_SCRIPT_RESULT, DataType.STRING, "Script default result"));
        }
    }

    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            Expression exp = evaluator.compile(params.getScriptId(), params.getScript(), true);
            Object result = exp.execute(getInputParameter());
            if (result != null) {
                OutputParameter outputParameter = getActionOutput().stream().findFirst().get();
                actionResult.put(outputParameter.getName(), result);
            }
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }
}
