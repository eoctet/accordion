package pro.octet.accordion.exceptions;


public class AccordionExecuteException extends RuntimeException {

    public AccordionExecuteException(String message) {
        super(message);
    }

    public AccordionExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
}