package bel.learn._21_mvc.table;

import bel.en.test.TestData;
import bel.learn._01_bean.Person;
import lombok.Getter;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 *
 */
public class Model extends Observable {


    private List<Person> persons = new ArrayList<>();

    @Getter
    private int selected; // -1 unselected or >= 0 which is the idx of the selected

    // listen to changes in Persons
    PropertyChangeListener l = evt -> {
        System.out.println("------------------------------");
        System.out.println("Person changed!");
        System.out.println("Property: " + evt.getPropertyName());
        System.out.println("old: " + evt.getOldValue());
        System.out.println("new: " + evt.getNewValue());
        System.out.println("------------------------------");
        setChanged();
        notifyObservers();
    };

    public Model() {
        TestData.populate(persons);
        for(Person p: persons) {
            p.addPropertyChangeListener(l);
        }
    }

    public void addPerson(Person p) {
        persons.add(p);
        p.addPropertyChangeListener(l);
        setChanged();
        notifyObservers();
    }

    public void setSelected(int idx) {
        if(idx == -1) {
            // ignore
        } else if(idx >= persons.size() || idx < -1) {
            throw new IndexOutOfBoundsException("Index : " +idx);
        } else {
            selected = idx;
            setChanged();
            notifyObservers();
        }
    }

    public List<Person> getPersons() {
        return Collections.unmodifiableList(persons);
    }
}
