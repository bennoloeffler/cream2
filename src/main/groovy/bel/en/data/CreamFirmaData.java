package bel.en.data;

import com.evernote.edam.type.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CreamFirmaData extends CreamData {

    @Getter @Setter private Note note;

    public List<CreamPersonData> persons = new ArrayList<>();

    //@Getter @Setter private SearchCache searchCache;

    @Getter @Setter
    private List<String> abos = new ArrayList<>();

    @Override
    public Map<Integer, CreamAttributeDescription> getOrderedAttributeDescriptors() {
        return AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription();
    }


    //private HashMap<String, CreamAttributeData> data = new HashMap<String, CreamAttributeData>();

    /*
    public void setAttr(CreamAttributeData attr) {
        data.put(attr.description.attribName, attr);
    }*/

    /*
    public CreamAttributeData getAttr(String attrName) {
        CreamAttributeData attr = data.get(attrName);
        return attr;
    }*/

    /*
    public CreamAttributeData getAttr(int idx) {
        assert(idx >= 0);
        assert(idx < AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().size());
        assert(AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().get(idx) != null);
        CreamAttributeDescription creamAttributeDescription = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().get(idx);
        CreamAttributeData attr = data.get(creamAttributeDescription.attribName);
        assert(attr != null);
        return attr;
    }*/

    /*
    public Map getAttributes(){return data;}
    */

    /*
    public boolean matchAllAttribs(String allRegex) {
        for(CreamAttributeData d : data.values()) {
            boolean result = RegexUtils.matchWithRegex(d.value, allRegex);
            if(result) return true;
        }
        return false;
    }*/

}
