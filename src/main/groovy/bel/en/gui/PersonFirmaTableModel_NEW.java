package bel.en.gui;

import bel.en.data.*;
import bel.en.localstore.SyncHandler;
import org.apache.commons.lang3.Pair;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Readonly TableModel for Person + Firma data.
 */
public class PersonFirmaTableModel_NEW extends AbstractTableModel {

    private ArrayList<CreamAttributeDescription> columnNames;
    private List<Pair<CreamPersonData, CreamFirmaData>> data;
    private int numberOfPersonAttribs;

    public PersonFirmaTableModel_NEW() {
        SyncHandler.get().addCreamDataListener(new CreamDataListener() {
            @Override
            public void dataChanged(Object origin) {
                if(origin != this) {
                    data = null;
                    fireTableDataChanged();
                }
            }
        });
    }


    private void initColumns() {

        columnNames = new ArrayList<>(AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription().values());
        numberOfPersonAttribs = columnNames.size();
        columnNames.addAll(AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().values());
    }

    private void readData() {
        List<CreamFirmaData> firmaData = SyncHandler.get().readDataList();
        data = new ArrayList<>();
        for(CreamFirmaData f: firmaData) {
            for(CreamPersonData p: f.persons) {
                Pair<CreamPersonData, CreamFirmaData> pair = new Pair<>(p,f);
                data.add( pair);
            }
        }
    }


    @Override
    public int getRowCount() {
        if (data == null) {
            readData();
        }
        return data.size();
    }


    @Override
    public int getColumnCount() {
        if(columnNames == null) {
            initColumns();
        }
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
            if(columnNames == null) {
                initColumns();
            }
            return columnNames.get(column).attribName;
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (data == null) {
            readData();
        }
        Pair<CreamPersonData, CreamFirmaData> personFirmaPair = data.get(rowIndex);
        if(columnIndex < numberOfPersonAttribs) {
            CreamAttributeData attrib = personFirmaPair.left.getAttr(columnIndex);
            return attrib.value;
        } else {
            CreamAttributeData attrib = personFirmaPair.right.getAttr(columnIndex-numberOfPersonAttribs);
            return attrib.value;
        }
    }

    public Pair<CreamPersonData, CreamFirmaData> getPairAtRow(int idx) {
        return data.get(idx);
    }
}
