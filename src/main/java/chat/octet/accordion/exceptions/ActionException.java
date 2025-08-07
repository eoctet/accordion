package chat.octet.accordion.exceptions;


/**
 * Runtime exception thrown when errors occur during individual action execution.
 *
 * <p>ActionException is thrown when specific actions encounter errors during
 * their execution lifecycle. This includes problems with action configuration,
 * parameter validation, resource access, or business logic failures within
 * individual actions.</p>
 *
 * <p>Common scenarios that trigger this exception:</p>
 * <ul>
 *   <li><strong>Configuration Errors</strong>: Invalid action parameters, missing required fields</li>
 *   <li><strong>Validation Failures</strong>: Parameter type mismatches, constraint violations</li>
 *   <li><strong>Resource Access</strong>: Network failures, file system errors, database issues</li>
 *   <li><strong>Business Logic</strong>: Application-specific validation or processing errors</li>
 *   <li><strong>Integration Issues</strong>: API failures, service unavailability, authentication problems</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>In Action Implementation:</strong></p>
 * <pre>{@code
 * @Override
 * public ExecuteResult execute() throws ActionException {
 *     try {
 *         // Action logic here
 *         return ExecuteResult.success();
 *     } catch (IOException e) {
 *         throw new ActionException("Failed to read configuration file", e);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Action Registration:</strong></p>
 * <pre>{@code
 * try {
 *     ActionService action = register.build(config);
 * } catch (ActionException e) {
 *     log.error("Failed to create action {}: {}", config.getId(), e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Parameter Validation:</strong></p>
 * <pre>{@code
 * if (StringUtils.isBlank(url)) {
 *     throw new ActionException("URL parameter is required for API action");
 * }
 * }</pre>
 *
 * <p>This exception extends {@link RuntimeException}, making it an unchecked
 * exception. Actions should catch and handle expected exceptions, converting
 * them to ActionException with meaningful error messages.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see AccordionException
 * @see ActionService
 * @see AbstractAction
 * @since 1.0.0
 */
public class ActionException extends RuntimeException {

    /**
     * Constructs a new ActionException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     * @since 1.0.0
     */
    public ActionException(String message) {
        super(message);
    }

    /**
     * Constructs a new ActionException with the specified detail message and cause.
     *
     * <p>This constructor is useful when wrapping lower-level exceptions that
     * caused the action execution to fail, preserving the original stack trace
     * for debugging purposes.</p>
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause   the underlying cause of this exception
     * @since 1.0.0
     */
    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }
}