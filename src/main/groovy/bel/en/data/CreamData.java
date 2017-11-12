package bel.en.data;

import bel.util.RegexUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all attribute based classes
 */
abstract public class CreamData {

    private HashMap<String, CreamAttributeData> data = new HashMap<String, CreamAttributeData>();

    abstract public Map<Integer, CreamAttributeDescription> getOrderedAttributeDescriptors();

    public void setAttr(CreamAttributeData attr) {
        data.put(attr.description.attribName, attr);
    }

    public CreamAttributeData getAttr(String attrName) {
        CreamAttributeData attr = data.get(attrName);
        return attr;
    }

    public CreamAttributeData getAttr(int idx) {
        assert(idx >=0);
        assert(idx < getOrderedAttributeDescriptors().size());
        assert(getOrderedAttributeDescriptors().get(idx) != null);
        CreamAttributeDescription creamAttributeDescription = getOrderedAttributeDescriptors().get(idx);
        CreamAttributeData attr = data.get(creamAttributeDescription.attribName);
        assert(attr != null);
        return attr;
    }

    public boolean matchAllAttribs(String allRegex) {
        for(CreamAttributeData d : data.values()) {
            boolean result = RegexUtils.matchWithRegex(d.value,allRegex);
            if(result) return true;
        }
        return false;
    }

    public void createAttributes() {
        Collection<CreamAttributeDescription> attribDescriptors = getOrderedAttributeDescriptors().values();
        for (CreamAttributeDescription attribDescriptor: attribDescriptors ) {
            CreamAttributeData cad = new CreamAttributeData();
            cad.description = attribDescriptor;
            cad.value = "";
            setAttr(cad);
        }
    }

    public int numberOfAttribs(){return data.size();}

    public String toString() {
        String result = "";
        for(CreamAttributeDescription a: getOrderedAttributeDescriptors().values()) {
            if(getAttr(a.attribName) == null) {
                result += a.attribName+": " + "NOT AVAILABLE" + ",  ";

            } else {
                result += a.attribName + ": " + getAttr(a.attribName).value + ",  ";
            }
        }
        return result;
    }
}
