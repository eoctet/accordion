package chat.octet.accordion.action.api;

import chat.octet.accordion.Accordion;
import chat.octet.accordion.AccordionPlan;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.core.enums.DataFormatType;
import chat.octet.accordion.core.enums.DataType;
import chat.octet.accordion.core.enums.HttpMethod;
import chat.octet.accordion.utils.CommonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for ApiAction using MockWebServer.
 * These tests verify ApiAction behavior in realistic workflow scenarios.
 */
@Slf4j
@DisplayName("ApiAction Integration Tests")
class ApiActionIntegrationTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        log.info("MockWebServer started on port: {}", mockWebServer.getPort());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
        log.info("MockWebServer stopped");
    }

    /**
     * Helper method to execute API action with custom headers.
     *
     * @param url the API endpoint URL
     * @param headers custom headers to send with the request
     * @param message optional message for parameter substitution (can be null)
     * @return the recorded request from the mock server
     * @throws Exception if execution fails
     */
    private RecordedRequest executeApiActionWithHeaders(String url, Map<String, String> headers, Message message) throws Exception {
        ActionConfig action = ActionConfig.builder()
                .id(CommonUtils.randomString("ACT"))
                .actionType(ActionType.API.name())
                .actionName("Secure API")
                .actionParams(ApiParameter.builder()
                        .url(url)
                        .method(HttpMethod.GET)
                        .headers(headers)
                        .responseDataFormat(DataFormatType.JSON)
                        .build())
                .build();

        AccordionPlan plan = AccordionPlan.of().start(action);

        assertThatCode(() -> {
            try (Accordion accordion = new Accordion(plan)) {
                if (message != null) {
                    accordion.play(message, false);
                } else {
                    accordion.play(false);
                }
            }
        }).doesNotThrowAnyException();

        return mockWebServer.takeRequest();
    }

    @Nested
    @DisplayName("Basic API Call Tests")
    class BasicApiCallTests {

        @Test
        @DisplayName("Should execute GET request and extract JSON response")
        void shouldExecuteGetRequestWithJsonResponse() throws Exception {
            log.info("Testing ApiAction with GET request");

            // Mock response
            String jsonResponse = "{\"status\":\"success\",\"userId\":12345,\"userName\":\"testuser\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/user").toString();

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Get User API")
                    .actionDesc("Fetch user information")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.GET)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .actionOutput(Lists.newArrayList(
                            new OutputParameter("userId", DataType.LONG, "User ID"),
                            new OutputParameter("userName", DataType.STRING, "User Name")
                    ))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(true);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getMethod()).isEqualTo("GET");
            assertThat(request.getPath()).isEqualTo("/api/user");
        }

        @Test
        @DisplayName("Should execute POST request with JSON body")
        void shouldExecutePostRequestWithJsonBody() throws Exception {
            log.info("Testing ApiAction with POST request");

            String jsonResponse = "{\"id\":999,\"status\":\"created\"}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/create").toString();
            String requestBody = "{\"name\":\"John\",\"email\":\"john@example.com\"}";

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Create User API")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.POST)
                            .body(requestBody)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .actionOutput(Lists.newArrayList(
                            new OutputParameter("id", DataType.LONG, "Created ID")
                    ))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getBody().readUtf8()).isEqualTo(requestBody);
        }

        @Test
        @DisplayName("Should execute PUT request")
        void shouldExecutePutRequest() throws Exception {
            log.info("Testing ApiAction with PUT request");

            String jsonResponse = "{\"updated\":true}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/update/123").toString();

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Update API")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.PUT)
                            .body("{\"status\":\"active\"}")
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getMethod()).isEqualTo("PUT");
        }

        @Test
        @DisplayName("Should execute DELETE request")
        void shouldExecuteDeleteRequest() throws Exception {
            log.info("Testing ApiAction with DELETE request");

            String jsonResponse = "{\"deleted\":true}";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(jsonResponse)
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/delete/456").toString();

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Delete API")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.DELETE)
                            .body("{}")
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getMethod()).isEqualTo("DELETE");
        }
    }

    @Nested
    @DisplayName("Parameter Substitution Tests")
    class ParameterSubstitutionTests {

        @Test
        @DisplayName("Should substitute parameters in URL")
        void shouldSubstituteParametersInUrl() throws Exception {
            log.info("Testing ApiAction with URL parameter substitution");

            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"result\":\"ok\"}")
                    .setHeader("Content-Type", "application/json"));

            String baseUrl = mockWebServer.url("/").toString();
            String url = baseUrl + "api/users/${userId}/profile";

            Message message = new Message();
            message.put("userId", "12345");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Get User Profile")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.GET)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath()).contains("12345");
        }

        @Test
        @DisplayName("Should substitute parameters in request body")
        void shouldSubstituteParametersInRequestBody() throws Exception {
            log.info("Testing ApiAction with body parameter substitution");

            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"created\":true}")
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/create").toString();

            Message message = new Message();
            message.put("userName", "Alice");
            message.put("userAge", 25);

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Create User")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.POST)
                            .body("{\"name\":\"${userName}\",\"age\":${userAge}}")
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            String bodyContent = request.getBody().readUtf8();
            assertThat(bodyContent).contains("Alice");
            assertThat(bodyContent).contains("25");
        }

        @Test
        @DisplayName("Should substitute parameters in headers")
        void shouldSubstituteParametersInHeaders() throws Exception {
            log.info("Testing ApiAction with header parameter substitution");

            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"result\":\"ok\"}")
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/secure").toString();

            Message message = new Message();
            message.put("token", "Bearer abc123xyz");

            Map<String, String> headers = Maps.newLinkedHashMap();
            headers.put("Authorization", "${token}");

            RecordedRequest request = executeApiActionWithHeaders(url, headers, message);
            assertThat(request.getHeader("Authorization")).isEqualTo("Bearer abc123xyz");
        }
    }

    @Nested
    @DisplayName("XML Response Tests")
    class XmlResponseTests {

        @Test
        @DisplayName("Should parse XML response")
        void shouldParseXmlResponse() throws Exception {
            log.info("Testing ApiAction with XML response");

            String xmlResponse = "<response><status>success</status><userId>789</userId></response>";
            mockWebServer.enqueue(new MockResponse()
                    .setBody(xmlResponse)
                    .setHeader("Content-Type", "application/xml"));

            String url = mockWebServer.url("/api/xml/user").toString();

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("XML API")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.GET)
                            .responseDataFormat(DataFormatType.XML)
                            .build())
                    .actionOutput(Lists.newArrayList(
                            new OutputParameter("userId", DataType.LONG, "User ID")
                    ))
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Query Parameters Tests")
    class QueryParametersTests {

        @Test
        @DisplayName("Should send query parameters")
        void shouldSendQueryParameters() throws Exception {
            log.info("Testing ApiAction with query parameters");

            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"results\":[]}")
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/search").toString();

            Map<String, String> queryParams = Maps.newLinkedHashMap();
            queryParams.put("keyword", "test");
            queryParams.put("limit", "10");
            queryParams.put("page", "1");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Search API")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.GET)
                            .request(queryParams)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath()).contains("keyword=test");
            assertThat(request.getPath()).contains("limit=10");
            assertThat(request.getPath()).contains("page=1");
        }

        @Test
        @DisplayName("Should substitute variables in query parameters")
        void shouldSubstituteVariablesInQueryParameters() throws Exception {
            log.info("Testing ApiAction with variable substitution in query params");

            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"results\":[]}")
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/search").toString();

            Message message = new Message();
            message.put("searchTerm", "accordion");
            message.put("pageSize", "20");

            Map<String, String> queryParams = Maps.newLinkedHashMap();
            queryParams.put("q", "${searchTerm}");
            queryParams.put("size", "${pageSize}");

            ActionConfig action = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Dynamic Search")
                    .actionParams(ApiParameter.builder()
                            .url(url)
                            .method(HttpMethod.GET)
                            .request(queryParams)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .build();

            AccordionPlan plan = AccordionPlan.of().start(action);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(message, false);
                }
            }).doesNotThrowAnyException();

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath()).contains("q=accordion");
            assertThat(request.getPath()).contains("size=20");
        }
    }

    @Nested
    @DisplayName("Custom Headers Tests")
    class CustomHeadersTests {

        @Test
        @DisplayName("Should send custom headers")
        void shouldSendCustomHeaders() throws Exception {
            log.info("Testing ApiAction with custom headers");

            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"result\":\"ok\"}")
                    .setHeader("Content-Type", "application/json"));

            String url = mockWebServer.url("/api/secure").toString();

            Map<String, String> headers = Maps.newLinkedHashMap();
            headers.put("Authorization", "Bearer test-token");
            headers.put("X-API-Key", "api-key-123");
            headers.put("X-Client-Version", "1.0.0");

            RecordedRequest request = executeApiActionWithHeaders(url, headers, null);
            assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token");
            assertThat(request.getHeader("X-API-Key")).isEqualTo("api-key-123");
            assertThat(request.getHeader("X-Client-Version")).isEqualTo("1.0.0");
        }
    }

    @Nested
    @DisplayName("Complex Workflow Tests")
    class ComplexWorkflowTests {

        @Test
        @DisplayName("Should execute chained API calls")
        void shouldExecuteChainedApiCalls() throws Exception {
            log.info("Testing chained API calls workflow");

            // First API: Get user ID
            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"userId\":777}")
                    .setHeader("Content-Type", "application/json"));

            // Second API: Get user details using the userId
            mockWebServer.enqueue(new MockResponse()
                    .setBody("{\"name\":\"Test User\",\"email\":\"test@example.com\"}")
                    .setHeader("Content-Type", "application/json"));

            String baseUrl = mockWebServer.url("/api").toString();

            ActionConfig getUserId = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Get User ID")
                    .actionParams(ApiParameter.builder()
                            .url(baseUrl + "/auth")
                            .method(HttpMethod.GET)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .actionOutput(Lists.newArrayList(
                            new OutputParameter("userId", DataType.LONG, "User ID")
                    ))
                    .build();

            ActionConfig getUserDetails = ActionConfig.builder()
                    .id(CommonUtils.randomString("ACT"))
                    .actionType(ActionType.API.name())
                    .actionName("Get User Details")
                    .actionParams(ApiParameter.builder()
                            .url(baseUrl + "/user/${userId}")
                            .method(HttpMethod.GET)
                            .responseDataFormat(DataFormatType.JSON)
                            .build())
                    .actionOutput(Lists.newArrayList(
                            new OutputParameter("name", DataType.STRING, "User Name"),
                            new OutputParameter("email", DataType.STRING, "User Email")
                    ))
                    .build();

            AccordionPlan plan = AccordionPlan.of()
                    .start(getUserId)
                    .next(getUserId, getUserDetails);

            assertThatCode(() -> {
                try (Accordion accordion = new Accordion(plan)) {
                    accordion.play(true);
                }
            }).doesNotThrowAnyException();
        }
    }
}