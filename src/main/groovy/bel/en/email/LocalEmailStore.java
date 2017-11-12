package bel.en.email;

/**
 * LocalEmailStore is used to locally save emails gotten from email source
 */
public interface LocalEmailStore {

    boolean hasData();

    EmailData getEmailData();

    void releaseLastAccessedEmail();

    void storeEmailData(EmailData d);
}
