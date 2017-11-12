package bel.learn._21_mvc.table;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.RELATIVE;

/**
 * Shows one person in a dialog.
 */
public class OnePersonView extends JFrame {

    @Getter
    private JTextField anrede = new JTextField();

    @Getter
    private JTextField fullName  = new JTextField();

    @Getter
    private JTextField alter  = new JTextField();

    @Getter
    private JTextField konto  = new JTextField();

    //@Getter
    //private JTextField noten  = new JTextField();

    @Getter
    private JButton button = new JButton("Neue Person");


    public OnePersonView() {

        setTitle("ONE Person");

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gbl);

        //
        // add all the labels
        //

        gbc.gridx=0; gbc.gridy=RELATIVE; //gbc.gridheight = 3;
        gbc.weightx = 0.0; gbc.weighty = 0.0;
        //gbc.ipadx = 10; gbc.ipady = 10;
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.NONE;

        add(new JLabel("Anrede: "), gbc);
        add(new JLabel("Name: "), gbc);
        add(new JLabel("Alter: "), gbc);
        add(new JLabel("Konto: "), gbc);
        //add(new JLabel("Noten: "), gbc);

        //
        // add all the textfields
        //

        gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.gridx=1; gbc.gridy=0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Dimension d = new Dimension(100, 20);
        anrede.setPreferredSize(d);
        add(anrede, gbc);
        gbc.gridy=RELATIVE;
        fullName.setPreferredSize(d);
        add(fullName, gbc);
        alter.setPreferredSize(d);
        add(alter, gbc);
        konto.setPreferredSize(d);
        add(konto, gbc);
        //noten.setPreferredSize(d);
        //add(noten, gbc);

        // add a spacer
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        add(new JPanel(), gbc);

        // and finally add the button
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        add(button, gbc);
        pack();


    }


    /**
     * testing the view
     * @param args
     */
    public static void main(String[] args) {
        new OnePersonView().setVisible(true);
    }
}
