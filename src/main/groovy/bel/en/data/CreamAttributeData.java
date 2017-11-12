package bel.en.data;

/**
 * a field / Attribute in Person or Firma. attribName and helpstring are found in description
 */
public class CreamAttributeData {

    public String value;
    public CreamAttributeDescription description;
    // TODO not in use until now...
    public String modifiedFromWhen;

    @Override
    public String toString() {
        return value;
    }

    /**
     * if the attribte is a list of strings, it may be separated by
     * comma or space and any number of additional spaces
     * @return
     */
    public String[] splitList() { return value.split(" *[,;] *| +");}

    private String[] tagsAndSpaces;

    public boolean containsTag(String tag) {
        if(tagsAndSpaces == null) {
            tagsAndSpaces = splitList();
        }
        for (int i = 0; i < tagsAndSpaces.length; i++) {
            if (tagsAndSpaces[i].equals(tag.trim())) {
                return true;
            }
        }
        return false;
    }

    public void addTag(String tag) {
        if(tagsAndSpaces == null) {
            tagsAndSpaces = splitList();
        }
        value = tag;
        for (int i = 0; i < tagsAndSpaces.length; i++) {
            if(! tagsAndSpaces[i].equals("")) {
                value += ",  " + tagsAndSpaces[i];
            }
        }
        tagsAndSpaces = null;
    }

    public void removeTag(String tag) {
        if(tagsAndSpaces == null) {
            tagsAndSpaces = splitList();
        }
        value = "";
        for (int i = 0; i < tagsAndSpaces.length; i++) {
            if(! tagsAndSpaces[i].equals(tag)) {
                if(! "".equals(value)) {
                    value += ",  ";
                }
                value += tagsAndSpaces[i];
            }
        }
        tagsAndSpaces = null;
    }
}
