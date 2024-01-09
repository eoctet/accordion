package chat.octet.accordion.action;

import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ActionResult;
import chat.octet.accordion.action.model.InputParameter;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.entity.Session;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AbstractAction.
 * This is an abstract class, all Action's implementation classes need to inherit this class.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Slf4j
public abstract class AbstractAction implements ActionService, Serializable {
    public static final String ACCORDION_MESSAGE = "ACCORDION_MESSAGE";
    public static final String PREV_ACTION_OUTPUT = "PREV_ACTION_OUTPUT";
    private final ActionConfig actionConfig;
    private final InputParameter inputParameter;
    private Session session;
    private final AtomicReference<Throwable> executeThrowable = new AtomicReference<>();

    public AbstractAction(ActionConfig actionConfig) {
        this.actionConfig = actionConfig;
        this.inputParameter = new InputParameter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionService prepare(Session session) {
        this.session = session;
        this.executeThrowable.set(null);
        this.inputParameter.clear();
        if (session.containsKey(ACCORDION_MESSAGE)) {
            Message message = (Message) session.get(ACCORDION_MESSAGE);
            inputParameter.putAll(message);
        }
        if (session.containsKey(PREV_ACTION_OUTPUT)) {
            List<OutputParameter> prevActionOutput = (List<OutputParameter>) session.get(PREV_ACTION_OUTPUT);
            prevActionOutput.forEach(param -> inputParameter.put(param.getName(), param.getValue()));
        }
        return this;
    }

    @Override
    public void updateOutput(ActionResult actionResult) {
        this.session.remove(PREV_ACTION_OUTPUT);

        List<OutputParameter> output = getActionOutput();
        if (!CommonUtils.isEmpty(output)) {
            List<OutputParameter> result = Lists.newArrayList();
            output.forEach(param -> {
                String key = param.getName();
                Object value = param.getValue();
                if (actionResult.containsKey(key)) {
                    value = DataTypeConvert.getValue(param.getDatatype(), actionResult.get(key));
                }
                if (value != null) {
                    result.add(new OutputParameter(param.getName(), param.getDatatype(), param.getDesc(), value));
                }
            });
            this.session.put(PREV_ACTION_OUTPUT, result);
        }
    }

    @Override
    public boolean checkError() {
        Throwable cause = this.executeThrowable.get();
        if (cause != null) {
            log.error(MessageFormat.format("Execute action error, action id: {0}, action name: {1}.", actionConfig.getId(), actionConfig.getActionName()), cause);
            return true;
        }
        return false;
    }

    @Override
    public ActionConfig getConfig() {
        return this.actionConfig;
    }

    protected void setExecuteThrowable(Throwable throwable) {
        this.executeThrowable.compareAndSet(null, throwable);
    }

    protected InputParameter getInputParameter() {
        return inputParameter;
    }

    protected List<OutputParameter> getActionOutput() {
        return getConfig().getActionOutput();
    }

    @SuppressWarnings("unchecked")
    protected void findOutputParameters(List<OutputParameter> outputParameter, LinkedHashMap<String, Object> responseMaps, ActionResult actionResult) {
        responseMaps.forEach((key, value) -> {
            outputParameter.forEach(parameter -> {
                if (parameter.getName().equalsIgnoreCase(key)) {
                    actionResult.put(parameter.getName(), DataTypeConvert.getValue(parameter.getDatatype(), value));
                }
            });
            if (value instanceof LinkedHashMap) {
                findOutputParameters(outputParameter, (LinkedHashMap<String, Object>) value, actionResult);
            }
            if (value instanceof ArrayList) {
                ((ArrayList<?>) value).forEach(e -> {
                    if (e instanceof LinkedHashMap) {
                        findOutputParameters(outputParameter, (LinkedHashMap<String, Object>) e, actionResult);
                    }
                });
            }
        });
    }
}
