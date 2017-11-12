package bel.en.email;

import java.util.logging.Logger;

/**
 * main loop of email processing
 */
public class EmailProcessor {

    private static final Logger LOGGER = Logger.getLogger(EmailProcessor.class.getName());

    /*
     * Login data to exchange server
     */

    String user = "crm";
    String pw = "c_r-M17";
    String urlService = "https://vunds.epc-cloud.de/EWS/Exchange.asmx";
    String from = "crm@v-und-s.de";

    EmailSource reader;
    EmailDrain writer;

    private String localEmailStorePath = "emails_locale";
    private LocalEmailStoreFileBased localEmailStore;
    private LocalEmailStoreFileBased localEmailExceptionsStore;

    private EmailDispatcher dispatcher;

    private boolean recover = true;



    public EmailProcessor(String rootFolder) throws Exception {
        reader = new EmailReaderExchange(urlService, user, pw);
        writer = new EmailSenderExchange(urlService, user, pw, from);
        dispatcher = new EmailDispatcher(writer);
        localEmailStore = new LocalEmailStoreFileBased(rootFolder+"/"+localEmailStorePath);
        localEmailStore.init();
        localEmailExceptionsStore = new LocalEmailStoreFileBased(rootFolder+"/"+localEmailStorePath + "/caused_exceptions");
        localEmailExceptionsStore.init();
    }


    /**
     * this can be emulated by

     */
    public void start() {


        while (true) {
            try {
                if (recover) {
                    LOGGER.info("going to recover old mail that have been locally stored!");
                    dispatchMessagesFromLocalStore();
                    recover = false;
                }

                // poll every 30 seconds for new mails
                while(hasMessages() == 0) {
                    Thread.sleep(30*1000);
                }

                readAndDispatch();
            } catch (Exception e) {
                LOGGER.severe("Email processing failed! Stopping CRM-Mailer!" + e);
                System.exit(-1);
            }
        }
    }

    public void readAndDispatch() throws Exception {
        EmailData[] mails = reader.readNextMessages();
        if (mails.length > 0) {
            LOGGER.info("saving new mails from email source");
            for (int i = 0; i < mails.length; i++) {
                EmailData mail = mails[i];
                localEmailStore.storeEmailData(mail);
            }
            dispatchMessagesFromLocalStore();
        } else {
            //LOGGER.info("no mail");
        }
    }

    public int hasMessages() throws Exception {
        return reader.hasMessages();
    }


    private void dispatchMessagesFromLocalStore() throws Exception {
        while (localEmailStore.hasData()) {

            EmailData d = localEmailStore.getEmailData();
            LOGGER.info("processing next email");

            dispatcher.dispatch(d);
            if(dispatcher.isLastEmailCausedException()) {
                localEmailExceptionsStore.storeEmailData(d);
            }
            localEmailStore.releaseLastAccessedEmail();
        }
    }

    public void register(EmailInterpreter i) {dispatcher.register(i);}
}
