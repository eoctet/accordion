package pro.octet.accordion.action;


import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.core.entity.Session;
import pro.octet.accordion.exceptions.ActionException;

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
     * @return Action result
     * @throws ActionException Possible exceptions during action execution.
     */
    default ActionResult execute() throws ActionException {
        throw new ActionException("Action not implemented.");
    }

    /**
     * Update the output parameters of the current action.
     *
     * @param actionResult Action result
     */
    void updateOutput(ActionResult actionResult);

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

}
