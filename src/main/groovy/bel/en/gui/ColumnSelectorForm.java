package bel.en.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Connect a ColumnSelectorForm with a JTable and you can hide columns.
 * Just by rightclicking to the header line. A Dialog appears with a rich set:
 * shcwAll, hideAll, reset sequence.
 * The FIRST is alwas kept visible and at its place - because its assumed, that
 * this is the "marked" column.
 */
public class ColumnSelectorForm {
    private JPanel panel1;
    private JButton schließenButton;
    private JTable creamAttribTable;
    private JButton alleAusblendenButton;
    private JButton alleEinblendenButton;
    private JButton resetButton;
    AttribTableModel attribTableModel;
    JDialog d;

    public ColumnSelectorForm(JTable table) {

        schließenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
            }
        });

        alleAusblendenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attribTableModel.hideAll();
            }
        });

        alleEinblendenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attribTableModel.showAll();
            }
        });


        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attribTableModel.backToOriginal();
            }
        });

        attribTableModel = new AttribTableModel(table);
        //creamAttribTable = new JTable(attribTableModel);
        creamAttribTable.setModel(attribTableModel);
        AutofitTableColumns.autoResizeTable(creamAttribTable, false, 5);
        creamAttribTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 4, new Insets(5, 5, 5, 5), -1, -1));
        panel1.setEnabled(true);
        panel1.setMaximumSize(new Dimension(-1, -1));
        panel1.setMinimumSize(new Dimension(-1, -1));
        panel1.setPreferredSize(new Dimension(200, 600));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        creamAttribTable = new JTable();
        scrollPane1.setViewportView(creamAttribTable);
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        alleEinblendenButton = new JButton();
        alleEinblendenButton.setText("alle einblenden");
        panel1.add(alleEinblendenButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        alleAusblendenButton = new JButton();
        alleAusblendenButton.setText("alle ausblenden");
        panel1.add(alleAusblendenButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetButton = new JButton();
        resetButton.setText("reset");
        panel1.add(resetButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        schließenButton = new JButton();
        schließenButton.setText("schließen");
        panel1.add(schließenButton, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }


    class AttribTableModel extends AbstractTableModel implements TableColumnModelListener, MouseListener, PropertyChangeListener {

        private String[] columnNames = {"X", "Attribut"};
        private JTable table;
        private TableColumnModel tcm;
        private List<TableColumn> allColumns;
        private List<Boolean> selected;


        public AttribTableModel(JTable table) {
            this.table = table;
            reset();
        }


        /**
         * Reset the TableColumnManager to only manage the TableColumns that are
         * currently visible in the table.
         * <p>
         * Generally this method should only be invoked by the TableColumnManager
         * when the TableModel of the table is changed.
         */
        public void reset() {
            table.addPropertyChangeListener(this);
            table.getTableHeader().removeMouseListener(this);
            table.getTableHeader().addMouseListener(this);
            table.getColumnModel().removeColumnModelListener(this);
            tcm = table.getColumnModel();
            tcm.addColumnModelListener(this);

            //  Keep a duplicate TableColumns for managing hidden TableColumns

            int count = tcm.getColumnCount();
            allColumns = new ArrayList<TableColumn>(count);

            for (int i = 0; i < count; i++) {
                allColumns.add(tcm.getColumn(i));
            }
            selected = new ArrayList(); //boolean[allColumns.size()];
            for (int i = 0; i < allColumns.size(); i++) {
                selected.add(true);
            }
        }

        @Override
        public int getRowCount() {
            return allColumns.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return selected.get(rowIndex);
            } else {
                return ((TableColumn) allColumns.get(rowIndex)).getHeaderValue();
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Boolean.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 && rowIndex > 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            boolean visible = (boolean) aValue;
            selected.set(rowIndex, visible);
            if (visible) {
                showColumn(allColumns.get(rowIndex));
            } else {
                hideColumn(allColumns.get(rowIndex));
            }

        }

        /**
         * Hide a column from view in the table.
         *
         * @param modelColumn the column index from the TableModel
         *                    of the column to be removed
         */
        public void hideColumn(int modelColumn) {
            int viewColumn = table.convertColumnIndexToView(modelColumn);

            if (viewColumn != -1) {
                TableColumn column = tcm.getColumn(viewColumn);
                hideColumn(column);
            }
        }

        /**
         * Hide a column from view in the table.
         *
         * @param columnName the column name of the column to be removed
         */
        public void hideColumn(Object columnName) {
            if (columnName == null) return;

            for (int i = 0; i < tcm.getColumnCount(); i++) {
                TableColumn column = tcm.getColumn(i);

                if (columnName.equals(column.getHeaderValue())) {
                    hideColumn(column);
                    break;
                }
            }
        }

        /**
         * Hide a column from view in the table.
         *
         * @param column the TableColumn to be removed from the
         *               TableColumnModel of the table
         */
        public void hideColumn(TableColumn column) {
            if (tcm.getColumnCount() == 1) return;

            //  Ignore changes to the TableColumnModel made by the TableColumnManager

            tcm.removeColumnModelListener(this);
            tcm.removeColumn(column);
            tcm.addColumnModelListener(this);
        }

        /**
         * Show a hidden column in the table.
         *
         * @param modelColumn the column index from the TableModel
         *                    of the column to be added
         */
        public void showColumn(int modelColumn) {
            for (TableColumn column : allColumns) {
                if (column.getModelIndex() == modelColumn) {
                    showColumn(column);
                    break;
                }
            }
        }

        /**
         * Show a hidden column in the table.
         *
         * @param columnName the column name from the TableModel
         *                   of the column to be added
         */
        public void showColumn(Object columnName) {
            for (TableColumn column : allColumns) {
                if (column.getHeaderValue().equals(columnName)) {
                    showColumn(column);
                    break;
                }
            }
        }

        /**
         * Show a hidden column in the table. The column will be positioned
         * at its proper place in the view of the table.
         *
         * @param column the TableColumn to be shown.
         */
        private void showColumn(TableColumn column) {
            // check if already there. if yes: ignore
            Enumeration<TableColumn> viewColumns = table.getColumnModel().getColumns();
            while (viewColumns.hasMoreElements()) {
                if (viewColumns.nextElement().equals(column))
                    return;
            }


            //  Ignore changes to the TableColumnModel made by the TableColumnManager

            tcm.removeColumnModelListener(this);

            //  Add the column to the end of the table

            tcm.addColumn(column);

            //  Move the column to its position before it was hidden.
            //  (Multiple columns may be hidden so we need to find the first
            //  visible column before this column so the column can be moved
            //  to the appropriate position)

            int position = allColumns.indexOf(column);
            int from = tcm.getColumnCount() - 1;
            int to = 0;

            for (int i = position - 1; i > -1; i--) {
                try {
                    TableColumn visibleColumn = allColumns.get(i);
                    to = tcm.getColumnIndex(visibleColumn.getHeaderValue()) + 1;
                    break;
                } catch (IllegalArgumentException e) {
                }
            }

            tcm.moveColumn(from, to);

            tcm.addColumnModelListener(this);
        }

        //
        //  Implement TableColumnModelListener
        //
        public void columnAdded(TableColumnModelEvent e) {
            //  A table column was added to the TableColumnModel so we need
            //  to update the manager to track this column

            TableColumn column = tcm.getColumn(e.getToIndex());

            if (allColumns.contains(column))
                return;
            else
                allColumns.add(column);
        }

        public void columnMoved(TableColumnModelEvent e) {
            if (e.getFromIndex() == e.getToIndex()) return;

            //  A table column has been moved one position to the left or right
            //  in the view of the table so we need to update the manager to
            //  track the new location

            int index = e.getToIndex();
            TableColumn column = tcm.getColumn(index);
            int idx = allColumns.indexOf(column);
            allColumns.remove(column);
            Boolean sel = selected.get(idx);
            selected.remove(idx);

            if (index == 0) {
                // DO NOT allow "markiert" to move
                allColumns.add(0, column);
                selected.add(0, sel);
            } else {
                index--;
                TableColumn visibleColumn = tcm.getColumn(index);
                int insertionColumn = allColumns.indexOf(visibleColumn);
                allColumns.add(insertionColumn + 1, column);

                selected.add(insertionColumn + 1, sel);
            }
            fireTableDataChanged();
        }

        public void columnMarginChanged(ChangeEvent e) {
        }

        public void columnRemoved(TableColumnModelEvent e) {
        }

        public void columnSelectionChanged(ListSelectionEvent e) {
        }

        //
        //  Implement MouseListener
        //
        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton((e))) doPopup(e);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        private void doPopup(MouseEvent e) {
            JTableHeader header = (JTableHeader) e.getComponent();
            int column = header.columnAtPoint(e.getPoint());
            Rectangle r = header.getHeaderRect(column);
            d = new JDialog((JFrame) table.getRootPane().getParent(), "Attribute auswählen");
            d.setContentPane(panel1);
            //attribTableModel.fireTableStructureChanged();
            d.setLocation(r.getLocation());
            d.pack();
            //d.setModal(false);
            d.setVisible(true);
        }

        //
        //  Implement PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent e) {
            if ("model".equals(e.getPropertyName())) {
                if (table.getAutoCreateColumnsFromModel())
                    reset();
            }

        }

        public void hideAll() {
            for (int i = 1; i < allColumns.size(); i++) {
                hideColumn(i);
                selected.set(i, false);
            }
            fireTableDataChanged();
        }

        public void showAll() {
            for (int i = 1; i < allColumns.size(); i++) {
                showColumn(i);
                selected.set(i, true);
            }
            fireTableDataChanged();
        }

        public void backToOriginal() {
            TableModel model = table.getModel();
            table.setModel(new DefaultTableModel());
            table.setModel(model);
            reset();
            fireTableDataChanged();
        }
    }


}
