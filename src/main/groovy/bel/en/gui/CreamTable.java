package bel.en.gui;

import bel.en.data.CreamAttributeData;
import bel.en.data.CreamAttributeDescription;
import bel.en.data.CreamDataListener;
import bel.en.data.CreamFirmaData;
import bel.en.localstore.SyncHandler;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Map;

import static bel.en.gui.NoteChooserForm.CRASH_NIT;

/**
 * The help in the third.
 * Make the last row ALWAYS contain the tags. Provide a tag-editor there.
 * Chances colors from white/grey in order to see the lines better.

 */
@Log4j2
class CreamTable extends JTable {

    public static final int LAST_ROW_IS_TAG = 1;
    public static final int LAST_COLUMN_IS_TAG = 2;
    private int lastRowOrLastColumn;

    public CreamTable() {
        super();
        setFillsViewportHeight(true);
        setDoubleBuffered(true);
    }

    public boolean getScrollableTracksViewportWidth()
    {
        return getPreferredSize().width < getParent().getWidth();
    }

    public void initAfterDataAvailable(Map<Integer, CreamAttributeDescription> tagsOrderedDescription, int lastRowOrLastColumn) {
        assert (lastRowOrLastColumn ==LAST_COLUMN_IS_TAG || lastRowOrLastColumn == LAST_ROW_IS_TAG);
        this.lastRowOrLastColumn = lastRowOrLastColumn;
        setDefaultEditor(CreamAttributeData.class, new TagEditor(this, tagsOrderedDescription));
        setDefaultRenderer(CreamAttributeData.class, new DefaultTableCellRenderer());
        //setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        getModel().addTableModelListener(e -> {
            adaptColumsSizeToData();
        });
        addPropertyChangeListener("model", evt -> {
            adaptColumsSizeToData();
            getModel().addTableModelListener(e -> adaptColumsSizeToData());
        });
        //AutofitTableColumns.autoResizeTable(this, true);
        SyncHandler.get().addCreamDataListener(new CreamDataListener() {

            @Override
            public void selectionChanged(Object origin, CreamFirmaData creamFirmaData) {
                //log.trace(CRASH_NIT, "CreamTable start");
                adaptColumsSizeToData();
                //log.trace(CRASH_NIT, "CreamTable finished");
            }
        });
        adaptColumsSizeToData();
        //repaint();
    }

    private void adaptColumsSizeToData() {
        SwingUtilities.invokeLater(() -> {

            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            int viewportSize = AutofitTableColumns.autoResizeTable(this, true);
            int realSize = (int) ((JViewport) getParent()).getViewSize().getWidth();
            if (viewportSize < realSize) {
                setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                viewportSize = realSize;
            }
            //setPreferredScrollableViewportSize(new Dimension(viewportSize, -1));
            //setPreferredSize(new Dimension(viewportSize, -1));
            setPreferredSize(null);

            //setMinimumSize(new Dimension(viewportSize, -1));
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

    /**
     * make the last row or column use the TagCellEditor
     */
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        TableCellEditor e = super.getCellEditor(row, column);
        if(lastRowOrLastColumn == LAST_ROW_IS_TAG) {
            if (row == getModel().getRowCount() - 1) {
                e = getDefaultEditor(CreamAttributeData.class);
            }
        } else {
            if (column == getModel().getColumnCount()- 1) {
                e = getDefaultEditor(CreamAttributeData.class);
            }
        }
        return e;
    }
}
