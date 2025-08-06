package chat.octet.accordion.graph.model;

import chat.octet.accordion.core.enums.Constant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccordionConfig implements Serializable {

    private String id;

    private String name;

    private String desc;

    private AccordionGraphConfig graphConfig;

    @JsonFormat(pattern = Constant.DATE_FORMAT_WITH_TIME)
    private LocalDateTime updatetime;

    public AccordionConfig(String id, String name, String desc, AccordionGraphConfig graphConfig, LocalDateTime updatetime) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.graphConfig = graphConfig;
        this.updatetime = updatetime;
    }

    public AccordionConfig() {
    }
}
