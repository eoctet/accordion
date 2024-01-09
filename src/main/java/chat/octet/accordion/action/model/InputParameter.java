package chat.octet.accordion.action.model;


import chat.octet.accordion.core.entity.Tuple;
import chat.octet.accordion.utils.JsonUtils;

/**
 * Action input parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
public class InputParameter extends Tuple<String, Object> {
    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
