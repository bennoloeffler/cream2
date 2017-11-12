package bel.en.gui.dataimport;

import bel.en.data.CreamFirmaData;
import bel.en.gui.dataimport.beanBinding.ImportFirmaDataSet;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.lang3.Pair;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 18.04.2017.
 */
@Log4j2
public class DataImportController {
    private final DataImportForm form;
    private File firmaFile;
    private File personFile;
    private File historieFile;
    private File wiedervorlageFile;
    JFileChooser fileChooser = new JFileChooser();
    private List<ImportSuggestion> importSuggestions;

    public DataImportController(DataImportForm form) {
        this.form = form;
        form.getFirmaButton().addActionListener(e-> {
            firmaFile = openFile(form);
            form.getFirmaFileTextField().setText(firmaFile == null ? "keine Auswahl!" : firmaFile.getAbsolutePath());
        });
        form.getPersonButton().addActionListener(e-> {
            personFile = openFile(form);
            form.getPersonFileTextField().setText(personFile==null ? "keine Auswahl!" : personFile.getAbsolutePath());
        });
        form.getHistorieButton().addActionListener(e-> {
            historieFile = openFile(form);
            form.getHistorieFileTextField().setText(historieFile==null ? "keine Auswahl!" : historieFile.getAbsolutePath());
        });
        form.getWiedervorlageButton().addActionListener(e-> {
            wiedervorlageFile = openFile(form);
            form.getWiedervorlageFileTextField().setText(wiedervorlageFile==null ? "keine Auswahl!" : wiedervorlageFile.getAbsolutePath());
        });

        form.getImportStartenButton().addActionListener(e-> {
            if(firmaFile == null || personFile==null || historieFile ==null || wiedervorlageFile == null) {
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Alle Dateien müssen ausgewählt sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                doTheImport();
            }

        });
    }

    private void doTheImport() {

        // PASS 1. RESULT: Import-Bean-Lists are filled

        //
        // read all firma
        //
        CSVReader firmaReader = null;
        List<ImportFirmaDataSet> firmaList = null;
        try {
            firmaReader = new CSVReader(new FileReader(firmaFile));
            HeaderColumnNameMappingStrategy<ImportFirmaDataSet> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(ImportFirmaDataSet.class);
            CsvToBean<ImportFirmaDataSet> csvToBean = new CsvToBean<>();
            firmaList = csvToBean.parse(strategy, firmaReader);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Fehler beim Einlesen der Firma-Datei... " + e);
            log.catching(e);
            return;
        }

        //
        // read all persons
        //

        //
        // read all historie
        //

        //
        // read all wiedervorlage
        //

        System.out.println(firmaList);

        //
        // PASS 2. Result: A Map with the found corresponding CreamFirmaData entries is available
        //
        // iterate over firma.
        //      1: find the firma by exact matching or by name similarity
        //
        importSuggestions = new ArrayList<>();
        for(ImportFirmaDataSet f: firmaList) {
            Pair<CreamFirmaData, String> pair = findSimilar(f);
            CreamFirmaData d;
            ImportSuggestion suggestion = new ImportSuggestion();
            importSuggestions.add(suggestion);
            if(pair.left == null) { // not found
                d = new CreamFirmaData();
                fillData(d, f);
                suggestion.setFoundOldDataSet(false);
                suggestion.addToSuggestionForList("Neuer Datensatz.");
            } else {
                String foundHow = pair.right;
                d = pair.left;
                fillData(d, f);
                suggestion.setFoundOldDataSet(true);
                suggestion.addToSuggestionForList(foundHow);
            }
            suggestion.setImportFirmaDataSet(f);
            suggestion.setData(d);

        }

        //
        // PASS 3. Result: CreamFirmaData clones, connected with Person and Note (todo & history) in a Map with
        //                 suggestion-entry. The suggestion Entry has: Text, reference to the original. So this is not yet of ANY effect to the data.
        //
        //

    }

    /**
     * Move the data from an ImportFirmaDataSet to CreamFirmaData.
     * Does all the transformation. OVERWRITES existing Data!
     * @param d
     * @param f
     */
    private void fillData(CreamFirmaData d, ImportFirmaDataSet f) {

    }

    /**
     * Finds a CreamFirmaData from the syncHandler based on:
     * 1) email-adresses
     * 2) mobile-numbers
     * 3) phone-Numbers
     * IN THE COMPLETE ENML. This does also work, if there is no structured data right now.
     * @param f
     * @return
     */
    private Pair<CreamFirmaData, String> findSimilar(ImportFirmaDataSet f) {
        return null;
    }

    private File openFile(DataImportForm form) {
        val returnVal = fileChooser.showDialog(form.getPanel(), "auswählen");
        //int returnVal = fc.showOpenDialog(FileChooserDemo.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    @Data
    private static class ImportSuggestion {
        String suggestionForList = "";
        boolean foundOldDataSet;
        CreamFirmaData data;
        ImportFirmaDataSet importFirmaDataSet;

        public void addToSuggestionForList(String s) {
            if(!suggestionForList.equals("")) {
                suggestionForList +=", ";
            }
            suggestionForList += s;
        }
        //ImportPersonDataSet importPersonDataSet;
        //ImportPersonDataSet importPersonDataSet;
        //ImportPersonDataSet importPersonDataSet;
    }
}
