package chat.octet.accordion.core.entity;


import chat.octet.accordion.action.AbstractAction;

/**
 * Message entity for passing initial data to accordion execution plans.
 *
 * <p>Message extends {@link Tuple} to provide a structured way to pass initial
 * parameters and data to an accordion execution. It serves as the entry point
 * for external data into the execution workflow.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li><strong>Structured Data</strong>: Inherits key-value storage from Tuple</li>
 *   <li><strong>Type Safety</strong>: Supports typed parameter access</li>
 *   <li><strong>Initialization</strong>: Provides initial context for action execution</li>
 *   <li><strong>Serializable</strong>: Can be persisted and transmitted</li>
 * </ul>
 *
 * <p>Usage Examples:</p>
 *
 * <p><strong>Creating and Populating a Message:</strong></p>
 * <pre>{@code
 * Message message = new Message();
 * message.put("userId", "12345");
 * message.put("action", "processOrder");
 * message.put("timestamp", System.currentTimeMillis());
 * }</pre>
 *
 * <p><strong>Using with Accordion:</strong></p>
 * <pre>{@code
 * try (Accordion accordion = new Accordion(plan)) {
 *     ExecuteResult result = accordion.play(message, true);
 * }
 * }</pre>
 *
 * <p><strong>Accessing in Actions:</strong></p>
 * <pre>{@code
 * // In action implementation
 * Message message = session.getValue(AbstractAction.ACCORDION_MESSAGE, Message.class);
 * String userId = message.getString("userId");
 * }</pre>
 *
 * <p>The message is automatically made available to all actions in the execution
 * plan through the session under the key {@link AbstractAction#ACCORDION_MESSAGE}.</p>
 *
 * <p>Thread Safety: This class inherits thread safety characteristics from its
 * parent {@link Tuple} class.</p>
 *
 * @author <a href="https://github.com/eoctet">William</a>
 * @see Tuple
 * @see Session
 * @see AbstractAction#ACCORDION_MESSAGE
 * @since 1.0.0
 */
public class Message extends Tuple<String, Object> {

}
