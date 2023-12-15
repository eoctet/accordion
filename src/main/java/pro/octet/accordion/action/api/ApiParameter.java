package pro.octet.accordion.action.api;

import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import pro.octet.accordion.core.enums.DataFormatType;
import pro.octet.accordion.core.enums.HttpMethod;

import javax.annotation.Nullable;
import java.net.Proxy;
import java.util.Map;


@Getter
@Builder
@ToString
@Jacksonized
public class ApiParameter {
    private String url;
    private HttpMethod method;
    @Builder.Default
    private Map<String, String> headers = Maps.newLinkedHashMap();
    @Builder.Default
    private Map<String, String> request = Maps.newLinkedHashMap();
    @Builder.Default
    private Map<String, String> form = Maps.newLinkedHashMap();
    @Nullable
    private String body;
    @Builder.Default
    private Long timeout = 1000L * 5;
    @Builder.Default
    private DataFormatType responseDataFormat = DataFormatType.JSON;
    @Builder.Default
    private boolean retryOnConnectionFailure = true;
    private Proxy.Type proxyType;
    private String proxyServerAddress;
    @Builder.Default
    private int proxyServerPort = -1;
}
