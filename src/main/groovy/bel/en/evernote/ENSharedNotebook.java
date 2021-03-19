package bel.en.evernote;


import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.limits.Constants;
import com.evernote.edam.notestore.*;
import com.evernote.edam.type.*;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;

import java.util.ArrayList;
import java.util.List;

import static bel.en.evernote.ENHelper.escapeHTML;

/**
 * connects to a shared notebook with a specific shareName and make it easy to:
 * create
 * read notes
 * update notes
 * deleteFileFromLocalStore notes
 * get links to notes
 */
public class ENSharedNotebook {

    //public static ENSharedNotebook singleton;
    private LinkedNotebook linkToSharedNotebook;
    private NoteStore.Client sharedNoteStore;

    // TODO: THIS sharedAuthToken has to be replaced by the renewed business-token. That will work
    private String sharedAuthToken;
    private SharedNotebook sharedNotebook;

    ENConnection connection;
    private String shareName;

    public ENSharedNotebook(ENConnection connection, String shareName) throws Exception {
        this.connection = connection;
        connectSharedNotebook(shareName);
        //singleton = this;
    }

    /*
    public ENSharedNotebook(ENConnection connection, String shareName, String guid) throws Exception {
        this.connection = connection;
        if(guid == null) {
        connectSharedNotebook(shareName, guid);
        //singleton = this;
    }*/

    public LinkedNotebook getLinkToSharedNotebook() {
        return linkToSharedNotebook;
    }

    public NoteStore.Client getSharedNoteStore() {
        return sharedNoteStore;
    }

    public String getSharedAuthToken() {
        return sharedAuthToken;
    }

    public SharedNotebook getSharedNotebook() {
        return sharedNotebook;
    }

    public ENConnection getConnection() {
        return connection;
    }

    private void connectSharedNotebook(String shareName) throws Exception {
        this.shareName = shareName;
        linkToSharedNotebook = null;
        List<LinkedNotebook> linkedNotebooks = null;
        if(connection == null || connection.getNoteStoreClient() == null) {
            throw new Exception("there is no connection to evernote");
        }
        linkedNotebooks = connection.getNoteStoreClient().listLinkedNotebooks();

        //linkedNotebooks = connection.getNoteStoreClient().getNotebook();

        for (LinkedNotebook lnb : linkedNotebooks) {
            if (shareName.equals(lnb.getShareName())) {
                //System.out.println("found linked notebook: " + lnb.getShareName());
                linkToSharedNotebook = lnb;
                break;
            }
        }
        if (linkToSharedNotebook == null) {
            String err = "Error. linked Notebook named '" + shareName + "' not found.";
            //System.err.println(err);
            throw new Exception(err);
        } else {
            //System.out.println("Found linked '" + shareName + "' Notebook. Guid: " + linkToSharedNotebook.getGuid());

            // Create a new NoteStore Client to access the linked notebook, which may be on a different shard
            String sharedNoteStoreUrl = "https://www.evernote.com/edam/note/" + linkToSharedNotebook.getShardId();
            TBinaryProtocol sharedNoteStoreProt = new TBinaryProtocol(new THttpClient(sharedNoteStoreUrl));
            sharedNoteStore = new NoteStore.Client(sharedNoteStoreProt, sharedNoteStoreProt);
            // Get an auth token to read from the LinkedNotebook
            sharedAuthToken =
                    sharedNoteStore.authenticateToSharedNotebook(linkToSharedNotebook.getShareKey(), connection.getAuthToken()).getAuthenticationToken();

            sharedNotebook = sharedNoteStore.getSharedNotebookByAuth(sharedAuthToken);

            // TODO: already here? business token
        }
    }


    /**
     * read all notes in Notebook
     *
     * @throws com.evernote.edam.error.EDAMUserException
     * @throws com.evernote.edam.error.EDAMSystemException
     * @throws com.evernote.thrift.TException
     * @throws com.evernote.edam.error.EDAMNotFoundException
     */
    public List<Note> getAllNotes() throws Exception {
        NoteFilter filter = new NoteFilter();
        return filterNotes(filter);
    }


    public List<Note> findNotes(String title) throws Exception {
        NoteFilter filter = new NoteFilter();
        filter.setWords(title);
        return filterNotes(filter);

    }


    private List<Note> filterNotes(NoteFilter filter) throws Exception {
        //System.out.println("filter: " + filter.getWords());
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        int offset = 0;
        int pageSize = 10;
        List<Note> result = new ArrayList<Note>();
        //filter.setInactive(false);
        //NoteFilter filter = new NoteFilter();
        //filter.setOrder(NoteSortOrder.UPDATED.getValue());
        filter.setNotebookGuid(sharedNotebook.getNotebookGuid());
        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);
        //spec.setIncludeNotebookGuid(true);
        //spec.setIncludeUpdated(true);
        //spec.setIncludeUpdateSequenceNum(true);

        //spec.setIncludeDeleted(true); // WORKAROUNT: ONLY GET THE ACTIVE AONES. SEEMS TO BE EVERNOTE BUG

        //spec.setIncludeAttributes();

        // STRANGE BUG: Search include the deleted, too...
        //if(filter.getWords() != null && filter.getWords().equals("intitle:[ANGEBOTE_und_HOT_UEBERSICHT]")) {
        //    System.out.println("searching Angebote und Hot...");
        //}
        NotesMetadataList notes = null;

        do {
            notes = sharedNoteStore.findNotesMetadata(sharedAuthToken, filter, offset, pageSize, spec);
            for (NoteMetadata note : notes.getNotes()) {
                if (note.getDeleted()==0) { // WORKAROUNT: ONLY GET THE ACTIVE AONES. SEEMS TO BE EVERNOTE BUG
                    Note fullNote = sharedNoteStore.getNote(sharedAuthToken, note.getGuid(), true, false, false, false);
                    result.add(fullNote);
                    //System.out.println("isDeleted: " + fullNote.getDeleted());
                } else {
                    System.out.println("Loaded deleted... " + note.getTitle());
                }

                // WORKAROUNT: ONLY GET THE ACTIVE AONES. SEEMS TO BE EVERNOTE BUG
                //if(fullNote.getDeleted()==0) {
                //    result.add(fullNote);
                //}

            }
            offset = offset + notes.getNotesSize();

            //System.out.println("notes.getTotalNotes(): " + notes.getTotalNotes());
            //System.out.println("notes.getNotesSize(): " + notes.getNotesSize());
            //System.out.println("offset: " + offset);
            if(notes.getTotalNotes() == 1 && notes.getNotesSize() == 0 && offset == 0) {
                String problem = "FILTER-PROBLEM... BITTE lösche die Notiz: " + filter.getWords();
                System.out.println(problem);
                throw new RuntimeException(problem);
            }

        } while (notes.getTotalNotes() > offset);

        return result;
        /*
        List<Note> result = new ArrayList<Note>();
        int offset = 0;
        int pageSize = 10;
        filter.setNotebookGuid(sharedNotebook.getNotebookGuid());
        NoteList notes = null;
        do { // the nodes have to be read in junks of pageSize... Otherwise, there are only 50 coming over in the current implementation.
            notes = sharedNoteStore.findNotes(sharedAuthToken, filter, offset, pageSize);
            List<Note> notesList = notes.getNotes();
            int i = 0;
            for (Note n : notesList) {
                result.add(n);
            }
            offset += notes.getNotesSize();
        } while (notes.getTotalNotes() > offset);
        return result;
        */
    }


    public Note createNote(String title) throws Exception{
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        //create empty Note
        Note created = new Note();
        created.setNotebookGuid(sharedNotebook.getNotebookGuid());
        created.setTitle(title);

        // TODO: should that be replaced by business-Token?
        created = sharedNoteStore.createNote(sharedAuthToken, created);
        return created;
    }

    public Note createNote(Note n) throws Exception{
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        //create empty Note
        n.setNotebookGuid(sharedNotebook.getNotebookGuid());
        checkNote(n);
        // TODO: should that be replaced by business-Token?
        Note created = sharedNoteStore.createNote(sharedAuthToken, n);
        return created;
    }

    private Note checkNote(Note n) {
        if(n.getTitle().length() > Constants.EDAM_NOTE_TITLE_LEN_MAX) {
            System.out.println("too long");
        }
        // this may happen, if text is just copied into tables...
        String invalidTitle = " Hier die Personen- und Adressdaten reinkopieren Sierralta Nachname  USE CASE: AdressMagic ()";
        n.setTitle(n.getTitle().replace("\t", " "));
        return n;
    }


    public Note updateNote(Note note) throws Exception {
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        note.unsetUpdated(); // TODO: check if that works...
        // TODO: should sharedAuthToken be replaced by business-Token?
        Note newOne = sharedNoteStore.updateNote(sharedAuthToken, note);
        return newOne;
    }


    /**
     * TODO: das ist ziemlich verwirrend...
     * @param note
     * @return
     */
    public String getNoteContent(Note note) throws Exception {
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        String noteContent = null;
        try {
            if(note.getContent() == null) {
                noteContent = sharedNoteStore.getNoteContent(sharedAuthToken, note.getGuid());
                note.setContent(noteContent);
            } else {
                noteContent = note.getContent();
            }
            return noteContent;
        } catch (EDAMUserException ee) {
            // TODO: remove all that...
            if (ee.getErrorCode() == EDAMErrorCode.AUTH_EXPIRED) {
                try {
                    System.out.println("TRYING TO RECONNECT...");
                    connection.connect();
                    connectSharedNotebook(shareName);
                    noteContent = sharedNoteStore.getNoteContent(sharedAuthToken, note.getGuid());
                    note.setContent(noteContent);
                    return noteContent;
                } catch (Exception e) {
                    System.out.println("FAILED... GIVING UP.");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("should not happen...");
    }

    public void loadNoteRessources(Note note) throws Exception {
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        String noteContent = note.getContent();
        String guid = note.getGuid();
        try {
            if(noteContent != null && guid != null) {
                loadAndCopyNote(note, guid);
                //NoteAttributes attributes = fullNote.getAttributes();
                //fullNote.set

            } else {
                throw new RuntimeException("ERROR: note guid and content is empty while fetching ressources");
            }
        } catch (EDAMUserException ee) { // TODO: Clean that up. No handling needed any more...
            if (ee.getErrorCode() == EDAMErrorCode.AUTH_EXPIRED) {
                try {
                    System.out.println("TRYING TO RECONNECT...");
                    connection.connect();
                    connectSharedNotebook(shareName);
                    loadAndCopyNote(note, guid);

                } catch (Exception e) {
                    System.out.println("FAILED... GIVING UP.");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

        } catch (Exception e) {
            // TODO: bei offline arbeiten bricht das Ressourcenladen ab...
            throw new RuntimeException(e);
        }
        //throw new RuntimeException("should not happen...");
    }

    private void loadAndCopyNote(Note note, String guid) throws Exception {
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();

        Note fullNote = sharedNoteStore.getNote(sharedAuthToken, guid, true, true, true, true);
        List<Resource> resources = fullNote.getResources();
        note.setResources(resources);

        note.setAttributes(fullNote.getAttributes());
        note.setContentHash(fullNote.getContentHash());

    }


    public void deleteNote(Note note) throws Exception {
        sharedAuthToken = ENConnection.get().getBusinessAuthToken();
        sharedNoteStore.deleteNote(sharedAuthToken, note.getGuid());
    }



    public String getInternalLinkTo(Note note) {
        return getInternalLinkTo(note, note.getTitle());
    }

    public String getInternalLinkTo(Note note, String linkText) {
        linkText = escapeHTML(linkText);
        String UID = Integer.toString(sharedNotebook.getUserId());
        String shard = linkToSharedNotebook.getShardId();
        String link = "<a href=\"evernote:///view/" + UID + "/" + shard + "/" + note.getGuid() + "/" + note.getGuid() + "/" + linkToSharedNotebook.getGuid() + "\"" + " style=\"color:#69aa35\">" + linkText + "</a>";
        return link;
    }

    public String getEvernoteRawLinkTo(Note note) {
        //String title = note.getTitle();
        //title = escapeHTML(title);
        String UID = Integer.toString(sharedNotebook.getUserId());
        String shard = linkToSharedNotebook.getShardId();
        String link = "evernote:///view/" + UID + "/" + shard + "/" + note.getGuid() + "/" + note.getGuid() + "/" + linkToSharedNotebook.getGuid();
        return link;
    }

    public String getExternalLinkTo(Note note, String linkText) {
        linkText = escapeHTML(linkText);
        String link = "<a href=\""+ getRawExternalLinkTo(note)+ "\"" + " style=\"color:#69aa35\">" + linkText + "</a>";
        return link;
    }

    /**
     * VORSICHT: es scheint so, als müsse man die Autorisierung bei Evernote freischalten lassen
     * @param note
     * @return
     */
    public String getRawExternalLinkTo(Note note) {
        //String UID = Integer.toString(sharedNotebook.getUserId());
        try {
            String shard = linkToSharedNotebook.getShardId();
            String shareKey = null;
            shareKey = getSharedNoteStore().shareNote(sharedAuthToken, note.getGuid());
            String link = "www.evernote.com/shard/" + shard + "/sh/" + note.getGuid() + "/" + shareKey;
            return link;
        } catch (Exception e) {
            e.printStackTrace();
            return "no-link-becaus-exception";
        }

    }
}
