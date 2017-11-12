package bel.en.deamon;

import bel.en.Entry;
import bel.en.data.AbstractConfiguration;
import bel.en.evernote.ENAuth;
import bel.en.evernote.ENConnection;
import bel.en.evernote.ENSharedNotebook;
import bel.util.Util;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Note;
import com.evernote.edam.userstore.AuthenticationResult;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thread that runs parallel, grabs to connect lock by polling and
 * switches to deamon-mode, if it can grab the lock.
 * Deamon-mode means, that it does the CRREAM-Services for
 * theTODO-Overviews, for emails etc.
 */
@Log4j2
public class DeamonCream implements Runnable {

    //
    // Singleton
    //
    private static DeamonCream ourInstance;

    public static DeamonCream get() {
        if (ourInstance == null) {
            ourInstance = new DeamonCream();
        }
        return ourInstance;
    }

    private DeamonCream() {}

    //
    // lock
    //
    private String session;
    private Note lock;
    private ENSharedNotebook enSharedConfigNotebook;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // TODO: including offsett V

    //
    // controlling deamon thread
    //
    private boolean initialized = false;
    private boolean stopDeamon = true;
    private Thread deamon;

    public boolean isStoppedDeamon() {
        return stopDeamon;
    }

    /**
     * start the deamon. This can be done several times, since network failure may lead to stopping...
     */
    public boolean start() {

        log.traceEntry();
        stopDeamon = false; // after restart
        try {
            if(!initialized) {
                session = AbstractConfiguration.getConfig().getCurrentUser().getShortName() + Long.toString(System.currentTimeMillis());
                enSharedConfigNotebook = new ENSharedNotebook(ENConnection.get(), Entry.CONFIG_NOTEBOOK_SHARE_NAME);
                initialized = true;
            }
            deamon = new Thread(this);
            deamon.start();
            return log.traceExit(true);
        } catch (Exception e) {
            //log.catching(e);
            log.warn("cream-deamon not started because of exception: " + e.getMessage());
        }
        return log.traceExit(false);
    }

    /**
     * Stop the deamon
     * This will happen from another thread!
     */
    public void releaseLockAndStop() {
        log.info("Going to stop DEAMON-Mode and release lock...");
        //synchronized (this) {
            releaseLock();
        //}
        stopDeamon = true;
        if(deamon != null) {
            deamon.interrupt();
        }
    }

    //
    // working loop
    //
    @Override
    public void run() {
        while(! stopDeamon) {
            try {
                //synchronized (this) {
                    if(hasLock() || tryToGrabLock()) {
                            // do the work
                            //log.error("TODO: DEAMON still IS DOING NOTHING");

                            //DeamonCreamWorker dcw = new DeamonCreamWorker();
                            //dcw.doIt();


                    }
                    //long validTo = getValidityDurationMSecs();
                    deamon.sleep(60*1000);
                //}

            }catch (InterruptedException e) {
                if(stopDeamon) {
                    log.info("INTERUPDED deamon thread. STOPPING normal.");
                    System.out.println("INTERUPDED deamon thread. STOPPING normal.");
                } else {
                    log.warn("INTERUPDED deamon thread. Stopping ABNORMAL.");
                    System.err.println("INTERUPDED deamon thread. Stopping ABNORMAL.");
                    stopDeamon = true;
                    log.catching(e);
                }
            }
        }
        log.info("STOPPING DEAMON...");
    }

    int recCounter = 0;
    /**
     * if lock was grabbed, then:
     * lock != null
     * enSharedConfigNotebook != null
     * @return true, if the lock was grabbed
     */
    public boolean tryToGrabLock() {
        //log.traceEntry();
        try {                lock = null;
                val notes = getLockNote();
                int size = notes.size();
                if (size == 0) {
                    lock = enSharedConfigNotebook.createNote(getLockTitleWithUserAndTimeStamp(false));
                    return log.traceExit("found no lock-note. Created new... ", true);
                } else if (size == 1) {
                    lock = notes.get(0);
                    String lockStr = lock.getTitle();
                    List<String> strings = parseLockTitle(lockStr);
                    String timeStr = strings.get(1);
                    String sessionStr = strings.get(2);
                    if(!sessionStr.equals(session)) { // another session
                        if(ageOfLockInSeconds(timeStr) > 5*60 || Entry.NO_DEAMON_RUNNING.equals(sessionStr)) { // that is older than 5 minutes and belongs somebody else OR released. GRAB
                            lock.setTitle(getLockTitleWithUserAndTimeStamp(false));
                            lock = enSharedConfigNotebook.updateNote(lock);
                            if(Entry.NO_DEAMON_RUNNING.equals(sessionStr)) {
                                return log.traceExit("lock-note existed and was free: " + lockStr, true);
                            } else {
                                return log.traceExit("grabbed other old lock: " + lockStr, true);
                            }
                        }
                    } else {
                        if (ageOfLockInSeconds(timeStr) > 3 * 60) { // that is older than 3 minutes and belongs us. RENEW
                            lock.setTitle(getLockTitleWithUserAndTimeStamp(false));
                            lock = enSharedConfigNotebook.updateNote(lock);
                            return log.traceExit("renewed own lock: " + lockStr, true);
                        }
                    }
                } else {
                    log.error("Error grabbing lock: more than one lock-note in config notebook. deleting all manually...");
                    throw new Exception("PLEASE DELETE THE MANY LOCK-FILES MANUALLY. Automatic deletion is not implemented.");
                }
        } catch (Exception e) {
            log.warn("problem during lock grabbing: " + getExcepitonPlainText(e));
            //log.catching(e);
            if(e instanceof EDAMUserException) {
                if (((EDAMUserException)e).getErrorCode().equals(EDAMErrorCode.AUTH_EXPIRED)) {
                    //ENConnection.get().connect();
                    try {

                        // TODO: put all that reconnection code in the ENSharedNotebook funktions
                        // OR put reconnect wrappers
                        log.info("Auth expired. Going to reonnect...");
                        ENConnection enConnection = ENAuth.get().connectToEvernote();
                        AuthenticationResult ar = enConnection.getUserStoreClient().authenticateToBusiness();
                        log.info("business token: " + ar.getAuthenticationToken());
                        log.info("valid (expirationTime - currentTime): " + (Util.readableTime(ar.getExpiration() - ar.getCurrentTime())));
                        log.info("public user info: " + ar.getPublicUserInfo());

                        // TODO: ENSharedNotebook works, when:
                        // TODO: ENSharedNotebook.sharedAuthToken is replaced by the renewed business-token. (ar.getAuthenticationToken()) That will work


                        if(recCounter == 0) {
                            recCounter++;
                            log.info("SECOND TIME tryToGrabLock");
                            boolean result =  tryToGrabLock();
                            recCounter--;
                            return result;
                        }
                    } catch (Exception e1) {
                        log.catching(e1);
                        log.info("RECONNECT FAILED");
                    }
                }
            }
        }
        return log.traceExit("Could not grab the lock...", false);
    }

    private String getExcepitonPlainText(Exception e) {
        if(e instanceof EDAMUserException) {
            return "EDAMUserException: " + ((EDAMUserException)e).getErrorCode().toString();
        } else if (e instanceof EDAMSystemException) {
            return "EDAMSystemException: " +((EDAMSystemException)e).getErrorCode().toString();
        } else {
            return e.getClass().toString() + e.getMessage();
        }
    }

    /**
     * @return notes in config notebook that have the LOCK_STR in title
     * @throws Exception
     */
    private List<Note> getLockNote() throws Exception {
        return enSharedConfigNotebook.findNotes("intitle:[" + Entry.CREAM_DEAMON_LOCK_STR + "]");
    }

    /**
     * Mark the lock, so that it can be grabbed by another CREAM deamon immediately...
     */
    public void releaseLock() {
        try {
            if(hasLock()) {
                lock.setTitle(getLockTitleWithUserAndTimeStamp(true));
                enSharedConfigNotebook.updateNote(lock);
            }
        } catch (Exception e) {
            log.warn("release of deamon-lock failed. But should be no problem... After 5 minutes, another one will take over.");
        }
    }

    /**
     * Do we own the lock? Attention: WE DO CHECK the age of the lock. If it is older than some minutes, we assume,
     * that another CREAM deamon will have aquired it. So we return false!
     * If this has an exception, indicating some trouble with network, the deamon will be stopped...
     * @return true, if 1) online and 2) the lock was found 3) has the same sessionID than this deamon and is younger than 4:30 min
     */
    public boolean hasLock() {
        //log.traceEntry();
        try {
            if(ENConnection.get() != null && ENConnection.get().isConnected()) {
                List<Note> notes = getLockNote();
                if(notes.size() == 1) {
                    lock = notes.get(0);
                    List<String> strings = parseLockTitle(lock.getTitle());
                    String sessionStr = strings.get(2);
                    if(sessionStr.equals(session)) {
                        String timeStr = strings.get(1);
                        if(ageOfLockInSeconds(timeStr) < 4 * 60 + 30) {
                            return log.traceExit("This CREAM owns the lock...", true);
                        } else {
                            log.trace("the lock by this CREAM but needs to be renewed!");
                        }
                    } else {
                        log.trace("the lock is not held by this CREAM!");
                    }
                }
            }
        } catch (Exception e) {
            if(e instanceof EDAMUserException) {
                log.warn("EDAMException: " + ((EDAMUserException)e).getErrorCode());
            } else {
                //log.warn("STOPPING DEAMON due to exception in hasLock()" + e.getMessage());
                log.warn("Exception during checking of deamon lock: " + getExcepitonPlainText(e));
                //log.catching(e);
                //stopDeamon = true;
            }
        }
        lock = null;
        return log.traceExit("We dont have the deamon lock.", false);
    }

    /**
     * @param dateTimeAsString parsable LocaleDateTime
     * @return seconds of since that dateTime
     */
    public long ageOfLockInSeconds(String dateTimeAsString) {
        val fromWhenIsLock = LocalDateTime.parse(dateTimeAsString, formatter);
        val now = LocalDateTime.now();
        Duration duration = Duration.between(fromWhenIsLock, now);
        return duration.getSeconds();
    }


    /**
     * @param title original title of the note
     * @return a list with 3 elements:
     * 0: CREAM_DEAMON_LOCK_STR
     * 1: the LocalDateTeime
     * 2: NO_DEAMON_RUNNING or the BEL1234556 (session)
     */
    private List<String> parseLockTitle(String title) {
        String[] result = title.split(",");
        if(result.length != 3) throw new RuntimeException("lock title parsed wrong... " + title);
        return Arrays.stream(result).map(String::trim).collect(Collectors.toList());
    }

    /**
     * @param releaseLock when true, this returns the String with NO_DEAMON_RUNNING indicating a released lock
     *                    when false, this creates title for a grabbed lock
     * @return the complete note title for a lock (either grabed or released)
     */
    private String getLockTitleWithUserAndTimeStamp(boolean releaseLock) {
        val dateTime = LocalDateTime.now();
        //dateTime.atZone(ZoneId.of("Z")); // UTC eben gerade NICHT!
        String now = dateTime.format(formatter);
        return Entry.CREAM_DEAMON_LOCK_STR + ",  " + now + ",  " + (releaseLock?Entry.NO_DEAMON_RUNNING:session);
    }


}
