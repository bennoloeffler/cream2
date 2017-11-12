package bel.en.gui;

import javax.swing.*;

/**
 * Created 01.04.2017.
 */
public class GuiUtil {

    public static void regexHelp() {
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                "Wortgrenze \\b   Wenn Max NICHT Maxim finden soll: Max\\b\n" +
                        "Mehrere Worte |   Max oder Moritz: Max|Moritz\n" +
                        "Ziffern \\d   (digit) Alle PLZs mit 7: 7\\d\\d\\d\\d\n" +
                        "Alle Zeichn .   Ma.. findet zB Mark Ma91 Ma@X und Mama\n" +
                        "Beliebig oft *   Ma.*x findet zB Mauermatrix oder 'Mama muss nix'\n" +
                        "Genau x {x}   7\\d{4} findet alle 7er PLZs (7 und 4 Ziffern)\n" +
                        "(?i)regex kein-groß-klein (?i)max findet Max max maX\n" +
                        "REGEX erproben: Total cool! https://regex101.com/",
                "Regex Hilfe",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void notYet() {
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "diese Funktion gibt's noch nicht...", "Mannoo!", JOptionPane.INFORMATION_MESSAGE);
    }
}
