package bel.learn._21_mvc.simple;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by VundS02 on 15.01.2017.
 */
public class ControllerTextField implements Observer, ActionListener {

    private Model m;
    private ViewTextField v;
    private boolean updating = false;
    Color normalBackground;

    public ControllerTextField(Model m, ViewTextField v) {
        this.m = m;
        this.v = v;
        m.addObserver(this);
        v.getTextField().addActionListener(this);
        normalBackground = v.getTextField().getBackground();
        updateView();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(m == o) {
            if(!updating) {
                updateView();
            } else {
                System.out.println("Change comes from here. ignoring...");
            }
        }
    }

    private void updateView() {
        v.getTextField().setText(String.valueOf(m.getCounter()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ViewTextField.TEXT_ACTION:
                try {
                    updating = true;
                    int val = Integer.parseInt(v.getTextField().getText());
                    m.setCounter(val);
                    v.getTextField().setBackground(normalBackground);
                } catch (Exception ex) {
                    // parsing err
                    v.getTextField().setBackground(new Color(255, 200, 200));
                } finally {
                    updating = false;
                }
        }
    }

}
