package chat.octet.accordion.core.entity;


import lombok.Getter;

/**
 * Execution session that manages state and parameters during accordion plan execution.
 *
 * <p>The Session serves as the central data store and communication mechanism between
 * actions in an execution plan. It maintains both local session data and global parameters
 * that persist throughout the entire execution lifecycle.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Parameter Management</strong>: Stores and retrieves typed parameters</li>
 *   <li><strong>Global Scope</strong>: Maintains global parameters accessible to all actions</li>
 *   <li><strong>Type Safety</strong>: Provides type-safe parameter retrieval</li>
 *   <li><strong>Lifecycle Management</strong>: Supports session cleanup and reset</li>
 * </ul>
 *
 * <p>Parameter Scopes:</p>
 * <ul>
 *   <li><strong>Local Parameters</strong>: Available within the current execution context</li>
 *   <li><strong>Global Parameters</strong>: Available to all actions throughout execution</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Adding Parameters:</strong></p>
 * <pre>{@code
 * Session session = new Session();
 * session.add("localParam", "value");           // Local parameter
 * session.add("globalParam", "value", true);    // Global parameter
 * }</pre>
 *
 * <p><strong>Type-Safe Retrieval:</strong></p>
 * <pre>{@code
 * String value = session.getValue("paramName", String.class);
 * Integer count = session.getValue("count", Integer.class);
 * }</pre>
 *
 * <p><strong>Parameter Checking:</strong></p>
 * <pre>{@code
 * if (session.containsKey("optionalParam")) {
 *     // Handle optional parameter
 * }
 * }</pre>
 *
 * <p>Thread Safety: This class is not thread-safe. Each execution thread should
 * use its own session instance.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see Tuple
 * @see Message
 * @since 1.0.0
 */
public class Session {
    private static final String SESSION_GLOBAL_PARAMETER = "SESSION_GLOBAL_PARAMETER";
    private final Tuple<String, Object> data;
    @Getter
    private final Tuple<String, Object> global;

    /**
     * Constructs a new empty session.
     *
     * <p>Initializes both local and global parameter storage and establishes
     * the internal reference structure for parameter management.</p>
     *
     * @since 1.0.0
     */
    public Session() {
        this.data = new Tuple<>();
        this.global = new Tuple<>();
        this.data.put(SESSION_GLOBAL_PARAMETER, global);
    }

    /**
     * Checks if a parameter with the specified key exists in the session.
     *
     * <p>This method checks for the existence of a parameter in the local session
     * data. It does not check global parameters directly.</p>
     *
     * @param key the parameter key to check
     * @return true if the parameter exists, false otherwise
     * @since 1.0.0
     */
    public boolean containsKey(final String key) {
        return this.data.containsKey(key);
    }

    /**
     * Adds a local parameter to the session.
     *
     * <p>This is a convenience method equivalent to calling {@code add(key, value, false)}.
     * The parameter will be stored in the local session scope.</p>
     *
     * @param key   the parameter key, must not be null or empty
     * @param value the parameter value, may be null
     * @return this session instance for method chaining
     * @throws IllegalArgumentException if key is null or empty
     * @since 1.0.0
     */
    public Session add(final String key, final Object value) {
        return add(key, value, false);
    }

    /**
     * Adds a parameter to the session with specified scope.
     *
     * <p>Parameters can be added to either local or global scope:</p>
     * <ul>
     *   <li><strong>Local (isGlobal=false)</strong>: Available within current execution context</li>
     *   <li><strong>Global (isGlobal=true)</strong>: Available to all actions throughout execution</li>
     * </ul>
     *
     * <p>Global parameters are particularly useful for configuration values,
     * authentication tokens, or other data that should be accessible to all actions.</p>
     *
     * @param key      the parameter key, must not be null or empty
     * @param value    the parameter value, may be null
     * @param isGlobal if true, stores in global scope; if false, stores in local scope
     * @return this session instance for method chaining
     * @throws IllegalArgumentException if key is null or empty
     * @since 1.0.0
     */
    public Session add(final String key, final Object value, final boolean isGlobal) {
        if (key == null) {
            throw new IllegalArgumentException("Session key cannot be null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("Session key cannot be empty");
        }

        if (isGlobal) {
            this.global.put(key, value);
        } else {
            this.data.put(key, value);
        }
        return this;
    }

    /**
     * Removes a parameter from the local session data.
     *
     * <p>This method only removes parameters from the local scope.
     * Global parameters are not affected by this operation.</p>
     *
     * @param key the key of the parameter to remove
     * @since 1.0.0
     */
    public void remove(final String key) {
        this.data.remove(key);
    }

    /**
     * Retrieves a parameter value from the session.
     *
     * <p>This method returns the raw object value without type checking.
     * For type-safe retrieval, use {@link #getValue(String, Class)} instead.</p>
     *
     * @param key the parameter key
     * @return the parameter value, or null if not found
     * @since 1.0.0
     */
    public Object getValue(final String key) {
        return this.data.get(key);
    }

    /**
     * Retrieves a parameter value with type safety.
     *
     * <p>This method performs type checking and casting to ensure the returned
     * value is of the expected type. If the value cannot be cast to the specified
     * type, an exception is thrown with detailed error information.</p>
     *
     * <p>Usage examples:</p>
     * <pre>{@code
     * String name = session.getValue("userName", String.class);
     * Integer count = session.getValue("itemCount", Integer.class);
     * List<String> items = session.getValue("items", List.class);
     * }</pre>
     *
     * @param <T>   the expected type of the parameter value
     * @param key   the parameter key
     * @param clazz the expected class type
     * @return the parameter value cast to the specified type, or null if not found
     * @throws IllegalArgumentException if key or clazz is null, or if type casting fails
     * @since 1.0.0
     */
    public <T> T getValue(final String key, final Class<T> clazz) {
        if (key == null || clazz == null) {
            throw new IllegalArgumentException("Key and class cannot be null");
        }

        Object value = this.data.get(key);
        if (value == null) {
            return null;
        }

        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Cannot cast value for key '" + key
                    + "' to type " + clazz.getSimpleName() + ". Actual type: " + value.getClass().getSimpleName(), e);
        }
    }

    /**
     * Clears all local session data while preserving global parameters.
     *
     * <p>This method removes all local parameters from the session but maintains
     * the global parameter store and its reference structure. This is useful
     * for resetting session state between executions while keeping global
     * configuration intact.</p>
     *
     * @since 1.0.0
     */
    public void clear() {
        this.data.clear();
        // Re-add the global parameter reference after clearing
        this.data.put(SESSION_GLOBAL_PARAMETER, global);
    }

    /**
     * Returns a string representation of the session data.
     *
     * <p>This method provides a readable representation of all session data
     * for debugging and logging purposes. The format includes both local
     * and global parameters.</p>
     *
     * @return string representation of session data
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return data.toString();
    }
}
