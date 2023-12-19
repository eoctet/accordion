package pro.octet.accordion.action.api;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import pro.octet.accordion.action.AbstractAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.action.model.InputParameter;
import pro.octet.accordion.action.model.OutputParameter;
import pro.octet.accordion.action.parameters.ApiParameter;
import pro.octet.accordion.core.enums.HttpMethod;
import pro.octet.accordion.exceptions.ActionException;
import pro.octet.accordion.utils.CommonUtils;
import pro.octet.accordion.utils.JsonUtils;
import pro.octet.accordion.utils.XmlParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static pro.octet.accordion.core.enums.DataFormatType.JSON;
import static pro.octet.accordion.core.enums.DataFormatType.XML;


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

        Proxy proxyServer = null;
        if (StringUtils.isNotBlank(params.getProxyServerAddress()) && params.getProxyServerPort() != -1) {
            proxyServer = new Proxy(params.getProxyType(), new InetSocketAddress(params.getProxyServerAddress(), params.getProxyServerPort()));
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
        //Set request body
        RequestBody body = null;
        if (StringUtils.isNotBlank(params.getBody())) {
            MediaType mediaType = getMediaType();
            String bodyStr = StringSubstitutor.replace(params.getBody(), inputParameter);
            body = RequestBody.create(bodyStr, mediaType);
        }
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
    public ActionResult execute() throws ActionException {
        ActionResult actionResult = new ActionResult();
        try {
            String responseBody = exchange(getInputParameter());
            // System.out.println("responseBody: " + responseBody);
            if (StringUtils.isBlank(responseBody)) {
                log.warn("Response result is empty, please check if the API is normal.");
                return actionResult;
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
                findOutputParameters(outputParameter, responseMaps, actionResult);
            }
        } catch (Exception e) {
            setExecuteThrowable(new ActionException(e.getMessage(), e));
        }
        return actionResult;
    }

}
