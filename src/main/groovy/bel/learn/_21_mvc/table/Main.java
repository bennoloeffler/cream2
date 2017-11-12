package bel.learn._21_mvc.table;

/**
 * Created by VundS02 on 15.01.2017.
 */
public class Main {

    public static void main(String[] args) {
        Model m = new Model();

        StatisticsView sv = new StatisticsView();
        StatisticsController sc = new StatisticsController(m, sv);
        sv.setVisible(true);

        TablePersonView pv = new TablePersonView();
        TablePersonController pc = new TablePersonController(m, pv);
        pv.setVisible(true);

        OnePersonView personView = new OnePersonView();
        OnePersonController personController = new OnePersonController(m, personView);
        personView.setVisible(true);

        ConsoleView cv = new ConsoleView();
        ConsoleController cc = new ConsoleController(m, cv);
    }
}
