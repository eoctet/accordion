package pro.octet.accordion.action.model;


import pro.octet.accordion.core.entity.Tuple;
import pro.octet.accordion.utils.JsonUtils;

public class InputParameter extends Tuple<String, Object> {
    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
