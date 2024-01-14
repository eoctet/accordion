package chat.octet.accordion.action.model;


import chat.octet.accordion.action.base.ConditionAction;
import chat.octet.accordion.action.base.SwitchAction;
import chat.octet.accordion.core.entity.Tuple;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.graph.entity.SwitchFilter;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Action execution result.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
public class ExecuteResult implements Serializable {
    private final Tuple<String, Object> result;

    public ExecuteResult() {
        this.result = new Tuple<>();
    }

    public boolean contains(String key) {
        return this.result.containsKey(key);
    }

    public ExecuteResult add(String key, Object value) {
        this.result.put(key, value);
        return this;
    }

    public Object getValue(String key) {
        return this.result.get(key);
    }

    public <T> T getValue(String key, Class<T> clazz) {
        return clazz.cast(this.result.get(key));
    }

    public boolean isBreak() {
        if (contains(ConditionAction.ACTION_CONDITION_STATE)) {
            return !this.result.getBoolean(ConditionAction.ACTION_CONDITION_STATE);
        }
        return false;
    }

    public SwitchFilter getSwitchFilter() {
        if (contains(SwitchAction.ACTION_SWITCH_CONTROL)) {
            return (SwitchFilter) this.result.get(SwitchAction.ACTION_SWITCH_CONTROL);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void findAndAddParameters(List<OutputParameter> outputParameter, Map<String, Object> result) {
        result.forEach((key, value) -> {
            outputParameter.forEach(parameter -> {
                if (parameter.getName().equalsIgnoreCase(key)) {
                    this.add(parameter.getName(), DataTypeConvert.getValue(parameter.getDataType(), value));
                }
            });
            if (value instanceof Map) {
                findAndAddParameters(outputParameter, (Map<String, Object>) value);
            }
            if (value instanceof List) {
                ((List<?>) value).forEach(e -> {
                    if (e instanceof Map) {
                        findAndAddParameters(outputParameter, (Map<String, Object>) e);
                    }
                });
            }
        });
    }

    public void clear() {
        this.result.clear();
    }

    @Override
    public String toString() {
        return this.result.toString();
    }
}
