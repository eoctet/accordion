package chat.octet.accordion.action.api;


import chat.octet.accordion.action.AbstractAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.InputParameter;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.action.parameters.ApiParameter;
import chat.octet.accordion.core.enums.HttpMethod;
import chat.octet.accordion.exceptions.ActionException;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.accordion.utils.JsonUtils;
import chat.octet.accordion.utils.XmlParser;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
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

    private final OkHttpClient client;
    private final ApiParameter params;

    public ApiAction(ActionConfig actionConfig) {
        super(actionConfig);
        this.params = actionConfig.getActionParams(ApiParameter.class, "API parameter cannot be null.");
        Preconditions.checkArgument(StringUtils.isNotBlank(params.getUrl()), "Request url cannot be empty.");
        if (HttpMethod.GET != params.getMethod()) {
            Preconditions.checkArgument(StringUtils.isNotBlank(params.getBody()), "Request body cannot be empty.");
        }
        log.debug("Create API action, parameters: {}.", JsonUtils.toJson(params));
        Proxy proxyServer = null;
        if (StringUtils.isNotBlank(params.getProxyServerAddress()) && params.getProxyServerPort() != -1) {
            proxyServer = new Proxy(params.getProxyType(), new InetSocketAddress(params.getProxyServerAddress(), params.getProxyServerPort()));
            log.debug("Enable proxy service support, proxy server address: {}.", StringUtils.join(params.getProxyServerAddress(), ":", params.getProxyServerPort()));
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

    private String exchange(InputParameter inputParameter) throws IOException {
        //Set request headers
        Map<String, String> headersMaps = Maps.newHashMap();
        if (!params.getHeaders().isEmpty()) {
            params.getHeaders().forEach((key, value) -> headersMaps.put(key, StringSubstitutor.replace(value, inputParameter)));
        }
        Headers headers = Headers.of(headersMaps);
        //Set request params
        String url = StringSubstitutor.replace(params.getUrl(), inputParameter);
        HttpUrl.Builder urlBuilder = HttpUrl.get(url).newBuilder();
        if (!params.getRequest().isEmpty()) {
            params.getRequest().forEach((key, value) -> urlBuilder.addQueryParameter(key, StringSubstitutor.replace(value, inputParameter)));
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

    @Override
    public ExecuteResult execute() throws ActionException {
        ExecuteResult executeResult = new ExecuteResult();
        try {
            String responseBody = exchange(getInputParameter());
            // System.out.println("responseBody: " + responseBody);
            if (StringUtils.isBlank(responseBody)) {
                log.warn("Response result is empty, please check if the API is normal.");
                return executeResult;
            }
            LinkedHashMap<String, Object> responseMaps = null;
            switch (params.getResponseDataFormat()) {
                case JSON:
                    responseMaps = JsonUtils.parseJsonToMap(responseBody, String.class, Object.class);
                    break;
                case XML:
                    responseMaps = XmlParser.parseXmlToMap(responseBody);
                    break;
            }
            if (responseMaps == null) {
                throw new IllegalArgumentException("Parse response string to map failed, the response data is empty");
            }
            List<OutputParameter> outputParameter = getActionOutput();
            if (!CommonUtils.isEmpty(outputParameter)) {
                executeResult.findAndAddParameters(outputParameter, responseMaps);
            }
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return executeResult;
    }

}
