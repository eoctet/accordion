package chat.octet.accordion.exceptions;


public class ActionException extends RuntimeException {

    public ActionException(String message) {
        super(message);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }
}