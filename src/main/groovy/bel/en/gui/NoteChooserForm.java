package bel.en.gui;

import bel.en.data.CreamDataListener;
import bel.en.data.CreamFirmaData;
import bel.en.localstore.SyncHandler;
import bel.util.StringSimilarity;
import com.evernote.edam.type.Note;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Select one Note.
 * Does almost everything on its own:
 * Connects to SyncHandlers CreamDataListener.
 * Sets selected Note, if user selects one.
 * You can also ask the panel for the selected one.
 */
@Log4j2
public class NoteChooserForm {

    public static final Marker CRASH_NIT = MarkerManager.getMarker("CRASH_NIT");


    private JTextField sucheTextField;
    private JList notesList;
    private JButton hifeZuRegExButton;
    private JCheckBox oneWordCheckBox;
    private JCheckBox keyPressedCheckBox;
    private JPanel noteChooserPanel;
    private JCheckBox ignoreCaseCheckBox;
    private JCheckBox volltextCheckBox;
    private JCheckBox unscharfCheckBox;
    private JSlider unscharfSlider;

    private DoFilterAfterActionListener l;
    private NoteListModel model;

    @Getter
    private Note selectedNote;

    @Setter
    private boolean showOnlyNotesWithoutFirmaData = false;


    public NoteChooserForm() { // in order to get it to palette in GUI designer
        $$$setupUI$$$();
        hifeZuRegExButton.addActionListener(e -> GuiUtil.regexHelp());
        //unscharfCheckBox.addChangeListener(e -> unscharfSlider.setEnabled(unscharfCheckBox.isEnabled()));
        //unscharfSlider.setEnabled(false);
        unscharfSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                model.filterNotesAndUpdateView();
            }
        });
    }


    public Component getPanel() {
        return noteChooserPanel;
    }


    public void initAfterDataAvailable() {

        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // get updates, when notes firmaData changed
        SyncHandler.get().addCreamDataListener(new CreamDataListener() {

            @Override
            public void dataChanged(Object origin) {
                model.setData(SyncHandler.get().readDataList());
                model.filterNotesAndUpdateView();
            }

            @Override
            public void selectionChanged(Object origin, CreamFirmaData data) {
                //log.trace(CRASH_NIT, "NoteChooserForm start");

                if (!origin.equals(NoteChooserForm.this)) {
                    if (data != null) {
                        notesList.setSelectedValue(data.getNote(), true);
                    }
                }
                //log.trace(CRASH_NIT, "NoteChooserForm finished");
            }
        });

        // do the search, when return in the textbox hit or mode is switched
        l = new DoFilterAfterActionListener();
        sucheTextField.addActionListener(l);
        oneWordCheckBox.addActionListener(l);
        ignoreCaseCheckBox.addActionListener(l);
        volltextCheckBox.addActionListener(l);
        unscharfCheckBox.addActionListener(l);

        // or if key is pressed...
        sucheTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (keyPressedCheckBox.isSelected()) {
                    model.filterNotesAndUpdateView();
                }
            }
        });

        model = new NoteListModel();
        notesList.setModel(model);
        notesList.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                //log.trace(CRASH_NIT, "SELECTED & not valueIsAdjusting");
                ListSelectionModel source = (ListSelectionModel) e.getSource();
                if (!source.isSelectionEmpty()) {
                    //log.trace(CRASH_NIT, "SELECTED & not selectionEmpty");
                    int selected = source.getAnchorSelectionIndex();
                    CreamFirmaData d = model.getNoteAt(selected);
                    //log.trace(CRASH_NIT, "SELECTED Note: " + (d == null ? "null" : d));
                    //System.out.println(d);
                    //System.out.println("new one one");
                    // in order to get the note - even if CreamFirmaData is empty...
                    if (d != null) {
                        selectedNote = d.getNote();
                        SyncHandler.get().setSelectedNote(NoteChooserForm.this, d);
                    } else {
                        log.warn("STRANGE. Selection not empty, but d == null...");
                    }

                } else {
                    selectedNote = null;
                }
            }
        });

    }

    /*
    public void clearSelection() {
        notesList.getSelectionModel().clearSelection();
        //selectedNote = null; should be set by listener...
    }*/

    public ListSelectionModel getSelectionModel() {
        return notesList.getSelectionModel();
    }

    public void setEnabled(boolean enabled) {
        notesList.setEnabled(enabled);
        sucheTextField.setEnabled(enabled);
    }

    public void setFilterRegex(String filterRegex) {
        sucheTextField.setText(filterRegex);
        model.filterNotesAndUpdateView();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        noteChooserPanel = new JPanel();
        noteChooserPanel.setLayout(new GridLayoutManager(3, 5, new Insets(5, 5, 5, 5), -1, -1));
        noteChooserPanel.setMinimumSize(new Dimension(100, -1));
        noteChooserPanel.setPreferredSize(new Dimension(-1, -1));
        sucheTextField = new JTextField();
        noteChooserPanel.add(sucheTextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(60, -1), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        noteChooserPanel.add(panel1, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        oneWordCheckBox = new JCheckBox();
        oneWordCheckBox.setText("ein Wort");
        panel1.add(oneWordCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ignoreCaseCheckBox = new JCheckBox();
        ignoreCaseCheckBox.setSelected(true);
        ignoreCaseCheckBox.setText(" GROSS=klein");
        panel1.add(ignoreCaseCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        volltextCheckBox = new JCheckBox();
        volltextCheckBox.setText("Volltext");
        panel1.add(volltextCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        keyPressedCheckBox = new JCheckBox();
        keyPressedCheckBox.setSelected(true);
        keyPressedCheckBox.setText("Taste");
        noteChooserPanel.add(keyPressedCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        noteChooserPanel.add(scrollPane1, new GridConstraints(2, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        notesList = new JList();
        scrollPane1.setViewportView(notesList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        noteChooserPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Suche");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hifeZuRegExButton = new JButton();
        hifeZuRegExButton.setText("Hilfe");
        panel2.add(hifeZuRegExButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unscharfCheckBox = new JCheckBox();
        unscharfCheckBox.setText("unscharf");
        noteChooserPanel.add(unscharfCheckBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        noteChooserPanel.add(spacer2, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        unscharfSlider = new JSlider();
        unscharfSlider.setInverted(false);
        unscharfSlider.setMinimum(30);
        unscharfSlider.setPaintLabels(true);
        unscharfSlider.setToolTipText("minimum: 30% maximum 100% = perfekte Passung");
        unscharfSlider.setValue(75);
        unscharfSlider.putClientProperty("JSlider.isFilled", Boolean.FALSE);
        unscharfSlider.putClientProperty("Slider.paintThumbArrowShape", Boolean.FALSE);
        noteChooserPanel.add(unscharfSlider, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return noteChooserPanel;
    }


    //
    // All the listeners
    //

    private class DoFilterAfterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.filterNotesAndUpdateView();
        }
    }

/*
    private class ListSelectionListener implements javax.swing.event.ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int idx = lsm.getMinSelectionIndex();  //e.getFirstIndex();
                    CreamFirmaData n = model.getNoteAt(idx);
                    //System.out.println("Selected: " + idx + "Note: " + n);
                    System.out.println("old one");
                    SyncHandler.get().setSelectedNote(NoteChooserForm.this, n);


                    selectedNote = n.getNote();
                } else {
                    selectedNote = null;
                }
            }
        }
    }
*/


    @FunctionalInterface
    interface Filter {
        String filter(String toFilter);
        //F doReverse(T from); // try it. Compiler error...
    }

    //
    // the list model and filter logic.
    //

    private class NoteListModel extends AbstractListModel {

        private List<CreamFirmaData> allNotes = new ArrayList<>();
        private List<CreamFirmaData> filteredNotes = new ArrayList<>();


        public void setData(List<CreamFirmaData> allNotes) {
            this.allNotes = allNotes;
            filterNotesAndUpdateView();
        }

        @Override
        public int getSize() {
            return filteredNotes.size();
        }

        @Override
        public Object getElementAt(int index) {
            CreamFirmaData data = filteredNotes.get(index);
            return data.getNote().getTitle();
        }

        /*
        private String doFiltering(Filter dataFile,) {
            return dataFile.filter(str)
        }*/

        private void filterNotesAndUpdateView() {

            List<CreamFirmaData> prefiltered = new ArrayList<>();
            if (showOnlyNotesWithoutFirmaData) {
                for (CreamFirmaData fd : allNotes) {
                    if (fd.numberOfAttribs() == 0) { // just the note... nothing else
                        prefiltered.add(fd);
                    }
                }
                //prefiltered  = allNotes.stream().filter(creamFirmaData -> creamFirmaData.numberOfAttribs() == 0).collect(Collectors.toList());
            } else {
                prefiltered = allNotes;
            }


            List<CreamFirmaData> result = new ArrayList<>();
            if (!sucheTextField.getText().equals("")) {
                for (CreamFirmaData n : prefiltered) {
                    String regExStr;
                    if (oneWordCheckBox.isSelected()) {
                        regExStr = ".*\\b" + sucheTextField.getText() + "\\b.*";
                    } else {
                        regExStr = ".*(" + sucheTextField.getText() + ").*";
                    }
                    if (ignoreCaseCheckBox.isSelected()) {
                        regExStr = "(?i)" + regExStr;
                    }

                    //
                    // FIRST search in title
                    //
                    if (unscharfCheckBox.isSelected()) {
                        StringSimilarity.findBestSimilarityByEqualLenthMatch(sucheTextField.getText(), n.getNote().getTitle());
                        double sim = StringSimilarity.bestLastSimilarity;
                        double lim = unscharfSlider.getValue() / 100d;
                        //System.out.println("sim: " + sim + "  lim: " + lim);
                        if (sim >= lim) {
                            result.add(n);
                        }
                    } else {
                        if (n.getNote().getTitle().matches(regExStr)) {
                            result.add(n);
                        }
                    }

                    //
                    // search the whole note
                    //

                    if (volltextCheckBox.isSelected()) {
                        if (unscharfCheckBox.isSelected()) {
                            StringSimilarity.findBestSimilarityByEqualLenthMatch(sucheTextField.getText(), n.getNote().getContent());
                            double sim = StringSimilarity.bestLastSimilarity;
                            double lim = unscharfSlider.getValue() / 100d;
                            //System.out.println(" VOLLTEXT: sim: " + sim + "  lim: " + lim);
                            if (sim >= lim) {
                                if (!result.contains(n)) {
                                    result.add(n);
                                }
                            }
                        } else {
                            if (n.getNote().getContent().matches(regExStr)) {
                                // schon drin?
                                if (!result.contains(n)) {
                                    result.add(n);
                                }
                            }
                        }

                    }


                }
            } else {
                result = prefiltered;
            }
            filteredNotes = result;

            /*
            if(filteredNotes.size()> 0) {
                fireContentsChanged(this, 0, filteredNotes.size() - 1);
            } else {
                //fireIntervalRemoved(this, 0, allNotes.size()-1);
                notesList.repaint();
            }*/

            // TODO: this is an ugly, maybe unperformant workaround. Did not get it to work with fireEvents. See above.
            notesList.setModel(new DefaultListModel());
            notesList.setModel(this);

        }


        public CreamFirmaData getNoteAt(int idx) {
            return filteredNotes.get(idx);
        }
    }


}
