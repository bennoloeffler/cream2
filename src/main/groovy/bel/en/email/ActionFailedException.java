package bel.en.email;


/**
 * thrown in case scipt fails
 */
public class ActionFailedException extends Exception {
    public ActionFailedException(String message) {
        super(message);
    }
    public ActionFailedException(String message, Exception e) {
        super(message, e);
    }
}
