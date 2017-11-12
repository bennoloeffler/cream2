package bel.en.email;

/**
 * EmailDrain
 */
public interface EmailDrain {
    void send(String to, String subject, String text) throws Exception;
}
