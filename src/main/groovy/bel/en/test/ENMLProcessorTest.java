package bel.en.test;

import bel.en.evernote.ENHelper;
import bel.learn._14_timingExecution.RunTimer;
import com.evernote.edam.type.Note;
import com.syncthemall.enml4j.ENMLProcessor;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;

/**
 * try to isolate the enml transfrom bug...
 */
@Log4j2
public class ENMLProcessorTest {

    @Test
    public void testConversionFromData() throws XMLStreamException {
        ENMLProcessor enmlProcessor = ENMLProcessor.get();
        Note n = new Note();
        n.setTitle("-- ÜBERSCHRIFT ---");
        String content = ENHelper.createValidEmptyContentWithEmptyDataBlock();
        n.setContent(content);
        ENHelper.addHistoryEntry(n, "das wurde getan");
        ENHelper.addHistoryEntry(n, "das ist ein todo", true);


        System.out.println("STARTE DEN TEST");
        RunTimer t = new RunTimer();
        String html = enmlProcessor.noteToInlineHTMLString(n);
        t.stop();
        System.out.println("TEST IST GELAUFEN");
        System.out.println("AUSGANGSDATEN: " + n.getContent());
        System.out.println("ERGEBNIS: " + html);
    }

    public static void main(String[] args) throws XMLStreamException {
        new ENMLProcessorTest().testConversionFromData();
    }
}
