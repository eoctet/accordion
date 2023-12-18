package pro.octet.accordion.action.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
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
    private String actionType;

    @JsonProperty("action_name")
    private String actionName;

    @JsonProperty("action_desc")
    private String actionDesc;

    @JsonProperty("action_params")
    private Object actionParams;

    @JsonProperty("action_output")
    @Builder.Default
    private List<OutputParameter> actionOutput = Lists.newArrayList();

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
