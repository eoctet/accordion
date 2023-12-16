package pro.octet.accordion.graph.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pro.octet.accordion.core.enums.Constant;

import java.io.Serializable;
import java.util.Date;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccordionConfig implements Serializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("graph_config")
    private AccordionGraphConfig graphConfig;

    @JsonProperty("updatetime")
    @JsonFormat(pattern = Constant.DATE_FORMAT_WITH_TIME, timezone = "GMT+8")
    private Date updatetime;

    public AccordionConfig(String id, String name, String desc, AccordionGraphConfig graphConfig, Date updatetime) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.graphConfig = graphConfig;
        this.updatetime = updatetime;
    }

    public AccordionConfig() {
    }
}
