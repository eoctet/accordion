package pro.octet.accordion.action.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.List;


@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionConfig {

    @JsonProperty("id")
    private String id;

    @JsonProperty("action_type")
    private ActionType actionType;

    @JsonProperty("action_name")
    private String actionName;

    @JsonProperty("action_desc")
    private String actionDesc;

    @JsonProperty("action_params")
    private Object actionParams;

    @JsonProperty("action_output")
    private List<OutputParameter> actionOutput;

    public <T> T getActionParams(Class<T> clazz) {
        if (actionParams instanceof LinkedHashMap) {
            actionParams = JsonUtils.parseToObject(JsonUtils.toJson(actionParams), clazz);
        }
        if (actionParams != null && actionParams.getClass() == clazz) {
            return clazz.cast(actionParams);
        }
        return null;
    }

}
