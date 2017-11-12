package bel.en;

import bel.en.data.AbstractConfiguration;
import bel.en.email.EmailProcessor;
import bel.en.evernote.ENConfiguration;
import bel.en.evernote.ENConnection;
import bel.en.evernote.ENSharedNotebook;
import bel.en.localstore.NoteStoreLocal;
import bel.en.localstore.SyncHandler;
import bel.en.usecases.EmailToEvernoteHistory;

/**
 * This one is sitting and listening for Emails, that are delivered to
 * crm@v-und-s.de.
 * There are several different functions, that can be triggered with an email:
 * 1.) adding data to crm history (the subject as link, the body as complete email in a separate note)
 * Therefore, the target can be determined
 *  a) by first email in field "TO"
 *  b) or, when forwarding an email, just copy the email at the very beginning of the SUBJECT field
 * The target will be found in two ways:
 *  a) the email can match exactly. This is tried first. (AND IMPLEMENTED at the moment)
 *  b) if a fails, then the domain may be a match.
 *  if b fails, too, the user gets an error message, because there was no chance to map the email.
 *
 *
 */
public class MainEmailProcessor {

    private ENConnection enConnection;
    ENSharedNotebook mailsNotebook;
    ENSharedNotebook inboxNotebook;

    void connect() throws Exception {
        ENConfiguration configuration = null; // TODO get local version, if EN is not available...
        ENSharedNotebook enSharedConfigNotebook;
        try {
            enConnection = ENConnection.from(Main.AUTH_TOKEN);
            if (  enConnection.connect() ) {
                enSharedConfigNotebook = new ENSharedNotebook(enConnection, Main.CONFIG_NOTEBOOK_SHARE_NAME);
                configuration = new ENConfiguration(enSharedConfigNotebook, enConnection);
            }
        } catch (Exception e1) {
            System.out.println("COULD NOT CONNECT TO EVERNOTE. REASON: " + e1);
            System.out.println("Offline! Keine Verbindung zu EN-Server. Synchronisiere später...");
            enSharedConfigNotebook = null;
            configuration = null;
        }


        // install local store, install sync handler in order to be able to find the
        // fitting email, etc in data fast...
        SyncHandler.init(enConnection, new NoteStoreLocal(configuration));
        mailsNotebook = new ENSharedNotebook(enConnection, AbstractConfiguration.getConfig().getCreamNotebooks().getMailsNotebook());
        inboxNotebook = new ENSharedNotebook(enConnection, AbstractConfiguration.getConfig().getCreamNotebooks().getInboxNotebook());

    }

    /*
    private void runSyncerInThread() {
        System.out.println("going to start sync thread...");
        Runnable syncer = new Runnable() {

            @Override
            public void run() {
                boolean interrupted = false;
                do {
                    SyncHandler.get().sync();
                    try {
                        Thread.sleep(1000*60*5);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                } while(!interrupted);
            }
        };
        new Thread(syncer).start();
        System.out.println("started sync thread successfully");
    }
*/

    private void runEmailProcessor() throws Exception {
        EmailProcessor ep = new EmailProcessor(NoteStoreLocal.NOTE_STORE_BASE+"/mails"); // TODO: Pull login data here, make email processor a module
        ep.register(new EmailToEvernoteHistory( mailsNotebook));
        //ep.register(new EmailToEvernoteInbox( inboxNotebook));
        while(true) {
            // poll every 30 seconds for new mails
            while(ep.hasMessages() == 0) {
                System.out.println("No Mail available. Sleeping...");
                Thread.sleep(30*1000);
            }

            // if new mails are there, FIRST sync, than execute the mails
            SyncHandler.get().sync();
            ep.readAndDispatch();
        }
    }


    public static void main(String[] args) throws Exception {

        MainEmailProcessor mp = new MainEmailProcessor();
        mp.connect();
        mp.runEmailProcessor();


    }

}
