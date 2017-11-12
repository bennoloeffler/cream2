package bel.en.evernote;

import bel.en.data.AbstractConfiguration;
import com.evernote.edam.type.Note;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.List;

/**
 * Config of Users, attributes of Firma and Person, tags of Firma and Person
 * Example:
 * Hinter Attributen und Tags in den Daten kann in runden Klammern stehen, wer das wann eingetragen hat: (12.04.2016:BEL)

 ***USER*** (Kürzel; Email; voller Name - der muss der selbe wie der bei Evernote sein)
 BEL; loeffler@v-und-s.de; Benno Löffler;
 NIT; tietz@v-und-s.de; Nicole Tietz;
 AZA; avdagic@v-und-s.de; Azra Avdagic;
 ANL; anna@v-und-s.de; Anna Ljubomirska;
 MIK: kasteleiner@v-und-s.de; Michael Kasteleiner;
 +++USER+++

 ***FIRMA-ATTRIBUTE*** (Attribute-Name;
 Name; voller Name, zB Robert Bosch GmbH
 Straße:
 Postfach:
 PLZ:
 Ort:
 Land:
 Domain:
 Mitarbeiter:
 Umsatz:
 Tel. Zentrale:
 +++FIRMA-ATTRIBUTE+++

 ***FIRMA-TAGS***
 WU_KL; Ist die Firma Wunschklient
 +++FIRMA-TAGS+++

 ***PERSON-ATTRIBUTE***
 Titel:
 Vorname:
 Nachname:
 Funktion:
 Abteilung:
 Sekretariat:
 Mobile:
 Tel:
 Mail:
 +++PERSON-ATTRIBUTE+++

 ***PERSON-TAGS***
 LF; soll zum LagerFeuer eingeladen werden
 BU_DWZ; hat BUch Denkwerkzeuge bekommen
 BU_7P; hat Buch 7Prinzipien bekommen
 LF_2016; war auf LF 2016
 +++PERSON-TAGS+++

 */
@Log4j2
public class ENConfiguration extends AbstractConfiguration implements Serializable {

    static final long serialVersionUID = 1L;

    public static String CONFIG_TITLE_STRING ="CREAM-BASIC-CONFIG"; // VORSICHT: WIRD in StuctureNoteFormView überschrieben


    public ENConfiguration(ENSharedNotebook n, ENConnection con) throws Exception {

        log.trace("starting reading and parsing EN-CONFIG...");

        List<Note> notes = n.findNotes("intitle:["+CONFIG_TITLE_STRING+"]"); // TODO: use Meta...
        int size = notes.size();
        if(size == 0) {
            throw new Exception("Keine Konfiguration gefunden! (Note mit Namen ***Konfiguration***)");
        } else if (size > 1) {
            throw new Exception("Mehr als eine Konfiguration gefunden! (Note mit Namen ***Konfiguration***)");
        }

        Note config = notes.get(0);
        String content = n.getNoteContent(config);

        log.trace("going to parse user data");
        users = ENHelper.findUsers(content);

        log.trace("going to parse notebooks");
        creamNotebooks = ENHelper.findCreamNotebooks(content);

        // CHECK HERE if those notebooks exist. And to be able to store the guid--name mapping...
        ENSharedNotebookGroup enSharedNotebookGroup = new ENSharedNotebookGroup(con, creamNotebooks.getDefaultNotebook(), creamNotebooks.getGroupNotebooks());
        creamNotebooks.createMapping(enSharedNotebookGroup);

        log.trace("going to parse firma attribs");
        firmaAttributes = ENHelper.findFirmaAttributesDescriptions(content);
        log.trace("going to parse firma tags");
        firmaTags = ENHelper.findFirmaTagsDescriptions(content);

        log.trace("going to parse person attribs");
        personAttributes = ENHelper.findPersonAttributesDescriptions(content);
        log.trace("going to parse person tags");
        personTags = ENHelper.findPersonTagsDescriptions(content);

        persistentCurrentUsersFullName = con.getUserStoreClient().getUser().getName();
        finishSetup(persistentCurrentUsersFullName);
        log.trace("EN-CONFIG successfully read and parsed");
    }

}
