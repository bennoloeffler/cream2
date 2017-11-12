package bel.en.email;

/**
 * EmailSource represents a abstract concept to access a source of emails,
 * without folders, states, ... just get the next one... and delete it on
 * the store.
 */
public interface EmailSource {

    /**
     * read next email messages and delete them physically from the source
     * @return email messages
     */
    EmailData[] readNextMessages() throws Exception;

    public int hasMessages() throws Exception;
}
