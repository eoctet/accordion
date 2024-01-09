package chat.octet.accordion.action.model;

import chat.octet.accordion.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * Action Config model.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
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
    @Setter
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

    public <T> T getActionParams(Class<T> clazz, String errorMessage) {
        T object = getActionParams(clazz);
        if (object == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return object;
    }

}
