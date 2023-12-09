package pro.octet.accordion.flow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pro.octet.accordion.core.enums.Constant;

import java.io.Serializable;
import java.util.Date;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowConfig implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("graph_config")
    private FlowGraphConfig flowGraphConfig;

    @JsonProperty("updatetime")
    @JsonFormat(pattern = Constant.DATE_FORMAT, timezone = "GMT+8")
    private Date updatetime;

}
