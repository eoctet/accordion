package chat.octet.accordion.action.model;

import chat.octet.accordion.core.enums.DataType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Action output parameter model
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OutputParameter {
    private String name;

    private DataType dataType;

    private String desc;

    private Object defaultValue;

    private Object value;

    public OutputParameter(String name, DataType dataType, String desc, Object value) {
        this.name = name;
        this.dataType = dataType;
        this.desc = desc;
        this.value = value;
    }

    public OutputParameter(String name, DataType dataType, String desc) {
        this.name = name;
        this.dataType = dataType;
        this.desc = desc;
    }

    public OutputParameter() {
    }

}
