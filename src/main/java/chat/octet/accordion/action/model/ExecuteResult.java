package chat.octet.accordion.action.model;


import chat.octet.accordion.action.base.ConditionAction;
import chat.octet.accordion.action.base.SwitchAction;
import chat.octet.accordion.core.entity.Tuple;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.graph.entity.SwitchFilter;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Container for action execution results and output data.
 *
 * <p>ExecuteResult encapsulates the outcome of an action execution, including
 * success/failure status, output data, and special control flow information.
 * It serves as the primary communication mechanism between actions in an
 * execution chain.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Data Storage</strong>: Stores key-value pairs of execution results</li>
 *   <li><strong>Type Safety</strong>: Provides typed access to result values</li>
 *   <li><strong>Control Flow</strong>: Supports conditional and switch action control</li>
 *   <li><strong>Parameter Extraction</strong>: Automatically extracts configured output parameters</li>
 * </ul>
 *
 * <p>Special Control Flow Support:</p>
 * <ul>
 *   <li><strong>Condition Actions</strong>: {@link #isBreak()} for conditional execution control</li>
 *   <li><strong>Switch Actions</strong>: {@link #getSwitchFilter()} for multi-branch execution</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Creating Success Result:</strong></p>
 * <pre>{@code
 * ExecuteResult result = new ExecuteResult()
 *     .add("status", "success")
 *     .add("data", responseData)
 *     .add("timestamp", System.currentTimeMillis());
 * }</pre>
 *
 * <p><strong>Accessing Result Data:</strong></p>
 * <pre>{@code
 * String status = result.getValue("status", String.class);
 * if (result.contains("errorMessage")) {
 *     String error = result.getValue("errorMessage", String.class);
 * }
 * }</pre>
 *
 * <p><strong>Parameter Extraction:</strong></p>
 * <pre>{@code
 * List<OutputParameter> outputParams = action.getActionOutput();
 * result.findAndAddParameters(outputParams, apiResponseMap);
 * }</pre>
 *
 * <p>Thread Safety: This class is not thread-safe. Each action execution
 * should use its own result instance.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see OutputParameter
 * @see ConditionAction
 * @see SwitchAction
 * @see Tuple
 * @since 1.0.0
 */
@Getter
public class ExecuteResult implements Serializable {
    private final Tuple<String, Object> result;

    /**
     * Constructs a new empty ExecuteResult.
     *
     * <p>Initializes the internal result storage for holding execution
     * output data and control flow information.</p>
     *
     * @since 1.0.0
     */
    public ExecuteResult() {
        this.result = new Tuple<>();
    }

    /**
     * Creates a new successful execution result.
     *
     * <p>This is a convenience factory method for creating success results
     * in a fluent style.</p>
     *
     * @return a new empty ExecuteResult representing successful execution
     * @since 1.0.0
     */
    public static ExecuteResult success() {
        return new ExecuteResult();
    }

    /**
     * Creates a new successful execution result with initial data.
     *
     * <p>This is a convenience factory method for creating success results
     * with immediate data population.</p>
     *
     * @param key   the initial data key
     * @param value the initial data value
     * @return a new ExecuteResult with the specified data
     * @since 1.0.0
     */
    public static ExecuteResult success(String key, Object value) {
        return new ExecuteResult().add(key, value);
    }

    /**
     * Creates a new failure execution result with an error message.
     *
     * <p>This is a convenience factory method for creating failure results
     * with error information.</p>
     *
     * @param errorMessage the error message describing the failure
     * @return a new ExecuteResult representing failed execution
     * @since 1.0.0
     */
    public static ExecuteResult failure(String errorMessage) {
        return new ExecuteResult()
                .add("success", false)
                .add("error", errorMessage);
    }

    /**
     * Checks if the result contains a value for the specified key.
     *
     * @param key the key to check for
     * @return true if the key exists in the result, false otherwise
     * @since 1.0.0
     */
    public boolean contains(String key) {
        return this.result.containsKey(key);
    }

    /**
     * Adds a key-value pair to the execution result.
     *
     * <p>This method allows fluent-style result building by returning
     * the same instance for method chaining.</p>
     *
     * @param key   the result key
     * @param value the result value
     * @return this ExecuteResult instance for method chaining
     * @since 1.0.0
     */
    public ExecuteResult add(String key, Object value) {
        this.result.put(key, value);
        return this;
    }

    /**
     * Retrieves a value from the execution result.
     *
     * @param key the key of the value to retrieve
     * @return the value associated with the key, or null if not found
     * @since 1.0.0
     */
    public Object getValue(String key) {
        return this.result.get(key);
    }

    /**
     * Retrieves a typed value from the execution result.
     *
     * <p>This method performs type casting to ensure the returned value
     * is of the expected type.</p>
     *
     * @param <T>   the expected type of the value
     * @param key   the key of the value to retrieve
     * @param clazz the expected class type
     * @return the value cast to the specified type, or null if not found
     * @throws ClassCastException if the value cannot be cast to the specified type
     * @since 1.0.0
     */
    public <T> T getValue(String key, Class<T> clazz) {
        return clazz.cast(this.result.get(key));
    }

    /**
     * Checks if this result indicates a break in execution flow.
     *
     * <p>This method is used by {@link ConditionAction} to control execution flow.
     * When a condition evaluates to false, it sets a break state that can
     * terminate the execution chain.</p>
     *
     * @return true if execution should break, false otherwise
     * @see ConditionAction
     * @since 1.0.0
     */
    public boolean isBreak() {
        if (contains(ConditionAction.ACTION_CONDITION_STATE)) {
            return !this.result.getBoolean(ConditionAction.ACTION_CONDITION_STATE);
        }
        return false;
    }

    /**
     * Retrieves the switch filter for controlling multi-branch execution.
     *
     * <p>This method is used by {@link SwitchAction} to provide execution
     * control information that determines which branches of the execution
     * graph should be activated or skipped.</p>
     *
     * @return the switch filter if present, null otherwise
     * @see SwitchAction
     * @see SwitchFilter
     * @since 1.0.0
     */
    public SwitchFilter getSwitchFilter() {
        if (contains(SwitchAction.ACTION_SWITCH_CONTROL)) {
            return (SwitchFilter) this.result.get(SwitchAction.ACTION_SWITCH_CONTROL);
        }
        return null;
    }

    /**
     * Recursively extracts and adds configured output parameters from a result map.
     *
     * <p>This method searches through a potentially nested result structure (maps and lists)
     * to find values that match the configured output parameters. It performs automatic
     * data type conversion and adds matching values to this result instance.</p>
     *
     * <p>The method handles:</p>
     * <ul>
     *   <li>Direct key-value matches in the result map</li>
     *   <li>Nested maps (recursively searched)</li>
     *   <li>Lists containing maps (each map is searched)</li>
     *   <li>Automatic data type conversion based on parameter definitions</li>
     * </ul>
     *
     * <p>This is particularly useful for extracting specific values from complex
     * API responses or other structured data sources.</p>
     *
     * @param outputParameter list of output parameter definitions to search for
     * @param result          the result map to search through
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public void findAndAddParameters(List<OutputParameter> outputParameter, Map<String, Object> result) {
        result.forEach((key, value) -> {
            outputParameter.forEach(parameter -> {
                if (parameter.getName().equalsIgnoreCase(key)) {
                    this.add(parameter.getName(), DataTypeConvert.getValue(parameter.getDataType(), value));
                }
            });
            if (value instanceof Map) {
                findAndAddParameters(outputParameter, (Map<String, Object>) value);
            }
            if (value instanceof List) {
                ((List<?>) value).forEach(e -> {
                    if (e instanceof Map) {
                        findAndAddParameters(outputParameter, (Map<String, Object>) e);
                    }
                });
            }
        });
    }

    /**
     * Clears all data from the execution result.
     *
     * <p>This method removes all stored key-value pairs from the result,
     * effectively resetting it to an empty state.</p>
     *
     * @since 1.0.0
     */
    public void clear() {
        this.result.clear();
    }

    /**
     * Returns a string representation of the execution result.
     *
     * <p>This method provides a readable representation of all result data
     * for debugging and logging purposes.</p>
     *
     * @return string representation of the result data
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return this.result.toString();
    }
}
