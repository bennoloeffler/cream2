package bel.en.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A cool table that can do most of the things just "out of the box".
 */
public class CreamTable_NEW extends JTable {

    public boolean getScrollableTracksViewportWidth()
    {
        return getPreferredSize().width < getParent().getWidth();
    }

    public void initAfterDataAvailable() {
            setFillsViewportHeight(true);
            setDoubleBuffered(true);
            new ColumnSelectorForm(this);
            setPreferredSize(null);
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                @Override
                public void columnAdded(TableColumnModelEvent e) {
                    adaptColumsSizeToData();
                }

                @Override
                public void columnRemoved(TableColumnModelEvent e) {
                    adaptColumsSizeToData();
                }

                @Override
                public void columnMoved(TableColumnModelEvent e) {

                }

                @Override
                public void columnMarginChanged(ChangeEvent e) {

                }

                @Override
                public void columnSelectionChanged(ListSelectionEvent e) {

                }
            });

            getModel().addTableModelListener(e -> adaptColumsSizeToData());

            addPropertyChangeListener("model", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    CreamTable_NEW.this.adaptColumsSizeToData();
                    CreamTable_NEW.this.getModel().addTableModelListener(e -> CreamTable_NEW.this.adaptColumsSizeToData());
                }
            });

        }

    private void adaptColumsSizeToData() {
        SwingUtilities.invokeLater(() -> {
             AutofitTableColumns.autoResizeTable(this, true, 5);
            ((JViewport)getParent()).repaint();
            repaint();

        });
    }

    /**
     * make the table grey/white lined
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? null : new Color(250, 250, 245));
        return c;
    }
}
