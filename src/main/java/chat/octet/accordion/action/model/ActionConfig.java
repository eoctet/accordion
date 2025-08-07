package chat.octet.accordion.action.model;

import chat.octet.accordion.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.LinkedHashMap;
import java.util.List;


/**
 * Configuration model for defining action properties and parameters.
 *
 * <p>ActionConfig serves as the blueprint for action instances, containing all
 * necessary metadata and parameters required to create and execute actions
 * within the Accordion framework. It supports JSON serialization for
 * configuration persistence and sharing.</p>
 *
 * <p>Key Components:</p>
 * <ul>
 *   <li><strong>Identity</strong>: Unique ID and descriptive name</li>
 *   <li><strong>Type Information</strong>: Action type for registry lookup</li>
 *   <li><strong>Parameters</strong>: Action-specific configuration data</li>
 *   <li><strong>Output Definition</strong>: Expected output parameter schema</li>
 * </ul>
 *
 * <p>JSON Configuration:</p>
 * <ul>
 *   <li>Uses snake_case naming strategy for JSON properties</li>
 *   <li>Excludes null values from serialization</li>
 *   <li>Supports complex parameter objects</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Builder Pattern:</strong></p>
 * <pre>{@code
 * ActionConfig config = ActionConfig.builder()
 *     .id("api-001")
 *     .actionType("API")
 *     .actionName("Fetch User Data")
 *     .actionDesc("Retrieves user information from REST API")
 *     .actionParams(ApiParameter.builder()
 *         .url("https://api.example.com/users/{id}")
 *         .method("GET")
 *         .build())
 *     .build();
 * }</pre>
 *
 * <p><strong>Parameter Access:</strong></p>
 * <pre>{@code
 * ApiParameter params = config.getActionParams(ApiParameter.class);
 * String url = params.getUrl();
 * }</pre>
 *
 * <p><strong>JSON Serialization:</strong></p>
 * <pre>{@code
 * {
 *   "id": "api-001",
 *   "action_type": "API",
 *   "action_name": "Fetch User Data",
 *   "action_desc": "Retrieves user information from REST API",
 *   "action_params": {
 *     "url": "https://api.example.com/users/{id}",
 *     "method": "GET"
 *   }
 * }
 * }</pre>
 *
 * <p>Thread Safety: This class is immutable after construction (except for
 * actionOutput which can be set), making it safe for concurrent access.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ActionService
 * @see ActionRegister
 * @see OutputParameter
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ActionConfig {

    /**
     * Unique identifier for this action within the execution plan.
     * Must be unique across all actions in the same plan.
     */
    private String id;

    /**
     * The type of action, used for registry lookup and instantiation.
     * Must correspond to a registered action type in ActionRegister.
     */
    private String actionType;

    /**
     * Human-readable name for this action, used for display and logging.
     */
    private String actionName;

    /**
     * Optional description providing additional context about the action's purpose.
     */
    private String actionDesc;

    /**
     * Action-specific parameters and configuration data.
     * Can be a simple map or a complex parameter object.
     */
    private Object actionParams;

    /**
     * List of output parameter definitions that this action produces.
     * Can be modified after construction to support dynamic output configuration.
     */
    @Setter
    private List<OutputParameter> actionOutput;

    /**
     * Retrieves action parameters as a typed object.
     *
     * <p>This method handles the conversion of action parameters from their
     * stored format (which may be a LinkedHashMap from JSON deserialization)
     * to the expected parameter object type.</p>
     *
     * <p>Conversion Process:</p>
     * <ol>
     *   <li>If parameters are stored as LinkedHashMap, converts via JSON</li>
     *   <li>Checks if the result matches the expected type</li>
     *   <li>Returns the typed parameter object or null</li>
     * </ol>
     *
     * @param <T>   the expected parameter type
     * @param clazz the target class for parameter conversion
     * @return the typed parameter object, or null if conversion fails or types don't match
     * @since 1.0.0
     */
    public <T> T getActionParams(Class<T> clazz) {
        if (actionParams instanceof LinkedHashMap) {
            actionParams = JsonUtils.parseToObject(JsonUtils.toJson(actionParams), clazz);
        }
        if (actionParams != null && actionParams.getClass() == clazz) {
            return clazz.cast(actionParams);
        }
        return null;
    }

    /**
     * Retrieves action parameters as a typed object with validation.
     *
     * <p>This method is similar to {@link #getActionParams(Class)} but throws
     * an exception with a custom error message if the parameters cannot be
     * retrieved or converted to the expected type.</p>
     *
     * <p>This is useful for mandatory parameter validation during action
     * initialization where missing or invalid parameters should cause
     * immediate failure.</p>
     *
     * @param <T>          the expected parameter type
     * @param clazz        the target class for parameter conversion
     * @param errorMessage the error message to use if parameters are invalid
     * @return the typed parameter object, never null
     * @throws IllegalArgumentException if parameters cannot be retrieved or converted
     * @since 1.0.0
     */
    public <T> T getActionParams(Class<T> clazz, String errorMessage) {
        T object = getActionParams(clazz);
        if (object == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return object;
    }

}
