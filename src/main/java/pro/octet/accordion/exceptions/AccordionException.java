package pro.octet.accordion.exceptions;


public class AccordionException extends RuntimeException {

    public AccordionException(String message) {
        super(message);
    }

    public AccordionException(String message, Throwable cause) {
        super(message, cause);
    }
}