package bel.en.data;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;


/**
 * Structured values corresponding with ENML-Document
 */
public class CreamPersonData extends CreamData {

    @Getter @Setter private boolean selected = false;

    @Override
    public Map<Integer, CreamAttributeDescription> getOrderedAttributeDescriptors() {
        return AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription();
    }

    //public boolean isSelected() { return selected;}
    //public void setSelected(boolean selected) {this.selected = selected;}

}
