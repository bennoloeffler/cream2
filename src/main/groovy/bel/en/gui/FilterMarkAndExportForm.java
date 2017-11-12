package bel.en.gui;

import bel.en.data.*;
import bel.en.localstore.SyncHandler;
import bel.util.RegexUtils;
import com.evernote.edam.type.Note;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

/**
 * all
 */
public class FilterMarkAndExportForm {
    private JPanel rootPanel;

    private JPanel panelSearchResultTable;
    private JButton alleMarkierenButton;
    private JButton markierungBeiGefiltertenAufhebenButton;
    private JButton markierteExportierenButton;
    private JButton beiAllenMarkiertenTagButton;
    private JButton hilfeRegexButton;

    private JTable personFirmaSuchTable;
    private JTextField hierRegexSuchbegriffEintippenTextField;

    private JComboBox allTagsPersonComboBox;
    private JComboBox allTagsFirmaComboBox;
    private JTextField sucheTagsPersonTextField;
    private JTextField sucheTagsFirmaTextField;

    private JTextField personSuchTextField1;
    private JTextField personSuchTextField2;
    private JTextField firmaSuchTextField1;
    private JTextField firmaSuchTextField2;

    private JComboBox personComboBox1;
    private JComboBox personComboBox2;
    private JComboBox firmaComboBox1;
    private JComboBox firmaComboBox2;

    private JButton searchNowButton;
    private JTextField personDatumVonTextField;
    private JTextField personDatumBisTextField;
    private JTextField firmaDatumVonTextField;
    private JTextField firmaDatumBisTextField;
    private JTextField personZahlVonTextField;
    private JTextField personZahlBisTextField;
    private JTextField firmaZahlBisTextField;
    private JTextField firmaZahlVonTextField;

    PersonFirmaTableModel personFirmaTableModel;

    Comparator<Object> comparator;
    TableRowSorter<PersonFirmaTableModel> sorter;
    RowFilter<Object, Object> filter;

    TableStringConverter tableStringConverter;

    //ArrayList<CreamAttributeDescription> columnsDescriptors = new ArrayList<>();

    public JPanel getPanel() {
        return rootPanel;
    }

    public void initAfterDataAvailable() {

        List<String> allPersonTagsAsList = AbstractConfiguration.getConfig().getAllPersonTagsAsList();
        String[] allPersonTagsAsArray = allPersonTagsAsList.toArray(new String[allPersonTagsAsList.size()]);
        allTagsPersonComboBox.setModel(new DefaultComboBoxModel<String>(allPersonTagsAsArray));

        List<String> allFirmaTagsAsList = AbstractConfiguration.getConfig().getAllFirmaTagsAsList();
        String[] allFirmaTagsAsArray = allFirmaTagsAsList.toArray(new String[allFirmaTagsAsList.size()]);
        allTagsFirmaComboBox.setModel(new DefaultComboBoxModel<String>(allFirmaTagsAsArray));

        personComboBox1.setModel(getPersonAttribComboBoxModel());
        personComboBox2.setModel(getPersonAttribComboBoxModel());

        firmaComboBox1.setModel(getFirmaAttribComboBoxModel());
        firmaComboBox2.setModel(getFirmaAttribComboBoxModel());

        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        };

        personFirmaSuchTable.setColumnModel(new FirstNotMoveTableColumnModel());

        personFirmaTableModel = new PersonFirmaTableModel(this);
        sorter = new TableRowSorter<PersonFirmaTableModel>(personFirmaTableModel);
        sorter.setRowFilter(filter);
        personFirmaSuchTable.setRowSorter(sorter);
        personFirmaSuchTable.setModel(personFirmaTableModel);

        registerCreamDataListenerWithSyncHandler();
        for (int i = 1; i < personFirmaTableModel.getColumnCount(); i++) {
            sorter.setComparator(i, comparator);
        }

        ColumnSelectorForm cas = new ColumnSelectorForm(personFirmaSuchTable);

        AutofitTableColumns.autoResizeTable(personFirmaSuchTable, true);
    }

    public FilterMarkAndExportForm() {

        $$$setupUI$$$();

        ActionListener searchAction = e -> personFirmaTableModel.fireTableDataChanged();


        hierRegexSuchbegriffEintippenTextField.addActionListener(searchAction);
        sucheTagsPersonTextField.addActionListener(searchAction);
        sucheTagsFirmaTextField.addActionListener(searchAction);

        personComboBox1.addActionListener(searchAction);
        personSuchTextField1.addActionListener(searchAction);
        personDatumVonTextField.addActionListener(searchAction);
        personDatumBisTextField.addActionListener(searchAction);
        personZahlVonTextField.addActionListener(searchAction);
        personZahlBisTextField.addActionListener(searchAction);
        personComboBox2.addActionListener(searchAction);
        personSuchTextField2.addActionListener(searchAction);

        firmaComboBox1.addActionListener(searchAction);
        firmaSuchTextField1.addActionListener(searchAction);
        firmaDatumVonTextField.addActionListener(searchAction);
        firmaDatumBisTextField.addActionListener(searchAction);
        firmaZahlVonTextField.addActionListener(searchAction);
        firmaZahlBisTextField.addActionListener(searchAction);
        firmaComboBox2.addActionListener(searchAction);
        firmaSuchTextField2.addActionListener(searchAction);

        searchNowButton.addActionListener(searchAction);

        allTagsPersonComboBox.addActionListener(e -> {
            String item = (String) allTagsPersonComboBox.getSelectedItem();
            String[] split = item.split("  -  ");
            String newText = sucheTagsPersonTextField.getText().trim() + "; " + split[0];
            sucheTagsPersonTextField.setText(newText);
            personFirmaTableModel.fireTableDataChanged();

        });

        allTagsFirmaComboBox.addActionListener(e -> {
            String item = (String) allTagsFirmaComboBox.getSelectedItem();
            String[] split = item.split("  -  ");
            String newText = sucheTagsFirmaTextField.getText().trim() + "; " + split[0];
            sucheTagsFirmaTextField.setText(newText);
            personFirmaTableModel.fireTableDataChanged();

        });


        personFirmaSuchTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        personFirmaSuchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (((ListSelectionModel) e.getSource()).isSelectionEmpty()) {
                        SyncHandler.get().setSelectedNote(this, null);
                    } else {
                        int idx = personFirmaSuchTable.convertRowIndexToModel(((ListSelectionModel) e.getSource()).getAnchorSelectionIndex());
                        PersonFirmaLine row = personFirmaTableModel.getRow(idx);
                        SyncHandler.get().setSelectedNote(this, row.firmaData);
                    }
                }
            }
        });


        filter = new MyRowFilter();

        beiAllenMarkiertenTagButton.addActionListener(e -> {
            TaggerForAllMarked d = new TaggerForAllMarked();
            d.pack();
            d.setVisible(true);
            if (d.getTagToChange() != null) {
                try {
                    addTagToMarked(d.getTagToChange(), d.isAddTag(), d.isPerson());
                } catch (Exception e1) {
                    JOptionPane.showConfirmDialog(rootPanel, "Bitte prüfen Sie, welche Tags gesetzt wurden. Interne Meldung: " + e1.toString(), "PROGRAMM-FEHLER...", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        alleMarkierenButton.addActionListener(e -> {
            for (int row = 0; row < personFirmaSuchTable.getRowCount(); row++) {
                personFirmaSuchTable.getModel().setValueAt(true, personFirmaSuchTable.convertRowIndexToModel(row), 0);
            }
            personFirmaTableModel.fireTableDataChanged();
        });

        markierungBeiGefiltertenAufhebenButton.addActionListener(e -> {
            for (int row = 0; row < personFirmaSuchTable.getRowCount(); row++) {
                personFirmaSuchTable.getModel().setValueAt(false, personFirmaSuchTable.convertRowIndexToModel(row), 0);
            }
            personFirmaTableModel.fireTableDataChanged();
        });

        markierteExportierenButton.addActionListener(e -> writeDataFile());

        hilfeRegexButton.addActionListener(e -> GuiUtil.regexHelp());


    }


    //
    // export Data to file
    //
    public void writeDataFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(panelSearchResultTable);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();


            BufferedWriter bw = null;
            try {
                if (!f.getPath().endsWith(".csv")) {
                    f = new File(f.getPath() + ".csv");
                }
                if (f.exists()) f.delete();
                f.createNewFile();
                bw = new BufferedWriter
                        (new OutputStreamWriter(new FileOutputStream(f), "Cp1252")); // utf-8, otherwise german umlauts are crippled
                //bw = new BufferedWriter(new FileWriter(dataFile));

                // write the column names
                TableColumnModel columnModel = personFirmaSuchTable.getColumnModel();
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    int modelIndexOfCol = personFirmaSuchTable.convertColumnIndexToModel(i);
                    if (modelIndexOfCol != 0) {
                        TableColumn column = columnModel.getColumn(i);
                        bw.write(column.getHeaderValue() + ";");
                    }
                }
                bw.newLine();

                // write the firmaData rows
                //List<PersonFirmaLine> allMarked = personFirmaTableModel.getAllMarked();
                for (int row = 0; row < personFirmaTableModel.getRowCount(); row++) {
                    if (personFirmaTableModel.get(row).isSelected()) {
                        for (int i = 0; i < columnModel.getColumnCount(); i++) {
                            int modelIndexOfCol = personFirmaSuchTable.convertColumnIndexToModel(i);
                            if (modelIndexOfCol != 0) {
                                TableColumn column = columnModel.getColumn(i);
                                Object o = personFirmaTableModel.getValueAt(row, column.getModelIndex());
                                bw.write(o.toString() + ";");
                            }
                        }
                        bw.newLine();
                    }
                }
                bw.newLine();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(getPanel(), "FEHLER-NR: 001, " + ex.toString(), "Fehler beim Schreiben der Datei!", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    Desktop.getDesktop().open(f);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(getPanel(), "FEHLER-NR: 002, " + ex.toString(), "Fehler beim Öffnen der Datei in Excel!", JOptionPane.ERROR_MESSAGE);
                }
            }

        }
    }

    private void registerCreamDataListenerWithSyncHandler() {
        // TODO: ersetzen durch SyncHandler Listener im TableModel?
        personFirmaTableModel.updateData(SyncHandler.get().getAllNotes()); // do that once. In future, dataChanged will do it.

        SyncHandler.get().addCreamDataListener(new CreamDataListener() {

            @Override
            public void dataChanged(Object origin) {
                personFirmaTableModel.updateData(SyncHandler.get().getAllNotes());
            }

        });
        personFirmaTableModel.fireTableStructureChanged(); // initial draw
    }


    private void addTagToMarked(String tagToChange, boolean addTag, boolean isPerson) throws Exception {
        boolean dirty = false;
        List<PersonFirmaLine> allMarked = personFirmaTableModel.getAllMarked();
        for (PersonFirmaLine l : allMarked) {
            CreamAttributeData tags;
            if (isPerson) {
                tags = l.personData.getAttr("Tags");
            } else {
                tags = l.firmaData.getAttr("Tags");
            }
            if (addTag) {

                if (!tags.containsTag(tagToChange)) {
                    tags.addTag(tagToChange);
                    dirty = true;
                }
            } else {
                if (tags.containsTag(tagToChange)) {
                    tags.removeTag(tagToChange);
                    dirty = true;
                }
            }
            if (dirty) {
                dirty = false;
                //ENHelper.writeDataToNote(l.firmaData, l.note);
                SyncHandler.get().saveData(this, l.firmaData);
                //SyncHandler.get().updateNote(l.note);
            }
        }
        personFirmaTableModel.fireTableDataChanged();
    }


    private DefaultComboBoxModel getPersonAttribComboBoxModel() {
        Collection<CreamAttributeDescription> values = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription().values();
        CreamAttributeDescription[] personAttributes = values.toArray(new CreamAttributeDescription[values.size()]);
        return new DefaultComboBoxModel<>(personAttributes);
    }

    private DefaultComboBoxModel getFirmaAttribComboBoxModel() {
        Collection<CreamAttributeDescription> values = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription().values();
        CreamAttributeDescription[] firmaAttributes = values.toArray(new CreamAttributeDescription[values.size()]);
        return new DefaultComboBoxModel<>(firmaAttributes);
    }

    // use filtering in Java...


    private void createUIComponents() {
        personFirmaSuchTable = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? null : new Color(250, 250, 245));
                return c;
            }
        };
        personFirmaSuchTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        alleMarkierenButton = new JButton();
        alleMarkierenButton.setText("alle Gefilterten markieren");
        panel1.add(alleMarkierenButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        markierungBeiGefiltertenAufhebenButton = new JButton();
        markierungBeiGefiltertenAufhebenButton.setText("Markierung bei Gefilterten aufheben");
        panel1.add(markierungBeiGefiltertenAufhebenButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        markierteExportierenButton = new JButton();
        markierteExportierenButton.setText("Markierte exportieren");
        panel1.add(markierteExportierenButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        beiAllenMarkiertenTagButton = new JButton();
        beiAllenMarkiertenTagButton.setText("bei allen Markierten Tag setzen");
        panel1.add(beiAllenMarkiertenTagButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(true);
        splitPane1.setDividerLocation(800);
        splitPane1.setDividerSize(10);
        splitPane1.setDoubleBuffered(true);
        rootPanel.add(splitPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        panelSearchResultTable = new JPanel();
        panelSearchResultTable.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panelSearchResultTable);
        final JScrollPane scrollPane1 = new JScrollPane();
        panelSearchResultTable.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(personFirmaSuchTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setMinimumSize(new Dimension(25, 25));
        splitPane1.setRightComponent(panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder("Tags Person"));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        allTagsPersonComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("LF");
        defaultComboBoxModel1.addElement("GF");
        defaultComboBoxModel1.addElement("B_WRONG_T");
        defaultComboBoxModel1.addElement("B_DENK_WZ");
        allTagsPersonComboBox.setModel(defaultComboBoxModel1);
        panel4.add(allTagsPersonComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("ein oder mehrere Tags auswählen");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Such-Tags Person (UND-Verknüpfung):");
        panel5.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sucheTagsPersonTextField = new JTextField();
        panel5.add(sucheTagsPersonTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder("Tags Firma"));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        allTagsFirmaComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("WUNSCH_K");
        defaultComboBoxModel2.addElement("VDMA_MG");
        allTagsFirmaComboBox.setModel(defaultComboBoxModel2);
        panel7.add(allTagsFirmaComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Tags auswählen");
        panel7.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Such-Tags Firma: (UND-Verknüpfung)");
        panel8.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sucheTagsFirmaTextField = new JTextField();
        sucheTagsFirmaTextField.setText("");
        panel8.add(sucheTagsFirmaTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(2, 1, new Insets(10, 0, 0, 0), -1, -1));
        panel2.add(panel9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel9.setBorder(BorderFactory.createTitledBorder("Attribute von Person"));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(""));
        personComboBox1 = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        personComboBox1.setModel(defaultComboBoxModel3);
        panel10.add(personComboBox1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        personSuchTextField1 = new JTextField();
        panel10.add(personSuchTextField1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Regex");
        panel10.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Attribut");
        panel10.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Datum");
        panel10.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("von");
        panel11.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        personDatumVonTextField = new JTextField();
        panel11.add(personDatumVonTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("bis");
        panel11.add(label9, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        personDatumBisTextField = new JTextField();
        personDatumBisTextField.setText("");
        panel11.add(personDatumBisTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel11.add(spacer4, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Zahl");
        panel10.add(label10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel12, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("von");
        panel12.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        personZahlVonTextField = new JTextField();
        panel12.add(personZahlVonTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("bis");
        panel12.add(label12, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        personZahlBisTextField = new JTextField();
        panel12.add(personZahlBisTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel12.add(spacer5, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel13, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel13.setBorder(BorderFactory.createTitledBorder(""));
        final JLabel label13 = new JLabel();
        label13.setText("Attribut");
        panel13.add(label13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        personSuchTextField2 = new JTextField();
        panel13.add(personSuchTextField2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        personComboBox2 = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        personComboBox2.setModel(defaultComboBoxModel4);
        panel13.add(personComboBox2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Regex");
        panel13.add(label14, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchNowButton = new JButton();
        searchNowButton.setText("SUCHE JETZT AUSFÜHREN");
        panel2.add(searchNowButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel2.add(spacer6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(2, 1, new Insets(10, 0, 0, 0), -1, -1));
        panel2.add(panel14, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel14.setBorder(BorderFactory.createTitledBorder("Attribute von Firma"));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel14.add(panel15, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel15.setBorder(BorderFactory.createTitledBorder(""));
        firmaComboBox1 = new JComboBox();
        panel15.add(firmaComboBox1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firmaSuchTextField1 = new JTextField();
        panel15.add(firmaSuchTextField1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Attribut");
        panel15.add(label15, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Regex");
        panel15.add(label16, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Datum");
        panel15.add(label17, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel15.add(panel16, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("von");
        panel16.add(label18, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firmaDatumVonTextField = new JTextField();
        panel16.add(firmaDatumVonTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("bis");
        panel16.add(label19, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firmaDatumBisTextField = new JTextField();
        panel16.add(firmaDatumBisTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel16.add(spacer7, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Zahl");
        panel15.add(label20, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel15.add(panel17, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("von");
        panel17.add(label21, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firmaZahlVonTextField = new JTextField();
        panel17.add(firmaZahlVonTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("bis");
        panel17.add(label22, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firmaZahlBisTextField = new JTextField();
        panel17.add(firmaZahlBisTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel17.add(spacer8, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel14.add(panel18, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel18.setBorder(BorderFactory.createTitledBorder(""));
        firmaComboBox2 = new JComboBox();
        panel18.add(firmaComboBox2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firmaSuchTextField2 = new JTextField();
        panel18.add(firmaSuchTextField2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), new Dimension(80, -1), null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("Attribut");
        panel18.add(label23, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("Regex");
        panel18.add(label24, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel19, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setText("Regex Suche in allen Feldern");
        panel19.add(label25, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel19.add(spacer9, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        hierRegexSuchbegriffEintippenTextField = new JTextField();
        hierRegexSuchbegriffEintippenTextField.setText("");
        panel19.add(hierRegexSuchbegriffEintippenTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        hilfeRegexButton = new JButton();
        hilfeRegexButton.setText("Hilfe Regex");
        panel19.add(hilfeRegexButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    static class PersonFirmaLine {

        private List<Object> elements;
        public CreamPersonData personData;
        public CreamFirmaData firmaData;
        public Note note;

        public PersonFirmaLine(CreamPersonData personData, CreamFirmaData firmaData, Note n) {
            this.personData = personData;
            this.firmaData = firmaData;
            this.note = n;
        }

        public List<Object> getElements() {
            if (elements == null) {
                extractElements();
            }
            assert (elements != null);
            return elements;
        }

        private void extractElements() {
            elements = new ArrayList<>();

            elements.add(false); // all entries are unmarked at the beginning THIS IS NOT USED ANY MORE

            // add attribs and tags of PERSON
            Map<Integer, CreamAttributeDescription> tagMap = AbstractConfiguration.getConfig().getPersonTagsOrderedDescription();
            Map<Integer, CreamAttributeDescription> attribMap = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription();

            for (int i = 0; i < attribMap.size(); i++) {
                CreamAttributeDescription description = attribMap.get(i);
                if (!"Tags".equals(description.attribName)) {
                    elements.add(personData.getAttr(description.attribName));
                } else {
                    for (int j = 0; j < tagMap.size(); j++) {
                        CreamAttributeDescription descriptionTag = tagMap.get(j);
                        if (personData.getAttr("Tags").value.contains(descriptionTag.attribName)) {
                            elements.add(descriptionTag.attribName);
                        } else {
                            elements.add("");
                        }
                    }
                }
            }

            // add attribs and tags of FIRMA
            tagMap = AbstractConfiguration.getConfig().getFirmaTagsOrderedDescription();
            attribMap = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription();

            for (int i = 0; i < attribMap.size(); i++) {
                CreamAttributeDescription description = attribMap.get(i);
                if (!"Tags".equals(description.attribName)) {
                    elements.add(firmaData.getAttr(description.attribName));
                } else {
                    for (int j = 0; j < tagMap.size(); j++) {
                        CreamAttributeDescription descriptionTag = tagMap.get(j);
                        if (firmaData.getAttr("Tags").value.contains(descriptionTag.attribName)) {
                            elements.add(descriptionTag.attribName);
                        } else {
                            elements.add("");
                        }
                    }
                }
            }
        }

        public void setSelected(boolean selected) {
            personData.setSelected(selected);
        }

        public boolean isSelected() {
            return personData.isSelected();
        }
    }

    List<PersonFirmaLine> getLinesFromNotes(List<Note> notes) {

        List<PersonFirmaLine> result = new ArrayList<>();
        for (Note n : notes) {
            assert (n.getContent() != null);
            CreamFirmaData firmaData = SyncHandler.get().getData(n);
            if (firmaData != null) {
                int number = 0;
                for (CreamPersonData personData : firmaData.persons) {
                    PersonFirmaLine e = new PersonFirmaLine(personData, firmaData, n);
                    e.getElements(); // to be able to set selected...
                    e.setSelected(personFirmaTableModel.isSelected(e.note.getGuid() + "__" + number++));

                    result.add(e);
                }
            }
        }
        personFirmaTableModel.recreateGUIDMap();
        return result;
    }

    class FirstNotMoveTableColumnModel extends DefaultTableColumnModel {

        @Override
        public void moveColumn(int from, int to) {
            super.moveColumn(from, to);
            if (from == 0 || to == 0) {
                super.moveColumn(to, from);
            }
        }
    }

    //
    // ALL THE FUCKING FILTERING
    //

    private class MyRowFilter extends RowFilter<Object, Object> {
        public boolean include(Entry entry) {


            int rowInModel = ((Integer) entry.getIdentifier()).intValue();
            CreamPersonData person = personFirmaTableModel.getPersonFirmaLinesList().get(rowInModel).personData;
            CreamFirmaData firma = personFirmaTableModel.getPersonFirmaLinesList().get(rowInModel).firmaData;

            String allRegex = hierRegexSuchbegriffEintippenTextField.getText();
            if (!"".equals(allRegex)) {
                if (!person.matchAllAttribs(allRegex) && !firma.matchAllAttribs(allRegex)) return false;
            }

            //
            //  complete Note related
            //
            // Find the regexes, that have a value. Check all. if one does not fit, return false.
            //if(regexMatchNeededAndFailed(hierRegexSuchbegriffEintippenTextField.getText(), note.getContent())) return false;

            //
            //  Person related
            //
            String[] split = sucheTagsPersonTextField.getText().split(";");
            for (int i = 0; i < split.length; i++) {
                String s = split[i].trim();
                if (!person.getAttr("Tags").value.contains(s)) return false;
            }
            if (regexMatchNeededAndFailed(personComboBox1, personSuchTextField1, person)) return false;
            if (dateMatchNeededAndFailed(personComboBox1, personDatumVonTextField, personDatumBisTextField, person))
                return false;
            if (numberMatchNeededAndFailed(personComboBox1, personZahlVonTextField, personZahlBisTextField, person))
                return false;
            if (regexMatchNeededAndFailed(personComboBox2, personSuchTextField2, person)) return false;


            //
            //  Firma related
            //
            split = sucheTagsFirmaTextField.getText().split(";");
            for (int i = 0; i < split.length; i++) {
                String s = split[i].trim();
                if (!firma.getAttr("Tags").value.contains(s)) return false;
            }
            if (regexMatchNeededAndFailed(firmaComboBox1, firmaSuchTextField1, firma)) return false;
            if (dateMatchNeededAndFailed(firmaComboBox1, firmaDatumVonTextField, firmaDatumBisTextField, firma))
                return false;
            if (numberMatchNeededAndFailed(firmaComboBox1, firmaZahlVonTextField, firmaZahlBisTextField, firma))
                return false;
            if (regexMatchNeededAndFailed(firmaComboBox2, firmaSuchTextField2, firma)) return false;

            return true;
        }


        private boolean regexMatchNeededAndFailed(JComboBox cb, JTextField tf, Object personOrFirma) {
            CreamAttributeDescription attributeDescription = (CreamAttributeDescription) cb.getSelectedItem();
            if (attributeDescription == null) return false;
            CreamAttributeData attributeData = null;
            if (personOrFirma instanceof CreamPersonData) {
                CreamPersonData p = (CreamPersonData) personOrFirma;
                attributeData = p.getAttr(attributeDescription.attribName);
            }
            if (personOrFirma instanceof CreamFirmaData) {
                CreamFirmaData f = (CreamFirmaData) personOrFirma;
                attributeData = f.getAttr(attributeDescription.attribName);
            }
            if (attributeData == null) return false;
            String regex = tf.getText();
            if ("".equals(regex) || regex == null) return false; // no match needed...
            return !RegexUtils.matchWithRegex(attributeData.value, regex);
        }

        private boolean dateMatchNeededAndFailed(JComboBox cb, JTextField tfFrom, JTextField tfTo, Object personOrFirma) {
            CreamAttributeDescription attributeDescription = (CreamAttributeDescription) cb.getSelectedItem();
            if (attributeDescription == null) return false;
            CreamAttributeData attributeData = null;
            if (personOrFirma instanceof CreamPersonData) {
                CreamPersonData p = (CreamPersonData) personOrFirma;
                attributeData = p.getAttr(attributeDescription.attribName);
            }
            if (personOrFirma instanceof CreamFirmaData) {
                CreamFirmaData f = (CreamFirmaData) personOrFirma;
                attributeData = f.getAttr(attributeDescription.attribName);
            }
            if (attributeData == null) return false;

            LocalDate from;
            LocalDate to;
            LocalDate valueDate;
            if (!("".equals(tfFrom.getText()) && "".equals(tfTo.getText()))) {
                from = getDate(tfFrom.getText());
                to = getDate(tfTo.getText());
                valueDate = getDate(attributeData.value);
            } else {
                return false;
            }
            if (valueDate == null) {
                return true;
            }
            if (from != null && to != null) {
                return !(valueDate.isAfter(from) && valueDate.isBefore(to));
            }
            if (from != null) {
                return !valueDate.isAfter(from);
            }
            if (to != null) {
                return !valueDate.isBefore(to);
            }
            return false;
        }


        private boolean numberMatchNeededAndFailed(JComboBox cb, JTextField tfFrom, JTextField tfTo, Object personOrFirma) {
            CreamAttributeDescription attributeDescription = (CreamAttributeDescription) cb.getSelectedItem();
            if (attributeDescription == null) return false;
            CreamAttributeData attributeData = null;
            if (personOrFirma instanceof CreamPersonData) {
                CreamPersonData p = (CreamPersonData) personOrFirma;
                attributeData = p.getAttr(attributeDescription.attribName);
            }
            if (personOrFirma instanceof CreamFirmaData) {
                CreamFirmaData f = (CreamFirmaData) personOrFirma;
                attributeData = f.getAttr(attributeDescription.attribName);
            }
            if (attributeData == null) return false;

            long from;
            long to;
            long valueNumber;
            if (!("".equals(tfFrom.getText()) && "".equals(tfTo.getText()))) {
                from = getNumber(tfFrom.getText());
                to = getNumber(tfTo.getText());
                valueNumber = getNumber(attributeData.value);
            } else {
                return false;
            }
            if (valueNumber == Long.MAX_VALUE) {
                return true;
            }
            if (from != Long.MAX_VALUE && to != Long.MAX_VALUE) {
                return !(valueNumber > from && valueNumber < to);
            }
            if (from != Long.MAX_VALUE) {
                return !(valueNumber > from);
            }
            if (to != Long.MAX_VALUE) {
                return !(valueNumber < to);
            }
            return false;
        }

        private long getNumber(String s) {
            long result = Long.MAX_VALUE;
            try {
                result = Long.parseLong(s);
            } catch (NumberFormatException e) {
                // ignore...
            }
            return result;
        }


        private LocalDate getDate(String s) {
            DateTimeFormatter formatter;
            LocalDate d = null;
            try {
                formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                d = LocalDate.parse(s, formatter);
            } catch (DateTimeParseException e) {
                try {
                    formatter = DateTimeFormatter.ofPattern("d.MM.yyyy");
                    d = LocalDate.parse(s, formatter);
                } catch (DateTimeParseException e1) {
                    try {
                        formatter = DateTimeFormatter.ofPattern("dd.M.yyyy");
                        d = LocalDate.parse(s, formatter);
                    } catch (DateTimeParseException e2) {
                        try {
                            formatter = DateTimeFormatter.ofPattern("d.M.yyyy");
                            d = LocalDate.parse(s, formatter);
                        } catch (DateTimeParseException e3) {
                            try {
                                formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
                                d = LocalDate.parse(s, formatter);
                            } catch (DateTimeParseException e4) {
                                try {
                                    formatter = DateTimeFormatter.ofPattern("d.MM.yy");
                                    d = LocalDate.parse(s, formatter);
                                } catch (DateTimeParseException e5) {
                                    try {
                                        formatter = DateTimeFormatter.ofPattern("dd.M.yy");
                                        d = LocalDate.parse(s, formatter);
                                    } catch (DateTimeParseException e6) {
                                        try {
                                            formatter = DateTimeFormatter.ofPattern("d.M.yy");
                                            d = LocalDate.parse(s, formatter);
                                        } catch (DateTimeParseException e7) {
                                            // finally failed...
                                            d = null;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return d;
        }
    }
}
