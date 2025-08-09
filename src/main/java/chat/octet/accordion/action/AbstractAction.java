package chat.octet.accordion.action;

import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.action.model.InputParameter;
import chat.octet.accordion.action.model.OutputParameter;
import chat.octet.accordion.core.entity.Message;
import chat.octet.accordion.core.entity.Session;
import chat.octet.accordion.core.handler.DataTypeConvert;
import chat.octet.accordion.utils.CommonUtils;
import chat.octet.accordion.utils.JsonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for all action implementations in the Accordion framework.
 *
 * <p>This class provides the foundational functionality that all actions share, including
 * session management, parameter handling, error tracking, and lifecycle management.
 * All concrete action implementations must extend this class.</p>
 *
 * <p>Key Responsibilities:</p>
 * <ul>
 *   <li><strong>Session Management</strong>: Handles session preparation and parameter loading</li>
 *   <li><strong>Parameter Processing</strong>: Manages input parameters from various sources</li>
 *   <li><strong>Output Handling</strong>: Processes and stores action execution results</li>
 *   <li><strong>Error Tracking</strong>: Maintains execution error state</li>
 *   <li><strong>Resource Management</strong>: Provides cleanup capabilities</li>
 * </ul>
 *
 * <p>Parameter Loading Order:</p>
 * <ol>
 *   <li>Message parameters from {@link #ACCORDION_MESSAGE}</li>
 *   <li>Previous action output from {@link #PREV_ACTION_OUTPUT}</li>
 *   <li>Global session parameters</li>
 * </ol>
 *
 * <p>Implementation Example:</p>
 * <pre>{@code
 * public class CustomAction extends AbstractAction {
 *     public CustomAction(ActionConfig config) {
 *         super(config);
 *     }
 *
 *     @Override
 *     public ExecuteResult execute() throws ActionException {
 *         try {
 *             // Access input parameters
 *             String param = getInputParameter().getString("paramName");
 *
 *             // Perform action logic
 *             String result = performCustomLogic(param);
 *
 *             // Return result
 *             return ExecuteResult.success().put("result", result);
 *         } catch (Exception e) {
 *             setExecuteThrowable(e);
 *             return ExecuteResult.failure(e.getMessage());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>Thread Safety: This class is not thread-safe. Each action instance should be used
 * by only one execution thread at a time.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ActionService
 * @see ActionConfig
 * @see ExecuteResult
 * @see Session
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractAction implements ActionService, Serializable {
    /**
     * Session key for the initial message passed to the accordion execution.
     * Actions can access this message through the session to get initial context.
     */
    public static final String ACCORDION_MESSAGE = "ACCORDION_MESSAGE";

    /**
     * Session key for the output parameters from the previous action in the execution chain.
     * This allows actions to access and use results from their predecessor actions.
     */
    public static final String PREV_ACTION_OUTPUT = "PREV_ACTION_OUTPUT";
    private final ActionConfig actionConfig;
    private final InputParameter inputParameter;
    private Session session;
    private final AtomicReference<Throwable> executeThrowable = new AtomicReference<>();
    private final String actionId;

    /**
     * Constructs a new AbstractAction with the specified configuration.
     *
     * <p>Initializes the action with its configuration, creates an empty input parameter
     * container, and sets up error tracking. The action ID is extracted from the
     * configuration for logging and identification purposes.</p>
     *
     * @param actionConfig the configuration defining this action's behavior and parameters
     * @throws NullPointerException if actionConfig is null
     * @since 1.0.0
     */
    public AbstractAction(final ActionConfig actionConfig) {
        // Store reference to actionConfig - ActionConfig is immutable after construction
        this.actionConfig = actionConfig;
        this.inputParameter = new InputParameter();
        this.actionId = actionConfig != null ? actionConfig.getId() : null;
        this.session = null; // Initialize session field
    }

    /**
     * Prepares the action for execution by loading parameters from the session.
     *
     * <p>This method is called by the execution engine before {@link #execute()} to
     * initialize the action with all necessary parameters. It loads parameters from
     * multiple sources in a specific order to ensure proper precedence.</p>
     *
     * <p>Parameter Loading Process:</p>
     * <ol>
     *   <li><strong>Message Parameters</strong>: Initial message passed to accordion</li>
     *   <li><strong>Previous Action Output</strong>: Results from predecessor actions</li>
     *   <li><strong>Global Parameters</strong>: Session-wide parameters</li>
     * </ol>
     *
     * <p>If any error occurs during preparation, it's captured and can be checked
     * using {@link #checkError()}.</p>
     *
     * @param sessionParam the execution session containing parameters and context
     * @return this action service instance for method chaining
     * @throws IllegalArgumentException if session is null
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public ActionService prepare(final Session sessionParam) {
        if (sessionParam == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        this.session = sessionParam;

        // Store reference to session - Session is managed by the framework
        this.session = session;
        this.executeThrowable.set(null);
        this.inputParameter.clear();

        try {
            // Load message parameters
            if (session.containsKey(ACCORDION_MESSAGE)) {
                Message message = session.getValue(ACCORDION_MESSAGE, Message.class);
                if (message != null) {
                    inputParameter.putAll(message);
                    log.debug("({}) -> Loaded message parameters: {}", actionId, message);
                }
            }

            // Load previous action output
            if (session.containsKey(PREV_ACTION_OUTPUT)) {
                List<OutputParameter> prevActionOutput = session.getValue(PREV_ACTION_OUTPUT, List.class);
                if (prevActionOutput != null) {
                    prevActionOutput.forEach(param -> {
                        if (param != null && param.getName() != null) {
                            inputParameter.put(param.getName(), param.getValue());
                        }
                    });
                    log.debug("({}) -> Loaded previous action output: {}", actionId,
                            JsonUtils.toJson(prevActionOutput));
                }
            }

            // Load global parameters
            if (session.getGlobal() != null && !session.getGlobal().isEmpty()) {
                inputParameter.putAll(session.getGlobal());
                log.debug("({}) -> Loaded global parameters: {}", actionId, session.getGlobal());
            }

        } catch (Exception e) {
            log.error("({}) -> Error preparing action: {}", actionId, e.getMessage(), e);
            setExecuteThrowable(e);
        }

        return this;
    }

    /**
     * Processes and stores the action's output parameters in the session.
     *
     * <p>This method is called after action execution to make the action's results
     * available to subsequent actions in the execution chain. It processes the
     * configured output parameters and stores them in the session under the
     * {@link #PREV_ACTION_OUTPUT} key.</p>
     *
     * <p>Output Processing:</p>
     * <ul>
     *   <li>Retrieves configured output parameter definitions</li>
     *   <li>Extracts actual values from execution result</li>
     *   <li>Applies data type conversions as needed</li>
     *   <li>Stores processed parameters in session for next actions</li>
     * </ul>
     *
     * @param executeResult the result of action execution containing output values
     * @since 1.0.0
     */
    @Override
    public void output(final ExecuteResult executeResult) {
        this.session.remove(PREV_ACTION_OUTPUT);

        List<OutputParameter> output = getActionOutput();
        if (!CommonUtils.isEmpty(output)) {
            List<OutputParameter> result = Lists.newArrayList();
            output.forEach(param -> {
                String key = param.getName();
                Object value = param.getValue();
                if (executeResult.contains(key)) {
                    value = DataTypeConvert.getValue(param.getDataType(), executeResult.getValue(key));
                }
                if (value != null) {
                    result.add(new OutputParameter(param.getName(), param.getDataType(), param.getDesc(), value));
                }
            });
            this.session.add(PREV_ACTION_OUTPUT, result);
            log.debug("({}) -> Update output into the action session, output: {}.", actionId, JsonUtils.toJson(result));
        }
    }

    /**
     * Checks if an error occurred during action preparation or execution.
     *
     * <p>This method is called by the execution engine to determine if the action
     * encountered any errors. If an error is found, it's logged with full stack trace
     * for debugging purposes.</p>
     *
     * <p>Error sources include:</p>
     * <ul>
     *   <li>Exceptions during session preparation</li>
     *   <li>Exceptions during action execution</li>
     *   <li>Validation failures</li>
     *   <li>Resource access errors</li>
     * </ul>
     *
     * @return true if an error occurred, false if execution was successful
     * @since 1.0.0
     */
    @Override
    public boolean checkError() {
        Throwable cause = this.executeThrowable.get();
        if (cause != null) {
            log.error(MessageFormat.format("({0}) -> Execution action error.", actionId), cause);
            return true;
        }
        return false;
    }

    /**
     * Returns the configuration object for this action.
     *
     * <p>The configuration contains all the metadata and parameters needed
     * to execute this action, including ID, name, type, and parameter definitions.</p>
     *
     * @return the action configuration object
     * @since 1.0.0
     */
    @Override
    public ActionConfig getConfig() {
        return this.actionConfig;
    }

    /**
     * Closes the action and releases any resources.
     *
     * <p>This default implementation does nothing. Subclasses should override
     * this method if they need to release resources such as connections,
     * file handles, or other system resources.</p>
     *
     * <p>Example override:</p>
     * <pre>{@code
     * @Override
     * public void close() {
     *     if (connection != null) {
     *         connection.close();
     *     }
     *     super.close();
     * }
     * }</pre>
     *
     * @since 1.0.0
     */
    @Override
    public void close() {
    }

    /**
     * Records an exception that occurred during action execution.
     *
     * <p>This method should be called by subclasses when they encounter exceptions
     * during execution. It uses atomic operations to ensure thread safety and
     * only records the first exception encountered.</p>
     *
     * <p>Usage in subclasses:</p>
     * <pre>{@code
     * try {
     *     // Action execution logic
     * } catch (Exception e) {
     *     setExecuteThrowable(e);
     *     return ExecuteResult.failure(e.getMessage());
     * }
     * }</pre>
     *
     * @param throwable the exception that occurred during execution
     * @since 1.0.0
     */
    protected void setExecuteThrowable(final Throwable throwable) {
        this.executeThrowable.compareAndSet(null, throwable);
    }

    /**
     * Returns the input parameters loaded during action preparation.
     *
     * <p>This method provides access to all parameters that were loaded from
     * the session during the {@link #prepare(Session)} phase. Subclasses use
     * this to access their input data for execution.</p>
     *
     * <p>Parameter access example:</p>
     * <pre>{@code
     * String url = getInputParameter().getString("url");
     * int timeout = getInputParameter().getInteger("timeout", 30);
     * }</pre>
     *
     * @return the input parameter container with loaded values
     * @since 1.0.0
     */
    protected InputParameter getInputParameter() {
        return inputParameter;
    }

    /**
     * Returns the configured output parameter definitions for this action.
     *
     * <p>This method provides access to the output parameter schema defined
     * in the action configuration. It's used by the {@link #output(ExecuteResult)}
     * method to determine which values to extract from the execution result.</p>
     *
     * @return list of output parameter definitions, may be empty but never null
     * @since 1.0.0
     */
    protected List<OutputParameter> getActionOutput() {
        return getConfig().getActionOutput();
    }

}
