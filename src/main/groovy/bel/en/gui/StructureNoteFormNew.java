package bel.en.gui;

import bel.en.MainGUI;
import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamFirmaData;
import bel.en.data.CreamUserData;
import bel.en.evernote.ENAuth;
import bel.en.evernote.ENConfiguration;
import bel.en.evernote.ENConnection;
import bel.en.evernote.ENSharedNotebook;
import bel.en.gui.abo.AboController;
import bel.en.gui.abo.AboForm;
import bel.en.gui.dataimport.DataImportController;
import bel.en.gui.dataimport.DataImportForm;
import bel.en.gui.tel.TelefonListViewController;
import bel.en.gui.tel.TelefonListViewForm;
import bel.en.localstore.NoteStoreLocal;
import bel.en.localstore.SyncHandler;
import bel.learn._14_timingExecution.RunTimer;
import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.Note;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * Entry point for GUI
 */
@Log4j2
public class StructureNoteFormNew {

    //
    // GUI generated members
    //
    private JPanel rootPanel;
    private JTabbedPane tabbedPane;
    private JTable personenTable;
    private JPanel enmlPanel;
    private JPanel firmaPersonPanel;
    private JPanel adressMagic;
    private JTextField statusTextField;
    private JButton sichernButton;
    private JTable firmaTable;
    private JSplitPane firmaPersonSplitPane;
    private JProgressBar progressBar;
    private JTextField syncIntervalTextFlield;
    private JTextArea syncProtocollTextArea;
    private JButton syncEvernoteJetztButton;
    private JPanel extendedSearchExportPanel;
    private JPanel selectOnePanel;
    private JSplitPane firmaSuchfeldDivider;
    private JScrollPane personScrollPane;
    private JScrollPane firmaScrollPane;
    private JPanel helpAndLicensePanel;
    private JPanel telefonPanel;
    private JPanel angebotePanel;
    private JPanel aboPanel;
    private JPanel auth;
    private JButton loginBeiEvernoteButton;
    private JPanel enAuthPanel;
    private JPanel debug;
    private JButton logoutButton;
    private JPanel dataImport;

    private JFrame frame;

    //
    // my own members. also forms, that are reused... Because unable to put them to the palette...
    //
    private FilterMarkAndExportForm filterMarkAndExportForm;
    private EnmlForm enmlForm;
    private NoteChooserForm noteChooserForm;
    private HelpAndLicenseForm helpAndLicenseForm;
    private AdressMagicForm adressMagicForm;
    private TelefonListViewForm telefonForm;
    private AngeboteForm angeboteForm;
    private AboForm aboForm;
    private DebugForm debugForm;
    private DataImportForm dataImportForm;


    //
    // Data related stuff
    //

    private final Syncer syncer;
    private FirmaTableModel firmaTableModel = new FirmaTableModel();
    private PersonTableModel personTableModel = new PersonTableModel();


    //
    // Evernote related...
    //
    //private ENConnection enConnection;
    private ENSharedNotebook enSharedConfigNotebook;

    /**
     * holding firmaData
     */
    private CreamFirmaData creamFirmaData = null;
    private Note currentNote = null;


    private void createUIComponents() {
        filterMarkAndExportForm = new FilterMarkAndExportForm();
        extendedSearchExportPanel = filterMarkAndExportForm.getPanel();
        personenTable = new CreamTable();
        firmaTable = new CreamTable();
    }


    /**
     * this is the main GUI class with the action handlers.
     */
    public StructureNoteFormNew() {

        $$$setupUI$$$();

        //
        // init the frame
        //

        frame = new JFrame("CREAM");
        URL iconURL = getClass().getResource("logo_small.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            frame.setIconImage(icon.getImage());
        } else {
            log.error("did not find logo for frame: logo_small.png");
        }
        frame.getContentPane().add(this.rootPanel);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(1000, 700));
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        //
        // init login. get autologin firmaData
        //

        /*
        loginSpeichernUndAutoCheckBox.setSelected("on".equals(
                MainGUI.properties.getProperty("cream.autologin", "off")));
        if (!loginSpeichernUndAutoCheckBox.isSelected()) {
            MainGUI.properties.setProperty("cream.evernote.username", Util.rot13("hier das evernote login eintragen"));
            MainGUI.properties.setProperty("cream.evernote.password", Util.rot13(""));
        }

        usernameTextField.setText(Util.rot13(
                MainGUI.properties.getProperty("cream.evernote.username",
                        "hier das evernote login eintragen")));
        passwordTextField.setText(Util.rot13(
                MainGUI.properties.getProperty("cream.evernote.password",
                        "")));

        usernameTextField.grabFocus();
        usernameTextField.selectAll();
*/
        //
        // init syncing
        //

        syncer = new Syncer(this);
        syncIntervalTextFlield.setText(MainGUI.properties.getProperty("cream.sync_intervall", "15"));
        syncer.setSyncTimer(syncIntervalTextFlield.getText());

        //
        // init some gui components
        //

        progressBar.setVisible(false);
        personenTable.setModel(personTableModel);
        firmaTable.setModel(firmaTableModel);
        statusTextField.setEnabled(false);

        //
        //Register Listeners and implement logic
        //

        //ActionListener loginActionListener = new LoginActionListener();

        //loginButton.addActionListener(loginActionListener);

        sichernButton.addActionListener(e -> writeFirmaPersonDataToStore());

        syncEvernoteJetztButton.addActionListener(syncer.getSyncActionListener());

        //sichernButton.addActionListener(e -> AutofitTableColumns.autoResizeTable(personenTable, true));
        //sichernButton.addActionListener(e -> AutofitTableColumns.autoResizeTable(firmaTable, true));

        firmaTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                writeFirmaPersonDataToStore();
            }
        });

        personenTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                writeFirmaPersonDataToStore();
            }
        });

        syncIntervalTextFlield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                syncer.setSyncTimer(syncIntervalTextFlield.getText());

            }
        });

        /*
        loginSpeichernUndAutoCheckBox.addActionListener(e -> {
            if (loginSpeichernUndAutoCheckBox.isSelected()) {
                MainGUI.properties.setProperty("cream.autologin", "on");
            } else {
                MainGUI.properties.setProperty("cream.autologin", "off");
                MainGUI.properties.setProperty("cream.evernote.username", Util.rot13("hier das evernote login eintragen"));
                MainGUI.properties.setProperty("cream.evernote.password", Util.rot13(""));
                MainGUI.saveProperties();
            }
        });
*/

        //
        // add all other forms
        //
        noteChooserForm = new NoteChooserForm();
        selectOnePanel.add(noteChooserForm.getPanel());

        helpAndLicenseForm = new HelpAndLicenseForm();
        helpAndLicensePanel.add(helpAndLicenseForm.getPanel());

        adressMagicForm = new AdressMagicForm();
        adressMagic.add(adressMagicForm.getPanel());

        enmlForm = new EnmlForm();
        enmlPanel.add(enmlForm.getPanel());

        telefonForm = new TelefonListViewForm();
        telefonPanel.add(telefonForm.getPanel());

        angeboteForm = new AngeboteForm();
        angebotePanel.add(angeboteForm.getPanel());

        aboForm = new AboForm();
        aboPanel.add(aboForm.getPanel());

        debugForm = new DebugForm();
        debug.add(debugForm.getPanel());

        dataImportForm = new DataImportForm();
        dataImport.add(dataImportForm.getPanel());


        //
        // Key Accellerators
        //
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_T);
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_A);
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_E);
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_S);
        //
        // init version
        //
        helpAndLicenseForm.setVersionLabel(MainGUI.VERSION_STRING);

        /*
        if ("on".equals(MainGUI.properties.getProperty("cream.autologin", "off"))) {
            SwingUtilities.invokeLater(() -> loginActionListener.actionPerformed(null));
        }*/

        loginBeiEvernoteButton.addActionListener(e -> {
            GuiUtil.notYet();
            /*
            try {

                // token in properties will be overwritten
                SwingWorker<String, String> answer = ENAuth.get().authOverEvernoteWebsite(enAuthPanel);

                while (!answer.isDone()) {
                    Thread.sleep(500);
                    log.info("waiting for auth form to complete...");
                }
                //String authToken = answer.get();
                log.info("FINISHED: now apply login data...");

                // renew ENAuth
                ENAuth.get(MainGUI.getProperties(), EvernoteService.PRODUCTION);
                ENAuth.get().connectToEvernote();


            } catch (Exception ex) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "UUPS. Das ging schief...");

            }
            */
        });

        logoutButton.addActionListener(e -> {
            logoutCurrentUser();
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Neustart von CREAM ist erforderlich...", "Logout ok", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void logoutCurrentUser() {
        MainGUI.getProperties().remove(ENAuth.EVERNOTE_TOKEN);
        ENAuth.get(MainGUI.getProperties(), EvernoteService.PRODUCTION);
        ENConnection.from("");
        ENConnection.get().connect();
    }


    private void initAfterDataAvailable() {

        Timer t = new Timer(60 * 1000, (e) -> updateTitle());
        t.setRepeats(true);
        t.start();
        updateTitle();

        //
        // init the other forms, that rely on a filled SyncHandler
        //
        filterMarkAndExportForm.initAfterDataAvailable();
        noteChooserForm.initAfterDataAvailable();
        enmlForm.initAfterDataAvailable();
        adressMagicForm.initAfterDataAvailable();
        angeboteForm.initAfterDataAvailable();
        new TelefonListViewController(telefonForm).initAfterDataIsAvailable();
        new AboController(aboForm).initAfterDataIsAvailable();
        new DataImportController(dataImportForm); // no init needed... TODO: read datamodel and have a look at binding...

        firmaTableModel.initAfterDataIsAvailable();
        personTableModel.initAfterDataAvailable();
        ((CreamTable) personenTable).initAfterDataAvailable(AbstractConfiguration.getConfig().getPersonTagsOrderedDescription(), CreamTable.LAST_COLUMN_IS_TAG);
        ((CreamTable) firmaTable).initAfterDataAvailable(AbstractConfiguration.getConfig().getFirmaTagsOrderedDescription(), CreamTable.LAST_ROW_IS_TAG);


        //
        // configure all the rest
        //
        personenTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        AutofitTableColumns.autoResizeTable(personenTable, true);

        firmaSuchfeldDivider.setDividerLocation(0.5);
        firmaPersonSplitPane.setDividerLocation(0.5);
    }

    public void updateTitle() {
        CreamUserData u = AbstractConfiguration.getConfig().getCurrentUser();
        String shortName = u.getShortName();
        String realName = u.getCompleteName();
        String mail = u.getEmail();


        String bName = "keine Information";
        String connected = "OFFLINE :-(";
        if (ENConnection.get() != null && ENConnection.get().isConnected()) {
            try {
                bName = ENConnection.get().getUserStoreClient().getUser().getAccounting().getBusinessName();
                connected = "ONLINE :-)";
            } catch (Exception e) {
                //log.info("Offline because of: " + e.getMessage());
            }
        }


        frame.setTitle("CREAM          " +
                "Anwender: " + shortName + ",  " + realName + ",  " + mail + "          " +
                "Business: " + bName + "          " +
                "Status: " + connected + "          " +
                "Version: " + MainGUI.VERSION_STRING);
    }


    private void writeFirmaPersonDataToStore() {
        try {
            if (creamFirmaData != null) {
                if (currentNote == null) return;

                SyncHandler.get().saveData(this, creamFirmaData);
            }
        } catch (Exception e1) {
            System.out.println("Fehler beim schreiben: " + e1);
            //throw new Exception("Error writing Note: " + e1);
        }
    }

    void statusMessage(String message) {
        statusTextField.setText(message);
        new HighlightAndFade(statusTextField, Color.RED);

    }

    JProgressBar getProgressBar() {
        return progressBar;
    }

    JTextArea getSyncProtocollTextArea() {
        return syncProtocollTextArea;
    }

    JButton getSyncEvernoteJetztButton() {
        return syncEvernoteJetztButton;
    }


    public void loadConfigAndDataInitGuiAndSync() {
        statusMessage("Starting...");
            /*
            if (loginSpeichernUndAutoCheckBox.isSelected()) {
                MainGUI.properties.setProperty("cream.evernote.username", Util.rot13(
                        usernameTextField.getText().trim()));
                MainGUI.properties.setProperty("cream.evernote.password", Util.rot13(
                        String.valueOf(passwordTextField.getPassword()).trim()));
            }*/
        SwingUtilities.invokeLater(() -> {
            try {

                ENConfiguration configuration = null;
                try {
                    statusMessage("Connecting to Evernote. May take a second or two...");

                    // Connection already done in GUIMain
                    if (ENConnection.get() != null && ENConnection.get().isConnected()) {
                        enSharedConfigNotebook = new ENSharedNotebook(ENConnection.get(), "C__CONFIG");//Main.CONFIG_NOTEBOOK_SHARE_NAME);
                        ENConfiguration.CONFIG_TITLE_STRING = "CREAM_CONFIG_NOTE";
                        configuration = new ENConfiguration(enSharedConfigNotebook, ENConnection.get());
                    }
                } catch (Exception e1) {
                    log.catching(e1);
                    throw e1;
                    //statusMessage("PROBLEM beim verbinden! ERROR");
                    //enSharedConfigNotebook = null;
                    //configuration = null;
                }

                statusMessage("Reading local notes...");

                log.info("starting to read from harddisk...");
                RunTimer t = new RunTimer();
                SyncHandler.init(ENConnection.get(), new NoteStoreLocal(configuration));
                t.stop("reading from harddisk");
                initAfterDataAvailable();
                //SyncHandler.get().setSelectedNote(this, null);
                SyncHandler.get().fireDataChange(StructureNoteFormNew.this);
                SyncHandler.get().fireSelectionChanged(StructureNoteFormNew.this);
                syncer.syncEvernote();

            } catch (Exception e1) {
                log.catching(e1);
                String explain = ""; //"Vielleicht haben Sie sich als User eingeloggt,\nder keinen Zugriff auf die notwendigen\nCREAM-Notizbücher hat.\nSie werden ausgeloggt.";
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "CREAM wird beendet. FEHLER:\n" + e1.getMessage() + "\n\n" + explain, "FEHLER", JOptionPane.ERROR_MESSAGE);
                logoutCurrentUser();
                System.exit(-1);
            }

        });
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
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.setMaximumSize(new Dimension(-1, -1));
        rootPanel.setMinimumSize(new Dimension(-1, -1));
        rootPanel.setPreferredSize(new Dimension(-1, -1));
        tabbedPane = new JTabbedPane();
        rootPanel.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        angebotePanel = new JPanel();
        angebotePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Angebote & HOT", angebotePanel);
        angebotePanel.setBorder(BorderFactory.createTitledBorder(""));
        telefonPanel = new JPanel();
        telefonPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("TODO & Telefon", telefonPanel);
        adressMagic = new JPanel();
        adressMagic.setLayout(new BorderLayout(0, 0));
        adressMagic.setMaximumSize(new Dimension(-1, -1));
        adressMagic.setMinimumSize(new Dimension(-1, -1));
        adressMagic.setPreferredSize(new Dimension(-1, -1));
        tabbedPane.addTab("Adress-Import", adressMagic);
        firmaPersonPanel = new JPanel();
        firmaPersonPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        firmaPersonPanel.setMaximumSize(new Dimension(-1, -1));
        firmaPersonPanel.setMinimumSize(new Dimension(-1, -1));
        firmaPersonPanel.setPreferredSize(new Dimension(-1, -1));
        tabbedPane.addTab("Editor", firmaPersonPanel);
        firmaPersonSplitPane = new JSplitPane();
        firmaPersonSplitPane.setContinuousLayout(true);
        firmaPersonSplitPane.setDividerLocation(493);
        firmaPersonSplitPane.setOrientation(0);
        firmaPersonPanel.add(firmaPersonSplitPane, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel1.setMaximumSize(new Dimension(-1, -1));
        panel1.setMinimumSize(new Dimension(-1, -1));
        panel1.setPreferredSize(new Dimension(-1, -1));
        firmaPersonSplitPane.setRightComponent(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder("Person"));
        personScrollPane = new JScrollPane();
        personScrollPane.setDoubleBuffered(true);
        personScrollPane.setHorizontalScrollBarPolicy(30);
        personScrollPane.setInheritsPopupMenu(false);
        personScrollPane.setVerticalScrollBarPolicy(20);
        panel1.add(personScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        personenTable.setAutoResizeMode(4);
        personenTable.setDoubleBuffered(true);
        personenTable.setFillsViewportHeight(true);
        personenTable.setIntercellSpacing(new Dimension(3, 3));
        personenTable.setMaximumSize(new Dimension(300000, 300000));
        personenTable.setMinimumSize(new Dimension(100, 100));
        personenTable.setPreferredScrollableViewportSize(new Dimension(1000, 500));
        personenTable.setPreferredSize(new Dimension(300, 300));
        personScrollPane.setViewportView(personenTable);
        firmaSuchfeldDivider = new JSplitPane();
        firmaSuchfeldDivider.setDividerLocation(394);
        firmaSuchfeldDivider.setMaximumSize(new Dimension(-1, -1));
        firmaSuchfeldDivider.setMinimumSize(new Dimension(-1, -1));
        firmaSuchfeldDivider.setPreferredSize(new Dimension(-1, -1));
        firmaPersonSplitPane.setLeftComponent(firmaSuchfeldDivider);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel2.setMinimumSize(new Dimension(-1, -1));
        firmaSuchfeldDivider.setLeftComponent(panel2);
        panel2.setBorder(BorderFactory.createTitledBorder("Firma"));
        firmaScrollPane = new JScrollPane();
        firmaScrollPane.setDoubleBuffered(true);
        firmaScrollPane.setHorizontalScrollBarPolicy(30);
        firmaScrollPane.setInheritsPopupMenu(false);
        firmaScrollPane.setVerticalScrollBarPolicy(20);
        panel2.add(firmaScrollPane, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        firmaTable.setAutoResizeMode(0);
        firmaTable.setDoubleBuffered(true);
        firmaTable.setEnabled(true);
        firmaTable.setFillsViewportHeight(true);
        firmaTable.setMaximumSize(new Dimension(300000, 300000));
        firmaTable.setMinimumSize(new Dimension(100, 100));
        firmaTable.setPreferredScrollableViewportSize(new Dimension(1600, 400));
        firmaTable.setPreferredSize(new Dimension(1600, 300));
        firmaTable.setShowHorizontalLines(true);
        firmaTable.setShowVerticalLines(true);
        firmaScrollPane.setViewportView(firmaTable);
        sichernButton = new JButton();
        sichernButton.setEnabled(true);
        sichernButton.setText("lokal speichern                 (oder einfach dieses Fenster verlassen...)");
        panel2.add(sichernButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectOnePanel = new JPanel();
        selectOnePanel.setLayout(new BorderLayout(0, 0));
        selectOnePanel.setMaximumSize(new Dimension(-1, -1));
        selectOnePanel.setMinimumSize(new Dimension(-1, -1));
        selectOnePanel.setPreferredSize(new Dimension(-1, -1));
        firmaSuchfeldDivider.setRightComponent(selectOnePanel);
        extendedSearchExportPanel.setMaximumSize(new Dimension(-1, -1));
        extendedSearchExportPanel.setMinimumSize(new Dimension(-1, -1));
        extendedSearchExportPanel.setPreferredSize(new Dimension(-1, -1));
        tabbedPane.addTab("Suche &  Export", extendedSearchExportPanel);
        enmlPanel = new JPanel();
        enmlPanel.setLayout(new BorderLayout(0, 0));
        enmlPanel.setMaximumSize(new Dimension(-1, -1));
        enmlPanel.setMinimumSize(new Dimension(-1, -1));
        enmlPanel.setPreferredSize(new Dimension(-1, -1));
        tabbedPane.addTab("Versions-Ansicht", enmlPanel);
        aboPanel = new JPanel();
        aboPanel.setLayout(new BorderLayout(0, 0));
        aboPanel.setToolTipText("tut noch nicht...");
        tabbedPane.addTab("Abos", aboPanel);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setMaximumSize(new Dimension(-1, -1));
        panel3.setMinimumSize(new Dimension(-1, -1));
        panel3.setPreferredSize(new Dimension(-1, -1));
        tabbedPane.addTab("Sync", panel3);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        syncProtocollTextArea = new JTextArea();
        scrollPane1.setViewportView(syncProtocollTextArea);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Sync-Intervall [Minuten]");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        syncIntervalTextFlield = new JTextField();
        syncIntervalTextFlield.setText("15");
        panel4.add(syncIntervalTextFlield, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(50, -1), null, 0, false));
        helpAndLicensePanel = new JPanel();
        helpAndLicensePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Doku & Danke", helpAndLicensePanel);
        auth = new JPanel();
        auth.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Login Evernote", auth);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        auth.add(panel5, BorderLayout.NORTH);
        panel5.setBorder(BorderFactory.createTitledBorder("Das passiert auf der Evernote Website und zwar gilt das login erstmal für ein Jahr... Kann aber geändert werden."));
        loginBeiEvernoteButton = new JButton();
        loginBeiEvernoteButton.setText("change  bei Evernote");
        panel5.add(loginBeiEvernoteButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel5.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        logoutButton = new JButton();
        logoutButton.setText("Logout");
        panel5.add(logoutButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enAuthPanel = new JPanel();
        enAuthPanel.setLayout(new BorderLayout(0, 0));
        auth.add(enAuthPanel, BorderLayout.CENTER);
        dataImport = new JPanel();
        dataImport.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Daten-Import", dataImport);
        debug = new JPanel();
        debug.setLayout(new BorderLayout(0, 0));
        tabbedPane.addTab("Debug", debug);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        statusTextField = new JTextField();
        statusTextField.setEditable(false);
        panel6.add(statusTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        progressBar = new JProgressBar();
        panel6.add(progressBar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        syncEvernoteJetztButton = new JButton();
        syncEvernoteJetztButton.setText("Sync Evernote... Jetzt");
        panel6.add(syncEvernoteJetztButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
