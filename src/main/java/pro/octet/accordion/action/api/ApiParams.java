package pro.octet.accordion.action.api;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import pro.octet.accordion.core.entity.HttpParam;
import pro.octet.accordion.core.enums.DataFormatType;
import pro.octet.accordion.core.enums.HttpMethod;

import java.util.List;


@Getter
@Builder
@ToString
@Jacksonized
public class ApiParams {
    private String url;
    private HttpMethod method;
    private List<HttpParam> headers;
    private List<HttpParam> request;
    private String body;
    @Builder.Default
    private Long timeout = 1000L * 3;
    @Builder.Default
    private DataFormatType responseDataFormat = DataFormatType.JSON;

}
