package chat.octet.accordion.action.model;

import chat.octet.accordion.core.enums.DataType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Action output parameter model.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OutputParameter implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String name;

    private DataType dataType;

    private String desc;

    private Object defaultValue;

    private Object value;

    public OutputParameter(final String name, final DataType dataType, final String desc, final Object value) {
        this.name = name;
        this.dataType = dataType;
        this.desc = desc;
        this.value = value;
    }

    public OutputParameter(final String name, final DataType dataType, final String desc) {
        this.name = name;
        this.dataType = dataType;
        this.desc = desc;
    }

    public OutputParameter() {
    }

}
