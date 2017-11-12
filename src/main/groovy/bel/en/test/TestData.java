package bel.en.test;

import bel.en.data.AbstractConfiguration;
import bel.en.data.Configuration;
import bel.en.data.CreamAttributeDescription;
import bel.en.data.CreamUserData;
import bel.en.localstore.NoteStoreLocal;
import bel.learn._01_bean.Person;
import com.evernote.edam.type.Note;

import java.io.Serializable;
import java.util.List;

/**
 * TestData...
 */
public class TestData {
    public TestData() throws Exception {
        getTestConfig();
    }

    public String maxMusterMannENML = "<en-note><div><br/></div><div><en-todo checked=\"false\"/>AZA: 1.5.2016 WVL Termin &amp; mit BEL machen</div><div>15.9 BEL mit x gesprochen. Problem: Budget ab Mai 16 wieder sprechen.&nbsp;</div><div><en-todo checked=\"true\"/>&nbsp;BEL: Sep:&nbsp; Chips anbieten - äh,&nbsp;</div><div>16.7 BEL Email: Betreff, LINK TO Note mit Email&nbsp; <a href=\"evernote:///view/28032648/s226/beb4e6d4-7068-4ba2-a24d-51f7cab5c437/beb4e6d4-7068-4ba2-a24d-51f7cab5c437/\" style=\"color:#69aa35\">WG: Unsere gemeinsame Veranstaltung am 24. Februar: SCRUM</a></div><div><en-todo checked=\"true\"/>NIT:&nbsp;F &amp; abc ? # ^</div><div><br/></div><div>HISTORIE+TODO+WVL: (Ab hier aufwärts)</div><div><br/></div><div><br/></div><div><br/></div><div>***FIRMA***</div><div>Name: Drecksla GmbH Co KG</div><div>Straße: Münchner und Berliner Str. 34-32</div><div>Postfach: Feld &apos;Postfach&apos; fehlt!</div><div>PLZ: 80199</div><div>Ort: Augsburg</div><div>Land: Feld &apos;Land&apos; fehlt!</div><div>Domain: www.drecksla.de</div><div>Mitarbeiter: 200</div><div>Umsatz: 89</div><div>Tel. Zentrale: Feld &apos;Tel. Zentrale&apos; fehlt!</div><div>Notizen: Feld &apos;Notizen&apos; fehlt!</div><div>Tags: EX_KLIENT, MASCH, WUNSCH_K, 4hWS</div><div>+++FIRMA+++</div><div><br/></div><div>***PERSON***</div><div>Titel: &amp; + - * / \\ ´ ^ ° µ € [ ] { } 4 3 ³ ² % &amp; § &quot; ! | # ~ ö ä ü Ö Ä Ü ß ?</div><div>Vorname: Hugo</div><div>Nachname: Hülsensack</div><div>Funktion: Abteilungsleiter für Aufwändiges</div><div>Abteilung: AFA</div><div>Sekretariat: Feld &apos;Sekretariat&apos; fehlt!</div><div>Mobil: 0171 62 35 379</div><div>Festnetz: 089 12345656787</div><div>Email: h.huelsensack@drecksla.de</div><div>bekannt mit: TIM, NIT</div><div>Notizen: Feld &apos;Notizen&apos; fehlt!</div><div>Tags: LF</div><div>+++PERSON+++</div><div><br/></div><div>***PERSON***</div><div>Titel:</div><div>Vorname: Max</div><div>Nachname: Mastermann</div><div>Funktion: Abteilungsleiter für Dreckiges</div><div>Abteilung: AFD</div><div>Sekretariat: Herrn Hülsensack anrufen. Er ist Assi von Masterman.</div><div>Mobil: +47 171 7364664</div><div>Festnetz: (02334/756-234)</div><div>Email: m.mastermann@drecksla.de</div><div>bekannt mit: BEL, AZA</div><div>Notizen: Er hat zwei Kinder. Liest viel zu new Work. Auch von Lars.</div><div>Tags: LF BU_DWZ</div><div>+++PERSON+++</div><div><br/></div><div>***PERSON***</div><div>Titel:</div><div>Vorname: Maxim</div><div>Nachname: Mast</div><div>Funktion: Mädchen für alles</div><div>Abteilung:</div><div>Sekretariat:</div><div>Mobil: +47 171 736466489</div><div>Festnetz: (02334/756-234a)</div><div>Email: keine</div><div>bekannt mit: BEL, AZA</div><div>Notizen: Liest viel</div><div>Tags: LF BU_DWZ BU_7P</div><div>+++PERSON+++</div><div>***END-DATA+++</div><div><br/></div><div>ABO: BEL, NIT, AZA</div><div><br/></div><div>--------------------------</div><div>&lt;&gt;</div></en-note>";

    public static void main(String[] args) throws Exception {
        new TestData().createNewLocalTestDataFiles();
    }

    public static void populate(List<Person> persons) {
        persons.add(new Person(true, "Benno Löffler", 47, new int[]{1, 3, 2}, 10000.45  ));
        persons.add(new Person(false, "Sabine K.", 45, new int[]{1, 2, 1}, 200034.45  ));
        persons.add(new Person(true, "Benno K.", 16, new int[]{5, 2, 1}, 34.45  ));
        persons.add(new Person(true, "Paul K.", 14, new int[]{2, 2, 1}, 34.45  ));
        persons.add(new Person(true, "Leo K.", 10, new int[]{3, 2, 4}, 34.45  ));
    }

    public void createNewLocalTestDataFiles() {
        NoteStoreLocal noteStoreLocal = null;
        try {
            noteStoreLocal = new NoteStoreLocal(getTestConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
        noteStoreLocal.deleteAllLocalFiles();
        System.out.print("Going to create many notes...");
        for (int i = 0; i < 10000; i++) {
            if(i%500 == 0) {
                System.out.print(" " + i);
            }
            Note n = new Note();
            n.setGuid(Long.toString(System.currentTimeMillis()));
            n.setContent(maxMusterMannENML);
            n.setTitle(Integer.toString(i));
            noteStoreLocal.updateNote(n);
        }

        noteStoreLocal.invalidateCache();
        //noteStoreLocal.readAllFromDisk();
    }

    public Configuration getTestConfig() throws Exception {
        TestConfiguration c = new TestConfiguration();

        c.addFirmaAttrib("Name", "voller Name, zB Robert Bosch GmbH - Allerdings immer nur EIN STANDORT PRO NOTIZ");
        c.addFirmaAttrib("Straße","mit Hausnummer");
        c.addFirmaAttrib("PLZ","");
        c.addFirmaAttrib("Postfach","");
        c.addFirmaAttrib("Tags","");

        c.addFirmaTag("WU_KLIENT","Ist die Firma Wunschklient?");
        c.addFirmaTag("EX_KLIENT","");

        c.addPersonAttrib("Vorname",""); // TODO oder null?
        c.addPersonAttrib("Nachname","");
        c.addPersonAttrib("Mobil","");
        c.addPersonAttrib("Tags","");

        c.addPersonTag("LF","soll zum LagerFeuer eingeladen werden");
        c.addPersonTag("BU_DWZ","hat BUch Denkwerkzeuge bekommen");
        c.addPersonTag("BU_7P","hat Buch 7Prinzipien bekommen");
        c.addPersonTag("LF_2016","war auf LF 2016");

        c.addUser("BEL", "loeffler@v-und-s.de", "Benno Löffler", "admin", "diffMail");

        c.finishSetup("Benno Löffler");
        return c;
    }

    /**
     * DONT FORGET after all the adds and sets: CALL finishSetup!
     */
    public static class TestConfiguration extends AbstractConfiguration implements Serializable {

        static final long serialVersionUID = 1L;

        private static int firmaAttribOderNr = 0;
        private static int firmaTagOderNr = 0;
        private static int personAttribOderNr = 0;
        private static int personTagOderNr = 0;

        public void addUser(String shortName, String email, String longName, String rights, String diffMail) {
            users.add(new CreamUserData(shortName, email, longName, rights, diffMail));
        }

        public void addFirmaAttrib(String attribName, String description) {
            firmaAttributes.put(attribName, new CreamAttributeDescription(attribName, description, firmaAttribOderNr++));
        }

        public void addFirmaTag(String attribName, String description) {
            firmaTags.put(attribName, new CreamAttributeDescription(attribName, description, firmaTagOderNr++));
        }

        public void addPersonAttrib(String attribName, String description) {
            personAttributes.put(attribName, new CreamAttributeDescription(attribName, description, personAttribOderNr++));

        }

        public void addPersonTag(String attribName, String description) {
            personTags.put(attribName, new CreamAttributeDescription(attribName, description, personTagOderNr++));

        }

    }
}
