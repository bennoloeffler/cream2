package bel.learn._21_mvc.table;

import bel.learn._01_bean.Person;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.util.Observable;
import java.util.Observer;

/**
 *
 */
public class TablePersonController extends AbstractTableModel implements Observer, ListSelectionListener {
    Model m;
    TablePersonView v;
    boolean changeComesFromHere = false;

    public TablePersonController(Model m, TablePersonView v) {
        this.m = m;
        this.v = v;
        m.addObserver(this);
        v.getPersonTable().setModel(this);
        v.getPersonTable().getSelectionModel().addListSelectionListener(this);
        updateView();
    }

    private void updateView() {
        fireTableDataChanged();
    }

    //
    // Observer of the model
    //

    @Override
    public void update(Observable o, Object arg) {
        if( ! changeComesFromHere) {
            updateView();
        }
    }

    //
    // Listener (TableModel)
    //
    @Override
    public int getRowCount() {
        return m.getPersons().size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    String[] columnName = {"Name", "Alter", "Kontostand", "Noten"};

    @Override
    public String getColumnName(int idx) {
        return columnName[idx];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Person p = m.getPersons().get(rowIndex);
        switch (columnIndex) {
            case 0: return p.getFullName();
            case 1: return String.valueOf(p.getAge());
            case 2: return String.valueOf(p.getMoney());
            case 3: {
                String grades = "";
                for (int g : p.getTestGrades()) {
                    if(!grades.equals("")) {
                        grades += ", ";
                    }
                    grades += String.valueOf(g);
                }
                return grades;
            }
            default: return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
       return columnIndex == 0 || columnIndex == 1;
    }

    @Override
    public void setValueAt(Object val, int rowIndex, int columnIndex) {
        Person p = m.getPersons().get(rowIndex);
        switch (columnIndex) {
            case 0: p.setFullName(val.toString()); break;
            case 1: p.setAge(Integer.parseInt(val.toString()));
        }
    }

    //
    // ListSelectionModel
    //
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if( ! e.getValueIsAdjusting()) {
            ListSelectionModel source = (ListSelectionModel) e.getSource();
            int selected = source.getAnchorSelectionIndex();
            changeComesFromHere = true;
            m.setSelected(selected);
            changeComesFromHere = false;
        }
    }
}
