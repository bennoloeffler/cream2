package bel.learn._06_swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * a form that just returns a panel
 */
public class BigTablePanel extends JPanel {

    // all the contols
    private BigTable table;
    private JCheckBox checkbox;
    private JButton button;
    private JScrollPane scrollPane;

    // them main panel
    //@lombok.Getter private JPanel panel;
    // instead, extends JPanel

    public BigTablePanel(){

        //
        // create components
        //

        table = new BigTable();
        checkbox = new JCheckBox("mach mich an!", false);
        button = new JButton("drück mich nicht!");

        //
        // do the layout in the main panel
        //

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        //panel = new JPanel(gbl);
        setLayout(gbl);

        gbc.gridx=1; gbc.gridy=1; gbc.gridheight = 3;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        scrollPane = new JScrollPane(table);
        //panel.add(scrollPane, gbc);
        add(scrollPane, gbc);

        gbc.gridx=2; gbc.gridy=1; gbc.gridheight = 1;
        gbc.weightx = 0.0; gbc.weighty = 0.0;
        //panel.add(checkbox, gbc);
        add(checkbox, gbc);

        gbc.gridx=2; gbc.gridy=2; gbc.gridheight = 1;
        gbc.weightx = 0.0; gbc.weighty = 0.0;
        //panel.add(button, gbc);
        add(button, gbc);

        //
        // register all the listeners
        //


        button.addActionListener(al);
    }

    public void nextAction() {
        SwingUtilities.invokeLater(()->
        al.actionPerformed(null));
    }

    ActionListener al  = e -> {
        if(checkbox.isSelected()) {
            table.deleteSomeFields();
        } else {
            table.addStuff();
        }
    };
}
