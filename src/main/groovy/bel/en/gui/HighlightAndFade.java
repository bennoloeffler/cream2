package bel.en.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * used to "flash" a component...
 */



public class HighlightAndFade {

    private JTextField textField;
    private Timer statusTimer = null;
    private int speed = 10;
    private Color origDisabledColor;
    private Color origEnabledColor;



    public HighlightAndFade(JTextField textField, Color highLight) {
        this.textField = textField;
        origDisabledColor = textField.getDisabledTextColor();
        origEnabledColor = textField.getSelectionColor();
        textField.setDisabledTextColor(highLight);
        textField.setSelectionColor(highLight);
        statusTimer = new Timer(500, statusToBlackAtion);
        statusTimer.setRepeats(true);
        statusTimer.start();
    }



    private ActionListener statusToBlackAtion = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Color c = textField.getDisabledTextColor();
            int red = c.getRed()-speed <= 0 ? 0 : c.getRed()-speed;
            int green = c.getGreen()-speed <= 0 ? 0 : c.getGreen()-speed;
            int blue = c.getBlue()-speed <= 0 ? 0 : c.getBlue()-speed;
            textField.setDisabledTextColor(new Color(red, green, blue));
            textField.repaint();
            if(c.getRed() <= origDisabledColor.getRed() &&
                    c.getGreen() <= origDisabledColor.getGreen() &&
                    c.getBlue() <= origDisabledColor.getBlue() &&
                    c.getRed() <= origEnabledColor.getRed() &&
                    c.getGreen() <= origEnabledColor.getGreen() &&
                    c.getBlue() <= origEnabledColor.getBlue()) {
                statusTimer.stop();
            }
        }
    };

}
