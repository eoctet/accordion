package pro.octet.accordion.action.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pro.octet.accordion.core.enums.DataType;

/**
 * Action output parameter model
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputParameter {
    @JsonProperty("name")
    private String name;

    @JsonProperty("datatype")
    private DataType datatype;

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("default_value")
    private Object defaultValue;

    private Object value;

    public OutputParameter(String name, DataType datatype, String desc, Object value) {
        this.name = name;
        this.datatype = datatype;
        this.desc = desc;
        this.value = value;
    }

    public OutputParameter(String name, DataType datatype, String desc) {
        this.name = name;
        this.datatype = datatype;
        this.desc = desc;
    }

    public OutputParameter() {
    }

}
