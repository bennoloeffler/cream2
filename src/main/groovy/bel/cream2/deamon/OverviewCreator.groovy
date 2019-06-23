package bel.cream2.deamon

import bel.cream2.deamon.OverviewCreator.LinkEntry
import bel.en.Main
import bel.en.data.AbstractConfiguration
import bel.en.data.CreamUserData
import bel.en.email.ReadAndForwardExchangeMails
import bel.en.evernote.Angebot
import bel.en.evernote.ENConnection
import bel.en.evernote.ENHelper
import bel.en.evernote.ENSharedNotebook
import bel.en.helper.DateFinder
import bel.en.localstore.NoteStoreLocal
import bel.en.localstore.SyncHandler
import bel.util.versioning.DiffDeferrer
import bel.util.versioning.VersionedFile
import com.evernote.edam.type.Note
import groovy.util.logging.Log4j2
import org.apache.commons.lang3.Pair

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
/**
 * Creates an overview for all users that can be found in config...
 */
@Log4j2
class OverviewCreator {

    /**
     * This one is to sort the titleStrings according to due date
     */
    private Comparator<LinkEntry> stringComparator = new Comparator<LinkEntry>() {
        int compare(LinkEntry o1, LinkEntry o2) {
            Calendar c1 = o1.getEarliestDate()
            Calendar c2 = o2.getEarliestDate()
            if (c1 != null && c2 != null) {
                return (int) ((c1.getTimeInMillis() - c2.getTimeInMillis()) / 1000)
            } else if (c1 == null && c2 != null) {
                return Integer.MAX_VALUE
                //return (int) ((0 - c2.getTimeInMillis()) / 1000)
            } else if (c1 != null && c2 == null) {
                return Integer.MIN_VALUE
                //return (int) ((c1.getTimeInMillis() - 0) / 1000)
            }
            //return o1.completeString.compareTo(o2.completeString) // both are null
            return 0 // both are null
        }
    }


    private final String ALL_USERS = "__ALL__" // key to save all entries in one special map, to create the "total overview"

    /**
     * Evernote layer
     */
    //ENConnection enConnection // capsule for BELs account
    ENSharedNotebook enSharedNotebook // handler for the different group notes
    ENSharedNotebook theOverviewNotebook
    //ENConfiguration enConfiguration

    Map<String, ArrayList<LinkEntry>> userOverviewDataMap = new HashMap<>() // UserName, List of Overview Entries
    ArrayList<Pair<Note, Angebot>> hotLeads = new ArrayList<>()




    void createOverviews() throws  Exception{
        List<CreamUserData> users = AbstractConfiguration.getConfig().getUsers()
        userOverviewDataMap = new HashMap<>()
        hotLeads = new ArrayList<>()

        for (CreamUserData u: users) {
            userOverviewDataMap.put(u.getShortName(), new ArrayList<>())
        }
        userOverviewDataMap.put(ALL_USERS, new ArrayList<>())

        //System.out.println("\nGoing to read all notes for processing...")
        //System.out.println("OPEN Todos found:")
        readAndProcessData(users)

        //System.out.println("\nGoing to create ANGEBOTEundHOT overview...")
        createOverviewForHOTLEADS()


        //System.out.println("\nGoing to create all overviews...")
        for (CreamUserData u: users) {
            createOverviewFor(u.getShortName())
        }

        createOverviewFor(ALL_USERS)


    }

    // read all notes and create the data, neccesary for writing the notes
    private void readAndProcessData(List<CreamUserData> users) throws Exception {

        //List<Note> noteList = enSharedNotebook.getAllNotes()
        List<Note> noteList = SyncHandler.get().getAllNotes()
        log.debug("Number of NOTEs to search for TODOs and HOTs: ${noteList.size()}")

        for(Note n : noteList) {

            LinkEntry noteEntry = new LinkEntry()
            if(isNormalNote(n)) {

                // process leads
                ArrayList<Pair<Note, Angebot>> angebote = ENHelper.findAllHot(n)
                //log.debug("found ${angebote?.size()} HOTs in $n.title")
                hotLeads.addAll(angebote)
                // CHANGE
                enSharedNotebook = SyncHandler.get().getNotebook(n)
                if(enSharedNotebook == null) {
                    println(n)
                }
                assert enSharedNotebook != null
                String link = enSharedNotebook.getInternalLinkTo(n)

                noteEntry.title = link
                noteEntry.titleRaw = n.title

                // CHANGE
                //String noteContent = enSharedNotebook.getNoteContent(n)
                String noteContent = n.getContent()
                //System.out.print(".")
                String pn = ENHelper.findFirstPhoneNr(noteContent)
                noteEntry.phone = pn

                //RegexUtils.stopIfFound(noteContent, "dem alten Spirit zurücksehnen. Aber es braucht eben")

                ArrayList<String> todos = ENHelper.findTODOs(noteContent)

                Set<String> addToUser = new TreeSet<>()
                for (String todo : todos) {
                    // TODO: remove </span> elements, if there is no opening one... or, even remove ALL type of tags?
                    todo = todo.replaceAll("<[^>]*>","")
                    //System.out.println("[ ] " + todo + "    ( " + n.getTitle() + " )" )
                    for(CreamUserData u: users) {
                        if(todo.contains(u.getShortName()+":")) {
                            addToUser.add(u.getShortName())
                        }
                    }
                    // this is not escaped with escapeHTML, see: getInternalLinkTo
                    // ENML seems to be able to handle Umlaute and special symbols like < and > in todo strings.

                    link = link + "<div>" + todo + "</div>"

                    noteEntry.todos.add(todo)
                }
                for(String u: addToUser) {
                    userOverviewDataMap.get(u).add(noteEntry.clone())
                }
                if(addToUser.size() > 0) {
                    userOverviewDataMap.get(ALL_USERS).add(noteEntry.clone())
                }
            }
        }
    }

    private boolean isNormalNote(Note n) {
        if(n.getTitle().contains("UEBERSICHT")) return false
        if(n.getTitle().contains("CRM-KONFIGURATION")) return false
        if(n.getTitle().contains("ANGEBOTE_und_HOT")) return false
        return true
    }



    // create an overview exacty and ony for the TODOs of one user
    private void createOverviewFor(String shortName) throws Exception {
        filterUserWhenSort = shortName
        if(shortName.equals(ALL_USERS)) {
            filterUserWhenSort = null
        }

        ArrayList<LinkEntry> listOfTitelStrings = userOverviewDataMap.get(shortName)

        Collections.sort(listOfTitelStrings, stringComparator)

        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
        "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"+
        "<en-note>"
        String head = content

        LocalDateTime date = LocalDateTime.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        String dateText = date.format(formatter)

        content += "<div>Übersicht vom <b>" + dateText + "</b></div>"+
        "<div><b>" + listOfTitelStrings.size() + "</b> Vorgänge</div>"+
        "<div><b>Anleitung</b> siehe Ende dieser Notiz.</div>"+
        "<div><b>Zahl am Anfang:</b> Tage bis zum geplanten Kontakt. Negativ = Vergangenheit.</div>"+
        "<div><br/></div>"

        //System.out.println("Sorted set of notes has size: " + listOfTitelStrings.size())
        StringBuffer newOverviewVersion = new StringBuffer('')
        for (LinkEntry linkEntry : listOfTitelStrings) {
            def fs = linkEntry.toFileString()
            fs = ENHelper.transformENMLToNormal(fs)
            newOverviewVersion.append(fs+'\n\n\n')
            //ENHelper.stopIfFound(linkEntry.completeString, "Gleason")
            //ENHelper.stopIfFound(linkEntry.completeString, "Bosch PA")
            String block

            Calendar cal = linkEntry.getEarliestDate()
            String prefix = ""
            if (cal == null) {
                prefix = "EGAL... "
            } else {
                //long daysTitel = cal.getTimeInMillis() / MILLIS_TO_DAYS_DIVIDER
                //long diffDays = daysTitel - daysNow

                //String diffStr = Long.toString(diffDays)
                //if (diffDays < 7) diffStr = "<b>" + diffStr + "</b>"
                //if (diffDays < 1) diffStr = "<span style=\"color: rgb(227, 0, 0)\">" + diffStr + "</span>"
                prefix = Long.toString(linkEntry.daysToTodo())
                if (linkEntry.doSoon()) prefix = "<b>" + prefix + "</b>"
                if (linkEntry.doNow()) prefix = "<span style=\"color: rgb(227, 0, 0)\">" + prefix + "</span>"
                prefix += " "
            }
            block = "<div>" + prefix + linkEntry.title
            content += "<div>" + prefix + linkEntry.title
            if (linkEntry.phone != null) {
                block +=  " " + linkEntry.phone
                content += " " + linkEntry.phone

            }
            block  += "</div>"
            content += "</div>"
            for(String todo:linkEntry.todos) {
                // this is not escaped with escapeHTML, see: getInternalLinkTo
                // ENML seems to be able to handle Umlaute and special symbols like < and > in todo strings.
                block +=  "<div>" + todo + "</div>"
                content += "<div>" + todo + "</div>"
                //System.out.println(todo)
                //if(todo.contains("</span>")) {
                //    System.out.println("DESASTER")
                //}
            }
            block +=  "<div><br /></div>"
            content += "<div><br /></div>"
            //saveContent(n, head, block) //ONLY FOR DEBUGGING single blocks of enml
        }
        if(listOfTitelStrings.size() == 0) {
            content += "<div><br/></div><span style=\"color: rgb(227, 0, 0)\">GRAD NIX...</span><div><br/></div>"
        }
        content = addDescription(content)
        content += "</en-note>"

        def path = "$NoteStoreLocal.NOTE_STORE_BASE\\overviews\\$shortName"+'.txt'
        VersionedFile versionedFile = new VersionedFile(path)
        versionedFile.text = newOverviewVersion.toString()

        Note n; boolean emptyOnServer
        (n,emptyOnServer) = findOrCreateNote(shortName)

        if(versionedFile.versions.last().changed || emptyOnServer) {

            log.debug("erzeuge ÜBERSICHT  ( " + n.getTitle() + " ) mit Inhalt:\n" + content)
            n.setContent(content)
            overviewNotebook.updateNote(n)
            log.info("NEW OVERVIEW: " + n.getTitle() + "  --> wrote SUCCESSFUL to evernote")

            // and send the diff to the user...
            def htmlDiff = versionedFile.versions.last().HTMLDiff
            def adress = AbstractConfiguration.getConfig().getEmail(shortName)
            if(adress) { // except _ALL_USERS_
                def dm = AbstractConfiguration.getConfig().getUser(shortName)?.isDiffMail()
                def dmd = AbstractConfiguration.getConfig().getUser(shortName)?.isDiffMailOncePerDay()
                if(dm) {
                    if(dmd) {
                        def dd = new DiffDeferrer(versionedFile, 1000*60*60*24 )
                        if(dd.shouldShowDiff()) {
                            htmlDiff = dd.HTMLDiff
                            new ReadAndForwardExchangeMails().sentMailTo(adress, "CREAM TODOs...", htmlDiff , true)
                        }
                        /*
                        // check, if alread sent today
                        def last = new File (versionedFile.getPath() + ".lastMail.txt")
                        def lastSent = 0
                        def idx = 0
                        if(last.exists()) {
                            def millisAndIdx = last.text.split(" ")
                            lastSent = Long.parseLong(millisAndIdx[0].trim())
                            idx = Integer.parseInt(millisAndIdx[1].trim())
                        }
                        long duration = System.currentTimeMillis() - lastSent
                        if(duration > 1000*60*60*24) {
                            htmlDiff = versionedFile.versions[idx].HTMLDiffToLatest
                            new ReadAndForwardExchangeMails().sentMailTo(adress, "CREAM TODOs...", htmlDiff, true)
                            last.text = Long.toString(System.currentTimeMillis()) + " " + Integer.toString(versionedFile.versions.size()-1)
                        }
                        */

                    } else {
                        new ReadAndForwardExchangeMails().sentMailTo(adress, "CREAM TODOs...", htmlDiff, true)
                    }
                }
                new File("${path}.diff.html").text = htmlDiff
            }
        } else {
            log.info("Found no changes for $shortName. NO NEW OVERVIEW...")
        }
    }

    /**
    * make it accessible via overviewNotebook
    */
    private ENSharedNotebook getOverviewNotebook() {
        if (theOverviewNotebook == null) {
            String overviewNotebookName = AbstractConfiguration.getConfig().getCreamNotebooks().overviewNotebook
            theOverviewNotebook = new ENSharedNotebook(ENConnection.get(), overviewNotebookName)
        }
        theOverviewNotebook
    }

    private void createOverviewForHOTLEADS() throws Exception {
        LocalDateTime date = LocalDateTime.now()
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        String dateText = date.format(formatter)

        double weightedSum = 0
        double totalSum = 0
        String data = ""
        log.debug("Number of HOT LEADS: ${hotLeads.size()}")
        for (Pair<Note, Angebot> hot: hotLeads) {
            //ENHelper.stopIfFound(linkEntry.completeString, "Gleason")
            //ENHelper.stopIfFound(linkEntry.completeString, "Bosch PA")
            ENSharedNotebook notebook = SyncHandler.get().getNotebook(hot.left)
            String link = notebook.getInternalLinkTo(hot.left)
            data += "<div>"+link+"</div>"
            data += "<div>"+hot.right.toString()+"</div>"
            data += "<div><br/></div>"
            //saveContent(n, head, block) //ONLY FOR DEBUGGING single blocks of enml
            if(hot.right.probability > 0 && hot.right.euros > 0) {
                weightedSum +=  (double)hot.right.euros * ((double) hot.right.probability ) / 100.0
            }
            if(hot.right.euros > 0) {
                totalSum +=  hot.right.euros
            }
        }


        String content =
                """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
                   <!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">
                    <en-note>"""

        content += "<div>Übersicht vom <b>" + dateText + "</b></div>"  + "<div><b>" + hotLeads.size() + "</b> Vorgänge</div>" + "<div>Summe über alles:</div>"
        content += "<div><b>gewichtet: "+ String.format(" %.2f EUR", weightedSum)+"</b></div>"
        content += "<div>absolut: "+ String.format(" %.2f EUR", totalSum)+"</div>"+ "<div><br/></div>"

        content += data

        content += "</en-note>"

        def path = "$NoteStoreLocal.NOTE_STORE_BASE\\overviews\\ANGEBOTE_und_HOT"+'.txt'
        VersionedFile versionedFile = new VersionedFile(path)
        versionedFile.text = data

        Note n; boolean emptyOnServer
        (n,emptyOnServer) = findOrCreateNote("ANGEBOTE_und_HOT")

        if(versionedFile.versions.last().changed || emptyOnServer) {
            log.debug("Writing this content to " + n.getTitle() + ": " + content)
            n.setContent(content)
            overviewNotebook.updateNote(n)
        } else {
            log.info("Found no changes for ANGEBOTE_und_HOT. NO NEW OVERVIEW...")
        }

        //System.out.println("SUCCESS: Updated content of " + n.getTitle())

    }


/*
    private void saveContent(Note n, String head, String block) throws Exception {
        String content = head + block + "</en-note>"
        try {
            n.setContent(content)
            enSharedNotebook.updateNote(n)
        } catch (Exception e) {
            System.out.println("FAILED: " + content)
            e.printStackTrace()
        }
    }
*/

    // if not yet there, create it. Otherwise, return the existing one
    /**
     * @returns [ Note note, boolean createdOnServer ]
     */

    private List findOrCreateNote(String shortName) throws Exception {
        boolean created = false
        Note returnNote = null
        String searchFor = shortName + "_" + Main.UEBERSICHT_TITLE_STRING
        if(shortName.equals(ALL_USERS)) {
            searchFor = Main.UEBERSICHT_TITLE_STRING
        }
        List<Note> listUebersicht = overviewNotebook.findNotes("intitle:["+searchFor+"]")
        int size = listUebersicht.size()
        if (size == 0) { // not found. Create one...
            returnNote = overviewNotebook.createNote(searchFor)
            log.debug("Created new " + searchFor)
            created = true
        } else if (size == 1) { // Cool. Found exactly one. Use that.
            returnNote = listUebersicht.get(0)
            //System.out.println("Found " + searchFor)
        } else { //hmmm... Found more than one. Delete all but one and use that.
            for (int i = 1; i < size; i++) {
                Note n = listUebersicht.get(i)
                overviewNotebook.deleteNote(n)
                log.warn("found more than one overview. potential bug. deleted double note " + n.getTitle())
            }
            // and remember the one remaining
            returnNote = listUebersicht.get(0)
        }
        return [returnNote, created]
    }

    /**
     * Just moved that here, to clean up other function. This is the "Betriebsanleitung" for the Übersicht.
     * @param content original ENML text
     * @return the original content with added Betriebsanleitung
     */
    protected String addDescription(String content) {
        return content + "<div><br /></div>"+
        "<div><br /></div>"+
        "<div><br /></div>"+
        "<div><br /></div>"+
        "<div><b>BETRIEBS-ANLEITUNG:</b><br/></div>"+
        "<div><br/><b>TERMINE:</b> Datums-Formate in der Überschrift werden gescannt<br/>und die Einträge in der Übersicht danach sortiert.<br/>Folgende Datums-Formate funktionieren:</div>"+
        "<div>17.2.17 oder 13.7.2016 (mit Jahr)</div>"+
        "<div>15.6.  (ohne Jahr, PUNKT am Ende!)</div>"+
        "<div>KW23 oder KW 23 (erster Tag der KW wird angenommen)</div>"+
        "<div>Dez, Juli, Mär (3 Buchstaben, erster groß oder vollständig)</div>"+
        "<div>Dez ist gleichbedeutend mit 1.12.</div>"+
        "<div>Dez 2017 geht auch. Dezember 2017 auch.</div>"+
        "<div>VORSICHT: wenn das unbestimmte Datum (z.B. KW3, Dez, 15.6.) im aktuellen Jahr <b>laenger als ca. 8 Wochen</b></div>"+
        "<div>in der Vergangenheit liegt, wird das kommende Jahr angenommen!</div>"+
        "<div>Wenn mehrere Angaben in einer todo Zeile liegen, wird die genaueste genutzt.</div>"+
        "<div>Es gilt 15.11.14 vor 23.7 vor KW6 vor Januar.</div>"+
        "<div>Datumsangaben brauchen entweder ein folgendes Leerzeichen oder einen Doppelpunkt. 15.6.: KW8: Dez:</div>"+
        "<div><br/><b>TELEFON:</b> Die <b>erste Telefonnummer</b> in der Notiz wird angehaengt. <br/>Es sei denn, vor der Nummer steht IGN: - dann die zweite etc.</div>"+
        "<div><br/><b>TODOs:</b> Die <b>unerledigten todos</b> (Kontrollkästchen ohne Haken)<br/> in der Notiz werden übernommen.</div>"+
        "<div>Wenn in den todos Kürzel mit : <b>(Doppelpunkt!)</b> sind, dann erscheinen die Einträge in User-spezifischen Übersichten.</div>"+
        "<div>Also BEL: im todo befördert einen Eintrag in die Notiz BEL_UEBERSICHT - wenn es den User BEL gibt. Siehe CRM-KONFIGURATION.</div>"+
        "<div><b><br/>Mehrfach-TODOs und REDEN</b></div>"+
        "<div>Beispiel: BEL: NIT: KW35 REDEN Bücher angekommen, dann mit GF bla bla ... </div>"+
        "<div>Der erste Eintrag (BEL:) hat die Aufgabe.</div>"+
        "<div>Der zweite Eintrag (NIT:) bekommt die Aufgabe ebenfalls in der TODO-Liste gezeigt.</div>"+
        "<div>Wenn dort das Schlüsselwort REDEN steht, dann sollte der Ausführende (BEL:) vor dem Machen mit dem anderen (NIT:) sprechen.</div>"+
        "<div>anderes Beispiel: ANL: BEL: 1.10.17 REDEN 7P an GF schicken</div>"+
        "<div>Das kann man so lesen: BEL: bittet ANL: erst mit ihm zu reden und dann eine 7P-Broschüre an die GF zu schicken. Und zwar bis zum 1.10.17 </div>"+
        "<div>Wenn die Aufgabe erledigt ist, hakt Anna das TODO ab. Dann wird es auch aus der Liste bei BEL verschwinden.</div>"+
        "<div><b><br/>*HOT und *ANGEBOT</b></div>"+
        "<div>Wenn am Anfang der Zeile *HOT oder *ANGEBOT steht, dann erscheint ein Eintrag in der HOT_und_ANGEBOTE_UEBERSICHT</div>"+
        "<div>Beispiel:</div>"+
        "<div>*HOT FAS: 30% EUR 50.000 1.9.2018 Vorstand Grebisz hat angebissen - aber derzeit viele Baustellen</div>"+
        "<div>Das bedeutet: FAS schätzt eine Chance von 30% das was kommt. Und zwar in der Region von 50.000 EUR. Die Schätzung ist vom 1.1.2019.</div>"+
        "<div><br/><br/><b>MAIL-KOMMANDOS:</b> also dies Kommandos an crm@v-und-s.de schicken...<br/></div>" +
        "<div>"+ ReadAndForwardExchangeMails.mailHelpHtml + "</div>" +
        "<div><br/><br/><b>KNOWN BUGS:</b> Wenn das Programm mit EDAMMalformedException abstürzt, dann liegt das daran, dass 'malformed html-Tags' drin sind. Lösung: TODOs als Text formatieren in der PublicBath-App :-(<br/>Seltsam: Kaufmanns-Und und Umlaute in todo-Texten kommen in ENML 'im Klartext' und nicht als HTML-Sonderzeichen. Das könnte ein Problem werden.</div>"

    }


    public static void main(String[] args) {
        LinkEntry e = new LinkEntry(titleRaw: 'this is the title', todos: ["todo 1", '18.11.17 todo2 sdfasdf asdfasdf'], phone: "0171 / 23 4 55 4 444")
        println e.toFileString()
        assert !e.doNow()
        assert e.doSoon()
    }


    static String filterUserWhenSort = null

    static class LinkEntry {

        private static long MILLIS_TO_DAYS_DIVIDER = 1000 * 60 * 60 * 24

        boolean doNow() {
            daysToTodo() < 1
        }

        boolean doSoon() {
            daysToTodo() < 7
        }


        long daysToTodo() {
            long millis = System.currentTimeMillis()
            long daysNow = millis / MILLIS_TO_DAYS_DIVIDER
            getEarliestDate()
            if (calendar) {
                def daysToToDo = calendar.getTimeInMillis() / MILLIS_TO_DAYS_DIVIDER
                daysToToDo - daysNow
            } else {
                Integer.MAX_VALUE
            }
        }

        String toFileString() {
            getEarliestDate()
            def c = calendar?calendar.format("yyyy-MM-dd"):"EGAL..."
            def p = phone ? "TEL: " + phone : ""
            def n = doNow() ? "JETZT...   " : ""
            def s = doSoon() && !doNow() ? "BALD...   " : ""
            """$s$n$c   $titleRaw   $p\n${todos.join('\n')}"""
        }

        LinkEntry clone() {
            LinkEntry clone = new LinkEntry()
            clone.title = title
            clone.todos = todos
            clone.phone = phone
            clone.titleRaw = titleRaw
            return clone
        }

        String title
        String titleRaw
        String phone
        ArrayList<String> todos = new ArrayList<>()

        private Calendar calendar
        private boolean wasCalculated

        Calendar getEarliestDate() {
            //ENHelper.stopIfFound(completeString, "Rensing")
            //ENHelper.stopIfFound(completeString, "Albeck")

            if(wasCalculated) return calendar
            long earliestMS = Long.MAX_VALUE
            for(String todo:todos) {

                if(filterUserWhenSort != null) {
                    if (!todo.contains(filterUserWhenSort)) {
                        todo = ""
                    }
                }

                Calendar c = new DateFinder(todo).get()
                if (c != null) {
                    long cMS = c.getTimeInMillis()
                    if (earliestMS > cMS) {
                        earliestMS = cMS
                        calendar = c
                    }
                }

            }
            wasCalculated = true
            return calendar
        }
    }

}

