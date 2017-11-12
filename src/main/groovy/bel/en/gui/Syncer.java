package bel.en.gui;

import bel.en.MainGUI;
import bel.en.deamon.DeamonCream;
import bel.en.localstore.SyncHandler;
import bel.en.localstore.SyncProgress;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


public class Syncer {

    ActionListener syncAtion;
    StructureNoteFormNew form;

    public Syncer(StructureNoteFormNew form) {
        this.form = form;
        syncAtion = new SyncActionListener();

    }

    Timer syncTimer = null;

    public void setSyncTimer(String textNumber) {
        //String t = syncIntervalTextFlield.getText();
        int i = 15;
        try {
            i = Integer.parseInt(textNumber);
        } catch (NumberFormatException e) {
            // can happen... during typing
            i = 15;
        }

        if(i<7) {i=7;}
        boolean hasDeamonLock = DeamonCream.get().hasLock();
        if(hasDeamonLock) { // if deamon: user cannot change that any more...
            i=7;
        }

        MainGUI.properties.setProperty("cream.sync_intervall", Integer.toString(i));
        if(syncTimer == null) {
            syncTimer = new Timer(i * 1000 * 60, syncAtion);
            syncTimer.setRepeats(true);
            syncTimer.start();
        } else {
            syncTimer.setDelay(i * 1000 * 60);
            syncTimer.restart();
        }
        String message = "";
        if(!hasDeamonLock) {
            message = "Evernote Server wird ab jetzt alle " + i + " Minuten synchronisiert.";
        }else {
            message = "CREAM-Deamon: Daher Sync alle " + i + " Minuten...";
        }
        form.statusMessage(message);
    }

    public ActionListener getSyncActionListener() {
        return syncAtion;
    }

    class SyncActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(form.getSyncEvernoteJetztButton().isEnabled()) {
                syncEvernote();
            } else {
                form.statusMessage("Sync läuft noch...");
            }
        }
    }

    /**
     * do the sync to evernote in a worker thread and use the progress bar...
     */
    public void syncEvernote() {
        SyncerSwingWorker ssw = new SyncerSwingWorker(form.getSyncProtocollTextArea());
        try {
            form.statusMessage("starte Sync mit dem Evernote Server...");
            form.getSyncEvernoteJetztButton().setEnabled(false);
            ssw.execute();
        } catch (Exception e1) {
            form.statusMessage("SYNC gescheitert. Logfile hat mehr Infos...");
        }
        ssw.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                switch (event.getPropertyName()) {
                    case "progress":
                        form.getProgressBar().setIndeterminate(false);
                        form.getProgressBar().setValue((Integer) event.getNewValue());
                        break;
                    case "state":
                        switch ((SwingWorker.StateValue) event.getNewValue()) {
                            case DONE:
                                form.getSyncEvernoteJetztButton().setEnabled(true);
                                form.getProgressBar().setVisible(false);
                                /*
                                if(currentNote != null) {
                                    currentNote = SyncHandler.get().getNote(currentNote.getGuid());

                                    if(currentNote != null) {
                                        creamFirmaData = extractFirmaPersonFromContent(currentNote.getContent());
                                    } else {
                                        creamFirmaData = new CreamFirmaData();
                                    }
                                    personTableModel.update(creamFirmaData.persons);
                                    firmaTableModel.update(creamFirmaData);

                                }
                                */
                                //searchForm.updateData(notes);
                                form.statusMessage("fertig mit SYNC");

                                //suchErgebnisList.setModel(filterListModel);

                                //searchCancelAction.putValue(Action.NAME, "Search");

                                break;
                            case STARTED:
                            case PENDING:
                                //searchCancelAction.putValue(Action.NAME, "Cancel");
                                form.getProgressBar().setVisible(true);
                                //searchProgressBar.setIndeterminate(true);
                                break;
                        }
                        break;
                }
            }
        });

    }

    /**
     * the thread...
     */
    private class SyncerSwingWorker extends SwingWorker<Void, String> implements SyncProgress {
        JTextArea syncProtocollTextArea;

        public SyncerSwingWorker(JTextArea syncProtocollTextArea) {
            this.syncProtocollTextArea = syncProtocollTextArea;
        }

        @Override
        protected Void doInBackground() throws Exception {
            SyncHandler.get().sync(this);
            if(DeamonCream.get().hasLock()) {
                //DeamonCreamWorker dcw = new DeamonCreamWorker();
                //dcw.doIt();
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
        }

        @Override
        public void count(int progress) {
            // tell the progressBar the progress
            setProgress(progress);
        }

        @Override
        public void clear() {
            syncProtocollTextArea.setText("");
        }

        @Override
        public void message(String message) {
            syncProtocollTextArea.append(message + "\n");
        }


        protected void process(final List<String> chunks) {
            // Updates the messages text area
            for (final String string : chunks) {
                syncProtocollTextArea.append(string);
                syncProtocollTextArea.append("\n");
            }
        }
    }
}
