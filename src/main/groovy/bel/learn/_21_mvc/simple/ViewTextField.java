package bel.learn._21_mvc.simple;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class ViewTextField extends JFrame {

    public final static String TEXT_ACTION = "TEXT_ACTION";

    @Getter
    JTextField textField;

    public ViewTextField() {
        super();
        textField = new JTextField();
        textField.setActionCommand(TEXT_ACTION);
        textField.setText("hier tippen...");
        setLayout(new BorderLayout());
        add(textField);
        setLocationRelativeTo(null);
        pack();
    }
}
