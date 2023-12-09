package pro.octet.accordion.action;


import pro.octet.accordion.action.model.ActionConfig;
import pro.octet.accordion.action.model.ActionResult;
import pro.octet.accordion.core.entity.Session;
import pro.octet.accordion.exceptions.ActionException;

public interface ActionService {

    ActionService prepare(Session session);

    ActionResult execute() throws ActionException;

    void updateOutput(ActionResult actionResult);

    boolean checkError();

    ActionConfig getConfig();

}
