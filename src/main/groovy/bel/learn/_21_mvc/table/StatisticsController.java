package bel.learn._21_mvc.table;

import java.util.Observable;
import java.util.Observer;
import java.util.OptionalDouble;

/**
 *
 */
public class StatisticsController implements Observer {

    Model m;
    StatisticsView sv;

    public StatisticsController(Model m, StatisticsView sv) {
        this.m = m;
        this.sv = sv;
        m.addObserver(this);
        updateView();
    }

    //
    // Observer
    //
    @Override
    public void update(Observable o, Object arg) {
        updateView();
    }

    private void updateView() {
        // size
        int size = m.getPersons().size();
        sv.getNumber().setText(String.valueOf(size) + " Personen insgesamt");

        // average
        OptionalDouble avg = m.getPersons().stream().mapToInt(p -> p.getAge()).average();
        if(avg.isPresent()) {
            sv.getAverageAge().setText(String.format("%.1f  Jahre Altersdurchschnitt", avg.getAsDouble()));
        } else {
            sv.getAverageAge().setText("kein Altersdurchschnitt");
        }

        // fraction of male
        if(size > 0) {
            long male = m.getPersons().stream().filter(p -> p.isMale()).count();
            double fraction = ((double)male / (double)size) * 100;
            String fractionValue = String.format("%.1f%% Männer", fraction);
            sv.getFractionMale().setText(fractionValue);
        } else {
            sv.getFractionMale().setText("keine Männer, keine Frauen...");
        }
    }
}
