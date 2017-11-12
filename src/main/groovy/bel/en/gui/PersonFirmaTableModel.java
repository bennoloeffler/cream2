package bel.en.gui;

import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamAttributeDescription;
import bel.en.gui.FilterMarkAndExportForm.PersonFirmaLine;
import com.evernote.edam.type.Note;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * display persons as table model filtered by search and with an indicator, if it was selected
 */
class PersonFirmaTableModel extends AbstractTableModel {


    private FilterMarkAndExportForm filterMarkAndExportForm;

    public List<PersonFirmaLine> getPersonFirmaLinesList() {
        return personFirmaLinesList;
    }

    private List<PersonFirmaLine> personFirmaLinesList = new ArrayList<>();
    private Map<String, PersonFirmaLine> personFirmaLinesMap = new HashMap<>();
    private ArrayList<String> columnNames = new ArrayList<>();


    PersonFirmaTableModel(FilterMarkAndExportForm filterMarkAndExportForm) {
        this.filterMarkAndExportForm = filterMarkAndExportForm;
        initColumnNames();

    }

    public PersonFirmaLine getRow(int idx) {
        return personFirmaLinesList.get(idx);
    }

    public List<PersonFirmaLine> getAllMarked() {
        List<PersonFirmaLine> result = new ArrayList<>();
        for (PersonFirmaLine l : personFirmaLinesList) {
            if (l.isSelected()) {
                result.add(l);
            }
        }
        return result;
    }


    private void initColumnNames() {

        if (columnNames.size() == 0) {

            columnNames.add("markiert");

            // add attribs and tags of person as columnNames
            addColumnNames(AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription(), AbstractConfiguration.getConfig().getPersonTagsOrderedDescription());

            // add attribs and tags of firma as columnNames
            addColumnNames(AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription(), AbstractConfiguration.getConfig().getFirmaTagsOrderedDescription());

        }

    }

    private void addColumnNames(Map<Integer, CreamAttributeDescription> attribMap, Map<Integer, CreamAttributeDescription> tagMap) {
        for (int i = 0; i < attribMap.size(); i++) {
            CreamAttributeDescription description = attribMap.get(i);
            if (!"Tags".equals(description.attribName)) {
                columnNames.add(description.attribName);
            } else {
                for (int j = 0; j < tagMap.size(); j++) {
                    CreamAttributeDescription descriptionTag = tagMap.get(j);
                    columnNames.add(descriptionTag.attribName);
                }
            }
        }
    }


    public void updateData(List<Note> notes) {
        personFirmaLinesList = filterMarkAndExportForm.getLinesFromNotes(notes);

        fireTableDataChanged();
    }


    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class; //selection...
        } else {
            return String.class;
        }
    }

    public int getRowCount() {
        return personFirmaLinesList.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        PersonFirmaLine personFirmaLine = personFirmaLinesList.get(rowIndex);
        if (columnIndex == 0) {
            return personFirmaLine.isSelected();
        } else {
            return personFirmaLine.getElements().get(columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        assert (columnIndex == 0);
        boolean val = (boolean) aValue;
        PersonFirmaLine personFirmaLine = personFirmaLinesList.get(rowIndex);
        personFirmaLine.setSelected(val);
    }

    public boolean isSelected(String guidPlusNum) {
        PersonFirmaLine personFirmaLine = personFirmaLinesMap.get(guidPlusNum);
        return (personFirmaLine != null && personFirmaLine.isSelected());
    }

    public PersonFirmaLine get(int idx) {
        return personFirmaLinesList.get(idx);
    }

    public void recreateGUIDMap() {
        personFirmaLinesMap = new HashMap<>();
        int number = 0;
        String guid = "";
        for (PersonFirmaLine l : personFirmaLinesList) {
            if (!guid.equals(l.note.getGuid())) {
                guid = l.note.getGuid();
                number = 0;
            }
            personFirmaLinesMap.put(l.note.getGuid() + "__" + number++, l);
        }
    }
}
