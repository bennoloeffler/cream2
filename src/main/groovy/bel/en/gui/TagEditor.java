package bel.en.gui;

import bel.en.data.CreamAttributeData;
import bel.en.data.CreamAttributeDescription;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Map;

/**
 * Can edit Tags... Dialog
 */
public class TagEditor  extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private final MyJTable tagTable;
    private JButton buttonCell;
    private JButton close;
    private JDialog dialog;
    protected static final String EDIT = "edit";
    private JTable parentTable;

    String[] tags = new String[0];
    boolean[] selected = new boolean[0];
    String[] desc = new String[0];
    CreamAttributeData d;

    AbstractTableModel m = new AbstractTableModel() {

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(columnIndex == 1) {
                selected[rowIndex] = ((Boolean)aValue).booleanValue();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if(columnIndex == 1) return true;
            return false;
        }

        String[] names = {"Tag", "gesetzt?", "Beschreibung"};
        @Override
        public String getColumnName(int column) {
            return names[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if(columnIndex == 1) return Boolean.class;
            return super.getColumnClass(columnIndex);
        }

        @Override
        public int getRowCount() {
            return tags.length;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex == 0) {
                return tags[rowIndex];
            } else if (columnIndex == 1){
                return selected[rowIndex];
            } else {
                return desc[rowIndex];
            }
         }
    };
    private ArrayList<String> illegalTags;


    public TagEditor(java.awt.Component rootFinder, Map<Integer, CreamAttributeDescription> tagsDescription) {
        super();
        tags = new String[tagsDescription.size()];
        desc = new String[tagsDescription.size()];
        selected = new boolean[tagsDescription.size()];
        for (int i = 0; i < tagsDescription.size(); i++) {
            CreamAttributeDescription tagDescription = tagsDescription.get(i);
            tags[i] = tagDescription.attribName;
            desc[i] = tagDescription.help;
        }
        buttonCell = new JButton();
        buttonCell.setActionCommand(EDIT);
        buttonCell.addActionListener(this);
        close = new JButton("Schließen");
        close.addActionListener(this);
        buttonCell.setBorderPainted(false);

        tagTable = new MyJTable();
        JScrollPane sp = new JScrollPane(tagTable);
        //sp.setViewportView(tagTable);
        dialog= new JDialog((JFrame)SwingUtilities.getRoot(rootFinder));
        dialog.setModal(true);
        dialog.setLocationRelativeTo(buttonCell);
        dialog.setLayout(new GridBagLayout());
        dialog.add(sp, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0,0,0,0), 5,5));
        dialog.add(close, new GridBagConstraints(0,1, 1,1, 0.0,0.0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 5,5));
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                fireEditingStopped();
            }

        });
        //dialog.setLocationRelativeTo(parentTable);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        parentTable = table;
        d = (CreamAttributeData) value;
        String currentTags[] = d.splitList(); //= d.value.split(", *|; *| +");
        illegalTags = new ArrayList<>();
        // get the ones, that are "impossible" with regards to spec...
        for(String illegalTag: currentTags) {
            boolean illegal = true;
            for(String oneOfLegal: tags) {
                if(illegalTag.equals(oneOfLegal)) {
                    illegal = false;
                    break;
                }
            }
            if(illegal) {
                illegalTags.add(illegalTag);
            }
        }


        // alle selectionen löschen
        for (int i = 0; i < selected.length; i++) {
            selected[i] = false;
        }
        // die aktuellen tags als selektiert markieren
        for (int i = 0; i < currentTags.length; i++) {
            String currentTag = currentTags[i];
            for (int j = 0; j < tags.length; j++) {
                String tag = tags[j];
                if(tag.equals(currentTag)){
                    selected[j] = true;
                }
            }
        }
        // TODO ist das ok so? mal ohne probieren und mit null
        dialog.setLocationRelativeTo(parentTable);

        return buttonCell;
    }

    @Override
    public Object getCellEditorValue() {
        String result = new String();
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if(selected[i]) {
                if(!"".equals(result)) result+=",  ";
                result = result + tag;
            }
            // at the very END, append those, that are NOT recognized, but anyway available...
        }
        for(String illegalTag: illegalTags) {
            if(!"".equals(result)) result+=",  ";
            result = result + illegalTag;
        }
        //System.out.println(selected);
        //System.out.println("value: " + result);
        d.value = result;
        return d;
    }


    /**
     * Handles events from the editor buttonCell and from
     * the dialog's OK buttonCell.
     */
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            //The user has clicked the cell, so
            //bring up the dialog.

            int widthOfData = AutofitTableColumns.autoResizeTable(tagTable, true);
            int heigthOfData = (int) ((JViewport) tagTable.getParent()).getViewSize().getHeight();
            if(widthOfData < 200) widthOfData = 200;
            if(widthOfData > 900) widthOfData = 900;
            if(heigthOfData <200) heigthOfData = 200;
            if(heigthOfData >900) heigthOfData = 900;
            dialog.setMinimumSize(new Dimension(widthOfData+20, heigthOfData+15));
            dialog.setPreferredSize(new Dimension(-1,-1));
            dialog.setMaximumSize(new Dimension(1000,900));
            dialog.setLocationRelativeTo(null);


            dialog.pack();

            // TODO: Does not WORK. FUCK!

            if(widthOfData < (int) ((JViewport) tagTable.getParent()).getVisibleRect().getWidth()) {
                tagTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            } else {
                tagTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            }
//            if(dial)
            dialog.setVisible(true);
            //Make the renderer reappear.
            //fireEditingStopped();
            //((AbstractTableModel)parentTable.getModel()).fireTableStructureChanged();

        } else { //User pressed dialog's "close" buttonCell.
            //currentColor = colorChooser.getColor();
            fireEditingStopped();
            dialog.setVisible(false);
        }
    }

    private class MyJTable extends JTable {
        public MyJTable() {
            super(TagEditor.this.m);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            // brauchts das hier?
            AutofitTableColumns.autoResizeTable(MyJTable.this, true);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if(!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? null:Color.LIGHT_GRAY);
            }
            return c;
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
                setPreferredScrollableViewportSize(new Dimension(viewportSize, -1));
                setPreferredSize(new Dimension(viewportSize, -1));
                setMinimumSize(new Dimension(viewportSize, -1));
                repaint();

            });
        }
    }
}
