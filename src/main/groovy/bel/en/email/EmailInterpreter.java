package bel.en.email;


/**
 * EmailInterpreters are implemented as chain of responsability.
 */
public interface EmailInterpreter {

    /**
     * Trys to interpret and operate upon an email.
     *
     * @param email email to operate on
     * @return true, if this EmailInterpreter was responsible, false otherwise
     */
    public boolean tryAction(EmailData email, EmailDrain drain) throws ActionFailedException;

    /**
     * @return a human understandable name for the email interpreter.
     */
    String getName();

    /**
     * @return a human understandable help string that will be sent to the user if requsted.
     */
    String getHelp();
}
