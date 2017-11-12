package bel.en.localstore;

import bel.en.data.AbstractConfiguration;
import bel.en.data.Configuration;
import bel.en.data.CreamFirmaData;
import bel.en.evernote.ENHelper;
import bel.en.evernote.ENSharedNotebook;
import bel.util.DiffMatchPatch;
import bel.util.Util;
import com.evernote.edam.type.Note;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TODO: think about a MD5 at the end - in order to make sure, that note is ok
 * TODO: think about saving transaction save: first save new note. then deleteFileFromLocalStore old note.
 */
@Log4j2
public class NoteStoreLocal {

    //private static final Logger log = Logger.getLogger(NoteStoreLocal.class.getName());

    public final static String NOTE_STORE_BASE = "C:\\creamlocal";
    private final static String NOTE_STORE_DIR = NOTE_STORE_BASE+"\\notes";
    private final static String CONFIG_BACKUP_DIR = NOTE_STORE_BASE+"\\config";
    private final static String CONFIG_BACKUP_FILENAME = CONFIG_BACKUP_DIR + "\\config.ser";
    private final static String NOTE_FILE_EXT = "_note.xml";
    private final static String DIFF_FILE_EXT = "_versions.txt";
    //private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(NoteStoreLocal.class);

    private Map<String, Note> localNotes;
    private Map<String, CreamFirmaData> localData;
    //Map<String, Note> localDirtyNotes;

    public NoteStoreLocal(Configuration c) {
        setConfiguration(c);
        readAllFromDisk();
        //log.setLevel(Level.INFO);
    }

    /**
     * Set the configuration read from evernote and save it.
     * Otherwise (c=null) read the saved backup.
     * @param c configuration. if null, read config from local store
     */
    public void setConfiguration(Configuration c) {
        if(c == null) {
            readBackupConfig(); // this will have the effect, that config is available by AbstractConfiguration.get...
        } else {
            writeBackupConfig(c); // save it for the next local start
        }
    }

    /**
     *
     * @param notebook filter for notes, that belong to that notebook
     * @return
     */

    public Map<String, Note> getAll(ENSharedNotebook notebook) {
        Map<String, Note> result = new HashMap<>();
        for(Note n: localNotes.values()) {
            if(notebook == null || (Util.belongsTo(notebook, n))) {
                result.put(n.getGuid(),n);
            }
        }
        return result;
    }


    private void readAllFromDisk() {
        if(localNotes == null) {
            localData = new HashMap<>();
            localNotes = new HashMap<>();
            long start = System.currentTimeMillis();
            // dir all files in the
            File[] notes = getAllNoteFiles();

            //Map<String, Note> guidNotes = new HashMap<String, Note>();
            for (int i = 0; i < notes.length; i++) {
                if(i%500 == 0) {
                    log.info(" " + i + " " + Util.memStat() + " ");
                }
                Note n = extractNote(notes[i]);
                localNotes.put(n.getGuid(), n);
                CreamFirmaData data = ENHelper.extractFirmaPersonFromContent(n);
                localData.put(n.getGuid(), data);
                List<String> abos = ENHelper.getAbos(n);
                data.setAbos(abos);
            }
            long end = System.currentTimeMillis();
            log.info("read " + notes.length + " notes from disk, took: " + Util.readableTime(end - start));
        }
    }

    private Note extractNote(File note) {
        StringBuilder content = new StringBuilder();
        String line;
        Note n = null;
        try {
            @Cleanup BufferedReader br = new BufferedReader(new FileReader(note));
            n = new Note();
            line = br.readLine();
            n.setTitle(line);
            line = br.readLine();
            int seq = -1; // new
            //try {
                seq =Integer.parseInt(line);
            //} catch (NumberFormatException e) {
            //    e.printStackTrace();
            //}
            n.setUpdateSequenceNum(seq);
            line = br.readLine();
            n.setNotebookGuid(line);
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        } catch (Exception e) {
            log.fatal("reading note from file failed: " + note);
            throw new RuntimeException(e);
        }
        int len = note.getName().length() - 9;
        String guid = new String(note.getName().substring(0, len));
        n.setGuid(guid);
        n.setContent(content.toString());
        //extractRessources(n, guid);
        return n;
    }

    /**
     * TODO: put the writing code in an extra thread
     * @param n
     * @throws Exception
     */
    public void updateNote(Note n) {
        //Resource resource = n.getResources().get(0);
        //resource.getData().clear();
        //Data d = new Data();

        //updateRessources(n);
        assert(n != null);
        assert(n.getContent() != null);
        assert(n.getNotebookGuid() != null);
        assert(n.getTitle() != null);
        assert(n.isSetUpdateSequenceNum()); // TODO: Checkthis...
        localNotes.put(n.getGuid(), n);
        long start = System.currentTimeMillis();
        File noteFile = getNoteFile(n.getGuid());
        try {
            writePatchFile(n, noteFile);
        } catch (Exception e) {
            throw new RuntimeException("writing patch file for note failed: " + noteFile);
        }
        if(noteFile.exists()) {
            boolean deteted = noteFile.delete();
            if(!deteted) {
                log.error("deleting did not work: " + noteFile.getAbsolutePath());
                throw new RuntimeException("Could not delete: " + noteFile.getAbsolutePath());
            }
        }
        try (FileWriter fw = new FileWriter(noteFile);
             BufferedWriter bw = new BufferedWriter(fw)) {


            //log.info("writing note file: " + noteFile.getAbsolutePath());
            //noteFile.createNewFile();
            bw.write(n.getTitle());
            bw.newLine();
            bw.write(Integer.toString(n.getUpdateSequenceNum()));
            bw.newLine();
            //if(n.getNotebookGuid() == null) {throw new RuntimeException("GUID of notebook not set");}
            bw.write(n.getNotebookGuid());
            bw.newLine();
            bw.write(n.getContent()); // possible problem: the newLines are different - maybe the content is not read fully...

            //localData.remove(n.getGuid()); // just make it invalid, so that it will be created next time again...
        } catch (Exception e) {
            log.fatal("update failed with note: " + n.getTitle() + "  --> " + n.getContent(), e);
            throw new RuntimeException(e);
        }
        localData.remove(n.getGuid());
        getData(n);  // create new Data entry
        long end = System.currentTimeMillis();
        log.debug("updateNote(Note n) took: " + Util.readableTime(end-start));
    }

    // TODO: Synchronize with writing that file...
    public List<String> getPatchList(Note n) throws IOException {
        File notePatchFile = getNotePatchFile(n.getGuid());
        List<String> result = new ArrayList<>();
        if(notePatchFile.exists()) {
            @Cleanup FileReader in = new FileReader(notePatchFile);
            @Cleanup BufferedReader r = new BufferedReader(in);
            String line;
            while ((line = r.readLine()) != null) {
                // TODO: filter the patches for empty lines etc?
                result.add(line);
            }
            //String[] patches = b.toString().split("\\*\\*\\*PATCH\\*\\*\\*");
            //List<String> result = Arrays.asList(patches);
            //result.forEach(String::trim);
            Collections.reverse(result);
        } else {
            //System.out.println("patchFile: " + notePatchFile.getName() + "does not exist...");
            //log.warn("patchFile: " + notePatchFile.getName() + "does not exist...");
        }
        return result;
    }

    private void writePatchFile(Note n, File noteFile) throws IOException {
        String oldNoteContent = "";
        if(noteFile.exists()) {
            Note oldNote = extractNote(noteFile);
            oldNoteContent = oldNote.getContent();
        } else {
            return;
        }
        //TODO: Title muss da mit rein...
        LinkedList<DiffMatchPatch.Diff> diffs = DiffMatchPatch.get().diff_main(n.getContent(), oldNoteContent);
        //DiffMatchPatch.get().diff_cleanupSemantic(diffs);
        //DiffMatchPatch.get().diff_cleanupSemanticLossless(diffs);
        //DiffMatchPatch.get().diff_cleanupEfficiency(diffs);
        //DiffMatchPatch.get().diff_cleanupMerge(diffs);
        /*
        LinkedList<DiffMatchPatch.Patch> patches = DiffMatchPatch.get().patch_make(diffs);
        String patchTextLine = DiffMatchPatch.get().patch_toText(patches);
        */
        String patchTextLine = DiffMatchPatch.get().diff_toDelta(diffs);
        File notePatchFile = getNotePatchFile(n.getGuid());
        @Cleanup Writer fw = new BufferedWriter(new FileWriter(notePatchFile, true));

        LocalDateTime ldt = LocalDateTime.now();
        String ldtStr = ldt.format(DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm:ss"));

        // TODO: Who did the change!

        fw.write("XXX: " +ldtStr + " " + patchTextLine + "\n");


        // TODO: if deamon send visual diff as html to all ABO (ENHelper.getAbos(note)) in an extra thread

        // TODO: write the diff to the note diff file and to the global diff fine in an extra thread

    }

    /**
     * this is only for testing deleting files...
     */
    public void deleteAllLocalFiles() {

        long start = System.currentTimeMillis();
        // dir all files in the
        File[] notes = getAllNoteFiles();
        for (int i = 0; i < notes.length; i++) {
            File note = notes[i];
            note.delete();
        }
        long end = System.currentTimeMillis();
        log.info("deleteAllLocalFiles deleted " + notes.length +" notes in " + Util.readableTime(end-start));

        // go back to a valid, empty state
        localNotes = null;
        readAllFromDisk();

    }

    private File[] getAllNoteFiles() {
        File folder = new File(NOTE_STORE_DIR);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new RuntimeException("Konnte verzeichnis für lokale Notizen nicht anlegen: " + NOTE_STORE_DIR + "\nbitte legen Sie das Verzeichnis an...");
            }
        }
        return folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(NOTE_FILE_EXT);
            }
        });
    }

    /**
     * mark as deleted - move to virtual trash
     * @param n
     */
    public void delete(Note n) { // das ist quatsch... mark as deleted...
        if(true) throw new RuntimeException("not yet"); // implement that totally different...
        // TODO: move to "deleted", keep the guid in deleted map - for syncing
        n.setDeleted(System.currentTimeMillis());
        n.setActive(false);
        updateNote(n);
        String guid = n.getGuid();
        //File noteFile = new File(NOTE_STORE_DIR+"\\"+guid+NOTE_FILE_EXT);
        //if(noteFile.exists())noteFile.delete();
        //localNotes.remove(guid);
    }

    /**
     * really remove. kill. expunge.
     * @param n
     */
    public void deleteFileFromLocalStore(Note n) {

        String guid = n.getGuid();
        File noteFile = getNoteFile(guid);
        if(noteFile.exists()) {
            boolean delete = noteFile.delete();
            if(!delete) {
                log.error("could not delete file: " + noteFile.getAbsolutePath());
                throw new RuntimeException("Could not delete: " + noteFile.getAbsolutePath());
            }
        } else {
            log.warn("file does not exist: " + noteFile.getAbsolutePath());
        }
        localNotes.remove(guid);
        localData.remove(guid);
    }

    private File getNoteFile(String guid) {
        return new File(NOTE_STORE_DIR+"\\"+guid+NOTE_FILE_EXT);
    }
    private File getNotePatchFile(String guid) {
        return new File(NOTE_STORE_DIR+"\\"+guid+DIFF_FILE_EXT);
    }

    public Note createLocalNote() {
        Note n = new Note();
        n.setGuid(newLocalGUID());
        //updateNote(n);
        log.info("created Note locally with guid: " + n.getGuid());
        return n;
    }

    private String newLocalGUID () {
        return "NEW_"+Long.toString(System.currentTimeMillis());
    }

    public void writeBackupConfig(Configuration c) {
        try {
            //System.out.println("Writing config backup...");
            // create dir (if missing)
            File f = new File(CONFIG_BACKUP_DIR);
            f.mkdirs();

            // deleteFileFromLocalStore old one, if existing
            f = new File(CONFIG_BACKUP_FILENAME);
            f.delete();

            OutputStream file = new FileOutputStream(CONFIG_BACKUP_FILENAME);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(c);
            output.close();
            //file.close();
            //System.out.println("check by reading again...");
            //readBackupConfig();
        } catch(IOException ex){
            log.fatal("could not write config: " + CONFIG_BACKUP_FILENAME, ex);
            throw new RuntimeException("cannot save local configuration as backup: " + CONFIG_BACKUP_FILENAME);
        }
    }

    public Configuration readBackupConfig() {
        try {
            InputStream file = new FileInputStream(CONFIG_BACKUP_FILENAME);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream (buffer);
            AbstractConfiguration c = (AbstractConfiguration)input.readObject();

            c.finishSetup(c.getPersistentCurrentUsersFullName());
            file.close();
            return c;
        } catch(Exception ex){
            log.fatal("could not read config: " + CONFIG_BACKUP_FILENAME, ex);
            throw new RuntimeException("cannot load local configuration as backup: " + CONFIG_BACKUP_FILENAME);
        }
    }

    /**
     * only for testing!
     */
    public void invalidateCache() {
         localNotes = null;
    }

    public List<String> getAbos(@NonNull Note n) {
        CreamFirmaData data = getData(n);
        if(data == null) {
            data = localData.get(n.getGuid());
        }
        return data.getAbos();
    }

    public CreamFirmaData getData(Note n) {
        CreamFirmaData cfd = localData.get(n.getGuid());
        if(cfd == null) { // not yet created the data entry ...
            cfd = ENHelper.extractFirmaPersonFromContent(n);
            localData.put(n.getGuid(), cfd);
            List<String> abos = ENHelper.getAbos(n);
            cfd.setAbos(abos);
        }

        if(cfd.numberOfAttribs() == 0) { // the ENHelper delivers an empty one in case no data is found...
            cfd = null;
        }
        return cfd;
    }

    public Map<String, Note> getNotesMap() {
        return localNotes;
    }

    public Map<String, CreamFirmaData> readDataList() {
        return localData;
    }


}
