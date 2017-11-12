package bel.learn._21_mvc.simple;

import lombok.Getter;

import java.util.Observable;

/**
 * just adds...
 */
public class Model extends Observable {

    @Getter
    private int counter;

    public void incCounter() {
        setCounter(getCounter()+1);
    }

    public void setCounter(int counter) {
        this.counter = counter;
        setChanged();
        super.notifyObservers();
    }
}
