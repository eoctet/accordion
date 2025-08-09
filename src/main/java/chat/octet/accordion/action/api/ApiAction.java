package chat.octet.accordion.action.api;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.InputParameter;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.core.enums.HttpMethod;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.accordion.utils.JsonUtils;
import chat.octet.accordion.utils.XmlParser;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static chat.octet.accordion.core.enums.DataFormatType.JSON;
import static chat.octet.accordion.core.enums.DataFormatType.XML;

/**
 * ApiAction Supports calling third-party Restful APIs.
 * supports requests and responses in JSON and XML data formats, and also supports the use of proxy services.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ApiParameter
 */
@Slf4j
public class ApiAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private static final int MAX_RESPONSE_PREVIEW_LENGTH = 200;

    private final transient OkHttpClient client;
    private final transient ApiParameter params;

    public ApiAction(final ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ApiParameter.class, "API parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getUrl()), "Request url cannot be empty.");
        if (HttpMethod.GET != params.getMethod()) {
            Preconditions.checkArgument(StringUtils.isNotBlank(params.getBody()), "Request body cannot be empty.");
        }
        log.debug("Create API action, parameters: {}.", JsonUtils.toJson(params));
        Proxy proxyServer = null;
        if (StringUtils.isNotBlank(params.getProxyServerAddress()) && params.getProxyServerPort() != -1) {
            proxyServer = new Proxy(params.getProxyType(),
                    new InetSocketAddress(params.getProxyServerAddress(), params.getProxyServerPort()));
            log.debug("Enable proxy service support, proxy server address: {}.",
                    StringUtils.join(params.getProxyServerAddress(), ":", params.getProxyServerPort()));
        }
        this.client = new OkHttpClient().newBuilder()
                .proxy(proxyServer)
                .callTimeout(params.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(params.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(params.getTimeout(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(params.isRetryOnConnectionFailure())
                .build();
    }

    private MediaType getMediaType() {
        String contentType = params.getHeaders().get("Content-Type");
        if (StringUtils.isBlank(contentType)) {
            //Set it according to the response data format
            if (params.getResponseDataFormat() == JSON) {
                contentType = "application/json";
            } else if (params.getResponseDataFormat() == XML) {
                contentType = "application/xml";
            } else {
                contentType = "";
                log.warn("Request header [Content-Type] is not specified.");
            }
        }
        return MediaType.parse(contentType);
    }

    private String exchange(final InputParameter inputParameter) throws IOException {
        //Set request headers
        Map<String, String> headersMaps = Maps.newHashMap();
        if (!params.getHeaders().isEmpty()) {
            params.getHeaders().forEach((key, value) -> headersMaps.put(key,
                    StringSubstitutor.replace(value, inputParameter)));
        }
        Headers headers = Headers.of(headersMaps);
        //Set request params
        String url = StringSubstitutor.replace(params.getUrl(), inputParameter);
        HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
        if (!params.getRequest().isEmpty()) {
            params.getRequest().forEach((key, value) -> urlBuilder.addQueryParameter(key,
                    StringSubstitutor.replace(value, inputParameter)));
        }
        HttpUrl httpUrl = urlBuilder.build();
        //Set request body
        RequestBody body = null;
        if (StringUtils.isNotBlank(params.getBody())) {
            MediaType mediaType = getMediaType();
            String bodyStr = StringSubstitutor.replace(params.getBody(), inputParameter);
            body = RequestBody.create(bodyStr, mediaType);
        }
        log.debug("Request url: {}, request headers: {}, request body: {}.", httpUrl.url(), headersMaps, body);
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .headers(headers)
                .method(params.getMethod().name(), body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }

    /**
     * Executes the API call and returns the result.
     *
     * @return the execution result containing API response data
     * @throws ActionException if the API call fails
     */
    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            String responseBody = exchange(getInputParameter());

            if (StringUtils.isBlank(responseBody)) {
                log.warn("({}) -> Response result is empty, please check if the API is normal.", getConfig().getId());
                return executeResult;
            }

            LinkedHashMap<String, Object> responseMaps = null;
            try {
                switch (params.getResponseDataFormat()) {
                    case JSON:
                        responseMaps = JsonUtils.parseJsonToMap(responseBody, String.class, Object.class);
                        break;
                    case XML:
                        responseMaps = XmlParser.parseXmlToMap(responseBody);
                        break;
                    default:
                        log.warn("({}) -> Unsupported response data format: {}",
                                getConfig().getId(), params.getResponseDataFormat());
                        return executeResult;
                }
            } catch (Exception parseException) {
                throw new ActionException("Failed to parse response body as "
                        + params.getResponseDataFormat() + ": " + parseException.getMessage(), parseException);
            }

            if (responseMaps == null) {
                throw new ActionException("Parsed response is null - response body: "
                        + (responseBody.length() > MAX_RESPONSE_PREVIEW_LENGTH
                        ? responseBody.substring(0, MAX_RESPONSE_PREVIEW_LENGTH) + "..." : responseBody));
            }

            List<OutputParameter> outputParameter = getActionOutput();
            if (!CommonUtils.isEmpty(outputParameter)) {
                executeResult.findAndAddParameters(outputParameter, responseMaps);
            }

            log.debug("({}) -> API call completed successfully, response size: {} characters",
                    getConfig().getId(), responseBody.length());

        } catch (ActionException e) {
            setExecuteThrowable(e);
            throw e;
        } catch (Exception e) {
            ActionException actionException = new ActionException("API action execution failed: " + e.getMessage(), e);
            setExecuteThrowable(actionException);
            throw actionException;
        }
        return executeResult;
    }

    /**
     * Closes the API action and releases any resources.
     * The OkHttpClient manages its own connection pool and doesn't require explicit closing.
     */
    @Override
    public void close() {
        // Close HTTP client if needed
        // Note: OkHttpClient manages its own connection pool and doesn't need explicit closing
        // But we could add cleanup logic here if needed
        super.close();
    }

}
