package bel.learn._21_mvc.simple;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 *
 */
public class ControllerButtonLabel implements Observer, ActionListener {

    Model m;
    ViewButtonLabel v;

    public ControllerButtonLabel(Model m, ViewButtonLabel v) {
        this.m = m;
        this.v = v;
        m.addObserver(this);
        v.getButton().addActionListener(this);
        updateView();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(m == o) {
            updateView();
        }
    }

    private void updateView() {
        v.getLabel().setText(String.valueOf(m.getCounter()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ViewButtonLabel.BUTTON_INC_ACTION: m.incCounter();
        }
    }
}
