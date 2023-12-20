package pro.octet.accordion.action.parameters;

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


/**
 * Api action parameter.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
@Getter
@Builder
@ToString
@Jacksonized
public class ApiParameter {
    /**
     * Request url.
     */
    private String url;
    /**
     * Request method.
     *
     * @see HttpMethod
     */
    @Builder.Default
    private HttpMethod method = HttpMethod.GET;
    /**
     * Request headers.
     */
    @Builder.Default
    private Map<String, String> headers = Maps.newLinkedHashMap();
    /**
     * Request query parameters.
     */
    @Builder.Default
    private Map<String, String> request = Maps.newLinkedHashMap();
    /**
     * Request form parameters.
     */
    @Builder.Default
    private Map<String, String> form = Maps.newLinkedHashMap();
    /**
     * Request body, supports JSON and XML data format.
     */
    @Nullable
    private String body;
    /**
     * Request timeout period (ms), default value: 5000 ms.
     */
    @Builder.Default
    private Long timeout = 1000L * 5;
    /**
     * Response data format, supports JSON and XML data format.
     * default value: JSON.
     *
     * @see DataFormatType
     */
    @Builder.Default
    private DataFormatType responseDataFormat = DataFormatType.JSON;
    /**
     * Whether to retry on connection failure.
     */
    @Builder.Default
    private boolean retryOnConnectionFailure = true;
    /**
     * Proxy type: DIRECT/HTTP/SOCKS.
     */
    private Proxy.Type proxyType;
    /**
     * Proxy server address.
     */
    private String proxyServerAddress;
    /**
     * Proxy server port.
     */
    @Builder.Default
    private int proxyServerPort = -1;
}
