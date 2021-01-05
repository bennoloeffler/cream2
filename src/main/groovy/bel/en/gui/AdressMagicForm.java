package bel.en.gui;

import bel.en.data.*;
import bel.en.evernote.ENHelper;
import bel.en.localstore.SyncHandler;
import bel.util.AdressMagic;
import com.evernote.edam.type.Note;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Pair;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 *
 */

/* TEST DATA

Dr. Benno Löffler
Projektleiter
benno.loeffler@gmx.de
M: 0171 62 35 378
T: 0711 112 4711
F: +49 (0711) 111222 333
GMX GmbH
Adlerstr. 46
70199 Stuttgart
 */
@Log4j2
public class AdressMagicForm {
    private JPanel rootPanel;
    private JEditorPane editorPanePasteRaw;
    private JTable dataTableFirma;
    private JTable personTable;
    private JButton neueNotizButton;
    private JButton datenHinzufuegenButton;
    private JTextField searchTextField;
    private JEditorPane magicResultEditorPane;
    private JTable dataTablePerson;
    private JButton speichernButton;
    private NoteChooserForm noteChooserForm;
    private JButton abbrechenButton;
    private JButton oderVielleichtAusUnseremButton;
    //private JButton alleVorschlägeÜbernehmenButton;

    TableRowSorter<PersonFirmaTableModel_NEW> tableRowSorter;

    CreamFirmaData firmaData;
    CreamPersonData personData;

    //CreamFirmaData firmaDataBackup;
    //CreamPersonData personDataBackup;

    MagicPersonTableModel magicPersonTableModel;
    MagicFirmaTableModel magicFirmaTableModel;

    AdressMagic am;

    public AdressMagicForm() {

        $$$setupUI$$$();

        neueNotizButton.addActionListener(e -> {
            firmaData = new CreamFirmaData();
            firmaData.createAttributes();
            personData = new CreamPersonData();
            personData.createAttributes();
            firmaData.persons.add(personData);
            //noteChooserForm.getSelectionModel().clearSelection();
            //dataTablePerson.getSelectionModel().clearSelection();

            /*
            if (noteChooserForm.getSelectedNote() != null) {
                firmaData.setNote(noteChooserForm.getSelectedNote());
            }*/
            disableUntilNewDataSaved();
            magicFirmaTableModel.fireTableDataChanged();
            magicPersonTableModel.fireTableDataChanged();
        });

        datenHinzufuegenButton.addActionListener(e -> {
            if (firmaData != null && noteChooserForm.getSelectedNote() == null) {
                personData = new CreamPersonData();
                personData.createAttributes();
                firmaData.persons.add(personData);
                disableUntilNewDataSaved();
                magicPersonTableModel.fireTableDataChanged();
            } else if (firmaData == null && noteChooserForm.getSelectedNote() != null) {
                firmaData = new CreamFirmaData();
                firmaData.createAttributes();
                personData = new CreamPersonData();
                personData.createAttributes();
                firmaData.persons.add(personData);
                firmaData.setNote(noteChooserForm.getSelectedNote());
                disableUntilNewDataSaved();
            } else {
                log.error("ERROR with butten state...");
                //JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Sie müssen zuvor eine Firma auswählen...", "Keine Firma ausgewählt", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        datenHinzufuegenButton.setEnabled(false);

        editorPanePasteRaw.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchForHandyPLZAndEmail();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchForHandyPLZAndEmail();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchForHandyPLZAndEmail();
            }
        });
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setFilter();
            }
        });

        // was planned, but not yet realized...
        /*
        alleVorschlägeÜbernehmenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Person

                int rows = magicPersonTableModel.getRowCount();
                for(int i = 0; i< rows; i++) {
                    setValueToPerson(i, );
                }
            }
        });
        */

        speichernButton.addActionListener(e -> {
            Note n = noteChooserForm.getSelectedNote();
            if (n != null) {
                try {
                    if (!ENHelper.hasDataBlock(n.getContent())) {
                        String contentWithDataBlock = ENHelper.addDataBlockToNoteContent(n.getContent());
                        n.setContent(contentWithDataBlock);
                    }
                    //ENHelper.writeDataToNote(firmaData, n);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Bitte prüfen Sie die bearbeitete Adresse und wiederholen sie die Eingabe. Sorry...", "Fehler beim zurückschreiben", JOptionPane.ERROR_MESSAGE);

                }
            }

            SyncHandler.get().saveData(AdressMagicForm.this, firmaData);
            enableAfterNewDataSaved();
        });
        speichernButton.setEnabled(false);

        abbrechenButton.addActionListener(event -> {
            //speichernButton.setEnabled(true);

            firmaData = null;
            personData = null;
            SwingUtilities.invokeLater(() -> {
                noteChooserForm.getSelectionModel().clearSelection();
                personTable.getSelectionModel().clearSelection();
                setButtonsAccordingToSelection();
                enableAfterNewDataSaved();
            });
        });
        oderVielleichtAusUnseremButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiUtil.notYet();
            }
        });
    }


    private void disableUntilNewDataSaved() {

        speichernButton.setEnabled(true);


        //
        // everything else: Disable during data entry
        //
        noteChooserForm.setEnabled(false);
        personTable.setEnabled(false);
        datenHinzufuegenButton.setEnabled(false);
        neueNotizButton.setEnabled(false);
        searchTextField.setText("NICHTS AUSGEWÄHLT");
        searchTextField.setEnabled(false);

    }

    private void enableAfterNewDataSaved() {

        speichernButton.setEnabled(false);


        noteChooserForm.setEnabled(true);
        personTable.setEnabled(true);
        //datenHinzufuegenButton.setEnabled(true);
        neueNotizButton.setEnabled(true);
        searchTextField.setText("");
        searchTextField.setEnabled(true);
        firmaData = null;
        personData = null;
        noteChooserForm.getSelectionModel().clearSelection();
        setFilter();
        searchForHandyPLZAndEmail();
        magicPersonTableModel.fireTableDataChanged();
        magicFirmaTableModel.fireTableDataChanged();

    }

    private void setFilter() {
        tableRowSorter.setRowFilter(null);
        RowFilter<PersonFirmaTableModel_NEW, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter(searchTextField.getText());
        } catch (PatternSyntaxException e) {
            return;
        }
        tableRowSorter.setRowFilter(rf);
    }

    private void searchForHandyPLZAndEmail() {
        try {
            String text = editorPanePasteRaw.getText();
            am = new AdressMagic(text);
            String email = am.getEmail();
            String mobile = am.getMobile();
            mobile = mobile.replace("+", "\\+");
            String domain = am.getWww();
            //System.out.println(am);
            String searchRegex = "";
            if (!"".equals(email) && !"".equals(domain)) {
                searchRegex = email + "|" + domain;
            } else if (!"".equals(email)) {
                searchRegex = email;
            } else if (!"".equals(domain)) {
                searchRegex = domain;
            }
            String adressMagResult = "\nGEFUNDENE ADRESS-MAGIC-ELEMENTE:\n\n";
            adressMagResult += "\nFIRMEN-DATEN:\n";
            adressMagResult += "Firmen-Name: " + am.getCompany() + "\n";
            adressMagResult += "Straße: " + am.getStreetAndNr() + "\n";
            adressMagResult += "Postfach: " + am.getPostbox() + "\n";
            adressMagResult += "PLZ: " + am.getZipCode() + "\n";
            adressMagResult += "Ort: " + am.getTown() + "\n";
            adressMagResult += "Domain: " + am.getWww() + "\n";
            adressMagResult += "\n\n\n\n\n\nPERSONEN-DATEN:\n";
            adressMagResult += "Hr/Fr: " + am.getMrMrs() + "\n";
            adressMagResult += "Titel: " + am.getTitle() + "\n";
            adressMagResult += "Vorname: " + am.getChristianNames() + "\n";
            adressMagResult += "Nachname: " + am.getSurName() + "\n";
            adressMagResult += "Mobil: " + am.getMobile() + "\n";
            adressMagResult += "Tel: " + am.getPhone() + "\n";
            adressMagResult += "Fax: " + am.getFax() + "\n";
            adressMagResult += "Email: " + am.getEmail() + "\n";
            adressMagResult += "Funktion/Abt: " + am.getFunctionDepartment() + "\n";


            String finalAdressMagResult = adressMagResult;
            String finalSearchRegex = searchRegex;
            SwingUtilities.invokeLater(() -> {
                //editorPanePasteRaw.setText(finalText + finalAdressMagResult);
                magicResultEditorPane.setText(finalAdressMagResult);
                //if (datenHinzufuegenButton.isEnabled()) {
                searchTextField.setText(finalSearchRegex);
                setFilter();
                noteChooserForm.setFilterRegex(am.getSurName());
                //}
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        dataTableFirma = new CreamTable();
        personTable = new CreamTable_NEW();
        dataTableFirma = new CreamTable();
        dataTablePerson = new CreamTable();
    }

    public void initAfterDataAvailable() {

        editorPanePasteRaw.setSelectionStart(0);
        editorPanePasteRaw.setSelectionEnd(editorPanePasteRaw.getText().length());

        //
        // the person search and selection table
        //
        PersonFirmaTableModel_NEW pfm = new PersonFirmaTableModel_NEW();
        tableRowSorter = new TableRowSorter<>(pfm);
        personTable.setModel(pfm);
        personTable.setRowSorter(tableRowSorter);
        ((CreamTable_NEW) personTable).initAfterDataAvailable();
        personTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (((ListSelectionModel) e.getSource()).isSelectionEmpty()) {
                    //SyncHandler.get().setSelectedNote(this, null); //???
                    //neueNotizButton.setEnabled(false);
                    firmaData = null;
                    personData = null;
                } else {
                    int idx = personTable.convertRowIndexToModel(((ListSelectionModel) e.getSource()).getAnchorSelectionIndex());
                    PersonFirmaTableModel_NEW m = (PersonFirmaTableModel_NEW) personTable.getModel();
                    Pair<CreamPersonData, CreamFirmaData> pairAtRow = m.getPairAtRow(idx);
                    firmaData = pairAtRow.right;
                    personData = pairAtRow.left;
                    magicPersonTableModel.fireTableDataChanged();
                    magicFirmaTableModel.fireTableDataChanged();
                    //neueNotizButton.setEnabled(true);
                    //FilterMarkAndExportForm.PersonFirmaLine row = personFirmaTableModel.getRow(idx);
                    //SyncHandler.get().setSelectedNote(this, row.firmaData);
                    noteChooserForm.getSelectionModel().clearSelection();
                }
                setButtonsAccordingToSelection();
            }
        });

        noteChooserForm.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (((ListSelectionModel) e.getSource()).isSelectionEmpty()) {
                    //SyncHandler.get().setSelectedNote(this, null);
                    //neueNotizButton.setEnabled(false);
                } else {
                    firmaData = null;
                    personData = null;
                    magicPersonTableModel.fireTableDataChanged();
                    magicFirmaTableModel.fireTableDataChanged();
                    //neueNotizButton.setEnabled(false);
                    //datenHinzufuegenButton.setEnabled(true);
                    //FilterMarkAndExportForm.PersonFirmaLine row = personFirmaTableModel.getRow(idx);
                    //SyncHandler.get().setSelectedNote(this, row.firmaData);
                    personTable.getSelectionModel().clearSelection();
                }
                setButtonsAccordingToSelection();
            }

        });
        noteChooserForm.setShowOnlyNotesWithoutFirmaData(true);

        // now the magic tables...
        // PERSON
        //
        magicPersonTableModel = new MagicPersonTableModel();
        magicPersonTableModel.initAfterDataAvailable();
        dataTablePerson.setModel(magicPersonTableModel);

        dataTablePerson.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = dataTablePerson.rowAtPoint(evt.getPoint());
                int col = dataTablePerson.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 0) {
                    String selected = getSelectedInEditorPanes();
                    String magic = getMagicPersonSuggestion(row);
                    if (selected != null) {
                        setValueToPerson(row, selected);
                    } else if (magic != null && !magic.equals("")) {
                        setValueToPerson(row, magic);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        });

        // now the magic tables...
        // FIRMA
        //
        magicFirmaTableModel = new MagicFirmaTableModel();
        magicFirmaTableModel.initAfterDataAvailable();
        dataTableFirma.setModel(magicFirmaTableModel);

        dataTableFirma.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = dataTableFirma.rowAtPoint(evt.getPoint());
                int col = dataTableFirma.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 0) {
                    String selected = getSelectedInEditorPanes();
                    String magic = getMagicFirmaSuggestion(row);
                    if (selected != null) {
                        setValueToFirma(row, selected);
                    } else if (magic != null && !magic.equals("")) {
                        setValueToFirma(row, magic);
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        });

        //dataTablePerson.

        // Show the selections in magic table as suggestions
        editorPanePasteRaw.addCaretListener(e -> {
            magicPersonTableModel.fireTableDataChanged();
            magicFirmaTableModel.fireTableDataChanged();
        });
        magicResultEditorPane.addCaretListener(e -> {
            editorPanePasteRaw.setSelectionEnd(editorPanePasteRaw.getSelectionStart()); // the selection does not disappear, if magicPane is clicked...
            //editorPanePasteRaw.setSelectionEnd(0);
            magicPersonTableModel.fireTableDataChanged();
            magicFirmaTableModel.fireTableDataChanged();
        });

        noteChooserForm.initAfterDataAvailable();

    }

    private void setButtonsAccordingToSelection() {
        boolean personFirmaEmpty = personTable.getSelectionModel().isSelectionEmpty();
        boolean titleEmpty = noteChooserForm.getSelectionModel().isSelectionEmpty();
        if (personFirmaEmpty && titleEmpty) {
            datenHinzufuegenButton.setEnabled(false);
            neueNotizButton.setEnabled(true);
        } else if (personFirmaEmpty) { // and titleNOTempty
            datenHinzufuegenButton.setEnabled(true);
            neueNotizButton.setEnabled(false);
        } else if (titleEmpty) { // and personFirmaNOTempty
            datenHinzufuegenButton.setEnabled(true);
            neueNotizButton.setEnabled(false);
        } else {
            log.error("DESASTER... alle zwei Listen gleichzeitig selektiert...");
        }
    }

    /*
    private void createKeybindings(JTable table) {
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        table.getActionMap().put("Enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //do something on JTable enter pressed
            }
        });
    }*/

    private void setValueToPerson(int row, String value) {
        if (personData != null) {
            CreamAttributeData d = personData.getAttr(row);
            d.value = value;
            magicPersonTableModel.fireTableDataChanged();
        }
    }

    private void setValueToFirma(int row, String value) {
        if (firmaData != null) {
            CreamAttributeData d = firmaData.getAttr(row);
            d.value = value;
            magicFirmaTableModel.fireTableDataChanged();
        }
    }


    public JComponent getPanel() {
        return rootPanel;
    }

    public String getSelectedInEditorPanes() {
        String selected = editorPanePasteRaw.getSelectedText();
        if (selected != null) {
            //System.out.println("RAW: " + selected);
            return selected;
        } else {
            selected = magicResultEditorPane.getSelectedText();
            //System.out.println("MAGIC: " + selected);
            return selected;
        }
    }


    private String getMagicPersonSuggestion(int rowIndex) {
        if (am != null) {
            Map<Integer, CreamAttributeDescription> personAttributesOrderedDescription = AbstractConfiguration.getConfig().getPersonAttributesOrderedDescription();
            CreamAttributeDescription attributeDescription = personAttributesOrderedDescription.get(rowIndex);
            //String attribName =

            //if(attributeDescription.attribName.equals("Vorname"))
            switch (attributeDescription.attribName) {
                case "Anrede":
                    return am.getMrMrs();
                case "Titel":
                    return am.getTitle();
                case "Vorname":
                    return am.getChristianNames();
                case "Nachname":
                    return am.getSurName();
                case "Funktion":
                    return am.getFunctionDepartment();
                case "Abteilung":
                    return am.getFunctionDepartment();
                case "Mobil":
                    return am.getMobile();
                case "Festnetz":
                    return am.getPhone();
                case "Fax":
                    return am.getFax();
                case "Emails":
                    return am.getEmail();
                case "Notizen":
                    String notizen = "";
                    for (String unreg : am.getUnrecognized()) {
                        if (!notizen.equals("")) notizen += ", ";
                        notizen += unreg;
                    }
                    return notizen;
            }
        }
        return null;

    }

/*

Firmenname; zB Robert Bosch GmbH - EIN STANDORT PRO NOTIZ, USE CASE: AdressMagic
Domain; ideal im Format: firmaxy.de USE CASE: EmailToEvernoteHistory, AdressMagic
Tel. Zentrale;
Mitarbeiter; Anzahl der Mitarbeiter am Standort
Umsatz; in Mio EUR (STANDORT!). Konzernumsatz in die Notizen!
Straße; mit Hausnummer, USE CASE: AdressMagic
PLZ; USE CASE: AdressMagic
Ort; USE CASE: AdressMagic
Land; Schweiz? oder sonstwo?
Notizen; zB spezielle Infos zu Rechnungsadressen oder Anfahrt-Infos, etc
Postfach; nur wenn notwendig, USE CASE: AdressMagic

     */

    private String getMagicFirmaSuggestion(int rowIndex) {
        if (am != null) {
            Map<Integer, CreamAttributeDescription> firmaAttributesOrderedDescription = AbstractConfiguration.getConfig().getFirmaAttributesOrderedDescription();
            CreamAttributeDescription attributeDescription = firmaAttributesOrderedDescription.get(rowIndex);
            switch (attributeDescription.attribName) {
                case "Firmenname":
                    return am.getCompany();
                case "Domain":
                    return am.getWww();
                case "Tel. Zentrale":
                    return null;
                case "Straße":
                    return am.getStreetAndNr();
                case "PLZ":
                    return am.getZipCode();
                case "Ort":
                    return am.getTown();
                case "Postfach":
                    return am.getPostbox();
                case "Notizen":
                    String notizen = "";
                    for (String unreg : am.getUnrecognized()) {
                        if (!notizen.equals("")) notizen += ", ";
                        notizen += unreg;
                    }
                    return notizen;
            }
        }
        return null;

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
        rootPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(true);
        splitPane1.setDividerLocation(500);
        splitPane1.setDividerSize(10);
        splitPane1.setDoubleBuffered(true);
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setOrientation(0);
        rootPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setContinuousLayout(true);
        splitPane2.setDividerLocation(540);
        splitPane2.setDividerSize(10);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setResizeWeight(0.5);
        splitPane1.setLeftComponent(splitPane2);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setContinuousLayout(true);
        splitPane3.setDividerLocation(250);
        splitPane3.setDividerSize(10);
        splitPane3.setDoubleBuffered(true);
        splitPane3.setOneTouchExpandable(true);
        splitPane2.setRightComponent(splitPane3);
        final JSplitPane splitPane4 = new JSplitPane();
        splitPane4.setDividerLocation(250);
        splitPane4.setOrientation(0);
        splitPane3.setRightComponent(splitPane4);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setDoubleBuffered(true);
        splitPane4.setLeftComponent(scrollPane1);
        scrollPane1.setBorder(BorderFactory.createTitledBorder("6. Firmendaten übernehmen"));
        scrollPane1.setViewportView(dataTableFirma);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        splitPane4.setRightComponent(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder("7. Personendaten übernehmen"));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add(scrollPane2, BorderLayout.CENTER);
        scrollPane2.setViewportView(dataTablePerson);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        splitPane3.setLeftComponent(panel2);
        panel2.setBorder(BorderFactory.createTitledBorder("2. Hier erscheint dann die Magie..."));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel2.add(scrollPane3, BorderLayout.CENTER);
        magicResultEditorPane = new JEditorPane();
        magicResultEditorPane.setText("");
        scrollPane3.setViewportView(magicResultEditorPane);
        final JSplitPane splitPane5 = new JSplitPane();
        splitPane5.setDividerLocation(250);
        splitPane5.setOrientation(0);
        splitPane2.setLeftComponent(splitPane5);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        splitPane5.setRightComponent(panel3);
        panel3.setBorder(BorderFactory.createTitledBorder("3. falls nur eine Notiz - ohne Adresse - da ist: auswählen..."));
        noteChooserForm = new NoteChooserForm();
        panel3.add(noteChooserForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        splitPane5.setLeftComponent(panel4);
        panel4.setBorder(BorderFactory.createTitledBorder("1. Hier die Personen- und Adressdaten reinkopieren"));
        final JScrollPane scrollPane4 = new JScrollPane();
        panel4.add(scrollPane4, BorderLayout.CENTER);
        editorPanePasteRaw = new JEditorPane();
        editorPanePasteRaw.setDoubleBuffered(true);
        editorPanePasteRaw.setDropMode(DropMode.INSERT);
        editorPanePasteRaw.setSelectionStart(0);
        editorPanePasteRaw.setText("");
        scrollPane4.setViewportView(editorPanePasteRaw);
        oderVielleichtAusUnseremButton = new JButton();
        oderVielleichtAusUnseremButton.setText("... oder vielleicht aus unserem Outlook CRM Adressbuch importieren");
        panel4.add(oderVielleichtAusUnseremButton, BorderLayout.NORTH);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel5);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 7, new Insets(5, 5, 5, 5), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "4. falls zu existierender Adresse eine Person hinzugefügt werden soll: auswählen..."));
        neueNotizButton = new JButton();
        neueNotizButton.setText("5.a neue Notiz");
        neueNotizButton.setToolTipText("es wird eine neue Notiz mit den eingegebenen Daten erzeugt");
        panel6.add(neueNotizButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        datenHinzufuegenButton = new JButton();
        datenHinzufuegenButton.setText("5.b Daten hinzufügen");
        datenHinzufuegenButton.setToolTipText("die Daten werden zu der ausgewählten Notiz oder der ausgewählten Adresse hinzugefügt");
        panel6.add(datenHinzufuegenButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchTextField = new JTextField();
        panel6.add(searchTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Regex-Suche");
        panel6.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        speichernButton = new JButton();
        speichernButton.setText("8. speichern");
        panel6.add(speichernButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel6.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 1, false));
        abbrechenButton = new JButton();
        abbrechenButton.setText("abbrechen");
        abbrechenButton.setToolTipText("zurück zur Auswahl aus Notiz oder Person/Firma");
        panel6.add(abbrechenButton, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane5 = new JScrollPane();
        panel5.add(scrollPane5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(100, 50), null, null, 0, false));
        scrollPane5.setViewportView(personTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    abstract class MagicTableModel extends AbstractTableModel {


        String[] columns = {"Mausklick = übernehmen -->", "aktueller Wert", "Name Attribut ", "Erläuterung"};
        int rows = 0;

        abstract int initRowsCount();

        String[] getColumnsNames() {
            return columns;
        }

        public void initAfterDataAvailable() {
            rows = initRowsCount();
        }

        @Override
        public int getColumnCount() {
            return getColumnsNames().length;
        }

        @Override
        public String getColumnName(int column) {
            return getColumnsNames()[column];
        }

        @Override
        public int getRowCount() {
            return rows;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

    }

    class MagicPersonTableModel extends MagicTableModel {


        @Override
        int initRowsCount() {
            return AbstractConfiguration.getConfig().getPersonAttributesDescription().size();
        }


        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            if (personData == null) return "";

            switch (columnIndex) {
                case 0:
                    String selectedValue = getSelectedInEditorPanes();
                    if (selectedValue == null) {
                        selectedValue = getMagicPersonSuggestion(rowIndex);
                    }
                    if (selectedValue == null || "".equals(selectedValue)) {
                        return "---";
                    } else {
                        return selectedValue;
                    }
                case 1:
                    return personData.getAttr(rowIndex).value;
                case 2:
                    return personData.getAttr(rowIndex).description.attribName;
                case 3:
                    return personData.getAttr(rowIndex).description.help;
            }
            return "ERROR with rowIndex or columnIndex";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (personData != null) {
                CreamAttributeData d = personData.getAttr(rowIndex);
                d.value = (String) aValue;
            }
        }

    }

    class MagicFirmaTableModel extends MagicTableModel {


        @Override
        int initRowsCount() {
            return AbstractConfiguration.getConfig().getFirmaAttributesDescription().size();
        }


        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            if (personData == null) return "";

            switch (columnIndex) {
                case 0:
                    String selectedValue = getSelectedInEditorPanes();
                    if (selectedValue == null) {
                        selectedValue = getMagicFirmaSuggestion(rowIndex);
                    }
                    if (selectedValue == null || "".equals(selectedValue)) {
                        return "---";
                    } else {
                        return selectedValue;
                    }
                case 1:
                    return firmaData.getAttr(rowIndex).value;
                case 2:
                    return firmaData.getAttr(rowIndex).description.attribName;
                case 3:
                    return firmaData.getAttr(rowIndex).description.help;
            }
            return "ERROR with rowIndex or columnIndex";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (firmaData != null) {
                CreamAttributeData d = firmaData.getAttr(rowIndex);
                d.value = (String) aValue;
            }
        }
    }

}
