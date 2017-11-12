package bel.learn._21_mvc.simple;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class ViewButtonLabel  extends JFrame {

    public final static String BUTTON_INC_ACTION = "BUTTON_INC_ACTION";


    @Getter JLabel label;
    @Getter JButton button;

    public ViewButtonLabel() {
        super();
        setTitle("INCREMENT");
        button = new JButton("+1");
        button.setActionCommand(BUTTON_INC_ACTION);
        label = new JLabel("hier tippen...");
        setLayout(new BorderLayout());
        add(button, BorderLayout.NORTH);
        add(label, BorderLayout.SOUTH);
        setLocation(200,200);
        pack();
    }
}