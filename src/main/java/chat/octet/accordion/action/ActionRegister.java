package chat.octet.accordion.action;


import chat.octet.accordion.action.api.ApiAction;
import chat.octet.accordion.action.base.ConditionAction;
import chat.octet.accordion.action.base.SwitchAction;
import chat.octet.accordion.action.base.TestAction;
import chat.octet.accordion.action.email.EmailAction;
import chat.octet.accordion.action.model.ActionConfig;
import chat.octet.accordion.action.script.ScriptAction;
import chat.octet.accordion.core.enums.ActionType;
import chat.octet.accordion.exceptions.ActionException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Action register, Manage and register all actions.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
public class ActionRegister {

    private static volatile ActionRegister register;
    private final static Map<String, String> ACTION_MAPPING = Maps.newLinkedHashMap();

    static {
        ACTION_MAPPING.put(ActionType.API.name(), ApiAction.class.getName());
        ACTION_MAPPING.put(ActionType.CONDITION.name(), ConditionAction.class.getName());
        ACTION_MAPPING.put(ActionType.SWITCH.name(), SwitchAction.class.getName());
        ACTION_MAPPING.put(ActionType.EMAIL.name(), EmailAction.class.getName());
        ACTION_MAPPING.put(ActionType.SCRIPT.name(), ScriptAction.class.getName());
        ACTION_MAPPING.put(ActionType.TEST.name(), TestAction.class.getName());
    }

    private ActionRegister() {
    }

    public static ActionRegister getInstance() {
        if (register == null) {
            synchronized (ActionRegister.class) {
                if (register == null) {
                    register = new ActionRegister();
                }
            }
        }
        return register;
    }


    /**
     * Check if the action type is registered.
     *
     * @param actionType Action type
     * @return true if registered, false otherwise.
     * @see ActionType
     */
    public boolean isRegistered(String actionType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(actionType), "Action type cannot be empty.");
        return ACTION_MAPPING.containsKey(actionType);
    }

    /**
     * Register action.
     *
     * @param actionType Action type
     * @param className  Action class name
     * @return Returns true if the registration is successful, otherwise false.
     * @see ActionType
     */
    public boolean register(ActionType actionType, String className) {
        return register(actionType.name(), className);
    }

    /**
     * Register action.
     *
     * @param actionType Action type
     * @param className  Action class name
     * @return Returns true if the registration is successful, otherwise false.
     * @see ActionType
     */
    public boolean register(String actionType, String className) {
        if (isRegistered(actionType)) {
            return false;
        }
        Preconditions.checkArgument(StringUtils.isNotBlank(className), "Action class name cannot be empty.");
        ACTION_MAPPING.put(actionType, className);
        return true;
    }

    /**
     * Remove a registered action
     *
     * @param actionType Action type
     * @return Returns true on success, otherwise false.
     */
    public boolean unregister(String actionType) {
        if (!isRegistered(actionType)) {
            return false;
        }
        ACTION_MAPPING.remove(actionType);
        return true;
    }

    /**
     * Build action service.
     *
     * @param actionConfig Action config
     * @return Action service
     * @throws ActionException If the corresponding action type cannot be found, throw an exception
     */
    public ActionService build(ActionConfig actionConfig) {
        Preconditions.checkArgument(StringUtils.isNotBlank(actionConfig.getId()), "Action ID cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(actionConfig.getActionType()), "Action type cannot be empty.");
        Preconditions.checkArgument(StringUtils.isNotBlank(actionConfig.getActionName()), "Action name cannot be empty.");

        String className = ACTION_MAPPING.get(actionConfig.getActionType());
        if (className == null) {
            throw new ActionException("Action type " + actionConfig.getActionType() + " is not registered.");
        }

        try {
            Class<?> clazz = Class.forName(className);
            return (ActionService) clazz.getConstructor(ActionConfig.class).newInstance(actionConfig);
        } catch (Exception e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

}
