package bel.en.gui;

import bel.en.data.*;
import bel.en.localstore.SyncHandler;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import static bel.en.gui.NoteChooserForm.CRASH_NIT;

/**
 * has the firma firmaData in three columns: attribName, value, description
 * Handles the special field "Tags".
 */
@Log4j2
class FirmaTableModel extends AbstractTableModel {

    private CreamFirmaData firmaData = new CreamFirmaData();

    String[] columnNames = {"Attribut","Wert","Beschreibung"};

    public FirmaTableModel() {
    }

    public void initAfterDataIsAvailable() {
        SyncHandler.get().addCreamDataListener(new CreamDataListener() {

            @Override
            public void selectionChanged(Object origin, CreamFirmaData firmaData) {
                //log.fine("CreamDataListener");
                //log.trace(CRASH_NIT, "FirmaTableModel start");

                if(!this.equals(origin)) {
                    if(firmaData!=null) {
                        FirmaTableModel.this.firmaData = firmaData;
                        SwingUtilities.invokeLater(() -> fireTableDataChanged());
                    }
                }
                //log.trace(CRASH_NIT, "FirmaTableModel finish");

            }

            @Override
            public void noteChanged(Object origin, CreamFirmaData firmaData) {
                if(!FirmaTableModel.this.equals(origin)) {
                    if(firmaData!=null && FirmaTableModel.this.firmaData != null && FirmaTableModel.this.firmaData.getNote() != null) {
                        if(firmaData.getNote().getGuid().equals(FirmaTableModel.this.firmaData.getNote().getGuid())) {
                            FirmaTableModel.this.firmaData = firmaData;
                            SwingUtilities.invokeLater(() -> fireTableDataChanged());
                        }
                    }
                }
            }
        });
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // this works, because the CreamAttributeData has a toString that returns the value
        //     public String toString() {return value;}
        return String.class;
    }


    // TODO: raus damit... Wenn der Listener da ist
    /*
    public void update(CreamFirmaData newFirma) {
        if(newFirma == null) {newFirma = new CreamFirmaData();} // provide empty firmaData
        firmaData = newFirma;
        fireTableStructureChanged();
    }*/

    public int getRowCount() {
        if(AbstractConfiguration.getConfig() == null) {
            return 0;
        }
        return AbstractConfiguration.getConfig().getFirmaAttributesDescription().size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CreamAttributeDescription description = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().get(rowIndex);
        if(columnIndex == 0) { // attrib Name
            return description.attribName;
        } else if(columnIndex == 1) { // value
            if("Tags".equals(description.attribName)) {
                return firmaData.getAttr(description.attribName); // deliver the whole CreamAttributeData
            } else {
                return firmaData.getAttr(description.attribName)== null?"":firmaData.getAttr(description.attribName).value;
            }
        } else {
            return description.help;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1; // second column is editable
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        CreamAttributeDescription description = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().get(rowIndex);
        CreamAttributeData data = firmaData.getAttr(description.attribName);
        if(description.attribName.equals("Tags")) {
            firmaData.setAttr((CreamAttributeData) aValue);
        } else {
            data.value = (String) aValue;
        }
        SyncHandler.get().saveData(FirmaTableModel.this, firmaData);
    }
}
