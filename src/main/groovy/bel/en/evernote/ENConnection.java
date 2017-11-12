package bel.en.evernote;

import bel.cream2.deamon.DeamonCreamWorker;
import com.evernote.auth.EvernoteAuth;
import com.evernote.auth.EvernoteService;
import com.evernote.clients.ClientFactory;
import com.evernote.clients.NoteStoreClient;
import com.evernote.clients.UserStoreClient;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.userstore.AuthenticationResult;
import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;


/**
 * Connect to Evernote, check version of SDK and remember connection value for later use.
 */
@Log4j2
public class ENConnection {


    private UserStoreClient userStore;
    private NoteStoreClient noteStore;
    private String authToken;
    AuthenticationResult businessAuth;
    private static ENConnection enConnection;

    /**
     * Remember: UserStoreClient, NoteStoreClient and authToken.
     *
     * @param authToken
     * @throws Exception
     */

    public static ENConnection from(String authToken) {

        enConnection = new ENConnection(authToken);
        return enConnection;
    }
    public static ENConnection get() {
        return enConnection;
    }

    private ENConnection(String authToken) {
            this.authToken = authToken;
    }

    public boolean connect() {
        log.traceEntry();
        try {
            EvernoteAuth evernoteAuth = new EvernoteAuth(EvernoteService.PRODUCTION, authToken);
            ClientFactory factory = new ClientFactory(evernoteAuth);
            userStore = factory.createUserStoreClient();
            boolean versionOk = userStore.checkVersion("CREAM",
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                    com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
            if (!versionOk) {
                throw new Exception("Incompatible Evernote client protocol version! YOU NEED TO UPGRADE SDK.");
            }
            noteStore= factory.createNoteStoreClient();
            businessAuth = userStore.authenticateToBusiness();

        } catch (EDAMSystemException es) { // e.g. Reason: RATE_LIMIT_REACHED
            //log.warn("Could not connect to evernote. Reason: {}", es.getErrorCode());
            DeamonCreamWorker.waitForRateLimitOver(es);
            //log.catching(es);
            return log.traceExit(false);
        } catch (Exception e) {
            userStore = null;
            noteStore = null;
            //log.catching(e);
            e.printStackTrace();
            log.warn("Could not connect to evernote. Reason: {}", e.getMessage());
            return log.traceExit(false);
        }

        return log.traceExit(true);
    }


    public UserStoreClient getUserStoreClient() {
        return userStore;
    }

    public NoteStoreClient getNoteStoreClient() { return noteStore; }

    public String getAuthToken() {
        return authToken;
    }

    public String getBusinessAuthToken() throws Exception {

        long ex = businessAuth.getExpiration();
        // TODO: memorize difference to server time
        long curServer = businessAuth.getCurrentTime();
        long curLocal = new DateTime().getMillis();
        long diff = (ex-curLocal)/1000/60;
        //if(Math.abs(curLocal-curServer) >5*1000*60) {
        //    log.warn("big time difference with business token validity. CHECK THAT!");
        //}
        //System.out.println("\n\nBUSINESS TOKEN expires in: " + diff+" min\n\n");
        if(diff < 10) {
            businessAuth = userStore.authenticateToBusiness();
            log.info("RENEW BUSINESS AUTH TOKEN");
        }
        return businessAuth.getAuthenticationToken();

    }

    public boolean isConnected() {
        return userStore != null && noteStore != null;
    }
}
