package pro.octet.accordion.action.model;


import pro.octet.accordion.core.entity.Tuple;
import pro.octet.accordion.utils.JsonUtils;

/**
 * Action result.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
public class ActionResult extends Tuple<String, Object> {

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
