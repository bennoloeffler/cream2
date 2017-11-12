package bel.en.gui;

import bel.en.data.*;
import bel.en.localstore.SyncHandler;
import lombok.extern.log4j.Log4j2;

import javax.swing.table.AbstractTableModel;

import static bel.en.gui.NoteChooserForm.CRASH_NIT;

/**
 * display persons as table model - with all Data in the Firma - Person View
 */
@Log4j2
class PersonTableModel extends AbstractTableModel {

    //private List<CreamPersonData> personList = new ArrayList<>();
    private CreamFirmaData firmaData = new CreamFirmaData();

    public PersonTableModel() {
    }

    public void initAfterDataAvailable() {
        SyncHandler.get().addCreamDataListener(new CreamDataListener(){

            @Override
            public void dataChanged(Object origin) {
                // we could be affected
                fireTableDataChanged();
            }

            @Override
            public void selectionChanged(Object origin, CreamFirmaData data) {
                //log.trace(CRASH_NIT, "NoteChooserForm start");

                PersonTableModel.this.firmaData = data;
                // TODO: Test, if an additional Person from evernote pops up.
                fireTableStructureChanged();
            }

            @Override
            public void noteChanged(Object origin, CreamFirmaData creamFirmaData) {
                if(PersonTableModel.this.firmaData != null && creamFirmaData != null) {
                    if (creamFirmaData.getNote().getGuid().equals(creamFirmaData.getNote().getGuid())) {
                        PersonTableModel.this.firmaData = creamFirmaData;
                        fireTableDataChanged();
                    }
                }
            }
        });
    }

    @Override
    public String getColumnName(int column) {
        return AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription().get(column).attribName;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        CreamAttributeDescription description = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription().get(columnIndex);
        if(description.attribName.equals("Tags")) { //last one always tags...
            return CreamAttributeData.class; // make it possible, to register a editor for tags
        } else {
            return super.getColumnClass(columnIndex);
        }
    }

    public int getRowCount() {
        if(firmaData == null) {
            return 0;
        }
        return firmaData.persons.size();
    }

    public int getColumnCount() {
       if (AbstractConfiguration.getConfig() == null) {
           return 0;
       } else {
           return AbstractConfiguration.getConfig().getPersonAttributesDescription().size();
       }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CreamPersonData creamPersonData = firmaData.persons.get(rowIndex);
        CreamAttributeDescription description = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription().get(columnIndex);
        if(description.attribName.equals("Tags")) {
            return creamPersonData.getAttr(description.attribName);
        } else {
            return creamPersonData.getAttr(description.attribName).value;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        CreamPersonData creamPersonData = firmaData.persons.get(rowIndex);
        CreamAttributeDescription description = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription().get(columnIndex);
        if(description.attribName.equals("Tags")) {
            //creamPersonData.setAttr((CreamAttributeData) aValue);
            //System.out.println("müsste schon gesetzt sein...");
            CreamAttributeData attribData = creamPersonData.getAttr(description.attribName);
            attribData.value = ((CreamAttributeData)aValue).value;
           // System.out.println(firmaData.value);
        } else {
            CreamAttributeData sttribData = creamPersonData.getAttr(description.attribName);
            sttribData.value = (String) aValue;
        }
        SyncHandler.get().saveData(PersonTableModel.this, firmaData);

     }
}
