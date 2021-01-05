package bel.en.evernote;

import bel.en.data.*;
import bel.en.helper.DateFinder;
import bel.en.localstore.SyncHandler;
import bel.util.ENMLToPlainText;
import bel.util.HtmlToPlainText;
import bel.util.RegexUtils;
import com.evernote.edam.type.Note;
import com.syncthemall.enml4j.ENMLProcessor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * HTML and Regex functions for ENML-Documents are here as globally available functions.
 * They transform ENML into Firma and Person and vice verca.
 */
@Log4j2
public class ENHelper {


    /**
     * HTML-Escape symbols like Umlaute and & and < and >, so that they can be used in
     * ENML.
     *
     * @param s
     * @return excaped version of s
     */
    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String newLineToBR(String s) {
        s =s.replaceAll("\\r\\n", "<br/>");
        return s;
    }


    /**
     * tramsform escaped ENML to normal text .
     *
     * @param s
     * @return "normalized" version of s
     */
    public static String transformENMLToNormal(String s) {
        // & + - * / \ ´ ^ ° µ € [ ] { } 4 3 ³ ² % & § " ! | # ~ ö ä ü Ö Ä Ü ß ?
        s= s.replaceAll("\\&nbsp;", " ");
        s= s.replaceAll("\\&amp;", "&");
        s= s.replaceAll("\\&quot;", "\"");
        s= s.replaceAll("\\&lt;", "<");
        s= s.replaceAll("\\&gt;", ">");
        s= s.replaceAll("\\&apos;", "'");

        return s;
    }

    public static String transformNormalToENML(String s) {
        // & + - * / \ ´ ^ ° µ € [ ] { } 4 3 ³ ² % & § " ! | # ~ ö ä ü Ö Ä Ü ß ?
        //s= s.replaceAll("\\&nbsp;", " ");
        s= s.replaceAll("\\&", "&amp;");
        s= s.replaceAll("\"", "&quot;");
        s= s.replaceAll("<", "&lt;");
        s= s.replaceAll(">", "&gt;");
        s= s.replaceAll("'", "&apos;");
        return s;
    }


    /**
     * Find the first phone number in the string s. Ignore phone numbers, if there are preceeded with IGN or IGN:
     * Major Problem: The Evernote links contain combinations of numbers, that fit those patterns pretty
     * much (short phone numbers...), when we allow for short numbers.
     * Therefore, we enforce the number to be at least 7 number long.
     *
     * @param s text to search in
     * @return isolated phone number
     */
    public static String findFirstPhoneNr(String s) {
        //String regexPhoneNr = "((\\+49|0)[()\\d\\-/\\s]*\\d)([\\s<])";
        String regexPhoneNr = "(\\s|;|:|>)(IGN:|IGN)*\\s*((\\+|0){1}[123456789()\\s][\\d\\s/\\-()]{7,})";
        //String regexPhoneNr = "\\s((\\+|0){1}[123456789()\\s][\\d\\s/\\-()]{5,})";
        //String regexPhoneNr = ".*(Schritt).*";

        Pattern p = Pattern.compile(regexPhoneNr);
        Matcher m = p.matcher(s);
        while (m.find()) {
            String ign = m.group(2);
            String number = m.group(3);
            if (ign == null) {
                return number;
            }
        }
        return null;
    }


    /**
     * In evernote ENML, there is a special tag that symbolises todos.
     * Ths function finds all open todos an returns the todo-texts in a list.
     * @param content
     * @return list of todo texts
     */
    public static ArrayList<String> findTODOs(String content) {
        //String regexTODO = "(<en-todo\\s*\\/|<en-todo checked=\\\"false\\\"\\s*\\/|<en-todo><\\/en-todo)>([^<]*)";
        String regexTODO = "(<en-todo\\s*\\/|<en-todo checked=\\\"false\\\"\\s*\\/|<en-todo><\\/en-todo)>(.*?)(?=</div>)";
        ArrayList<String> todos = new ArrayList<String>();
        Pattern p = Pattern.compile(regexTODO);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String todo = m.group(2);
            //todo = todo.replaceAll("</span>", "");
            todo = todo.replaceAll("&nbsp;", " ");

            todos.add(todo); // transform here not
        }
        return todos;
    }

    /**
     * get text from ENML
     * @param
     * @return
     */
    public static String getRawText(Note note) throws Exception {
        SyncHandler.get().loadRessources(note); // TODO: is this needed? When ressources are stored locally... Then not any more...
        String html = ENMLProcessor.get().noteToInlineHTMLString(note);
        //final String html = Util.inHtmlBody("TEST", "TelefonListViewController:231 JUST FOR THE TEST... removed ENMLProcessor.get().noteToInlineHTMLString(n)");
        String plainText = HtmlToPlainText.convert(html);
        return plainText;
    }

    /**
     * Finds all "ANGEBOT BEL: 22.5.2017 150.000 EUR 80%" an returns the values
     * @param note
     * @return list of todo texts
     */
    public static ArrayList<Pair<Note, Angebot>> findAllHot(Note note) {
        String regexAngebot = ">\\s*((ANGEBOT:|HOT:|BESUCH:)[^<]*)";
        ArrayList<String> angebote = new ArrayList<String>();
        Pattern p = Pattern.compile(regexAngebot);
        Matcher m = p.matcher(note.getContent());
        while (m.find()) {
            String angebot = m.group(1);
            //todo = todo.replaceAll("</span>", "");
            angebot = angebot.replaceAll("&nbsp;", " ");
            angebote.add(angebot); // transform here not
            //System.out.println("ANGEBOT: " + angebot);
        }

        ArrayList<Pair<Note, Angebot>> result = new ArrayList<>();
        for(String aStr: angebote) {
            Angebot a = extractAngebot(aStr);
            result.add(new Pair<>(note, a));
        }
        return result;
    }

    /**
     * extracts all fields from "ANGEBOT: BEL: 22.5.2017 150.000 EUR 80%" an returns the Angebot
     */
    private static Angebot extractAngebot(String aStr) {
        // if there is no % value: Default is 50%
        String shortName = getShortName(aStr);
        long eur = getEUR(aStr);
        long percent = getPercent(aStr);
        LocalDate d = getDate(aStr);
        Angebot a = new Angebot(d, eur, aStr.trim(), shortName, percent);
        return a;
    }

    private static LocalDate getDate(String aStr) {
        Calendar c = new DateFinder(aStr).get();
        if(c==null) return null;
        Instant instant = Instant.ofEpochMilli(c.getTimeInMillis());
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return date;
    }

    private static long getPercent(String aStr) {
        String p = getFirst(aStr, "[\\d]{1,3}%");
        if(p==null) return -1;
        p = p.replace("%", "");
        return Long.parseLong(p);
    }

    private static long getEUR(String aStr) {
        String eur = getFirst(aStr, "EUR[\\s]*([\\d.,]*[.,]?[\\d]{3})", 1);
        if(eur==null) return -1;
        eur = eur.replace(".", "");
        eur = eur.replace(",", "");
        return Long.parseLong(eur);
    }

    private static String getFirst(String aStr, String regex, int group) {
        List<String> withRegex = RegexUtils.findWithRegex(aStr, regex, group);
        return withRegex.size() == 0 ? null : withRegex.get(0);
    }

    public static String getFirst(String aStr, String regex) {
        return getFirst(aStr, regex, 0);
    }

    private static String getShortName(String aStr) {
        aStr = aStr.replace("ANGEBOT:", "");
        aStr = aStr.replace("HOT:", "");
        return getFirst(aStr, "[A-Z]{3}:");
    }

    public static String findPersonDataLink(String todoLine) {
        //List<String> withRegex = RegexUtils.findWithRegex(todoLine, "\\(K:\\s*[^\\)]({3,50})\\)", 1);
        List<String> withRegex = RegexUtils.findWithRegex(todoLine, "\\(P:\\s*([^\\)]{3,50})\\)", 1);
        if(withRegex.size() == 0) return null;
        return withRegex.get(0);
    }


    /**
     * central function - eats ENML and delivers structured value.
     * TODO: Create an enmlEditor class that:
     * - reads the enml and provides it as structured value
     * - remember a "from - to" String-range for every deliverd junk of value.
     * - is able to insert new value at very different spots into the ENML Dokument
     * - while inserting, editing or deleting, keeps track of the changes
     * of the "from - to" ranges of the remaining blocks, so that the ENML Dokument
     * can be changed without having to reconstruct all "cumbersome areas".
     *
     */
    public static CreamFirmaData extractFirmaPersonFromContent(Note note) {
        String content = note.getContent();
        String c = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
                "\n" +
                "<en-note><div>Hier sammeln wir Ideen für Verbesserungen.</div><div>Jede Idee bekommt ne Nummer.</div><div><br/></div><div>1. wir könnten feste Tags einführen, um auch strukturierte Infos abzulegen (Firma, Person, Kennzeichen, Todos, Historie) und damit&nbsp;</div><div>a) Auswertbar</div><div>b) in Excel exportierbar</div><div>zu bekommen.</div><div>Dazu könnte man auf die Idee kommen, einen Stapel zu machen, der CRM heißt. Dort gibt es Notebooks:</div><div>- Konfiguration</div><div>- Personen (Visitenkarten, Personen-Daten)</div><div>- Firmen</div><div>- Aktivitäten (TODO, Kennzeichen, Historie, Diskussion)</div><div><br/></div><div><br/></div><div>Alternativ wäre eine Notiz eine Firma mitsamt Adressen und allen Aktivitäten an den Adressen.&nbsp;</div><div><br/></div><div>------------------------------------------------------</div><div><br/></div><div><en-todo checked=\"false\"/>AZA: 1.5.2016 WVL Termin mit BEL machen</div><div>15.9 BEL mit x gesprochen. Problem: Budget ab Mai 16 wieder sprechen.&nbsp;</div><div><en-todo checked=\"true\"/>&nbsp;BEL: Sep:&nbsp; Chips anbieten - äh,&nbsp;</div><div>16.7 BEL Email: Betreff, LINK TO Note mit Email</div><div><en-todo checked=\"true\"/>NIT:&nbsp;F &amp; abc ? # ^</div><div><br/>" +
                "</div><div>HISTORIE+TODO+WVL: (Ab hier aufwärts)</div><div><br/></div><div><br/></div><div><br/></div><div>***FIRMA***</div><div>Name: Drecksla GmbH &amp; Co KG&nbsp;</div><div>Straße: Münchner und Berliner Str. 34-32</div><div>PLZ: 80199</div><div>Ort: Augsburg</div><div>Domain: www.drecksla.de</div><div>AnzMA: 200</div><div>Umsatz: 89&nbsp;</div><div>Tags: MASCH,&nbsp;</div><div>+++FIRMA+++</div><div><br/></div><div>***PERSON***</div><div>Titel: Dr.&nbsp;</div><div>VName: Hugo</div><div>NName:&nbsp;Hülsensack</div><div>Funkt: Abteilungsleiter für Aufwändiges</div><div>Abt: AFA</div><div>Mobil:&nbsp;</div><div>Festnetz:</div><div>Andere:</div><div>Mail: h.huelsensack@drecksla.de</div><div>Tags: SCRUM_9_16_ENERCON,</div><div>+++PERSON+++</div><div><br/></div><div>*** PERSON***</div><div>Titel:</div><div>VName: Max</div><div>NName:&nbsp;Mastermann</div><div>Funkt: Abteilungsleiter für Dreckiges</div><div>Abt: AFD</div><div>Mobil:&nbsp;</div><div>Festnetz:</div><div>Andere:</div><div>Mail: <a href=\"mailto:m.mastermann@drecksla.de\">m.mastermann@drecksla.de</a></div><div>Tags: LF, 7P, LF_2015, WRONG_T,&nbsp;</div><div>+++ PERSON+++</div><div><br/></div><div>ABO: BEL, NIT, AZA</div><div><br/></div><div>--------------------------</div><div><br/></div><div><br/></div><div><br/></div><div><br/></div><div><br/></div><div>2. NamNum: Hugo Hülsensack +49 (171) 3485737</div><div>Um in die Übersicht mehrere Namen + Tel zu bekommen</div><div><br/></div><div>3. ne TODO-Liste für alle TODOs, die mir einem Kürzel beginnen:&nbsp;</div><div>NIT: oder NIT@BEL:</div><div><br/></div><div>4. Historien-emails</div><div>Alles, was&nbsp;</div><div>a) an</div><div>b) cc</div><div>c) in der ersten Zeile der Mail</div><div>eine Domain haben, werden als Notiz mit Link bei der Person (Email) abgelegt.</div><div>Wenn es noch keinen Eintrag gibt, wird einer erzeugt.&nbsp;</div><div><br/></div><div>5. ABO</div><div>Liste von Emails (oder Handynummern), wer über Änderungen dieser Notitz informiert werden will (auch die Yammer Group)</div><div><br/></div><div>6. DISKUSSION</div><div>zugehörige Diskussion auf Yammer (könnte da ein Link sein? Geht das?)</div><div><br/></div><div>7. für alle Emails, die wir bekommen (außer gmx, gmail, web, Yahoo, t-online) wir ein Rumpf-Eintrag im CRM erzeugt.</div><div><br/></div><div>8. die Termine in den offenen Todos werden ebenfalls benutzt: der erste wird zur Dringlichkeit genutzt.</div><div><br/></div><div>9. &nbsp;Tags müssen automatisiert und satzweise setzbar sein</div><div><br/></div><div>10. PIPELINE: Sichtbar? Angebotssumme? Wäre einfach.&nbsp;</div><div><br/></div><div>11. Tags können einfach so getippt werden - ab dann stehen sie zur Wahl. In Konfig gibts Tag: Erklärung. Im Tool: Tooltip</div><div><br/></div><div>12. leere Felder auf Knopfdruck ausblenden</div><div><br/></div><div>13. Export Excel nach Suche</div><div>14 InportExcel</div><div><br/></div><div>14 Impotr Bitrix</div><div><br/></div><div>16 Wiki -Funktion Frei Editieren</div><div><br/></div><div>17 Handy: Tel, Info</div><div><br/></div><div>19: verschiedene Notizbücher (AZA, BEL, ...) oder Steuerung über Tags, die auch Filter heißen könnten</div><div><br/></div><div>20: Volltextsuche mit regex</div><div><br/></div><div>21: rohdaten Text / Struktur in MEMORY</div><div><br/></div><div><br/></div><div><br/></div><div>PRODUCT VISION</div><div>- Email-Signatures, vCards, ... Erzeugen CRM-Daten</div><div>- Historie in einem Tool</div><div>- Überblick-Ansichten</div><div>- Handy</div><div>- Social (whatsApp)</div></en-note>";
        CreamFirmaData fpd = new CreamFirmaData();
        List<String> firma = findFirma(content);
        if(firma.size() == 0) { // noch keine...
             // alle Felder auf null lassen
        } else if(firma.size()>1) { // mehr als eine... Was tun?
            // erste Firma nehmen, andere ignorieren.
            String firmaString = firma.get(0);
            fillFirmaFromStr(fpd, firmaString);
            System.out.println("Warning: there is more than one Firma in the Note");
        } else { // Genau eine. Perfekt
            //
            String firmaString = firma.get(0);
            fillFirmaFromStr(fpd, firmaString);
        }
        List<String> personen = findPerson(content);
        //System.out.println("found " + personen.size() + " personen");
        for(String pString: personen) {
            CreamPersonData p = new CreamPersonData();
            fpd.persons.add(p);
            fillPersonFromStr(p, pString);
        }
        fpd.setNote(note);
        return fpd;

    }


    /**
     * put infos from ENML String to Person
     * @param p value structure to be filled
     * @param personString
     */
    private static void fillPersonFromStr(CreamPersonData p, String personString) {
        Map<String, CreamAttributeDescription> map = AbstractConfiguration.getConfig().getPersonAttributesDescription();
        for (Map.Entry<String, CreamAttributeDescription> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + "/" + entry.getValue());
            String value = findAttribute(personString, entry.getValue().attribName);
            CreamAttributeData creamAttributeData = new CreamAttributeData();
            creamAttributeData.description = entry.getValue();
            creamAttributeData.value =value;
            p.setAttr(creamAttributeData);
        }
/*
        p.titel = findAttribute(personString, "Titel:");
        p.vName = findAttribute(personString, "VName:");
        p.nName = findAttribute(personString, "NName:");
        p.funktion = findAttribute(personString, "Funktion:");
        p.abteilung = findAttribute(personString, "Abteilung:");
        p.festnetz = findAttribute(personString, "Festnetz:");
        p.mobil = findAttribute(personString, "Mobil:");
        p.mail = findAttribute(personString, "Mail:");
        p.tags = findAttribute(personString, "Tags:");
*/
    }

    /**
     * put infos from ENML String to FirmaPersonData
     * @param f value structure to be filled
     * @param firmaString
     */
    private static void fillFirmaFromStr(CreamFirmaData f, String firmaString) {
        Map<String, CreamAttributeDescription> map = AbstractConfiguration.getConfig().getFirmaAttributesDescription();

        for (Map.Entry<String, CreamAttributeDescription> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + "/" + entry.getValue());
            String value = findAttribute(firmaString, entry.getValue().attribName);
            CreamAttributeData creamAttributeData = new CreamAttributeData();
            creamAttributeData.description = entry.getValue();
            creamAttributeData.value =value;

            f.setAttr(creamAttributeData);
        }

        /*
        fpd.name = findAttribute(firmaString, "Name:");
        fpd.strasse = findAttribute(firmaString, "Strasse:");
        fpd.plz = findAttribute(firmaString, "PLZ:");
        fpd.ort = findAttribute(firmaString, "Ort:");
        fpd.domain = findAttribute(firmaString, "Domain:");
        */
    }

    /**
     * find the blocks between ***FIRMA*** und +++FIRMA+++
     * @param content
     * @return attributes of person as ENML String
     */
    public static List<String> findFirma(String content) {
        return RegexUtils.findWithRegex(content, "(\\*\\*\\*FIRMA\\*\\*\\*)((.|\\r\\n)*?)(\\+\\+\\+FIRMA\\+\\+\\+)",0);
    }

    /**
     * find the Block between ***PERSON*** und +++PERSON+++
     * @param content
     * @return attributes of person as ENML string - a list of those strings
     */
    public static List<String> findPerson(String content) {
        return RegexUtils.findWithRegex(content, "(\\*\\*\\*PERSON\\*\\*\\*)((.|\\r\\n)*?)(\\+\\+\\+PERSON\\+\\+\\+)", 0);
    }


    /**
     *
     * @param content
     * @return data, that describes the CREAM Notebook structure in evernote
     * @throws Exception
     */
    public static ENCREAMNotebooks findCreamNotebooks(String content) throws Exception {
        List<String> block = RegexUtils.findWithRegex(content, "(\\*\\*\\*NOTEBOOKS\\*\\*\\*)(.|\\s|\\r|\\n)*?(\\+\\+\\+NOTEBOOKS\\+\\+\\+)", 0);
        if (block.size() == 1) {
            String blockNotebooks = block.get(0);
            List<String> notebooks = RegexUtils.findWithRegex(blockNotebooks, "(?<=<div>)[^;]*;[^;]*;", 0);
            ENCREAMNotebooks data  = new ENCREAMNotebooks();
            //data.groupNotebooks = new ArrayList<>(); // just in case, is is called a second time...
            for(String n : notebooks) {
                String[] split = n.split(";");

                if(split.length == 2) { // admin rights at the end, or read-restriction
                    String notebook = split[0].trim();
                    String function = split[1].trim();
                    if(function.equals("mails")) {
                        if(data.getMailsNotebook() != null) {
                            throw new RuntimeException("two mails folders specified in configuration. please specify only one.");
                        } else {
                            data.setMailsNotebook(notebook);
                        }
                    } else
                    if(function.equals("inbox")) {
                        if(data.getInboxNotebook() != null) {
                            throw new RuntimeException("two mails folders specified in configuration. please specify only one.");
                        } else {
                            data.setInboxNotebook(notebook);
                        }
                    } else
                    if(function.equals("overview")) {
                        if(data.getOverviewNotebook() != null) {
                            throw new RuntimeException("two overview folders specified in configuration. please specify only one.");
                        } else {
                            data.setOverviewNotebook(notebook);
                        }
                    } else
                    if(function.equals("default")) {
                        if(data.getDefaultNotebook() != null) {
                            throw new RuntimeException("two default folders specified in configuration. please specify only one.");
                        } else {
                            data.setDefaultNotebook(notebook);
                        }
                    } else
                    if(function.equals("group")) {
                            data.getGroupNotebooks().add(notebook);
                    } else {
                        throw new RuntimeException("function not defined in notebook config entry: " + n);

                    }
                } else {
                    throw new Exception("Wrong format in NOTEBOOKS configuration: " + n);
                }
            }

            if(data.getDefaultNotebook() == null) {
                throw new RuntimeException("no default folder specified in configuration. please specify one.");
            }
            if(data.getMailsNotebook() == null) {
                throw new RuntimeException("no mails folder specified in configuration. please specify one.");
            }
            if(data.getOverviewNotebook() == null) {
                throw new RuntimeException("no overview folder specified in configuration. please specify one.");
            }
            if(data.getInboxNotebook() == null) {
                throw new RuntimeException("no inbox folder specified in configuration. please specify one.");
            }

            return data;
        }
        //System.out.println("parsing error: attribute: " + attribute + " in content: "+ content );
        throw new Exception("Kein BLOCK ***NOTEBOOKS*** in Konfiguration gefunden!");
    }

    /**
     * find the Block between ***PERSON*** und +++PERSON+++ and extract user value
     * @param content
     * @return attributes of person as ENML string - a list of those strings
     */
    public static List<CreamUserData> findUsers(String content) throws Exception {
        //log.info("finding users in content: " + content);
        List<String> usersBlocks = null;
        try {
            //usersBlocks = RegexUtils.findWithRegex(content, "(\\*\\*\\*USER\\*\\*\\*)(.|\\s|\\r|\\n)*?(\\+\\+\\+USER\\+\\+\\+)", 0);
            usersBlocks = RegexUtils.findWithRegex(content, "(\\*\\*\\*USER\\*\\*\\*).*(\\+\\+\\+USER\\+\\+\\+)", 0);
        } catch (Exception e) {
            log.error("ERROR reading user block ***USER***  +++USER+++ ");
            log.catching(e);
            throw new Exception("ERROR finding the user block in content");
        }
        if (usersBlocks.size() == 1) {
            //log.info("userBlock in content: " + usersBlocks.get(0));
            String usersBlock = usersBlocks.get(0);
            List<String> users = RegexUtils.findWithRegex(usersBlock, "[A-Z]{3};[^;]*;[^;]*;[^;]*;[^;]*", 0);
            List<CreamUserData> creamUserList = new ArrayList<CreamUserData>();
            log.info("Reading users...");
            for(String u : users) {
                String[] split = u.split(";");
                CreamUserData creamUser = null;

                if(split.length != 5 ) {
                    throw new Exception("Format of user wrong (REGEX   [A-Z]{3};[^;]*;[^;]*;[^;]*;[^;]*  ) = 'BEL; loeffler@gmx.de; Benno Loeffler; write; diffMail;'\nBUT IS:  " + u );
                }
                // clean the html-ed email
                //log.info("user mail BEFORE clean: {}", split[1]);
                split[1] = RegexUtils.findEmailAdress(split[1]).get(0);
                //log.info("user mail AFTER clean: {}", split[1]);

                if (split[3].trim().equals("admin") || split[3].trim().equals("write")) {
                    creamUser = new CreamUserData(split[0], split[1], split[2], split[3], split[4]);
                } else {
                    throw new Exception("rights in user config are wrong. Either 'write' or 'admin'.\nBUT IS:   " + u);
                }
                log.debug(creamUser);

                creamUserList.add(creamUser);
            }
            return creamUserList;
        }
        //System.out.println("parsing error: attribute: " + attribute + " in content: "+ content );
        throw new Exception("Keine User in ***Konfiguration*** gefunden!");
    }

    /**
     * An attribute is in the text starting with "attribute: value" in a bla<b>attribue: value</b> bla bla text
     * @param content
     * @param attribute
     * @return just the value of the attribute
     */
    public static String findAttribute(String content, String attribute) {
        //List<String> attrib = findWithRegex(content, attribute+": ?([^<]*)",1);
        List<String> attrib = RegexUtils.findWithRegex(content, attribute+": ?(.*?)(?=</div>)",1); //TODO: when we read html hrefs, then "beautify them"

        if (attrib.size() == 1) {
            return attrib.get(0);
        } else if(attrib.size()>1) {
            return "Fehler beim Lesen des Feldes: " + attribute;
        }
        return ""; //"Feld '"+attribute + "' fehlt!";
    }

/*
    static ENMLProcessor enmlProcessor = ENMLProcessor.get();

    public static String enmlToHTML(Note note) throws Exception {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        enmlProcessor.noteToInlineHTML(note, stream);

        String result = stream.toString();

        return result; // java.nio.charset.StandardCharsets.UTF_8
    }
    */

    public static Map<String, CreamAttributeDescription> findFirmaAttributesDescriptions(String content) throws Exception {
        List<String> attribBlocks = RegexUtils.findWithRegex(content, "(\\*\\*\\*FIRMA-ATTRIBUTE\\*\\*\\*)(.|\\s|\\r|\\n)*?(\\+\\+\\+FIRMA-ATTRIBUTE\\+\\+\\+)", 0);
        String errorType = "FIRMA";
        if (attribBlocks.size() == 1) {
            return getCreamAttributeDescriptions(attribBlocks, errorType);
        }
        //System.out.println("parsing error: attribute: " + attribute + " in content: "+ content );
        throw new Exception("Keine Attribute für FIRMA in ***Konfiguration*** gefunden!");
    }

    public static Map<String, CreamAttributeDescription> findPersonAttributesDescriptions(String content) throws Exception {
        List<String> attribBlocks = RegexUtils.findWithRegex(content, "(\\*\\*\\*PERSON-ATTRIBUTE\\*\\*\\*)(.|\\s|\\r|\\n)*?(\\+\\+\\+PERSON-ATTRIBUTE\\+\\+\\+)", 0);
        String errorType = "PERSON";
        if (attribBlocks.size() == 1) {
            return getCreamAttributeDescriptions(attribBlocks, errorType);
        }
        //System.out.println("parsing error: attribute: " + attribute + " in content: "+ content );
        throw new Exception("Keine Attribute für PERSON in ***Konfiguration*** gefunden!");
    }

    public static Map<String, CreamAttributeDescription> findFirmaTagsDescriptions(String content) throws Exception {
        List<String> tagsBlocks = RegexUtils.findWithRegex(content, "(\\*\\*\\*FIRMA-TAGS\\*\\*\\*)(.|\\s|\\r|\\n)*?(\\+\\+\\+FIRMA-TAGS\\+\\+\\+)", 0);
        String errorType = "FIRMA-TAGS";
        if (tagsBlocks.size() == 1) {
            return getCreamAttributeDescriptions(tagsBlocks, errorType);
        }
        //System.out.println("parsing error: attribute: " + attribute + " in content: "+ content );
        throw new Exception("Keine Tags für FIRMA in ***Konfiguration*** gefunden!");
    }

    public static Map<String, CreamAttributeDescription> findPersonTagsDescriptions(String content) throws Exception {
        List<String> tagsBlocks = RegexUtils.findWithRegex(content, "(\\*\\*\\*PERSON-TAGS\\*\\*\\*)(.|\\s|\\r|\\n)*?(\\+\\+\\+PERSON-TAGS\\+\\+\\+)", 0);
        String errorType = "PERSON-TAGS";
        if (tagsBlocks.size() == 1) {
            return getCreamAttributeDescriptions(tagsBlocks, errorType);
        }
        //System.out.println("parsing error: attribute: " + attribute + " in content: "+ content );
        throw new Exception("Keine Tags für PERSON-TAGS in ***Konfiguration*** gefunden!");
    }

    private static Map<String, CreamAttributeDescription>  getCreamAttributeDescriptions(List<String> attribBlocks, String errorType) throws Exception {
        String usersBlock = attribBlocks.get(0);
        List<String> attribs = RegexUtils.findWithRegex(usersBlock, "(?<=>)[^<]*(?=<)", 0);
        Map<String, CreamAttributeDescription>  creamAttribDescriptionMap = new HashMap<String, CreamAttributeDescription>();
        int ordered = 0;
        for(String attr : attribs) {
            if(!"".equals(attr)) { // ignore empty matches. they result from lookahead / lookbehind of the search pattern. Cant suppress them...
                // attr = transformENMLToNormal(attr);
                String[] split = attr.split(";");
                CreamAttributeDescription creamAttributeDescription;
                if (split.length == 2) { // admin rights at the end, or read-restriction
                    creamAttributeDescription = new CreamAttributeDescription(split[0], split[1], ordered);
                } else if (split.length == 1) { //default rights: write
                    creamAttributeDescription = new CreamAttributeDescription(split[0], ordered);
                } else {
                    throw new Exception("Wrong format in " + errorType + " Attribute configuration: " + attribs + "\nESPECIALLY: " + attr);
                }
                creamAttribDescriptionMap.put(creamAttributeDescription.attribName, creamAttributeDescription);
                ordered++;
            }
        }
        return creamAttribDescriptionMap;
    }


    public static String getHistoryAndTodos(@NonNull Note note) {
        int idxStart = getStartOfHistory(note);
        int idxEnd = getStartOfDataBlock(note);
        String result = note.getContent().substring(idxStart, idxEnd-idxStart);
        System.out.println("CHECK +-1");
        return result;
    }

    /**
     * returns e.g. List of BEL NIT ...
     * @param note
     * @return
     */
    public static List<String> getAbos(@NonNull Note note) {
        List<String> abos = new ArrayList<>();
        List<String> found = RegexUtils.findWithRegex(note.getContent(), "\\*\\*\\*ABO\\*\\*\\*.*\\+\\+\\+ABO\\+\\+\\+", 0);
        if(found.size()>0) {
            abos = RegexUtils.findWithRegex(found.get(0), "[A-ZÖÜÄ]{3}:", 0);
            abos = abos.stream().map(s->s.replace(":", "")).collect(Collectors.toList());
        }
        return abos;
    }

    /**
     * write a list of BEL NIT ... to    ***ABO*** BEL: NIT: +++ABO+++    at the very end of the note
     * @param abos
     * @param note
     */
    public static void writeAbos(@NonNull List<String> abos, @NonNull Note note) {

        String aboString = "***ABO*** ";
        for(String abo:abos) {
            aboString+=abo+": ";
        }
        aboString+="+++ABO+++";

        String newContent;
        if(!hasAbos(note)) {
            String[] split = note.getContent().split("</en-note>"); // find everything before the END-Mark
            String first = split[0];
            String last = "</en-note>";
            newContent = first + aboString + last;
        } else {
            newContent = note.getContent().replaceFirst("\\*\\*\\*ABO\\*\\*\\*.*\\+\\+\\+ABO\\+\\+\\+", aboString);
        }

        note.setContent(newContent);
    }


    public static void addAbo(@NonNull String abo, @NonNull Note currentNote) {
        List<String> abos = new ArrayList<>();
        if(hasAbos(currentNote)) {
            abos = getAbos(currentNote);
        }
        if(!abos.contains(abo)) {
            abos.add(abo);
        }
        writeAbos(abos, currentNote);
    }

    public static void removeAbo(@NonNull String abo, @NonNull Note currentNote) {
        List<String> abos = new ArrayList<>();
        if(hasAbos(currentNote)) {
            abos = getAbos(currentNote);
        }
        abos.remove(abo);
        writeAbos(abos, currentNote);

    }


    public static boolean hasAbos(Note note) {
        List<String> found = RegexUtils.findWithRegex(note.getContent(), "\\*\\*\\*ABO\\*\\*\\*", 0);
        if(found.size() == 1) {
            return true;
        } else if (found.size() > 1) {
            log.error("Found more than one ***ABO*** in note: " + note.getTitle());
        }
        return false;
    }

    public static void writeDataToNote(CreamFirmaData creamFirmaData, Note currentNote) throws Exception {

        if(! ENHelper.hasDataBlock(currentNote.getContent())) {
            String contentWithDataBlock = ENHelper.addDataBlockToNoteContent(currentNote.getContent());
            currentNote.setContent(contentWithDataBlock);
        }

        int start = getStartOfDataBlock(currentNote);
        int end = getEndOfDataBlock(currentNote);

        // cut the data-Block out of the note.
        String content = currentNote.getContent();
        String anfangContent = content.substring(0,start);
        String endeContent = content.substring(end,content.length());
        String newDataBlockString = convertDataToENMLBlock(creamFirmaData);


        String contentToWrite = anfangContent + newDataBlockString + endeContent;
        currentNote.setContent(contentToWrite);
        //System.out.println("CONTENT: " + contentToWrite);
        // put the created data block into the content
        // unset all other fields in oder to be able to update only the conent
        // update the content
        String toWrite = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
                "\n" +
                "<en-note><div><br/></div><div><en-todo checked=\"false\"/>AZA: 1.5.2016 WVL Termin &amp; mit BEL machen</div><div>15.9 BEL mit x gesprochen. Problem: Budget ab Mai 16 wieder sprechen.&nbsp;</div><div><en-todo checked=\"true\"/>&nbsp;BEL: Sep:&nbsp; Chips anbieten - äh,&nbsp;</div><div>16.7 BEL Email: Betreff, LINK TO Note mit Email&nbsp; <a href=\"evernote:///view/28032648/s226/beb4e6d4-7068-4ba2-a24d-51f7cab5c437/beb4e6d4-7068-4ba2-a24d-51f7cab5c437/\" style=\"color:#69aa35\">WG: Unsere gemeinsame Veranstaltung am 24. Februar: SCRUM</a></div><div><en-todo checked=\"true\"/>NIT:&nbsp;F &amp; abc ? # ^</div><div><br/></div><div>HISTORIE+TODO+WVL: (Ab hier aufwärts)</div><div><br/></div><div><br/></div><div><br/></div><div>***FIRMA***</div><div>Name: Drecksla GmbH & Co KG</div><div>Straße: Münchner und Berliner Str. 34-32</div><div>Postfach: Feld 'Postfach' fehlt!</div><div>PLZ: 80199</div><div>Ort: Augsburg</div><div>Land: Feld 'Land' fehlt!</div><div>Domain: www.drecksla.de</div><div>Mitarbeiter: 200</div><div>Umsatz: 89</div><div>Tel. Zentrale: Feld 'Tel. Zentrale' fehlt!</div><div>Notizen: Feld 'Notizen' fehlt!</div><div>Tags: MASCH, WUNSCH_K, 4hWS</div><div>+++FIRMA+++</div><div></br></div>***PERSON***</div><div>Titel: Feld 'Titel' fehlt!</div><div>Vorname: Hugo</div><div>Nachname: Hülsensack</div><div>Funktion: Abteilungsleiter für Aufwändiges</div><div>Abteilung: AFA</div><div>Sekretariat: Feld 'Sekretariat' fehlt!</div><div>Mobil: 0171 62 35 379</div><div>Festnetz: 089 12345656787</div><div>Email: h.huelsensack@drecksla.de</div><div>bekannt mit: TIM, NIT</div><div>Notizen: Feld 'Notizen' fehlt!</div><div>Tags: LF </div><div>+++PERSON+++</div><div></br></div>***PERSON***</div><div>Titel: </div><div>Vorname: Max</div><div>Nachname: Mastermann</div><div>Funktion: Abteilungsleiter für Dreckiges</div><div>Abteilung: AFD</div><div>Sekretariat: Herrn Hülsensack anrufen. Er ist Assi von Masterman.</div><div>Mobil: +47 171 7364664</div><div>Festnetz: (02334/756-234)</div><div>Email: m.mastermann@drecksla.de</div><div>bekannt mit: BEL, AZA</div><div>Notizen: Er hat zwei Kinder. Liest viel zu new Work. Auch von Lars.</div><div>Tags: LF BU_DWZ </div><div>+++PERSON+++</div><div></br></div>***PERSON***</div><div>Titel: </div><div>Vorname: Maxim</div><div>Nachname: Mast</div><div>Funktion: Mädchen für alles</div><div>Abteilung: </div><div>Sekretariat: </div><div>Mobil: +47 171 736466489</div><div>Festnetz: (02334/756-234a)</div><div>Email: keine</div><div>bekannt mit: BEL, AZA</div><div>Notizen: Liest viel</div><div>Tags: LF BU_DWZ BU_7P LF_2016</div><div>+++PERSON+++</div></br><div>***END-DATA+++</div><div></div><div><br/></div><div>ABO: BEL, NIT, AZA</div><div><br/></div><div>--------------------------</div></en-note>";
        String orig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
                "\n" +
                "<en-note><div><br/></div><div><en-todo checked=\"false\"/>AZA: 1.5.2016 WVL Termin &amp; mit BEL machen</div><div>15.9 BEL mit x gesprochen. Problem: Budget ab Mai 16 wieder sprechen.&nbsp;</div><div><en-todo checked=\"true\"/>&nbsp;BEL: Sep:&nbsp; Chips anbieten - äh,&nbsp;</div><div>16.7 BEL Email: Betreff, LINK TO Note mit Email&nbsp; <a href=\"evernote:///view/28032648/s226/beb4e6d4-7068-4ba2-a24d-51f7cab5c437/beb4e6d4-7068-4ba2-a24d-51f7cab5c437/\" style=\"color:#69aa35\">WG: Unsere gemeinsame Veranstaltung am 24. Februar: SCRUM</a></div><div><en-todo checked=\"true\"/>NIT:&nbsp;F &amp; abc ? # ^</div><div><br/></div><div>HISTORIE+TODO+WVL: (Ab hier aufwärts)</div><div><br/></div><div><br/></div><div><br/></div><div>***FIRMA***</div><div>Name: Drecksla GmbH &amp; Co KG</div><div>Straße: Münchner und Berliner Str. 34-32</div><div>PLZ: 80199</div><div>Ort: Augsburg</div><div>Domain: www.drecksla.de</div><div>Mitarbeiter: 200</div><div>Umsatz: 89</div><div>Tags: MASCH, WUNSCH_K, 4hWS</div><div>+++FIRMA+++</div><div><br/></div><div>Titel: Dr.</div><div>***PERSON***</div><div>Vorname: Hugo</div><div>Nachname:&nbsp;Hülsensack</div><div>Funktion: Abteilungsleiter für Aufwändiges</div><div>Abteilung: AFA</div><div>Mobil: 0171 62 35 379</div><div>Festnetz: 089 12345656787</div><div>Andere:</div><div>bekannt mit: TIM, NIT</div><div>Email: h.huelsensack@drecksla.de</div><div>Tags: SCRUM_9_16_ENERCON,</div><div>+++PERSON+++</div><div><br/></div><div>***PERSON***</div><div>Titel:</div><div>Vorname: Max</div><div>Nachname:&nbsp;Mastermann</div><div>Funktion: Abteilungsleiter für Dreckiges</div><div>Abteilung: AFD</div><div>Sekretariat: Herrn Hülsensack anrufen. Er ist Assi von Masterman.</div><div>Mobil: +47 171 7364664</div><div>Festnetz: (02334/756-234)</div><div>Andere:</div><div>Email: m.mastermann@drecksla.de</div><div>bekannt mit: BEL, AZA</div><div>Notizen: Er hat zwei Kinder. Liest viel zu new Work. Auch von Lars.</div><div>Tags: LF, 7P, LF_2015, WRONG_T,</div><div>+++PERSON+++</div><div><br/></div><div>***PERSON***</div><div>Titel:</div><div>Vorname: Maxim</div><div>Nachname:&nbsp;Mast</div><div>Funktion: Mädchen für alles</div><div>Abteilung:&nbsp;</div><div>Sekretariat:&nbsp;</div><div>Mobil: +47 171 736466489</div><div>Festnetz: (02334/756-234a)</div><div>Andere:</div><div>Email: keine</div><div>bekannt mit: BEL, AZA</div><div>Notizen: Liest viel</div><div>Tags:&nbsp;LF&nbsp;BU_DWZ&nbsp;BU_7P&nbsp;LF_2016</div><div>+++PERSON+++</div><div><br/></div><div>***END-DATA+++</div><div><br/></div><div>ABO: BEL, NIT, AZA</div><div><br/></div><div>--------------------------</div></en-note>";
        //enSharedNotebook.updateNote(currentNote);
    }

    public static String convertDataToENMLBlock(CreamFirmaData creamFirmaData) {
        // create the new data block with <div> and </div> // that should be trivial
        StringBuffer newDataBlockString = new StringBuffer(2000);
        //                 "</div><div>HISTORIE+TODO+WVL: (Ab hier aufwärts)</div><div><br/></div><div><br/></div><div><br/></div><div>***FIRMA***</div><div>Name: Drecksla GmbH &amp; Co KG&nbsp;</div><div>Straße: Münchner und Berliner Str. 34-32</div><div>PLZ: 80199</div><div>Ort: Augsburg</div><div>Domain: www.drecksla.de</div><div>AnzMA: 200</div><div>Umsatz: 89&nbsp;</div><div>Tags: MASCH,&nbsp;</div><div>+++FIRMA+++</div><div><br/></div><div>***PERSON***</div><div>Titel: Dr.&nbsp;</div><div>VName: Hugo</div><div>NName:&nbsp;Hülsensack</div><div>Funkt: Abteilungsleiter für Aufwändiges</div><div>Abt: AFA</div><div>Mobil:&nbsp;</div><div>Festnetz:</div><div>Andere:</div><div>Mail: h.huelsensack@drecksla.de</div><div>Tags: SCRUM_9_16_ENERCON,</div><div>+++PERSON+++</div><div><br/></div><div>*** PERSON***</div><div>Titel:</div><div>VName: Max</div><div>NName:&nbsp;Mastermann</div><div>Funkt: Abteilungsleiter für Dreckiges</div><div>Abt: AFD</div><div>Mobil:&nbsp;</div><div>Festnetz:</div><div>Andere:</div><div>Mail: <a href=\"mailto:m.mastermann@drecksla.de\">m.mastermann@drecksla.de</a></div><div>Tags: LF, 7P, LF_2015, WRONG_T,&nbsp;</div><div>+++ PERSON+++</div><div><br/></div><div>ABO: BEL, NIT, AZA</div><div><br/></div><div>--------------------------</div><div><br/></div><div><br/></div><div><br/></div><div><br/></div><div><br/></div><div>2. NamNum: Hugo Hülsensack +49 (171) 3485737</div><div>Um in die Übersicht mehrere Namen + Tel zu bekommen</div><div><br/></div><div>3. ne TODO-Liste für alle TODOs, die mir einem Kürzel beginnen:&nbsp;</div><div>NIT: oder NIT@BEL:</div><div><br/></div><div>4. Historien-emails</div><div>Alles, was&nbsp;</div><div>a) an</div><div>b) cc</div><div>c) in der ersten Zeile der Mail</div><div>eine Domain haben, werden als Notiz mit Link bei der Person (Email) abgelegt.</div><div>Wenn es noch keinen Eintrag gibt, wird einer erzeugt.&nbsp;</div><div><br/></div><div>5. ABO</div><div>Liste von Emails (oder Handynummern), wer über Änderungen dieser Notitz informiert werden will (auch die Yammer Group)</div><div><br/></div><div>6. DISKUSSION</div><div>zugehörige Diskussion auf Yammer (könnte da ein Link sein? Geht das?)</div><div><br/></div><div>7. für alle Emails, die wir bekommen (außer gmx, gmail, web, Yahoo, t-online) wir ein Rumpf-Eintrag im CRM erzeugt.</div><div><br/></div><div>8. die Termine in den offenen Todos werden ebenfalls benutzt: der erste wird zur Dringlichkeit genutzt.</div><div><br/></div><div>9. &nbsp;Tags müssen automatisiert und satzweise setzbar sein</div><div><br/></div><div>10. PIPELINE: Sichtbar? Angebotssumme? Wäre einfach.&nbsp;</div><div><br/></div><div>11. Tags können einfach so getippt werden - ab dann stehen sie zur Wahl. In Konfig gibts Tag: Erklärung. Im Tool: Tooltip</div><div><br/></div><div>12. leere Felder auf Knopfdruck ausblenden</div><div><br/></div><div>13. Export Excel nach Suche</div><div>14 InportExcel</div><div><br/></div><div>14 Impotr Bitrix</div><div><br/></div><div>16 Wiki -Funktion Frei Editieren</div><div><br/></div><div>17 Handy: Tel, Info</div><div><br/></div><div>19: verschiedene Notizbücher (AZA, BEL, ...) oder Steuerung über Tags, die auch Filter heißen könnten</div><div><br/></div><div>20: Volltextsuche mit regex</div><div><br/></div><div>21: rohdaten Text / Struktur in MEMORY</div><div><br/></div><div><br/></div><div><br/></div><div>PRODUCT VISION</div><div>- Email-Signatures, vCards, ... Erzeugen CRM-Daten</div><div>- Historie in einem Tool</div><div>- Überblick-Ansichten</div><div>- Handy</div><div>- Social (whatsApp)</div></en-note>";

        newDataBlockString.append("***FIRMA***</div>");
        Map<Integer, CreamAttributeDescription> dataFirma = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription();
        for (int i = 0; i < dataFirma.size(); i++) {
            String value = creamFirmaData.getAttr(dataFirma.get(i).attribName).value;
            String name = dataFirma.get(i).attribName;
            // TODO append change data?
            newDataBlockString.append("<div>" + name + ": " + value + "</div>");
        }
        newDataBlockString.append("<div>+++FIRMA+++</div>");

        for (CreamPersonData p: creamFirmaData.persons) {
            newDataBlockString.append("<div><br/></div>");
            newDataBlockString.append("<div>***PERSON***</div>");
            Map<Integer, CreamAttributeDescription> dataPerson = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription();
            for (int i = 0; i < dataPerson.size(); i++) {
                String value = transformNormalToENML(p.getAttr(dataPerson.get(i).attribName).value); // TODO: html href is crippled
                String name = transformNormalToENML(dataPerson.get(i).attribName);
                // TODO append change data?
                newDataBlockString.append("<div>" + name + ": " + value + "</div>");
            }
            newDataBlockString.append("<div>+++PERSON+++</div>");
        }
        newDataBlockString.append("<div>***END-DATA+++</div><div>");
        return newDataBlockString.toString();
    }

    public static int getEndOfDataBlock(Note currentNote){
        Pattern pattern = Pattern.compile("\\*\\*\\*END-DATA\\+\\+\\+"); // greedy! read all persons!
        Matcher m = pattern.matcher(currentNote.getContent());
        int end = 0;
        if(m.find()) {
            String match = m.group(0);
            end = m.end(0);
        } else {
            //irgendwo hinten hin?
            throw new RuntimeException("Fehler: Ende Datenblock in Notiz nicht gefunden, Notiz " + currentNote.getTitle());
        }
        return end;
    }

    public static int getStartOfHistory(Note note) {
        int start;
        Pattern pattern = Pattern.compile("<en-note>");
        Matcher m = pattern.matcher(note.getContent());
        if(m.find()) {
            String match = m.group(0);
            start = m.end(0); // jump to the end of <en-note>
        } else {
            //irgendwo hinten hin?
            throw new RuntimeException("Fehler: Start Note <en-note> in Notiz nicht gefunden, Notiz " + note.getTitle());
        }
        return start;
    }

    public static int getEndOfNote(Note note) {
        int start;
        Pattern pattern = Pattern.compile("</en-note>");
        Matcher m = pattern.matcher(note.getContent());
        if(m.find()) {
            start = m.start(0);
        } else {
            throw new RuntimeException("Fehler: End Note </en-note> in Notiz nicht gefunden, Notiz " + note.getTitle());
        }
        return start;
    }


    public static int getStartOfDataBlock(Note currentNote){
        int start;
        Pattern pattern = Pattern.compile("\\*\\*\\*FIRMA\\*\\*\\*");
        Matcher m = pattern.matcher(currentNote.getContent());
        if(m.find()) {
            String match = m.group(0);
            start = m.start(0);
        } else {
            //irgendwo hinten hin?
            throw new RuntimeException("Fehler: Start Datenblock in Notiz nicht gefunden, Notiz " + currentNote.getTitle());
        }
        return start;
    }

    public static String createNoteFromEmailText(String text) {
        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                        + "<en-note>";
        content += text;
        content += "</en-note>";
        return content;
    }

    /**
     * Adds an empty data block to the content of a note.
     * If there is already a data block, nothing is done.
     * @param content content of a Evernote note
     * @return identical content, if there is already a data block. Additional data block, if there was no block yet.
     */
    public static String addDataBlockToNoteContent(String content) {
        StringBuffer newContent;
        if ( ! hasDataBlock(content)) {
            String[] split = content.split("</en-note>"); // find everything before the END-Mark
            newContent = new StringBuffer(split[0]);
            newContent.append("<div><br/><br/><br/></div>");
            newContent.append("<div>***FIRMA***</div>");
            Map<Integer, CreamAttributeDescription> dataFirma = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription();
            for (int i = 0; i < dataFirma.size(); i++) {
                String name = dataFirma.get(i).attribName;
                newContent.append("<div>" + name + ": " + "</div>");
            }
            newContent.append("<div>+++FIRMA+++</div>");
            newContent.append("<div><br/></div>");
            newContent.append("<div>***PERSON***</div>");
            Map<Integer, CreamAttributeDescription> dataPerson = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription();
            for (int i = 0; i < dataPerson.size(); i++) {
                String name = transformNormalToENML(dataPerson.get(i).attribName);
                // TODO append change data?
                newContent.append("<div>" + name + ": " + "</div>");
            }
            newContent.append("<div>+++PERSON+++</div>");
            newContent.append("<div>***END-DATA+++</div>");
            newContent.append("</en-note>");
            content = newContent.toString();

        }
        return content;
    }

    /**
     * Has the content a data block?
     * @param content Evernote content
     * @return
     */
    public static boolean hasDataBlock(@NonNull String content) {
        return content.indexOf("***END-DATA+++") != -1;
    }

    public static void addHistoryEntry(Note n, String entry) {
        addHistoryEntry(n,entry, false);
    }

    public static void addTodoEntry(Note n, String entry) {
        addHistoryEntry(n,entry, true);
    }

    /**
     * add an history entry at the very beginning of the note content
     * @param n the note where the entry should be added
     * @param entry the line to add at the beginning
     */
    public static void addHistoryEntry(Note n, String entry, boolean isTODO) {
        String content = n.getContent();
        String[] split = content.split("<en-note>");
        if(split.length == 2) {
            String todoStartStr="";
            String todoEndStr="";
            if(isTODO) {
                //Markup right?
                todoStartStr="<en-todo/>";
                //todoEndStr="</en-todo>";
            }

            content = split[0] + "<en-note><div><br/></div><div>" +todoStartStr + entry + todoEndStr+ "</div><div><br/></div>"+split[1];
        }
        n.setContent(content);
    }

    private static boolean startsWithBlankLine(String s) {
        return (s.startsWith("<br />") ||
                s.startsWith("<br/>") ||
                s.startsWith("<div><br /></div>") ||
                s.startsWith("<div><br/></div>") ||
                s.startsWith("<div><div><br /></div><div>") ||
                s.startsWith("<div><div><br/></div><div>"));
    }

    public static boolean addTwoNewlinesAtTop(Note n) {
        String content = n.getContent();
        String[] split = content.split("<en-note>");
        if(split.length == 2) {

            if( ! startsWithBlankLine(split[1])) {
                content = split[0] + "<en-note><br/><br/>" + split[1];
                n.setContent(content);

                System.out.println("-----------------");
                System.out.println(n.getTitle());
                System.out.println("<en-note> starts with:  " + split[1].substring(0, Math.min(20, split[1].length()-1)));
                System.out.println("--> ADDED 2 newlines");

                return true;
            }
        } else {
            log.warn("note could not be split and spaces added at beginning: " + n.getTitle());
        }
        return false;
    }

    /**
     * at the end, right before </en-note>
     * @param n
     * @param entry
     */
    public static void addAdressEntryAtEnd(Note n, String entry) {
        String content = n.getContent();
        String[] split = content.split("</en-note>");
        content = split[0] + "<div><br/></div><div>" + entry +  "</div><div><br/></div></en-note>";
        n.setContent(content);
    }

    public static String createValidEmptyContentWithEmptyDataBlock() {
        String content =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">"
                        + "<en-note>";
        content += "<div>***FIRMA***</div>";
        content += "<div>***END-DATA+++</div>";
        content += "</en-note>";
        return content;
    }

    public static String findFirstEmailAdress(String content) {
        val emails = RegexUtils.findEmailAdress(content);
        String result = "---";
        if(emails.size() > 0) {
            result = emails.get(0);
        }
        /*
        String result ="";
        boolean first = true;
        for(String mail: emails) {
            if(!first) {
                result += "; ";
                first = false;
            }
            result += mail;
        }*/
        return result;
    }
}
