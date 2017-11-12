package bel.learn._21_mvc.table;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Shows statistics about the persons
 */
public class StatisticsView extends JFrame {

    @Getter
    JLabel number;

    @Getter
    JLabel averageAge;

    @Getter
    JLabel fractionMale;


    public StatisticsView() throws HeadlessException {
        super();

        setLayout(new BorderLayout(10, 10));
        add(new JLabel("Anzahl   Altersdurchschnitt   Männeranteil"), BorderLayout.NORTH);

        number = new JLabel("Anzahl der Personen");
        add(number, BorderLayout.NORTH);

        averageAge = new JLabel("durchschnittliches Alter");
        add(averageAge, BorderLayout.CENTER);

        fractionMale = new JLabel("Anteil der Männer");
        add(fractionMale, BorderLayout.SOUTH);

        setLocation(300, 300);
        pack();
    }

    /**
     * testing the view
     * @param args
     */
    public static void main(String[] args) {
        new StatisticsView().setVisible(true);
    }
}
