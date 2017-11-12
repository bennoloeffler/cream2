package bel.util;

import bel.en.evernote.ENSharedNotebook;
import com.evernote.edam.type.Note;
import lombok.val;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * All the small helpers...
 */
public class Util {

    /**
     * https://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format-with-date-hour-and-minute
     * https://stackoverflow.com/questions/26142864/how-to-get-utc0-date-in-java-8
     * https://stackoverflow.com/questions/32826077/parsing-iso-instant-and-similar-date-time-strings
     * @return utc time as String
     */
    public static String utcTimeNow() {
        return ZonedDateTime.now(ZoneOffset.UTC).format( DateTimeFormatter.ISO_INSTANT );
    }

    public static Instant utcTimeFrom(String timestamp) {
        DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT;
        return Instant.from(f.parse(timestamp));
    }

    public static String utcTimeFrom(Instant time) {
        return DateTimeFormatter.ISO_INSTANT.format(time);
    }

    public static String readableTime(long millis) {
        long min =  millis / (1000 * 60);
        long sec =  (millis % (1000 *60)) / 1000;
        long ms = millis % 1000;
        String minStr = min > 0 ? "min: " + min + ", " : "";
        String secStr = sec > 0 ? "sec: " + sec + ", " : "";
        return  minStr + secStr + "ms: " + ms;

    }

    public static String memStat() {

        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();

        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        return "S:" +Long.toString(heapSize/(1024*1024)) + "mb";
    }

    public static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }

    public static String extractEmail(String subject) {
        String[] parts = subject.split("\\s");
        if(parts.length>0) {
            //String regexOrig = "^(?=.*(\\.((?![^\\.]$)[^\\.]*)$)|.*@(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$))(?=^.{6,320}$)(?:[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ!#$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ!#$%&'*+\\/=?^_`{|}~-]+)*|\"(?:[\\xE1\\xE0\\xE2\\xE4\\xE3\\xE5\\xE7\\xE9\\xE8\\xEA\\xEB\\xED\\xEC\\xEE\\xEF\\xF1\\xF3\\xF2\\xF4\\xF6\\xF5\\xFA\\xF9\\xFB\\xFC\\xFD\\xFF\\xE6\\xC1\\xC0\\xC2\\xC4\\xC3\\xC5\\xC7\\xC9\\xC8\\xCA\\xCB\\xCD\\xCC\\xCE\\xCF\\xD1\\xD3\\xD2\\xD4\\xD6\\xD5\\xDA\\xD9\\xDB\\xDC\\xDD\\xC6\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f\\s]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")(?:(?=@(?:[0-9]{1,3}.){3}[0-9]{1,3})@(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))$|(?!@(?:[0-9]{1,3}.){4,}$)@(?:(?:[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ](?:[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ-]*[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ])?\\.)+[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ](?:[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ-]*[a-zA-Z0-9áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ])?)$)";
            String regex = "^(?=.*(\\.((?![^\\.]$)[^\\.]*)$)|.*@(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$))(?=^.{6,320}$)(?:[a-zA-Z0-9!#$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+\\/=?^_`{|}~-]+)*|\"(?:[\\xE1\\xE0\\xE2\\xE4\\xE3\\xE5\\xE7\\xE9\\xE8\\xEA\\xEB\\xED\\xEC\\xEE\\xEF\\xF1\\xF3\\xF2\\xF4\\xF6\\xF5\\xFA\\xF9\\xFB\\xFC\\xFD\\xFF\\xE6\\xC1\\xC0\\xC2\\xC4\\xC3\\xC5\\xC7\\xC9\\xC8\\xCA\\xCB\\xCD\\xCC\\xCE\\xCF\\xD1\\xD3\\xD2\\xD4\\xD6\\xD5\\xDA\\xD9\\xDB\\xDC\\xDD\\xC6\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f\\s]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")(?:(?=@(?:[0-9]{1,3}.){3}[0-9]{1,3})@(?:(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))$|(?!@(?:[0-9]{1,3}.){4,}$)@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)$)";

            List<String> result = RegexUtils.findWithRegex(parts[0],
                    regex,
                    0);
            if(result.size() >= 1) {
                return result.get(0);
            }
        }
        return null;
    }


    static public boolean belongsTo(ENSharedNotebook notebook, Note n) {
        if(n==null) throw new RuntimeException("n null");
        //System.out.println("GUID: "+ n.getNotebookGuid());
        //System.out.println("CONTENT: "+ n.getContent());
        if(n.getNotebookGuid()==null) throw new RuntimeException("n.getNotebookGuid() null");


        if(notebook==null) throw new RuntimeException("notebook null");
        if(notebook.getSharedNotebook()==null) throw new RuntimeException("notebook.getSharedNotebook() null");
        if(notebook.getSharedNotebook().getNotebookGuid()==null) throw new RuntimeException("notebook.getSharedNotebook().getNotebookGuid() null");


        return n.getNotebookGuid().equals(notebook.getSharedNotebook().getNotebookGuid());
    }

    public static String inHtmlBody(String title, String body) {
        return "<html>\n" +
                "<head>\n" +
                "<completeString>"+title+"</completeString>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                body +
                "</body>\n" +
                "\n" +
                "</html>";
    }

    /**
     * special case, when unser marked mail adress with l:
     * @param s
     * @return
     */
    public static String extractEmailLinkTo(String s) {
        val regex = "l:([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6})";
        List<String> result = RegexUtils.findWithRegex(s, regex,1);
        if(result.size() >= 1) {
            return result.get(0);
        }
        return null;
    }

    public static void main(String[] args) {
        String now = Util.utcTimeNow();
        Instant zonedDateTime = Util.utcTimeFrom(now);
        String nowAgain = Util.utcTimeFrom(zonedDateTime);
        assert now.equals(nowAgain);
    }
}
