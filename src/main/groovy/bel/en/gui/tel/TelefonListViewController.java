package bel.en.gui.tel;

import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamDataListener;
import bel.en.data.CreamFirmaData;
import bel.en.evernote.ENHelper;
import bel.en.evernote.ENSharedNotebook;
import bel.en.gui.CreamTable_NEW;
import bel.en.gui.GuiUtil;
import bel.en.gui.PersonFirmaTableModel_NEW;
import bel.en.localstore.SyncHandler;
import bel.util.HtmlToPlainText;
import bel.util.enml.TextToEnml;
import com.evernote.edam.type.Note;
import com.syncthemall.enml4j.ENMLProcessor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Translates between SyncHandler and the view
 */
@Log4j2
@FieldDefaults(level= AccessLevel.PRIVATE)
public class TelefonListViewController {

    TelefonListViewForm f;
    TODOTableModel todoTableModel;
    PersonFirmaTableModel_NEW personFirmaTableModel;
    Color colorBackgroundOriginal = null;
    Note note; // the current todo-belongs to this note
    private TODOTableModel.CreamTodo selectedTODO;
    private CreamFirmaData firma;

    public TelefonListViewController(TelefonListViewForm form) {
        this.f = form;
        registerButtonListeners();
        f.getOutlookButton().setEnabled(false);
    }

    // only to preserve the selection...
    private void updateTodoModel() {
        //selectedTODO = todoTableModel.getTODO(idx);
        todoTableModel.update();
        SwingUtilities.invokeLater( () -> {
            if (selectedTODO != null) {
                int rowNow = todoTableModel.indexOf(selectedTODO);
                //System.out.println("rowNow: " + rowNow);
                if(rowNow>=0) {
                    int idxNow = f.getTodoTable().convertRowIndexToView(rowNow);
                    f.getTodoTable().getSelectionModel().setSelectionInterval(idxNow, idxNow);
                    //System.out.println("FOUND for reselect: " + selectedTODO);

                } else {
                    //System.out.println("_NOT_ FOUND for reselect: " + selectedTODO);
                }
            }
        });
    }


    public void initAfterDataIsAvailable() {

        // refresh timer
        //Timer t = new Timer(10 * 1000, e -> updateTodoModel());
        //t.setRepeats(true);
        //t.start();

        //
        // theTodo table
        //
        todoTableModel = new TODOTableModel();
        ((CreamTable_NEW)f.getTodoTable()).initAfterDataAvailable();
        f.getTodoTable().setModel(todoTableModel);
        f.getTodoTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        //
        // sort theTodoTable by first columnn - and fix it.
        //
        TableRowSorter<TODOTableModel> sorter = new TableRowSorter<>(todoTableModel);
        for (int i = 0; i<todoTableModel.getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }
        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        f.getTodoTable().setRowSorter(sorter);


        /*
        SyncHandler.get().addCreamDataListener(new CreamDataListener() {
            public void dataChanged(Object origin) {
                updateTodoModel();
            }
        });
        */

        f.getTodoTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {

                //
                // set the note body to browser component
                //
                int row = ((ListSelectionModel) e.getSource()).getMinSelectionIndex();
                if(row >= 0) { // idx -1 when updating the todoTable by timer...
                    f.getOutlookButton().setEnabled(true);
                    int idx = f.getTodoTable().convertRowIndexToModel(row);
                    note = todoTableModel.getTODO(idx).getNote();
                    showNoteContentInBrowser();

                    //
                    // set the firma and persons (if available)
                    //
                    firma = todoTableModel.getTODO(idx).getFirma();
                    setFirmaFilter();

                    //
                    // remember the VALUE of the TODO-table-model in order to resotre the selection
                    //

                    selectedTODO = todoTableModel.getTODO(idx);

                    f.getNoteChooser().getSelectionModel().clearSelection();

                    //}
                } else {
                    f.getOutlookButton().setEnabled(false);
                    //selectedTODO = null;
                }

            }
        });

        //
        // personFirma table
        //
        personFirmaTableModel = new PersonFirmaTableModel_NEW();
        ((CreamTable_NEW)f.getPersonInCompanyTable()).initAfterDataAvailable();
        f.getPersonInCompanyTable().setModel(personFirmaTableModel);
        //new ColumnSelectorForm(dataFile.getPersonInCompanyTable());
        f.getPersonInCompanyTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {

                //
                // set the note body to browser component
                //
                int row = ((ListSelectionModel) e.getSource()).getMinSelectionIndex();
                if(row >= 0) { // idx -1 when updating the todoTable by timer...
                    f.getOutlookButton().setEnabled(true);
                    int idx = f.getPersonInCompanyTable().convertRowIndexToModel(row);
                    note = personFirmaTableModel.getPairAtRow(idx).right.getNote();
                    showNoteContentInBrowser();

                    //
                    // set the firma and persons (if available)
                    //
                    firma = personFirmaTableModel.getPairAtRow(idx).right;
                    setFirmaFilter();

                    //
                    // remember the VALUE of the TODO-table-model in order to resotre the selection
                    //

                    selectedTODO = null; //todoTableModel.getTODO(idx);

                    f.getNoteChooser().getSelectionModel().clearSelection();

                    //}
                } else {
                    f.getOutlookButton().setEnabled(false);
                    //selectedTODO = null;
                }

            }

        });

        f.getOutlookButton().addActionListener(e-> GuiUtil.notYet());

        //dataFile.getAlle
        f.getAlleKontakteAnzeigenButton().addActionListener(e-> alleKontakteAnzeigen());

        f.getNoteChooser().initAfterDataAvailable();

        SyncHandler.get().addCreamDataListener(new CreamDataListener() {
            @Override
            public void selectionChanged(Object origin, CreamFirmaData creamFirmaData) {
                //log.trace(CRASH_NIT, "PersonTableModel start");
                if(creamFirmaData != null) {
                    alleKontakteAnzeigen();
                    note = creamFirmaData.getNote();
                    firma = creamFirmaData;
                    setFirmaFilter();
                    personFirmaTableModel.fireTableDataChanged();
                    showNoteContentInBrowser();
                }
                //log.trace(CRASH_NIT, "PersonTableModel finished");

            }
        });


    }

    private void setFirmaFilter() {
        TableRowSorter<PersonFirmaTableModel_NEW> personSorter = new TableRowSorter<>(personFirmaTableModel);
        personSorter.setRowFilter(new RowFilter<PersonFirmaTableModel_NEW, Integer>() {
            @Override
            public boolean include(Entry<? extends PersonFirmaTableModel_NEW, ? extends Integer> entry) {
                if(firma == null) return false;
                int rowInModel =  entry.getIdentifier().intValue();
                CreamFirmaData firmaFound = personFirmaTableModel.getPairAtRow(rowInModel).right;
                if(firma.equals(firmaFound)) return true;
                return false;
            }
        });
        f.getPersonInCompanyTable().setRowSorter(personSorter);
    }

    private void showNoteContentInBrowser() {
        try {
            SyncHandler.get().loadRessources(note); // TODO: is this needed? When ressources are stored locally... Then not any more...
            String html = ENMLProcessor.get().noteToInlineHTMLString(note);
            //final String html = Util.inHtmlBody("TEST", "TelefonListViewController:231 JUST FOR THE TEST... removed ENMLProcessor.get().noteToInlineHTMLString(n)");
            String plainText = HtmlToPlainText.convert(html);
            //Platform.runLater(() -> dataFile.getJavaFxWebBrowser().loadContent(html));
            f.getTodoHistoryTextPane().setText(plainText);
            f.getTodoHistoryTextPane().setCaretPosition( 0 ); // to prevent scrolling down...

        } catch (Exception e1) {
            f.getTodoHistoryTextPane().setText("ERROR: Fehler beim laden der Evernote Notiz.\n Möglich URSACHEN:\n 1.) Sie müssen online sein.\n 2.) Die Evernote-Notiz hat Elemente, die nicht konvertiert werden können... \n 3.) die Notiz gibts nicht mehr auf dem Server - bzw. das Notebook stimmt nicht");
            //Platform.runLater(() -> dataFile.getJavaFxWebBrowser().loadContent(Util.inHtmlBody("ERROR", "Fehler beim laden der Evernote Notiz")));
            e1.printStackTrace();
        }
    }

    private void saveHistory() {
        String history = f.getTodoHistoryTextPane().getText();
        String enml = TextToEnml.transformToENMLBody(history);
        if(note != null) {
            String newContent = note.getContent().replaceAll("<en-note>.*</en-note>", "<en-note>"+enml+"</en-note>");
            note.setContent(newContent);
            SyncHandler.get().saveData(this, note);
        }

    }


    private void alleKontakteAnzeigen() {
        //SwingUtilities.invokeLater(()-> {
            selectedTODO = null;
            f.getTodoTable().getSelectionModel().clearSelection();
            f.getPersonInCompanyTable().setRowSorter(null);
            f.getTodoHistoryTextPane().setText(":-) gibt grad nix anzuzeigen...");
        //});
    }


    //
    //  if user presses the button to create a new todo-entry
    //
    private void allesEintragen() {

        //
        // create history
        //
        String history = f.getCreateHistoryTextField().getText();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")).toString();
        String user = AbstractConfiguration.getConfig().getCurrentUser().getShortName();
        String historyEntry=null;
        if(!"".equals(history)) {
            historyEntry = user + ": " + now + "  " + history;
        }

        //
        // create todo
        //
        String todo = f.getCreateNewCallTextField().getText();
        String date = f.getDateNewCallTextField().getText();
        String time = f.getTimeNewCallTextField().getText();

        if("".equals(date) && "".equals(time)) {
            date = now;
        } else if ("".equals(date) && !"".equals(time)) {
            date = LocalDateTime.now().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString();;
        }
        String todoEntry=null;
        if(!"".equals(todo)) {
            todoEntry = "" +user+": " + date + " " + time + "  " + todo;
        }

        //
        // write back
        //

        //String historyEditorText = dataFile.getHistorieEditorPane().getText();
        //historyEditorText = (todoEntry!=null? (todoEntry+ "\n"):"") + (historyEntry != null? (historyEntry+"\n") : "") + historyEditorText;
        //dataFile.getHistorieEditorPane().setText(historyEditorText);
        if(historyEntry != null) {
            ENHelper.addHistoryEntry(note, historyEntry);
        }
        if(todoEntry != null) {
            ENHelper.addTodoEntry(note, todoEntry);
        }

        assert firma != null;

        if(firma != null) {
            SyncHandler.get().saveData(this, firma);
        } else {
            SyncHandler.get().saveData(this, note);
        }


        //selectedTODO = todoTableModel.getTODO(idx);
        //System.out.println("SELECTED TODO: " + selectedTODO);
        updateTodoModel();
        showNoteContentInBrowser();
    }


    private void addTime(int minutes) {

        String time = f.getTimeNewCallTextField().getText();
        if (time == null || time.equals("")) {
            setTimeTextFieldToNow();
        }
        time = f.getTimeNewCallTextField().getText();
        String split[] = time.split(":");


        int h = 0;
        int m = 0;
        int newTotalMin = 0;
        try {
            h = Integer.parseInt(split[0]);
            m = Integer.parseInt(split[1]);
            int totalMin = m + 60 * h;
            newTotalMin = totalMin + minutes;
        } catch (NumberFormatException e) {
            blinkRedTimeTextField();
            setTimeTextFieldToNow();
            return;
        }

        if (newTotalMin < 0 || newTotalMin > 24*60-1) {
            //
            // dont change and blink red
            //
            blinkRedTimeTextField();
            return;
        }

        //
        // change the value
        //

        h = newTotalMin / 60;
        m = newTotalMin % 60;
        f.getTimeNewCallTextField().setText(getTimeStr(h, m));

    }

    private void setTimeTextFieldToNow() {
        LocalDateTime t = LocalDateTime.now();
        int h = t.getHour();
        int m = t.getMinute();
        f.getTimeNewCallTextField().setText(getTimeStr(h, m));
    }

    private void blinkRedTimeTextField() {
        SwingUtilities.invokeLater(() -> {
            colorBackgroundOriginal = f.getTimeNewCallTextField().getBackground();
            f.getTimeNewCallTextField().setBackground(Color.RED);
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                f.getTimeNewCallTextField().setBackground(colorBackgroundOriginal);
            });
        });
    }

    private void addDays(int days) {
        String date = f.getDateNewCallTextField().getText();
        if (date == null || date.equals("")) {
            LocalDateTime t = LocalDateTime.now();
            int d = t.getDayOfMonth();
            int m = t.getMonthValue();
            int y = t.getYear();
            f.getDateNewCallTextField().setText(d + "." + m + "." + y);
        }
        date = f.getDateNewCallTextField().getText();
        String split[] = date.split("\\.");
        int d = Integer.parseInt(split[0]);
        int m = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        LocalDateTime read = LocalDateTime.of(y, m, d, 0, 0, 0);
        LocalDateTime added = read.plusDays(days);

        f.getDateNewCallTextField().setText(added.getDayOfMonth() + "." + added.getMonthValue() + "." + added.getYear());

    }


    private String getTimeStr(int h, int m) {
        return "" + h + ":" + (m < 10 ? "0" : "") + m;
    }

    /**
     * just to right click the button and get an action...
     */
    class MyMouseAdapter extends MouseAdapter {

        int add; // how many to add
        boolean day; // is it a day or a time

        public MyMouseAdapter(int add, boolean day) {
            this.add = add;
            this.day = day;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e))
                if (day) {
                    addDays(add);
                } else {
                    addTime(add);
                }
        }
    }


    private void registerButtonListeners() {
        f.getA5MinButton().addActionListener(e -> addTime(5));
        f.getA5MinButton().addMouseListener(new MyMouseAdapter(-5, false));

        f.getA15Button().addActionListener(e -> addTime(15));
        f.getA15Button().addMouseListener(new MyMouseAdapter(-15, false));

        f.getA30Button().addActionListener(e -> addTime(30));
        f.getA30Button().addMouseListener(new MyMouseAdapter(-30, false));

        f.getA1hButton().addActionListener(e -> addTime(60));
        f.getA1hButton().addMouseListener(new MyMouseAdapter(-60, false));

        f.getA1TButton().addActionListener(e -> addDays(1));
        f.getA1TButton().addMouseListener(new MyMouseAdapter(-1, true));

        f.getA1WButton().addActionListener(e -> addDays(7));
        f.getA1WButton().addMouseListener(new MyMouseAdapter(-7, true));

        f.getA1MButton().addActionListener(e -> addDays(30));
        f.getA1MButton().addMouseListener(new MyMouseAdapter(-30, true));

        f.getA6MButton().addActionListener(e -> addDays(182));
        f.getA6MButton().addMouseListener(new MyMouseAdapter(-182, true));

        f.getOkButton().addActionListener(e -> allesEintragen());

        f.getTODOsAktButton().addActionListener(e -> updateTodoModel());

        /*
        dataFile.getTodoHistoryTextPane().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if(e.getKeyChar() == '\n') {
                    saveHistory();
                }
            }
        });*/
        f.getTodoHistoryTextPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(note == null) return;
//                if(e.getClickCount() == 2) {
                final Color cOld = f.getTodoHistoryTextPane().getBackground();
                try {
                    ENSharedNotebook notebook = SyncHandler.get().getNotebook(note);
                    String linkForBrowser = notebook.getEvernoteRawLinkTo(note);
                    log.info("Öffne Link: " + linkForBrowser);
                    f.getTodoHistoryTextPane().setBackground(Color.GREEN);
                    Desktop.getDesktop().browse(new URI(linkForBrowser));
                } catch (Exception e1) {
                    log.error("Error opening link for: " + note.getTitle());
                    log.error(e1.getMessage());
                    f.getTodoHistoryTextPane().setBackground(Color.PINK);
                }

                final Timer t = new Timer(500, new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        f.getTodoHistoryTextPane().setBackground(cOld);
                    }
                });
                t.setRepeats(false);
                t.start();
            }
        });
    }

}
