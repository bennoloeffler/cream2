package bel.en.gui.abo;

import bel.en.data.AbstractConfiguration;
import bel.en.data.CreamDataListener;
import bel.en.data.CreamFirmaData;
import bel.en.evernote.ENHelper;
import bel.en.localstore.SyncHandler;
import com.evernote.edam.type.Note;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 09.04.2017.
 */
@FieldDefaults(level= AccessLevel.PRIVATE)
public class AboController {

    AboForm f;
    AbosListModel abosListModel;

    public AboController(AboForm f) {
        this.f = f;
    }

    public void initAfterDataIsAvailable() {
        f.getAboEntfernenButton().addActionListener(e->aboEntfernen());
        f.getZuDenAbosHinzufügenButton().addActionListener(e->aboHinzufuegen());
        f.getNoteChooser().initAfterDataAvailable();

        abosListModel = new AbosListModel();
        f.getCurrentAbosList().setModel(abosListModel);
        abosListModel.initAfterDataIsAvailable();

        /*
            dataFile.getPanel().addComponentListener(new ComponentAdapter() {

                @Override
                public void componentShown(ComponentEvent e) {
                    super.componentShown(e);
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    super.componentHidden(e);
                }
            });
            */
    }

    private void aboHinzufuegen() {
        Note n = f.getNoteChooser().getSelectedNote();
        if(n != null) {
            ENHelper.addAbo(AbstractConfiguration.getConfig().getCurrentUser().getShortName(), n);
            CreamFirmaData data = SyncHandler.get().getData(n);
            SyncHandler.get().saveData(this, data);
        } else {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "bitte eine Notiz im Suchen-Fenster auswählen...", "Fehlerchen", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void aboEntfernen() {
        int idx = f.getCurrentAbosList().getSelectedIndex();
        Note n = ((AboListItem)(abosListModel.getElementAt(idx))).getData().getNote();
        if(n != null) {
            ENHelper.removeAbo(AbstractConfiguration.getConfig().getCurrentUser().getShortName(), n);
            CreamFirmaData data = SyncHandler.get().getData(n);
            SyncHandler.get().saveData(this, data);
        } else {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "bitte eine Notiz bei den Abos auswählen...", "Fehlerchen", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    @Data
    @AllArgsConstructor
    static private class AboListItem {
        CreamFirmaData data;
        public String toString() {
            List<String> abos = ENHelper.getAbos(data.getNote()); // TODO: get it out of the cache
            return data.getNote().getTitle() + abos;
            /*
            return data.getNote().getTitle() + "  ABOs: " data.getSearchCache().getAbos();
            */
        }
    }

    static private class AbosListModel extends AbstractListModel {

        List<AboListItem> currentAbos = new ArrayList<>();

        @Override
        public int getSize() {
            return currentAbos.size();
        }

        @Override
        public Object getElementAt(int index) {
            return currentAbos.get(index);
        }


        public void initAfterDataIsAvailable() {
            SyncHandler.get().addCreamDataListener(new CreamDataListener() {
                @Override
                public void dataChanged(Object origin) {
                    update();
                }
            });
        }

        public void update() {
            //currentAbos = SyncHandler.get().getAbosForShortName(AbstractConfiguration.getConfig().getCurrentUser().getShortName());
            List<CreamFirmaData> abosForShortName = SyncHandler.get().getAbosForShortName(AbstractConfiguration.getConfig().getCurrentUser().getShortName());
            currentAbos = abosForShortName.stream().map(AboListItem::new).collect(Collectors.toList());
            fireContentsChanged(this, 0, currentAbos.size()-1);
        }
    }
}
