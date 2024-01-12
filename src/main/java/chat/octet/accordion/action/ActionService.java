package chat.octet.accordion.action;


import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.model.ExecuteResult;
import chat.octet.accordion.core.entity.Session;
import chat.octet.accordion.exceptions.ActionException;

/**
 * Action interface.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
public interface ActionService {

    /**
     * Prepare to execute, initialize and load the parameters of the session,
     * and load the output parameters of the pre-sequence action.
     *
     * @param session Accordion session
     * @return current action service
     */
    ActionService prepare(Session session);

    /**
     * Execute the action.
     *
     * @return Action execution result
     * @throws ActionException Possible exceptions during action execution.
     */
    default ExecuteResult execute() throws ActionException {
        throw new ActionException("Action not implemented.");
    }

    /**
     * Update the output parameters of the current action.
     *
     * @param executeResult Action execution result
     */
    void output(ExecuteResult executeResult);

    /**
     * Check if there is an error during the action execution.
     *
     * @return true if there is an error, otherwise false
     */
    boolean checkError();

    /**
     * Get the configuration for the current action.
     *
     * @return Action configuration
     */
    ActionConfig getConfig();

    /**
     * (Optional) Close action,
     * if you have resources that need to be released, please implement this method.
     */
    void close();

}
