package bel.en;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.thrift.TException;

public class Main {

    /**
     * fixed names, just by convention to find the MAW notebook and the UEBERSICHT note.
     */
    public static final String SALES_NOTEBOOK_SHARE_NAME = "Sales V&S";
    public static final String CONFIG_NOTEBOOK_SHARE_NAME = SALES_NOTEBOOK_SHARE_NAME;
    public static final String UEBERSICHT_TITLE_STRING = "UEBERSICHT";
    // all the notes information to assemble the uebersicht are stored here. One entry per note.

    //private ArrayList<String> listOfTitelStrings = new ArrayList<String>();

    /**
     * This one is to sort the titleStrings according to due date
     */
    /*
    private Comparator<String> stringComparator = new Comparator<String>() {
        public int compare(String o1, String o2) {
            Calendar c1 = new DateFinder(o1).get(); // THIS MAY BE FUCKING TIME CONSUMING and should be cashed if it makes problems.
            Calendar c2 = new DateFinder(o2).get();
            if (c1 != null && c2 != null) {
                return (int) ((c1.getTimeInMillis() - c2.getTimeInMillis()) / 1000);
            } else if (c1 == null && c2 != null) {
                return Integer.MAX_VALUE;
                //return (int) ((0 - c2.getTimeInMillis()) / 1000);
            } else if (c1 != null && c2 == null) {
                return Integer.MIN_VALUE;
                //return (int) ((c1.getTimeInMillis() - 0) / 1000);
            }
            return o1.compareTo(o2); // both are null
        }
    };
    */

    /**
     * Evernote layer
     */
//    ENConnection enConnection; // capsule for BELs account
//    ENSharedNotebook enSharedNotebook; // handler for the MAWs SharedNotebook
//    ENConfiguration enConfiguration;


    // this token uses the account of Benno Loeffler. TODO: Replace by login...
    public static final String AUTH_TOKEN_OLD = "S=s226:U=1abbe88:E=15da1999a5a:C=15649e86c00:P=1cd:A=en-devtoken:V=2:H=a224394752a8539f3783ca6ad138a6e5";
    public static final String AUTH_TOKEN_NEW_BUT_INVALID = "S=s226:U=1abbe88:E=164ff0fbf0e:C=15da75e9178:P=1cd:A=en-devtoken:V=2:H=3527562ed1d2cf900b81eff55cf25c74";
    public static final String AUTH_TOKEN ="S=s226:U=1abbe88:E=164654a4f1f:C=15d0d9922b0:P=185:A=bennoloeffler-2708:V=2:H=d372e6f24a516381c1a550ae5c273b1e";
    // TODO: read AUTH_TOKEN from C:\creamlocal\cream_user.properties lesen - und wenn EVERNOTE_TOKEN nicht da ist: Fehlermeldung und CreamGui starten.
    /**
     * remember the one note which is the Uebersicht
      */
//    private Note uebersicht = null;
//    private Note config = null;


    /**
     * Update the UEBERSICHT.
     *
     * @param args no
     */
    public static void main(String[] args) {

        try {
            UebersichtAllUsers uebersichtAllUsers = new UebersichtAllUsers();
            uebersichtAllUsers.connectENConnection();
            uebersichtAllUsers.createOverviews();
            /*
            Main m = new Main();
            m.connectENConnection();
            m.findUEBERSICHT();
            m.readAllNotesAndScan();
            m.createOverview();
            */
        } catch (EDAMUserException e) {
            System.out.println(e.getErrorCode());
            System.out.println("\n\n FEHLER. Erstellen der Übersichten wurde abgebrochen...");
            e.printStackTrace();
            System.exit(-1);
        } catch (EDAMNotFoundException e) {
            System.out.println(e.toString());
            System.out.println("\n\n FEHLER. Erstellen der Übersichten wurde abgebrochen...");
            e.printStackTrace();
            System.exit(-1);
        } catch (EDAMSystemException e) {
            System.out.println(e.getErrorCode());
            System.out.println("\n\n FEHLER. Erstellen der Übersichten wurde abgebrochen...");
            e.printStackTrace();
            System.exit(-1);
        } catch (TException e) {
            System.out.println(e.toString());
            System.out.println("\n\n FEHLER. Erstellen der Übersichten wurde abgebrochen...");
            e.printStackTrace();
            System.exit(-1);
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("\n\n FEHLER. Erstellen der Übersichten wurde abgebrochen...");
            e.printStackTrace();
            System.exit(-1);
        }
    }


    /**
     * Connect to BELs evernote account and to the shared and linked notebook of MAW.
     * @throws Exception
     */
 /*   private void connectENConnection() throws Exception {
        enConnection = new ENConnection(AUTH_TOKEN);
        enConnection.connect();
        enSharedNotebook = new ENSharedNotebook(enConnection, SALES_NOTEBOOK_SHARE_NAME);
        enConfiguration = new ENConfiguration(enSharedNotebook, enConnection);
    }
*/

    /**
     * Find the UEBERSICHT - Note.
     * create, find or deleteFileFromLocalStore if multiple. At the end, there is exactly one "*** UEBERSICHT ***" Note.
     * Do that, in order to make the internal evernote-link stable. As soon as the one is deleted and new created, links in "Quick-Access-Areas" don't work any more,
     * because the work on basis of GUIDs and not on basis of Title.
     *
     * @throws Exception
     */
/*    private void findUEBERSICHT() throws Exception {
        List<Note> listUebersicht = enSharedNotebook.findNotes("intitle:["+UEBERSICHT_TITLE_STRING+"]");
        int size = listUebersicht.size();
        if (size == 0) { // not found. Create one...
            uebersicht = enSharedNotebook.createNote(UEBERSICHT_TITLE_STRING);
            System.out.println("Created new " + UEBERSICHT_TITLE_STRING);
        } else if (size == 1) { // Cool. Found exactly one. Use that.
            uebersicht = listUebersicht.get(0);
            System.out.println("Found " + UEBERSICHT_TITLE_STRING);
        } else { //hmmm... Found more than one. Delete all but one and use that.
            for (int i = 1; i < size; i++) {
                Note n = listUebersicht.get(i);
                enSharedNotebook.deleteNote(n);
                System.out.println("Deleted double " + UEBERSICHT_TITLE_STRING);
            }
            // and remember the one remaining
            uebersicht = listUebersicht.get(0);

        }
        List<Note> listConfig = enSharedNotebook.findNotes("intitle:["+ ENConfiguration.CONFIG_TITLE_STRING+"]");
        if(listConfig.size() == 1) {
            config = listConfig.get(0);
        }
    }
*/

    /**
     * Read all notes in Notebook MAW_BEL_Sales, find due date, first phoneNr and all open todos, combine them to a String of the form:
     *
     * "DAYS_TO_DUE EN_LINK_TO_NOTE FIRST_PHONE_NR
     * TODO_1
     * TODO_2"
     *
     * and add that string to listOfTitelStrings
     *
     * @throws Exception
     */
    /*
    private void readAllNotesAndScan() throws Exception {

        System.out.print("Reading all notes titles and GUIDs...");
        List<Note> noteList = enSharedNotebook.getAllNotes();
        System.out.println(" finished.");
        System.out.print("Reading content and scan it...");
        String confguid = null;
        if(config != null) {
         confguid= config.getGuid();
        }
        for (Note n : noteList) {
            if (!uebersicht.getGuid().equals(n.getGuid()) && !n.getGuid().equals(confguid)) { // jump over Uebersicht and config
                //Todo: jump over all overview notes
                System.out.print(".");
                String link = enSharedNotebook.getInternalLinkTo(n);
                String noteContent = enSharedNotebook.getNoteContent(n);
                String pn = findFirstPhoneNr(noteContent);
                if (pn != null) {
                    link = link + " " + pn;
                }

                ArrayList<String> todos = findTODOs(noteContent);
                for (String todo : todos) {
                    // this is not escaped with escapeHTML, see: getInternalLinkTo
                    // ENML seems to be able to handle Umlaute and special symbols like < and > in todo strings.
                    link = link + "<div>" + todo + "</div>";
                }
                listOfTitelStrings.add(link);
            } else {
                //System.out.println("skipping: " + n.getTitle());
            }
        }
        System.out.println("finished.");
    }
*/

    /**
     * From the sorted (by due time) single lines collected in listOfTitelStrings,
     * build the new ENML document and save it.
     *
     * @throws Exception
     */
  /*  private void createOverview() throws Exception {
        Collections.sort(listOfTitelStrings, stringComparator);
        Calendar now = new GregorianCalendar(Locale.GERMAN);
        long MILLIS_TO_DAYS_DIVIDER = 1000 * 60 * 60 * 24;
        long daysNow = now.getTimeInMillis() / MILLIS_TO_DAYS_DIVIDER;

        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                        + "<en-note>";
        content += "<div><b>" + listOfTitelStrings.size() + "</b> Vorgänge in der Übersicht.</div>"
                + "<div><b>Anleitung</b> siehe Ende dieser Notiz.</div>"
                + "<div><b>Zahl am Anfang:</b> Tage bis zum geplanten Kontakt. Negativ = Vergangenheit.</div>"
                + "<div><br/></div>";

        System.out.println("Sorted set of notes has size: " + listOfTitelStrings.size());
        for (String titel : listOfTitelStrings) {
            Calendar cal = new DateFinder(titel).get();
            String prefix = "";
            if (cal == null) {
                prefix = "EGAL... ";
            } else {
                long daysTitel = cal.getTimeInMillis() / MILLIS_TO_DAYS_DIVIDER;
                long diffDays = daysTitel - daysNow;

                String diffStr = Long.toString(diffDays);
                if (diffDays < 7) diffStr = "<b>" + diffStr + "</b>";
                if (diffDays < 1) diffStr = "<span style=\"color: rgb(227, 0, 0);\">" + diffStr + "</span>";
                prefix = diffStr + " ";
            }
            String addStr = "<div>" + prefix + titel + "</div>";
            content += addStr;
            content += "<div><br /></div>";
        }
        content = addDescription(content);
        content += "</en-note>";
        System.out.println("Writing this content to " + UEBERSICHT_TITLE_STRING + ": " + content);

        uebersicht.setContent(content);
        enSharedNotebook.updateNote(uebersicht);

        System.out.println("SUCCESS: Updated content of " + UEBERSICHT_TITLE_STRING);
    }
*/

    /**
     * Just moved that here, to clean up other function. This is the "Betriebsanleitung" for the Übersicht.
     * @param content original ENML text
     * @return the original content with added Betriebsanleitung
     */
/*    protected String addDescription(String content) {
        return content + "<div><br /></div>"
                + "<div><br /></div>"
                + "<div><br /></div>"
                + "<div><br /></div>"
                + "<div><b>BETRIEBS-ANLEITUNG:</b><br/></div>"
                + "<div><br/><b>TERMINE:</b> Datums-Formate in der Überschrift werden gescannt<br/>und die Einträge in der Übersicht danach sortiert.<br/>Folgende Datums-Formate funktionieren:</div>"
                + "<div>17.2.17 oder 13.7.2016 (mit Jahr)</div>"
                + "<div>15.6.  (ohne Jahr, PUNKT am Ende!)</div>"
                + "<div>KW23 (erster Tag der KW wird angenommen)</div>"
                + "<div>Dez, Juli, Mär (3 Buchstaben, erster groß oder vollständig)</div>"
                + "<div>Dez ist gleichbedeutend mit 1.12.</div>"
                + "<div>VORSICHT: wenn das Datum im aktuellen Jahr <b>laenger als ca. 8 Wochen</b></div>"
                + "<div>in der Vergangenheit liegt, wird das kommende Jahr angenommen!</div>"
                + "<div>Wenn mehrere Angaben in einer Zeile liegen, wird nur die genaueste genutzt:</div>"
                + "<div>Es gilt 15.11.14 vor 23.7 vor KW6 vor Januar.</div>"
                + "<div>Datumsangaben brauchen entweder folgendes Leerzeichen oder einen Doppelpunkt. 15.6.: KW8: Dez:</div>"
                + "<div><br/><b>TELEFON:</b> Die <b>erste Telefonnummer</b> in der Notiz wird angehaengt. <br/>Es sei denn, vor der Nummer steht IGN: - dann die zweite etc.</div>"
                + "<div><br/><b>TODOs:</b> Die <b>unerledigten todos</b> (Kontrollkästchen ohne Haken)<br/> in der Notiz werden übernommen.</div>"
                + "<div><br/><b>KNOWN BUGS:</b> Keine. Alle raus gemacht. :-)<br/>Seltsam: Kaufmanns-Und und Umlaute in todo-Texten kommen in ENML 'im Klartext' und nicht als HTML-Sonderzeichen. Das könnte ein Problem werden.</div>"
                + "<div><br/></div>";

    }
*/
}
