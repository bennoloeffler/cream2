package bel.util.enml

import bel.en.Main
import bel.en.evernote.ENConnection
import bel.en.evernote.ENSharedNotebook
import bel.util.HtmlToPlainText
import com.evernote.edam.type.Note
import com.syncthemall.enml4j.ENMLProcessor
import groovy.util.logging.Log4j2
import org.junit.Test
import spock.lang.Specification
/**
 * Test playing with loading ressources (i.e. images) and links (hrefs) and
 * evernote links.
 *
 *
 */
@Log4j2
class HistoryTextFromENML extends Specification {

    ENConnection enConnection
    ENSharedNotebook enSharedNotebook

    void setup() {
        connectEvernote()
    }

    void cleanup() {
        // not needed...
    }

    private connectEvernote() {
            log.info("Going to connect to Evernote...");
            enConnection = ENConnection.from(Main.AUTH_TOKEN);
            System.out.print(".");
            enConnection.connect();
            System.out.print(".");
            enSharedNotebook = new ENSharedNotebook(enConnection, Main.SALES_NOTEBOOK_SHARE_NAME);
            log.info("connected");
    }

    @Test
    def "read note with ressources and convert to text"() {
        //setup:
        when:
        def notes = enSharedNotebook.findNotes("TESTXXXX")
        Note n = notes[0]
        def ressources = n.getResources()
        enSharedNotebook.loadNoteRessources(n)
        ressources = n.getResources()

        then:
        //notes.size() == 1
        n != null
        ressources != null
        def html = ENMLProcessor.get().noteToInlineHTMLString(n)
        html != null
        def plainText = HtmlToPlainText.convert(html)
        plainText != null
        println plainText
    }

    @Test
    def "read note WITHOUT ressources"() {
        //setup:
        when:
        def notes = enSharedNotebook.findNotes("TESTXXXX")
        Note n = notes[0]
        def ressources = n.getResources()

        then:
        notes.size() == 1
        n != null
        ressources != null
        def html = ENMLProcessor.get().noteToInlineHTMLString(n)

    }
}
