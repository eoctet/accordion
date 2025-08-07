package chat.octet.accordion.exceptions;


/**
 * Runtime exception thrown when errors occur during accordion plan execution.
 *
 * <p>AccordionException is the primary exception type for errors that occur
 * during the execution of accordion plans. It indicates problems with plan
 * structure, execution flow, or system-level issues that prevent successful
 * completion of the automation workflow.</p>
 *
 * <p>Common scenarios that trigger this exception:</p>
 * <ul>
 *   <li><strong>Plan Structure Issues</strong>: Invalid DAG structure, circular dependencies</li>
 *   <li><strong>Execution Errors</strong>: Unhandled action failures, resource exhaustion</li>
 *   <li><strong>Configuration Problems</strong>: Invalid JSON configuration, missing actions</li>
 *   <li><strong>System Issues</strong>: Resource allocation failures, unexpected system state</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Plan Validation:</strong></p>
 * <pre>{@code
 * try {
 *     AccordionPlan plan = AccordionPlan.of().importConfig(jsonConfig);
 * } catch (AccordionException e) {
 *     log.error("Invalid plan configuration: {}", e.getMessage());
 * }
 * }</pre>
 *
 * <p><strong>Execution Handling:</strong></p>
 * <pre>{@code
 * try (Accordion accordion = new Accordion(plan)) {
 *     ExecuteResult result = accordion.play();
 * } catch (AccordionException e) {
 *     log.error("Execution failed: {}", e.getMessage(), e);
 *     // Handle execution failure
 * }
 * }</pre>
 *
 * <p>This exception extends {@link RuntimeException}, making it an unchecked
 * exception that doesn't require explicit handling but should be caught
 * for proper error management.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see ActionException
 * @see AccordionPlan
 * @see Accordion
 * @since 1.0.0
 */
public class AccordionException extends RuntimeException {

    /**
     * Constructs a new AccordionException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     * @since 1.0.0
     */
    public AccordionException(String message) {
        super(message);
    }

    /**
     * Constructs a new AccordionException with the specified detail message and cause.
     *
     * <p>This constructor is useful when wrapping lower-level exceptions that
     * caused the accordion execution to fail.</p>
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause   the underlying cause of this exception
     * @since 1.0.0
     */
    public AccordionException(String message, Throwable cause) {
        super(message, cause);
    }
}