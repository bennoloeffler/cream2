package bel.learn._21_mvc.table;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by VundS02 on 15.01.2017.
 */
public class ConsoleController implements Observer {

    Model m;
    ConsoleView cv;

    public ConsoleController(Model m, ConsoleView cv) {
        this.m = m;
        this.cv = cv;
        m.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        StringBuffer s = new StringBuffer();
        s.append("------\n");
        s.append("Number Persones: " + m.getPersons().size() +"\n");
        m.getPersons().forEach(p -> s.append(p.toString() + "\n"));
        s.append("------\n");
        System.out.println(s.toString());
    }
}
