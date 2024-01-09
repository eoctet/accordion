package chat.octet.accordion.action.model;


import chat.octet.accordion.action.base.ConditionAction;
import chat.octet.accordion.action.base.SwitchAction;
import chat.octet.accordion.core.entity.Tuple;
import chat.octet.accordion.graph.entity.SwitchFilter;
import lombok.Getter;

/**
 * Action execution result.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
public class ExecuteResult {
    private final Tuple<String, Object> result;

    public ExecuteResult() {
        this.result = new Tuple<>();
    }

    public boolean containsKey(String key) {
        return this.result.containsKey(key);
    }

    public ExecuteResult add(String key, Object value) {
        this.result.put(key, value);
        return this;
    }

    public Object getValue(String key) {
        return this.result.get(key);
    }

    public boolean isBreak() {
        if (containsKey(ConditionAction.ACTION_CONDITION_STATE)) {
            return !this.result.getBoolean(ConditionAction.ACTION_CONDITION_STATE);
        }
        return false;
    }

    public SwitchFilter getSwitchFilter() {
        if (containsKey(SwitchAction.ACTION_SWITCH_CONTROL)) {
            return (SwitchFilter) this.result.get(SwitchAction.ACTION_SWITCH_CONTROL);
        }
        return null;
    }

}
