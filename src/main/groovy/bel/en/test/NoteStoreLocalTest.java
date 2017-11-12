package bel.en.test;

import junit.framework.TestCase;

/**
 * Created by VundS02 on 15.09.2016.
 */
public class NoteStoreLocalTest extends TestCase {

    public void testWriteReadSame() throws Exception{
        /*
        NoteStoreLocal nsl = new NoteStoreLocal(new TestData().getTestConfig());
        nsl.deleteAllLocalFiles();
        String guid = "soidzfosazt";
        Note n = new Note();
        n.setTitle("abc & sdfhö >&");
        n.setGuid(guid);
        n.setContent(new TestData().maxMusterMannENML);
        nsl.updateNote(n);
        nsl.invalidateCache();
        //nsl.readAllFromDisk();
        Map<String, Note> notesMap = nsl.getNotesMap();
        Note newNote = notesMap.get(guid);
        assertEquals(newNote.getContent(), n.getContent());
        */
    }

}
