package chat.octet.accordion.action.script;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.core.enums.DataType;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Feature;
import com.googlecode.aviator.Options;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ScriptAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public static final String ACTION_SCRIPT_RESULT = "ACTION_SCRIPT_RESULT";
    private final transient ScriptParameter params;
    private final transient AviatorEvaluatorInstance evaluator;

    public ScriptAction(final ActionConfig actionConfig) {
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
        List<OutputParameter> outputConfig = Optional.ofNullable(actionConfig.getActionOutput()).orElse(Lists.newArrayList());
        if (CommonUtils.isEmpty(outputConfig)) {
            outputConfig.add(new OutputParameter(ScriptAction.ACTION_SCRIPT_RESULT, DataType.STRING, "Script default result"));
            actionConfig.setActionOutput(outputConfig);
        }
    }

    /**
     * Executes the script and returns the result.
     *
     * @return the execution result containing script output and status
     * @throws ActionException if the script execution fails
     */
    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            Expression exp = evaluator.compile(params.getScriptId(), params.getScript(), true);
            Object result = exp.execute(getInputParameter());
            if (result != null) {
                OutputParameter outputParameter = getActionOutput().stream().findFirst().get();
                executeResult.add(outputParameter.getName(), result);
            }
            log.debug("Script action execution result: {}", result);
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }
}
