package pro.octet.accordion.action;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.model.InputParameter;
import pro.octet.accordion.action.model.OutputParameter;
import pro.octet.accordion.core.entity.Message;
import pro.octet.accordion.core.entity.Session;
import pro.octet.accordion.core.enums.Constant;
import pro.octet.accordion.core.handler.DataTypeConvert;
import pro.octet.accordion.utils.CommonUtils;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class AbstractAction implements ActionService, Serializable {
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
        if (session.containsKey(Constant.ACCORDION_MESSAGE)) {
            Message message = (Message) session.get(Constant.ACCORDION_MESSAGE);
            inputParameter.putAll(message);
        }
        if (session.containsKey(Constant.PREV_ACTION_OUTPUT)) {
            List<OutputParameter> prevComponentOutput = (List<OutputParameter>) session.get(Constant.PREV_ACTION_OUTPUT);
            prevComponentOutput.forEach(param -> inputParameter.put(param.getName(), param.getValue()));
        }
        return this;
    }

    @Override
    public void updateOutput(ActionResult actionResult) {
        this.session.remove(Constant.PREV_ACTION_OUTPUT);

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
            this.session.put(Constant.PREV_ACTION_OUTPUT, result);
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
}
