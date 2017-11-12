package bel.en.localstore;

import bel.en.data.*;
import bel.en.evernote.*;
import bel.util.Util;
import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Note;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Pair;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * holds local notes up to data.
 * holds notes in evernote up to date.
 * detects conficts. Marks conflicting notes.
 *
 * TODO: think about merging with "source code mering mechanism", append both old versions at the end
 *
 * WHAT is TO BE DONE:
 *
 * Scenario 1: network available
 * Usecase:
 * a) no update client side, no update server side (client: updateCount >= 0, server: updateChunk does not contains note)
 *      ACTION: ignore
 * b) update client side, no update server side (client: updateCount -1, server updateChunk does not contain note)
 *      ACTION: just update server side and set the local note to "clean"
 * c) no update client side, update server side (client: updateCound >=0, server updateChunk does contain note)
 *      X: the update server side was caused by another source
 *          ACTION: just read the note including content and remove the old local one
 *      Y: the update server side was caused by the client itself and the updateCount was remembered
 *          ACTION: ignore. everything in sync.
 * d) update on both sides (client: updateCound >=0, server updateChunk does contain note)
 *      ACTION: merge, set local and remote
 *
 *
 * Scenario 2: network NOT available
 *
 */
@Log4j2
public class SyncHandler {

    /**
     * singleton handling
     */

    private static SyncHandler singleton = null;

    public static SyncHandler get() {
        if(singleton==null) {
            throw new RuntimeException("SyncHandler not yet initialized");
        } else {
            return singleton;
        }
    }

    public static void init(ENConnection connection, NoteStoreLocal noteStoreLocal) throws Exception {
        //log.setLevel(Level.INFO);
        if(singleton==null) {
            //singleton = new SyncHandler(connection, noteStoreLocal, sharedNotebook);
        } else {
            //throw new RuntimeException("SyncHandler already initialized");
            log.warn("Initialized SyncHandler second time...");
        }
        singleton = new SyncHandler(connection, noteStoreLocal);
    }


    public static String SYNC_INFO_DIR = "C:\\creamlocal\\syncinfo";
    public static String SYNC_INFO_FILE = SYNC_INFO_DIR + "\\syncStamps";
    public static String SYNC_INFO_FILE_SUFFIX = ".txt";


    // to buffer the infos from SyncChunk

    // used for local storage of sync state
    int updateCount;
    long lastSync;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // the place where local and remote notes are stored
    private final ENConnection connection;
    NoteStoreLocal noteStoreLocal;
    ENSharedNotebookGroup sharedNotebookGroup;


    // listener for GUI update of progress
    private SyncProgress syncProgress = null;


    /**
     * singleton constructor
     * @param noteStoreLocal local store
     * @param
     */
    private SyncHandler(ENConnection connection, NoteStoreLocal noteStoreLocal) throws Exception{
        //log.setLevel(Level.INFO);
        assert(connection != null);
        this.connection = connection;
        this.noteStoreLocal = noteStoreLocal;
        try {
            String defaultNotebook = AbstractConfiguration.getConfig().getCreamNotebooks().getDefaultNotebook();
            ArrayList<String> groupNotebooks = AbstractConfiguration.getConfig().getCreamNotebooks().getGroupNotebooks();
            sharedNotebookGroup = new ENSharedNotebookGroup(connection, defaultNotebook, groupNotebooks);
        } catch (Exception e) {
            log.info("coud not connect. but thats ok... WORKING OFFLINE");
        }
    }


    public void sync(SyncProgress syncProgress) {
        this.syncProgress = syncProgress;
        sync();
    }


    private void updateSyncCounter(int count, int ofHowMany ) {
        if(syncProgress != null) {
            count = 100*count/ofHowMany;
            syncProgress.count(count>100?100:count);
        }
    }

    /**
     * log messages to GUI
     * @param log
     */
    private void syncLog(String log) {
        if(syncProgress != null) {
            syncProgress.message(log);
        }
    }

    /**
     * This is what the spec says:
     * 2. If the client has never synched with the service before, continue to Full Sync.
     * 3. NoteStore.getSyncState …) to get the server’s updateCount and fullSyncBefore values.
     * a. If ( fullSyncBefore > lastSyncTime ), continue to Full Sync.
     * b. If ( updateCount = lastUpdateCount ) , the server has no updates. Skip to Send Changes.
     * c. Otherwise, perform an incremental update sync (go to Incremental Sync)
     * see: https://dev.evernote.com/media/pdf/edam-sync.pdf, page 7
     */
    public void sync() {
        if(syncProgress!=null){syncProgress.clear();}
        Date date = new Date(System.currentTimeMillis());
        DateFormat f = new SimpleDateFormat("HH:mm:ss");
        syncLog("its time for a NEW SYNC, time: " + f.format(date));
        if(!connection.connect()) {
            SwingUtilities.invokeLater(() -> {
                fireDataChange(SyncHandler.this);
                syncLog("Im Moment kein Sync möglich. Keine Verbindung.");
            });
            return; // no connection... skip this time...
        } else {
            if(sharedNotebookGroup == null) {
                try {
                    sharedNotebookGroup = new ENSharedNotebookGroup(connection,
                            AbstractConfiguration.getConfig().getCreamNotebooks().getDefaultNotebook(),
                            AbstractConfiguration.getConfig().getCreamNotebooks().getGroupNotebooks());
                } catch (Exception e) {
                    e.printStackTrace();
                    syncLog("Im Moment kein Sync möglich. Keine Verbindung.");
                }
            }
        }

        log.info("STARTING SYNC");
        try {
            SyncState syncState = null;
            String localNotebookGuid;
            for(ENSharedNotebook sharedNotebook: sharedNotebookGroup.getAllNotebooks()) {
                syncLog("---");
                syncLog("NOTEBOOK: " + sharedNotebook.getLinkToSharedNotebook().getShareName());
                log.info("syncing NOTEBOOK: " + sharedNotebook.getLinkToSharedNotebook().getShareName());
                localNotebookGuid = sharedNotebook.getSharedNotebook().getNotebookGuid();

                if (readLocalSyncInfo(localNotebookGuid)) {

                    NoteStore.Client noteStoreClient = sharedNotebook.getSharedNoteStore();


                    syncState = noteStoreClient.getLinkedNotebookSyncState(
                            sharedNotebook.getSharedAuthToken(),
                            sharedNotebook.getLinkToSharedNotebook());

                    long serverFullSyncBefore = syncState.getFullSyncBefore();
                    int serverUpdateCount = syncState.getUpdateCount();
                    //lastSync = syncState.getCurrentTime();
                    if (serverFullSyncBefore > lastSync) {
                        log.info("too much time since last sync. Full Sync!");
                        syncLog("Zeitstempel im Server signalisiert FullSync...");
                        // server signals, that there should be full sync, eg because
                        // the of some server issues with updateCount numbering.
                        doFullSync(sharedNotebook);
                    } else { // incremental
                        // that with serverUpdateCount, not with 0
                        if (updateCount < serverUpdateCount) { // updateCount < serverUpdateCount
                            log.debug("incremental Sync with updateCount: " + updateCount + ", serverUpdateCount: " + serverUpdateCount);
                            syncLog("Inkrementeler Sync mit updateCount: " + updateCount + ", serverUpdateCount: " + serverUpdateCount);
                            doIncSync(updateCount, sharedNotebook);
                        } else {
                            Map<String, Note> dirtyNotes = getDirtyNotes(sharedNotebook);

                            if (dirtyNotes.size() > 0) {
                                log.debug("updateCount == serverUpdateCount. BUT dirty local notes. Writing them to server.");
                                syncLog("updateCount == serverUpdateCount. Aber die lokalen Änderungen werden zum Server geschickt...");
                                workDirtyClientNotesIntoServer(dirtyNotes, sharedNotebook);
                            } else {
                                syncLog("KEIN SYNC NOTWENDIG! updateCount == serverUpdateCount. Keine lokalen Änderungen.");
                                log.debug("updateCount == serverUpdateCount. No dirty local notes. No sync needed");
                            }
                        }
                    }
                } else {
                    // did not find the local sync info. That signals
                    // a) that this is either the first start or
                    // b) that the client sync info is corrupt or lost.
                    // In both cases: we do a full sync!
                    log.info("Notebook: "+sharedNotebook.getLinkToSharedNotebook().getShareName()+" --> Probably first start of Notebook. DOING FULL SYNC. May take a while...");
                    syncLog("Lokale Sync-Info nicht gefunden. Erster Start? FullSync startet...");
                    doFullSync(sharedNotebook);
                }
            }
        } catch (EDAMUserException ee) {
            if (ee.getErrorCode() == EDAMErrorCode.AUTH_EXPIRED) {
                // just login again... TODO: Auto-Login
                syncLog("Sie wurden vom Server ausgeloggt. BITTE WIEDER EINLOGGEN!");
            } else {
                log.warn(ee.toString());
                ee.printStackTrace();
            }
        } catch (Exception e) {
            //doFullSync();
            syncLog("FEHLER: " + e.toString());
            log.error("SYNC FAILED: "+e.getMessage());
            e.printStackTrace();
            //throw new RuntimeException(e);

        }

        Date dateEnd = new Date(System.currentTimeMillis());
        f = new SimpleDateFormat("HH:mm:ss");
        syncLog("---");

        syncLog("FINISHED SYNC, time: " + f.format(dateEnd));
        log.info("FINISHED SYNC, time: " + f.format(dateEnd));

        SwingUtilities.invokeLater(() -> {
            fireDataChange(SyncHandler.this);
            // TODO what about the selection? that may have been changed, too.
            // TODO When the element disappeared...
            //if(selected != xxx)
            //fireSelectionChanged(SyncHandler.this);
        });
    }

    private void doFullSync(ENSharedNotebook notebook) throws Exception {
        Pair<List<String>, Map<String, Note>> expungedANDserverUpdatedNotes = readAllSyncChunks(0, true, notebook);
        doSync(notebook, expungedANDserverUpdatedNotes);
    }

    private void doIncSync(int clientUpdateCount, ENSharedNotebook notebook) throws  Exception {
        Pair<List<String>, Map<String, Note>> expungedANDserverUpdatedNotes = readAllSyncChunks(clientUpdateCount, false, notebook);
        doSync(notebook, expungedANDserverUpdatedNotes);
    }

    private void doSync(ENSharedNotebook notebook, Pair<List<String>, Map<String, Note>> expungedANDserverUpdatedNotes) throws Exception {
        Map<String, Note> dirtyNotes = getDirtyNotes(notebook);
        Map<String, Note> allLocalNotes = noteStoreLocal.getAll(notebook);
        workServerNotesIntoLocalOnes(expungedANDserverUpdatedNotes, dirtyNotes, allLocalNotes, notebook);
        workDirtyClientNotesIntoServer(dirtyNotes, notebook);
    }

    private void workDirtyClientNotesIntoServer(Map<String, Note> dirtyNotes, ENSharedNotebook notebook) throws Exception {
        long start = System.currentTimeMillis();
        int c = 0;
        for (Map.Entry<String, Note> entry : dirtyNotes.entrySet()) {
            Note n = entry.getValue();
            updateSyncCounter(c++, dirtyNotes.size());

            if(n.getGuid().startsWith("NEW_")) { // client created that
                log.debug("writing NEW local note to server: " + n.getTitle());
                syncLog("Lokal new -->Evernote: " + n.getTitle());
                //if(true){ throw new RuntimeException("notYet");}
                ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(n.getNotebookGuid());
                Note newOne = null;
                try {
                    newOne = sharedNotebook.createNote(n);
                } catch (Exception e) {
                    e.printStackTrace();
                    //newOne = sharedNotebook.createNote(n);

                }
                newOne.setContent(n.getContent());
                //String content = sharedNotebook.getNoteContent(newOne); read again?
                // now we have a new guid. set that
                noteStoreLocal.deleteFileFromLocalStore(n); // and get rid of the "old new"
                updateNote(newOne);

            } else { // just dirty. Since not changed on server since last sync, just write there...
                log.debug("writing dirty note to server: " + n.getTitle());
                syncLog("Lokal-->Evernote: " + n.getTitle());
                Note tmp = null;
                try {
                    tmp = notebook.updateNote(n);
                } catch (Exception e) {
                    log.error("faild updating to server while trying note: " + n.getTitle());
                    throw e;
                }
                n.setUpdateSequenceNum(tmp.getUpdateSequenceNum());
                assert(!isDirty(n));
                assert(n.getContent() != null);
                //noteStoreLocal.updateNote(n); // to get the guid down to the file
                updateNote(n);
            }
        }
        long end = System.currentTimeMillis();
        syncLog("DAUER ALLE Lokal-->Evernote: " + Util.readableTime(end - start));
        log.debug("TIMING: writing all dirty notes to server took: " + Util.readableTime(end-start));
    }

    private void workServerNotesIntoLocalOnes(Pair<List<String>,
            Map<String, Note>> expungedANDserverUpdatedNotes,
                                              Map<String, Note> dirtyNotes,
                                              Map<String, Note> allLocalNotes,
                                              ENSharedNotebook notebook) throws Exception {
        // new notes that does not have synced have a special guid: it starts with NEW_
        long start = System.currentTimeMillis();
        int c = 0;
        if(expungedANDserverUpdatedNotes.right.size() == 0) {
            log.debug("NO UPDATES on server...");
        }
        for (Map.Entry<String, Note> entry : expungedANDserverUpdatedNotes.right.entrySet()){
            Note serverNote = entry.getValue();
            Note localNote = allLocalNotes.get(serverNote.getGuid());
            Note fileNote = noteStoreLocal.getNotesMap().get(serverNote.getGuid());
            if(fileNote != null && localNote == null) {
                log.debug("moved note on server: " + fileNote.getTitle());
                localNote = fileNote;
            }

            long deletedOnServer = serverNote.getDeleted();
            if(deletedOnServer > 0) {
                log.debug("MARKED DELETED on Server:  " + serverNote.getTitle());
                if(localNote != null) {
                    log.debug("REMOVING LOCALLY:  " + serverNote.getTitle());
                    noteStoreLocal.deleteFileFromLocalStore(localNote);
                } else {
                    log.debug("NO LOCAL version. Ignoring");
                }
                continue;
            }
            ENCREAMNotebooks creamNotebooks = AbstractConfiguration.getConfig().getCreamNotebooks();
            String newNotebookGuid = serverNote.getNotebookGuid();
            if(localNote != null) {
                String oldNotebookGuid = localNote.getNotebookGuid();
                if(!oldNotebookGuid.equals(newNotebookGuid)) {
                    log.debug("note changed notebook: " + localNote.getTitle());
                    localNote.setNotebookGuid(serverNote.getNotebookGuid()); // just in case it was moved (even to trash)
                } else {
                    String localNotebookName = creamNotebooks.getNameForNotebookGuid(newNotebookGuid);
                    log.debug(serverNote.getTitle() + " in locally available notebook: " + localNotebookName);
                }

            } else {
                log.debug("no local version of: " + serverNote.getTitle());
                if(creamNotebooks.isALocalNotebook(serverNote.getNotebookGuid())) {
                    String localNotebookName = creamNotebooks.getNameForNotebookGuid(newNotebookGuid);
                    log.debug("will save to notebook: " + localNotebookName);
                } else {
                    log.debug("no local notebook. wont sync: " + serverNote.getTitle());
                    continue;
                }

            }
            updateSyncCounter(c++, expungedANDserverUpdatedNotes.right.size());

            // detect "virtual detetion" --> disappeard from all local available notebooks
            if( ! creamNotebooks.isALocalNotebook(serverNote.getNotebookGuid())) {
                log.debug("virtual delete (Notebook guid locally not available): " + localNote.getTitle());
                syncLog("Evernote-->Lokal: VIRTUELLE LÖSCHUNG: Notebook lokal nicht vorhanden für Notiz: " + serverNote.getTitle());
                noteStoreLocal.deleteFileFromLocalStore(localNote);
            }


            if(localNote != null && isDirty(localNote)) { // exists & dirty...

                log.info("found conflict. Going to merge: " + serverNote.getTitle());
                syncLog("Evernote-->Lokal: KONFLIKT, merge: " + serverNote.getTitle());
                //ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(entry.getValue().getNotebookGuid()); // TODO: check if that field is there

                notebook.getNoteContent(serverNote);
                //KONFLIKT! updated on server and on client!
                Note merged = merge(localNote, serverNote);

                // now write merged to server and to local note store
                Note tmp = notebook.updateNote(merged);
                merged.setUpdateSequenceNum(tmp.getUpdateSequenceNum());
                //noteStoreLocal.updateNote(merged);
                updateNote(merged);
                dirtyNotes.remove(entry.getKey());
            } else {
                //System.out.println();
                if(localNote != null && localNote.getUpdateSequenceNum() == serverNote.getUpdateSequenceNum()) {
                    log.debug("found server note 'in sync' with client: " + localNote.getTitle());
                    syncLog("Lokale Notiz war schon synchronisiert: " + serverNote.getTitle());
                    // was edited and written to server. Local content is ok.
                } else {
                    // use the server version

                    //ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(entry.getValue().getNotebookGuid()); // TODO: check if that field is there
                    notebook.getNoteContent(serverNote); // this may be fucking time consuming!
                    //noteStoreLocal.updateNote(entry.getValue());
                    updateNote(serverNote);
                    //noteStoreLocal.getData(entry.getValue());
                    syncLog("Evernote-->Lokal: " + serverNote.getTitle());
                    log.debug("reading updated note from server: " + serverNote.getTitle());
                }
            }


            // TODO: here we could set AND persistently write the updateSequenceNum, so that even a full
            // sync could be interrupted...
        }
//
        // Delete the notes, that were deleted on the server
        //
        if(expungedANDserverUpdatedNotes.left.size() == 0) {
            log.debug("NO EXPUNGES on server...");
        }
        for (String expungedGuid : expungedANDserverUpdatedNotes.left) {
            // try to find them in the client
            Note n = noteStoreLocal.getNotesMap().get(expungedGuid);


            if(n!=null) {

                // CHECK IF THIS MAY HAVE BEEN MOVED BEFORE TO ANOTHRE FOLEDER. THAN: DONT EXPUNGE!
                // only if the current notebook that is synced contains it.. then delete it locally
                if(notebook.getSharedNotebook().getNotebookGuid().equals(n.getNotebookGuid())) {

                    if (isDirty(n)) {
                        // this could be the case, if locally worked on but moved to another notebook on the server
                        // for the moment: just remove locally and show the conflict
                        noteStoreLocal.deleteFileFromLocalStore(n);
                        syncLog("KONFLIKT: " + n.getTitle() + " wurde lokal bearbeitet und auf dem Server expunged/verschoben.");
                        log.debug("KONFLIKT: " + n.getTitle() + " wurde lokal bearbeitet und auf dem Server expunged/verschoben.");

                    } else {
                        noteStoreLocal.deleteFileFromLocalStore(n);
                        syncLog("EXPUNGE, Evernote -> lokal: " + n.getTitle());
                        log.debug("EXPUNGE, Evernote -> lokal: " + n.getTitle());

                    }
                } else {
                    log.debug("the EXPUNGE was a MOVE..." + n.getTitle());
                }
            } else {
                // wurde auf dem client schon gelöcht??? ignorieren...
                syncLog("EXPUNGE, Evernote -> lokal: aber lokal schon gelöscht. Seltsam. Ignore. " + expungedGuid);
                log.debug("EXPUNGE, Evernote -> lokal: aber lokal schon gelöscht. Seltsam. Ignore. " + expungedGuid);
            }
        }

        long end = System.currentTimeMillis();
        log.debug("TIMING: reading all updated notes from server took: " + Util.readableTime(end-start));
        syncLog("DAUER ALLE Evernote-->Lokal: " + Util.readableTime(end - start));

        writeLocalSyncInfo(notebook.getSharedNotebook().getNotebookGuid()); // TODO: do it in between the above reads of content?
    }

    /**
     * User changed the note, but is not yet synced
     * @param note note that is dirty
     */
    private boolean isDirty(Note note) {
        return note.getUpdateSequenceNum() == -1;
    }

    /**
     * User changed the note, but is not yet synced
     * @param note note that is dirty
     */
    private void setDirty(Note note) {
        note.setUpdateSequenceNum(-1); // THIS ist the dirty flag!
    }

    /**
     * maybe not useful...
     */
    /*
    private void removeDirtyFlags() {
        Map<String, Note> allLocalNotes = getAllLocalNotes();
        for (Map.Entry<String, Note> entry : allLocalNotes.entrySet()) {
            entry.getValue().setUpdateSequenceNum(0); // mark "undirty"
        }
    }*/

    private Note merge(Note localNote, Note serverNote) {
        //Note merged = new Note(value); // take the server one. Assume history was changed
        // get the structured data from the local one and paste it to the server note
        // see ENHelper at the end
        int StartDataBlockServer = ENHelper.getStartOfDataBlock(serverNote);
        int EndDataBlockServer = ENHelper.getEndOfDataBlock(serverNote);
        int StartDataBlockLocal = ENHelper.getStartOfDataBlock(localNote);
        int EndDataBlockLocal = ENHelper.getEndOfDataBlock(localNote);

        // get the dataBlock of client
        String dataBlockLocal = localNote.getContent().substring(StartDataBlockLocal, EndDataBlockLocal); // TODO: check, if this cuts right...

        // get the dataBlock of server
        String beforeDataBlockServer = serverNote.getContent().substring(0, StartDataBlockServer);
        String afterDataBlockServer = serverNote.getContent().substring(EndDataBlockServer, serverNote.getContent().length());

        // merge structured data from client and history from server (thats just an assumtion...)
        // TODO: do this based on a md5 that is appended to the data block.
        // TODO: In addtion, the data fields shoud get a time stamp (kind of dirty flag). So we could detect
        // TODO: on field level, if there are conflicts - and sent a conflic email to both users with differnt values.
        String newContent = beforeDataBlockServer + dataBlockLocal + afterDataBlockServer;
        serverNote.setContent(newContent);

        return serverNote;
    }

    private Pair<List<String>, Map<String, Note>>  readAllSyncChunks(int clientUpdateCount, boolean fullSyncOnly, ENSharedNotebook sharedNotebook) throws  Exception {
        boolean proceed = false;
        Map<String, Note> notesMapFromServer = new HashMap<String, Note>();
        List<String> allExpungedNotes = new ArrayList<>();
        NoteStore.Client noteStoreClient = sharedNotebook.getSharedNoteStore();
        int originalUpdateCount = clientUpdateCount;

        long start = System.currentTimeMillis();
        int c = 0;
        //syncLog("Veränderte Notes auf dem Server finden...");
        do {

            SyncChunk chunk = noteStoreClient.getLinkedNotebookSyncChunk(
                    sharedNotebook.getConnection().getBusinessAuthToken(),
                    sharedNotebook.getLinkToSharedNotebook(),
                    clientUpdateCount, 10, fullSyncOnly);
            updateSyncCounter(10*c++, (chunk.getChunkHighUSN()-originalUpdateCount+10)/10 );

            List<Note> notes = chunk.getNotes();
            if (notes != null && notes.size() > 0) {
                for(Note n: notes) {
                    notesMapFromServer.put(n.getGuid(), n);
                }
            }

            List<String> expungedNotes = chunk.getExpungedNotes();
            if (expungedNotes != null && expungedNotes.size() > 0) {
                allExpungedNotes.addAll(expungedNotes);
            }

            // check if end reached
            if (chunk.isSetChunkHighUSN()) {
                if(chunk.getUpdateCount() == chunk.getChunkHighUSN()) { // Sign: got all. Spec seams ambigous.
                    proceed = false;
                    updateCount = chunk.getUpdateCount();
                    lastSync = chunk.getCurrentTime();
                    log.debug("Found " + notesMapFromServer.size() + " updated notes on server. going to read them...");
                    syncLog(notesMapFromServer.size() + " veränderte Notizen auf Server gefunden... ");
                } else { // not yet done
                    proceed = true;
                    clientUpdateCount = chunk.getChunkHighUSN();

                }
            }

            if(!chunk.isSetChunkHighUSN()) {
                // the spec says, that would be another sign:
                // and empty chunk - thats it. Its over...
                proceed = false;
                updateCount = chunk.getUpdateCount();
                lastSync = chunk.getCurrentTime();
            }

        }while(proceed);
        long end = System.currentTimeMillis();
        log.debug("TIMING: readAllSyncChunks read " + notesMapFromServer.size() + " from server  in " + Util.readableTime(end-start));
        syncLog("DAUER: " + Util.readableTime(end - start));
        return new Pair<>(allExpungedNotes, notesMapFromServer);
    }

    private boolean readLocalSyncInfo(String notebookGUID) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(getSyncInfoFileName(notebookGUID)));
            String updateCountStr = r.readLine();
            String lastSyncStr = r.readLine();
            String lastSyncStrDate =  r.readLine(); // just for check by user and debugging
            updateCount = Integer.parseInt(updateCountStr);
            lastSync = Long.parseLong(lastSyncStr);
            Date lastSyncDebugDate = dateFormat.parse(lastSyncStrDate);
            r.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getSyncInfoFileName(String notebookGUID) {
        return SYNC_INFO_FILE+"_"+notebookGUID+SYNC_INFO_FILE_SUFFIX;
    }

    private boolean writeLocalSyncInfo(String notebookGUID) {
        File syncDir = new File(SYNC_INFO_DIR);
        File syncFile = new File(getSyncInfoFileName(notebookGUID));

        syncDir.mkdirs();

        if(syncFile.exists()){
            boolean success = syncFile.delete(); // TODO neccessary? Speed? Existing file?
            if(!success) {
                throw new RuntimeException("Could not deleteFileFromLocalStore old local sync info file: " + syncFile);
            }
        }
        BufferedWriter bw = null;
        try {
            //syncFile.createNewFile();
            bw = new BufferedWriter(new FileWriter(syncFile));
            bw.write(Integer.toString(updateCount));
            bw.newLine();
            bw.write(Long.toString(lastSync));
            bw.newLine();
            Date d = new Date(lastSync);
            bw.write(dateFormat.format(d));
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                // cant do anything about that...
                return false;
            }
        }
        return true;
    }




    private  Map<String,Note> getDirtyNotes(ENSharedNotebook notebook) {
        // TODO: get the deleted ones into the dirty map, too

        Map<String, Note> dirty = new HashMap<String, Note>();
        for(Note note: noteStoreLocal.getAll(notebook).values()) {
            if(isDirty(note) && Util.belongsTo(notebook, note)) { // this is the dirty mark
                dirty.put(note.getGuid(), note);
            }
        }
        return dirty;
    }

    public List<String> getPatchList(Note n) throws IOException {
        return noteStoreLocal.getPatchList(n);
    }

    // NOT DIRTY
    // for all the calls that come from syncing inside the SyncHandler itself
    public void updateNote(Note currentNote) {
        assert(currentNote.getContent() != null);
        noteStoreLocal.updateNote(currentNote);
        CreamFirmaData data = noteStoreLocal.getData(currentNote);
        fireDataChange(SyncHandler.this);
        fireNoteChanged(SyncHandler.this, data);
    }

    /**
     * This is needed by the mailer.
     * @param n
     */
    public ENSharedNotebook getNotebook(Note n) {
        if(sharedNotebookGroup == null) { throw new RuntimeException("no shared notebook group - no connection...");}
        ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(n.getNotebookGuid());
        return sharedNotebook;
    }

    /**
     * This is needed by the mailer and overview-creator
     * @param n
     */
    public void updateNoteImmediately(Note n) {
        ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(n.getNotebookGuid());
        try {
            // TODO: ??? x or n
            Note x = sharedNotebook.updateNote(n);
            updateNote(n); // to avoid conflicts with dirty note or wrong updateCounter
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("save to Evernote failed: " + e);
        }
    }


    public void createNote(Note currentNote) {
        throw new RuntimeException("notYet");
    }


    // TODO: move them to "deleted", in order to be able, to remove them from server?
    public void deleteNote(Note n) {
        throw new RuntimeException("notYet");
    }


    public List<Note> getAllNotes()  {
        return new ArrayList (noteStoreLocal.getNotesMap().values());
    }

    public String getNoteContent(Note note) throws Exception {
        if(note.getContent() == null) {
            ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(note.getNotebookGuid()); // TODO: check if that field is there

            if(sharedNotebook != null) {
                return sharedNotebook.getNoteContent(note);
            } else {
                return null;
            }
        } else {
            return note.getContent();
        }
    }

    public void loadRessources(Note note) throws Exception {
        //if(note.getResources() == null) {
        if(sharedNotebookGroup != null) {
            ENSharedNotebook sharedNotebook = sharedNotebookGroup.getNotebook(note.getNotebookGuid());
            if (sharedNotebook != null) {
                sharedNotebook.loadNoteRessources(note);
            } else {
                throw new RuntimeException("no shared Notebook...???");
            }
        } else {
            log.warn("could not load resource for note - no connection...");
        }
        //} else {
        // do nothing...
        //    log.info("ressources already there. ignoring.");
        //}
    }

    /*
    public Note getNote(String guid) {
        return noteStoreLocal.getNotesMap().get(guid);
    }*/

    public List<String> getAbos( Note n) {
        return noteStoreLocal.getAbos(n);
    }

    public CreamFirmaData getData( Note n) {
        return noteStoreLocal.getData(n);
    }

/*
    public Configuration getConfig() {
        return noteStoreLocal.getConfig();
    }
*/
    /**
     * Search for emailAdress match in persons email fields and then in domain...
     * If there is a match
     * @param searchForEmailAdress
     * @return
     */
    public Collection<Note> filterNotesWithEmail(String searchForEmailAdress) {
        Set<Note> result = new HashSet<>();
        String[] split = searchForEmailAdress.split("@");
        assert(split.length == 2);
        String searchForDomain = split[1];
        for(Note n: noteStoreLocal.getNotesMap().values()) {
            CreamFirmaData data = noteStoreLocal.getData(n);
            if (data != null) {
                CreamAttributeData domain = data.getAttr("Domain");
                String domainInData = domain.value;
                if (searchForDomain.trim().equals(domainInData.trim())) {
                    result.add(n);
                }
                for ( CreamPersonData person: data.persons) {
                    CreamAttributeData emailAdresses = person.getAttr("Emails");
                    String[] emails = emailAdresses.value.split("\\s|,|;|:|<|>|\"");
                    for(String email: emails) {
                        if(searchForEmailAdress.trim().equals(email.trim())) {
                            result.add(n);
                        }
                    }

                }
            }
        }
        return result;
    }


    public List<CreamFirmaData> getAbosForShortName(String shortName) {
        List<CreamFirmaData> result = new ArrayList<>();
        List<Note> allNotes = getAllNotes();
        for(Note n: allNotes) {
            List<String> abos = getAbos(n);
            if(abos.contains(shortName)) {
                result.add(getData(n));
            }
        }
        return result;
    }

    //
    // TODO: all functions to save changes from the ui
    //

    /**
     * if there is not yet structured data, just work with the note...
     * @param origin
     * @param note
     */
    public void saveData(Object origin,  Note note) {

        try {
            setDirty(note);
            updateNote(note);
        } catch (Exception e) {
            log.fatal("Could not save data! CREAM will be stopped. " + e);
            throw new RuntimeException("Speichern ist fehlgeschlagen.", e);
        }
    }

    /**
     * this is the way of saving data from the gui.
     * Sync handler takes care of extraction data and putting it
     * into the note.
     * There is a second way: If a raw Note should be
     * saved, that can be done. BUT this should be validated
     * online immediately. THEREFORE: DONT CHANGE THE NOTE before this call!
     *
     * if a runtime exception comes through, Show a message:
     * You should stopp the application!
     *
     * @param data
     * @return
     */
    public void saveData(Object origin, CreamFirmaData data) {
        //put the structured data into the note and save it (save locally and mark dirty)
        try {
            Note n = data.getNote();
            if(n==null) {
                n = noteStoreLocal.createLocalNote();
                data.setNote(n);
                String content = ENHelper.createValidEmptyContentWithEmptyDataBlock();
                n.setContent(content);
                String defaultNotebook = AbstractConfiguration.getConfig().getCreamNotebooks().getDefaultNotebook();
                String defaultGuid = AbstractConfiguration.getConfig().getCreamNotebooks().getNotebookGuidForName(defaultNotebook);
                n.setNotebookGuid(defaultGuid);
                String firmenname = data.getAttr("Firmenname").value;
                String vorname = data.persons.get(0).getAttr("Vorname").value;
                String nachname = data.persons.get(0).getAttr("Nachname").value;
                if("".equals(firmenname) && "".equals(vorname) && "".equals(nachname)) {
                    vorname = "KEIN";
                    nachname = "NAME";
                    firmenname = "BISHER JEDENFALLS";
                }
                String title = vorname +  " " + nachname + " (" + firmenname + ")";
                n.setTitle(title);
            }
            ENHelper.writeDataToNote(data, n);
            setDirty(data.getNote());
            updateNote(data.getNote());
        } catch (Exception e) {
            log.fatal("Could not save data! CREAM will be stopped. " + e);
            //e.printStackTrace();
            throw new RuntimeException("Speichern ist fehlgeschlagen.", e);
        }

    }

    // TODO: do we need that?
    public CreamFirmaData readData(Note n) {
        return noteStoreLocal.getData(n);
    }

    /**
     * returns an list that is unmodifieable.
     * TODO: Check performance, if this is a very large map.
     * @return
     */
    public List<CreamFirmaData> readDataList() {
        Map m = noteStoreLocal.readDataList();
        List<CreamFirmaData> l = Collections.unmodifiableList(new ArrayList<>(m.values()));
        return l;
    }

    //
    // handle all data listening and updating the views
    //

    private CreamFirmaData selected = null;
    private ArrayList<CreamDataListener> listeners = new ArrayList<>();

    public void addCreamDataListener(CreamDataListener l) {
        listeners.add(l);
    }

    public void setSelectedNote(Object origin, CreamFirmaData data) {
        selected = data;
        //log.info(CRASH_NIT, "going to fireSelectionChanged...");
        fireSelectionChanged(origin);
    }

    public CreamFirmaData getSelectedNote() {
        return selected;
    }

    public void fireSelectionChanged(Object origin) {
        //log.trace(CRASH_NIT, " start fireSelectionChanged");
        listeners.forEach(l->l.selectionChanged(origin, selected));
        //log.trace(CRASH_NIT, " end fireSelectionChanged");
    }

    public void fireDataChange(Object origin) {
        listeners.forEach(l->l.dataChanged(origin));
    }

    public void fireNoteChanged(Object origin, CreamFirmaData creamFirmaData) {
        listeners.forEach(l->l.noteChanged(origin, creamFirmaData));
    }


}
