package bel.en.data;

import java.io.Serializable;

/**
 * Describes attribName and helpstring of attribute/field or tag
 */
public class CreamAttributeDescription implements Serializable {

    static final long serialVersionUID = 1L;

    public String attribName;
    public String help;
    public int orderNr;

    public CreamAttributeDescription(String attribName, String help, int orderNr) {
        this.attribName = attribName;
        this.help = help;
        this.orderNr = orderNr;
    }

    /**
     * do not change that. Its used in Combo-boxes.
     * @return
     */
    public String toString() {
        return attribName + (help==null? "":"  -  " + help);
    }

    public CreamAttributeDescription(String attribName, int orderNr) {
        this.attribName = attribName;
        this.help = null;
        this.orderNr = orderNr;
    }
}
