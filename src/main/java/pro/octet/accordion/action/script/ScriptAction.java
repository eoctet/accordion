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

import java.util.List;

@Slf4j
public class ScriptAction extends AbstractAction {
    public static final String ACTION_SCRIPT_RESULT = "ACTION_SCRIPT_RESULT";
    private final ScriptParameter params;
    private final AviatorEvaluatorInstance evaluator;

    public ScriptAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ScriptParameter.class);
        Preconditions.checkNotNull(params, "Script parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getScript()), "Script cannot be empty.");
        evaluator = AviatorEvaluator.newInstance();
        evaluator.setOption(Options.MAX_LOOP_COUNT, Integer.MAX_VALUE);
        evaluator.setOption(Options.SERIALIZABLE, true);
        evaluator.setOption(Options.ALLOWED_CLASS_SET, Sets.newHashSet());
        evaluator.disableFeature(Feature.NewInstance);
        evaluator.disableFeature(Feature.Module);
        evaluator.disableFeature(Feature.InternalVars);
        evaluator.setOption(Options.TRACE_EVAL, params.isDebug());
    }

    @Override
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            Expression exp = evaluator.compile(params.getScriptId(), params.getScript(), true);
            Object result = exp.execute(getInputParameter());
            if (result != null) {
                List<OutputParameter> outputConfig = getActionOutput();
                OutputParameter outputParameter = outputConfig.stream().findFirst()
                        .orElse(new OutputParameter(ACTION_SCRIPT_RESULT, DataType.STRING, "Script default result"));
                outputParameter.setValue(result);
                actionResult.put(outputParameter.getName(), outputParameter.getValue());
            }
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }
}
