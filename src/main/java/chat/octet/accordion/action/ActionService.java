package chat.octet.accordion.action;


import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.entity.Session;
import chat.octet.accordion.exceptions.ActionException;

/**
 * Core service interface for all action implementations in the Accordion framework.
 *
 * <p>This interface defines the contract that all actions must implement to participate
 * in the Accordion execution lifecycle. It provides a standardized way to prepare,
 * execute, and manage actions within the automation framework.</p>
 *
 * <p>Execution Lifecycle:</p>
 * <ol>
 *   <li><strong>Preparation</strong>: {@link #prepare(Session)} - Load parameters and initialize</li>
 *   <li><strong>Execution</strong>: {@link #execute()} - Perform the action's core logic</li>
 *   <li><strong>Output Processing</strong>: {@link #output(ExecuteResult)} - Store results for next actions</li>
 *   <li><strong>Error Checking</strong>: {@link #checkError()} - Verify execution success</li>
 *   <li><strong>Cleanup</strong>: {@link #close()} - Release resources</li>
 * </ol>
 *
 * <p>Implementation Guidelines:</p>
 * <ul>
 *   <li>Actions should be stateless between executions</li>
 *   <li>All exceptions should be caught and handled gracefully</li>
 *   <li>Resource cleanup should be implemented in {@link #close()}</li>
 *   <li>Error states should be properly tracked and reported</li>
 * </ul>
 *
 * <p>Example Implementation:</p>
 * <pre>{@code
 * public class MyAction implements ActionService {
 *     private Session session;
 *     private ActionConfig config;
 *
 *     @Override
 *     public ActionService prepare(Session session) {
 *         this.session = session;
 *         // Load and validate parameters
 *         return this;
 *     }
 *
 *     @Override
 *     public ExecuteResult execute() throws ActionException {
 *         // Implement action logic
 *         return ExecuteResult.success();
 *     }
 *
 *     // ... other methods
 * }
 * }</pre>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see AbstractAction
 * @see ActionConfig
 * @see ExecuteResult
 * @see Session
 * @since 1.0.0
 */
public interface ActionService {

    /**
     * Prepares the action for execution by initializing with session data.
     *
     * <p>This method is called by the execution engine before {@link #execute()}
     * to provide the action with access to session parameters, previous action
     * outputs, and global context. Implementations should use this phase to:</p>
     *
     * <ul>
     *   <li>Load and validate required parameters</li>
     *   <li>Initialize internal state</li>
     *   <li>Prepare resources needed for execution</li>
     *   <li>Perform any pre-execution validation</li>
     * </ul>
     *
     * <p>The method should return the same instance to support method chaining
     * in the execution pipeline.</p>
     *
     * @param session the execution session containing parameters and context
     * @return this action service instance for method chaining
     * @throws IllegalArgumentException if session is null or invalid
     * @since 1.0.0
     */
    ActionService prepare(Session session);

    /**
     * Executes the core logic of the action.
     *
     * <p>This method contains the main functionality of the action and is called
     * after successful preparation. Implementations should:</p>
     *
     * <ul>
     *   <li>Perform the action's primary task</li>
     *   <li>Handle errors gracefully</li>
     *   <li>Return appropriate execution results</li>
     *   <li>Avoid blocking operations when possible</li>
     * </ul>
     *
     * <p>The default implementation throws an {@link ActionException} to ensure
     * that all concrete implementations provide their own execution logic.</p>
     *
     * <p>Result Guidelines:</p>
     * <ul>
     *   <li>Use {@code ExecuteResult.success()} for successful execution</li>
     *   <li>Use {@code ExecuteResult.failure(message)} for handled failures</li>
     *   <li>Include relevant data in the result for downstream actions</li>
     * </ul>
     *
     * @return the result of action execution, never null
     * @throws ActionException if execution fails or action is not implemented
     * @since 1.0.0
     */
    default ExecuteResult execute() throws ActionException {
        throw new ActionException("Action not implemented.");
    }

    /**
     * Processes and stores the action's output parameters for subsequent actions.
     *
     * <p>This method is called after successful execution to extract and store
     * the action's output values in the session. These values become available
     * to subsequent actions in the execution chain.</p>
     *
     * <p>Processing includes:</p>
     * <ul>
     *   <li>Extracting configured output parameters from execution result</li>
     *   <li>Applying data type conversions as needed</li>
     *   <li>Storing processed values in session for next actions</li>
     *   <li>Logging output for debugging purposes</li>
     * </ul>
     *
     * <p>The output is stored in the session under the key defined by
     * {@link AbstractAction#PREV_ACTION_OUTPUT}.</p>
     *
     * @param executeResult the result of action execution containing output values
     * @since 1.0.0
     */
    void output(ExecuteResult executeResult);

    /**
     * Checks if an error occurred during action preparation or execution.
     *
     * <p>This method is called by the execution engine to determine the success
     * status of the action. It should return true if any error occurred during
     * the action lifecycle, including:</p>
     *
     * <ul>
     *   <li>Parameter validation failures</li>
     *   <li>Resource initialization errors</li>
     *   <li>Execution exceptions</li>
     *   <li>Output processing errors</li>
     * </ul>
     *
     * <p>When this method returns true, the execution engine will mark the
     * action as failed and may terminate the execution chain depending on
     * the error handling configuration.</p>
     *
     * @return true if an error occurred, false if execution was successful
     * @since 1.0.0
     */
    boolean checkError();

    /**
     * Returns the configuration object for this action.
     *
     * <p>The configuration contains all metadata and parameters needed to
     * execute this action, including:</p>
     *
     * <ul>
     *   <li>Action ID and name for identification</li>
     *   <li>Action type for registry lookup</li>
     *   <li>Input parameter definitions and default values</li>
     *   <li>Output parameter definitions</li>
     *   <li>Action-specific configuration properties</li>
     * </ul>
     *
     * <p>This configuration is typically set during action construction and
     * remains immutable throughout the action's lifecycle.</p>
     *
     * @return the action configuration object, never null
     * @since 1.0.0
     */
    ActionConfig getConfig();

    /**
     * Closes the action and releases any allocated resources.
     *
     * <p>This method is called by the execution engine during cleanup to ensure
     * proper resource management. Implementations should override this method
     * if they allocate resources that need explicit cleanup, such as:</p>
     *
     * <ul>
     *   <li>Network connections</li>
     *   <li>File handles</li>
     *   <li>Database connections</li>
     *   <li>Thread pools</li>
     *   <li>Temporary files</li>
     * </ul>
     *
     * <p>The default implementation does nothing. Implementations should be
     * idempotent and handle multiple calls gracefully.</p>
     *
     * <p>Example implementation:</p>
     * <pre>{@code
     * @Override
     * public void close() {
     *     if (connection != null && !connection.isClosed()) {
     *         try {
     *             connection.close();
     *         } catch (Exception e) {
     *             log.warn("Error closing connection", e);
     *         }
     *     }
     * }
     * }</pre>
     *
     * @since 1.0.0
     */
    void close();

}
