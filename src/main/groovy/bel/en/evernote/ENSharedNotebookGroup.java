package bel.en.evernote;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ENSharedNotebookGroup {
    Map<String, ENSharedNotebook> notebookMap = new HashMap<>();

    public ENSharedNotebookGroup(ENConnection connection, String defaultNotebook, ArrayList<String> groupNotebooks) throws Exception {
            try {
                notebookMap.put(defaultNotebook, new ENSharedNotebook(connection, defaultNotebook));
            } catch (Exception e) {
                log.fatal("DEFAULT-Notebook NOT AVAILABLE: " + defaultNotebook);
                throw new RuntimeException("DEFAULT-Notebook NOT AVAILABLE: " + defaultNotebook);
            }

            for(String n: groupNotebooks) {
            try {
                notebookMap.put(n, new ENSharedNotebook(connection, n));
            } catch (Exception e) {
                log.warn("Group-Notebook NOT AVAILABLE: " + n);
            }
        }
    }
/*
    public List<Note> getAllDataNotes() throws Exception {
        List<Note> notes = new ArrayList<>();
        for(Map.Entry entry: notebookMap.entrySet()) {
            notes.addAll(((ENSharedNotebook) entry).getAllNotes());
        }
        return notes;
    }
*/
    public Collection<ENSharedNotebook> getAllNotebooks() {
            return notebookMap.values();
    }

    /**
     * when saving a Note, in order to find the corresponding notebook.
     * @param notebookGuid
     * @return
     */
    public ENSharedNotebook getNotebook(String notebookGuid) {
        for(ENSharedNotebook n: notebookMap.values()) {
            // TODO: Check if this works... the gui stored in the note is probably the one of the "mother notebook"...
            if(n.getSharedNotebook().getNotebookGuid().equals(notebookGuid)) {
                return n;
            }
        }
        log.debug("NOTEBOOK NOT FOUND! " + notebookGuid);
        return null; // not found...
    }

    /*
    public String getNotebookGuidForName(String notebook) {
        for(ENSharedNotebook n: notebookMap.values()) {

            if(n.getLinkToSharedNotebook().getShareName().equals(notebook)) {
                return n.getSharedNotebook().getNotebookGuid();
            }
        }
        return null;
    }*/
}
