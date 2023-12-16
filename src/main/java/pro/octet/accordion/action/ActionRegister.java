package pro.octet.accordion.action;


import com.google.common.collect.Maps;
import pro.octet.accordion.action.api.ApiAction;
import pro.octet.accordion.action.base.ConditionAction;
import pro.octet.accordion.action.base.SwitchAction;
import pro.octet.accordion.action.base.TestAction;
import pro.octet.accordion.action.eamil.EmailAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.script.ScriptAction;
import pro.octet.accordion.action.shell.ShellAction;
import pro.octet.accordion.core.enums.ActionType;
import pro.octet.accordion.exceptions.ActionException;

import java.util.Map;

public class ActionRegister {

    private static volatile ActionRegister register;
    private final static Map<String, String> ACTION_MAPPING = Maps.newLinkedHashMap();

    static {
        ACTION_MAPPING.put(ActionType.API.name(), ApiAction.class.getName());
        ACTION_MAPPING.put(ActionType.CONDITION.name(), ConditionAction.class.getName());
        ACTION_MAPPING.put(ActionType.SWITCH.name(), SwitchAction.class.getName());
        ACTION_MAPPING.put(ActionType.EMAIL.name(), EmailAction.class.getName());
        ACTION_MAPPING.put(ActionType.SHELL.name(), ShellAction.class.getName());
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

    public boolean isRegistered(String actionType) {
        return ACTION_MAPPING.containsKey(actionType);
    }

    public boolean register(ActionType actionType, String className) {
        return register(actionType.name(), className);
    }

    public boolean register(String actionType, String className) {
        if (isRegistered(actionType)) {
            return false;
        }
        ACTION_MAPPING.put(actionType, className);
        return true;
    }

    public ActionService build(ActionConfig actionConfig) {
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
