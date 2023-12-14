package pro.octet.accordion.action;

import lombok.extern.slf4j.Slf4j;
import pro.octet.accordion.action.api.ApiAction;
import pro.octet.accordion.action.base.ConditionAction;
import pro.octet.accordion.action.base.SwitchAction;
import pro.octet.accordion.action.base.TestAction;
import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.core.enums.ActionType;


@Slf4j
public final class ActionFactory {
    private static volatile ActionFactory factory;

    private ActionFactory() {
    }

    public static ActionFactory getInstance() {
        if (factory == null) {
            synchronized (ActionFactory.class) {
                if (factory == null) {
                    factory = new ActionFactory();
                }
            }
        }
        return factory;
    }

    public ActionService build(ActionType actionType, ActionConfig actionConfig) {
        ActionService service;
        switch (actionType) {
            case API:
                service = new ApiAction(actionConfig);
                break;
            case CONDITION:
                service = new ConditionAction(actionConfig);
                break;
            case SWITCH:
                service = new SwitchAction(actionConfig);
                break;
            case TEST:
                service = new TestAction(actionConfig);
                break;
            default:
                throw new IllegalArgumentException("Action type illegal error");
        }
        return service;
    }

}
