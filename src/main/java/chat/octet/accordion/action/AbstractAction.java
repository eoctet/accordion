package chat.octet.accordion.action;

import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.InputParameter;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.entity.Session;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.accordion.utils.JsonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.text.MessageFormat;
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
    private final String actionId;

    public AbstractAction(ActionConfig actionConfig) {
        this.actionConfig = actionConfig;
        this.inputParameter = new InputParameter();
        this.actionId = actionConfig.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionService prepare(Session session) {
        this.session = session;
        this.executeThrowable.set(null);
        this.inputParameter.clear();
        if (session.containsKey(ACCORDION_MESSAGE)) {
            Message message = session.getValue(ACCORDION_MESSAGE, Message.class);
            inputParameter.putAll(message);
            log.debug("({}) -> Find the message in the action session, update them as input parameters, message: {}.", actionId, message);
        }
        if (session.containsKey(PREV_ACTION_OUTPUT)) {
            List<OutputParameter> prevActionOutput = session.getValue(PREV_ACTION_OUTPUT, List.class);
            prevActionOutput.forEach(param -> inputParameter.put(param.getName(), param.getValue()));
            log.debug("({}) -> Find the prev-action output in the action session, update them as input parameters, output: {}.", actionId, JsonUtils.toJson(prevActionOutput));
        }
        if (!session.getGlobal().isEmpty()) {
            inputParameter.putAll(session.getGlobal());
            log.debug("({}) -> Find the global parameters, update them as input parameters, global: {}.", actionId, session.getGlobal());
        }
        return this;
    }

    @Override
    public void output(ExecuteResult executeResult) {
        this.session.remove(PREV_ACTION_OUTPUT);

        List<OutputParameter> output = getActionOutput();
        if (!CommonUtils.isEmpty(output)) {
            List<OutputParameter> result = Lists.newArrayList();
            output.forEach(param -> {
                String key = param.getName();
                Object value = param.getValue();
                if (executeResult.contains(key)) {
                    value = DataTypeConvert.getValue(param.getDataType(), executeResult.getValue(key));
                }
                if (value != null) {
                    result.add(new OutputParameter(param.getName(), param.getDataType(), param.getDesc(), value));
                }
            });
            this.session.add(PREV_ACTION_OUTPUT, result);
            log.debug("({}) -> Update output into the action session, output: {}.", actionId, JsonUtils.toJson(result));
        }
    }

    @Override
    public boolean checkError() {
        Throwable cause = this.executeThrowable.get();
        if (cause != null) {
            log.error(MessageFormat.format("({0}) -> Execution action error.", actionId), cause);
            return true;
        }
        return false;
    }

    @Override
    public ActionConfig getConfig() {
        return this.actionConfig;
    }

    @Override
    public void close() {
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

}
