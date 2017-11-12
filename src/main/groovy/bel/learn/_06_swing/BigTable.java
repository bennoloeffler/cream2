package bel.learn._06_swing;

import bel.en.gui.AutofitTableColumns;
import bel.en.gui.ColumnSelectorForm;
import bel.learn._xx_sampleData.SampleDataSource;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Just a fancy table with all I have learned about tables :-)
 */
@Log
public class BigTable extends javax.swing.JTable {


    public BigTable() {
        setModel(new BigTableModel());
        setAutoResizeMode(AUTO_RESIZE_OFF);
        AutofitTableColumns.autoResizeTable(this, true);
        new ColumnSelectorForm(this);
    }

    public void deleteSomeFields() {
        for(int i=0; i<400; i++) {
            int row = (int)(Math.random() * getModel().getRowCount());
            int col = (int)(Math.random() * getColumnCount());
            SwingUtilities.invokeLater(() -> setValueAt("BOMBE!", row, col));
            //log.info("Bombe: r:" + row + " c:"+col);
        }
    }

    public void addStuff() {
        for(int i=0; i<4000; i++) {
            int row = (int) (Math.random() * getModel().getRowCount());
            int col = (int) (Math.random() * getColumnCount());
            String val = (String) getValueAt(row, col);
            if (val.contains("Sabine") || val.contains("Leo") || val.contains("Benno") || val.contains("Paul")) {
                final String val2 = "   :-)    Schatz gefunden!    " + val;
                SwingUtilities.invokeLater(() -> setValueAt(val2, row, col));
                //log.info(":-) r:" + row + " c:"+col);
            }
        }
        SwingUtilities.invokeLater(()->AutofitTableColumns.autoResizeTable(this, true));

    }

    class BigTableModel extends AbstractTableModel {

        private final int ROWS = 2000;
        private final int COLUMNS = 300;

        List<List<String>> data = new ArrayList<>();

        BigTableModel() {
            for(int row = 0; row < ROWS; row++){
                List oneRow = new ArrayList();
                data.add(oneRow);
                for(int column = 0; column < COLUMNS; column++) {

                    oneRow.add(SampleDataSource.nextSample());
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data.get(rowIndex).set(columnIndex, (String) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public int getRowCount() {
            return ROWS;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).get(columnIndex);
        }
    }
}
