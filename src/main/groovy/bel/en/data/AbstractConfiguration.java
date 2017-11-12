package bel.en.data;

import bel.en.evernote.ENCREAMNotebooks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * holds all the structure. Just the filling is in the derived class.
 * At the end of the constructor, when firmaAttribs, firmaTags, personAttrib and personTags are filled,
 * just call "finishSetup" - thats it.
 */
public abstract class AbstractConfiguration implements Configuration, Serializable {

    static final long serialVersionUID = 1L;

    private static Configuration configuration;

    protected List<CreamUserData> users = new ArrayList<CreamUserData>();

    protected Map<String, CreamAttributeDescription> firmaAttributes = new HashMap<String, CreamAttributeDescription>();
    transient protected Map<Integer, CreamAttributeDescription> firmaAttributesOrdered;
    protected Map<String, CreamAttributeDescription> firmaTags = new HashMap<String, CreamAttributeDescription>();
    transient protected Map<Integer, CreamAttributeDescription> firmaTagsOrdered;

    protected Map<String, CreamAttributeDescription> personAttributes = new HashMap<String, CreamAttributeDescription>();
    transient protected Map<Integer, CreamAttributeDescription> personAttributesOrdered;
    protected Map<String, CreamAttributeDescription> personTags = new HashMap<String, CreamAttributeDescription>();
    transient protected Map<Integer, CreamAttributeDescription> personTagsOrdered;

    transient protected CreamUserData currentUser;

    protected ENCREAMNotebooks creamNotebooks;

    protected String persistentCurrentUsersFullName;

    public String getPersistentCurrentUsersFullName() {
        return persistentCurrentUsersFullName;
    }


    //
    // access to Config
    //

    private static void setConfiguration(Configuration con) {
        configuration = con;
    }

    public static Configuration getConfig() {
        return configuration;
    }



    /*
     * FIRMA
     */

    public Map<String, CreamAttributeDescription> getFirmaAttributesDescription() {
        return firmaAttributes;
    }

    public Map<Integer, CreamAttributeDescription> getFirmaAttributesOrderedDescription() {
        return firmaAttributesOrdered;
    }

    public Map<String, CreamAttributeDescription> getFirmaTagsDescription() {
        return firmaTags;
    }

    public Map<Integer, CreamAttributeDescription> getFirmaTagsOrderedDescription() {
        return firmaTagsOrdered;
    }

    /*
     * PERSON
     */

    public Map<String, CreamAttributeDescription> getPersonAttributesDescription() {
        return personAttributes;
    }

    public Map<Integer, CreamAttributeDescription> getPersonAttributesOrderedDescription() {
        return personAttributesOrdered;
    }

    public Map<String, CreamAttributeDescription> getPersonTagsDescription() {
        return personTags;
    }

    public Map<Integer, CreamAttributeDescription> getPersonTagsOrderedDescription() {
        return personTagsOrdered;
    }


    /*
     * USERS
     */

    public List<CreamUserData> getUsers() {
        return users;
    }

    public CreamUserData getCurrentUser() {
        return currentUser;
    }

    public List<CreamUserData> getAdmins() {
        List<CreamUserData> result = new ArrayList<CreamUserData>();
        for (CreamUserData u : users) {
            if (u.isAdmin()) {
                result.add(u);
            }
        }
        return result;
    }

    public String getShortName(String emailAdress) {
        for(CreamUserData u: getUsers()) {
            String uE = u.getEmail();
            if(u.getEmail().trim().equals(emailAdress.trim())) {
                return u.getShortName();
            }
        }
        return null;
    }


    public String getEmail(String shortName) {
        for(CreamUserData u: getUsers()) {
            String uS = u.getShortName();
            if(uS.trim().equals(shortName.trim())) {
                return u.getEmail();
            }
        }
        return null;
    }

    public CreamUserData getUser(String shortName) {
        for(CreamUserData u: getUsers()) {
            String uS = u.getShortName();
            if(uS.trim().equals(shortName.trim())) {
                return u;
            }
        }
        return null;
    }

    @Override
    /**
     * returns not only the tags, but also a help, so that it is pretty much human readable
     */
    public List<String> getAllFirmaTagsAsList() {
        ArrayList<String> result = new ArrayList<>();
        for (CreamAttributeDescription a: firmaTagsOrdered.values()) {
            result.add(a.attribName + (a.help==null?"":("  -  " + a.help)));
        }
        return result;
    }

    @Override
    /**
     * returns not only the tags, but also a help, so that it is pretty much human readable
     */
    public List<String> getAllPersonTagsAsList() {
        ArrayList<String> result = new ArrayList<>();
        for (CreamAttributeDescription a: personTagsOrdered.values()) {
            result.add(a.attribName + (a.help==null?"":("  -  " + a.help)));
        }
        return result;
    }

    public ENCREAMNotebooks getCreamNotebooks() {
        return creamNotebooks;
    }


    // helper for implementation - to create the ordered map

    protected HashMap<Integer, CreamAttributeDescription> getOrderedMap(Map<String, CreamAttributeDescription> attribMap) {
        HashMap<Integer, CreamAttributeDescription> orderedMap = new HashMap<Integer, CreamAttributeDescription>();
        for (Map.Entry<String, CreamAttributeDescription> entry : attribMap.entrySet()) {
            orderedMap.put(entry.getValue().orderNr, entry.getValue());
        }
        return orderedMap;
    }


    public void finishSetup(String currentUsersFullName) throws Exception {
        firmaAttributesOrdered = getOrderedMap(firmaAttributes);
        firmaTagsOrdered = getOrderedMap(firmaTags);
        personAttributesOrdered = getOrderedMap(personAttributes);
        personTagsOrdered = getOrderedMap(personTags);
        setConfiguration(this);

        // the Name of the user (e.g. Benno Löffler) is matched against evernote Name.
        // NOT username (bennoloeffler), not the email (benno.loeffler@gmx.de) but the natural attribName.
        if (currentUsersFullName == null || "".equals(currentUsersFullName) ) {
            throw new Exception("Konnte Namen des eingeloggten User nicht von Evernote lesen");
        }
        for(CreamUserData u: users) {
            if(currentUsersFullName.trim().equals(u.getCompleteName().trim())) {
                currentUser = u;
            }
        }
        if (currentUser == null) {
            throw new Exception("für den eingeloggten EN-User "+ currentUsersFullName + " konnte kein user in der Notiz ***Konfiguration*** gefunden werden");
        }

    }


}
