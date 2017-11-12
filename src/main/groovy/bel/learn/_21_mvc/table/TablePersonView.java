package bel.learn._21_mvc.table;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Shows the persons in a table
 */
public class TablePersonView extends JFrame {

    @Getter
    JTable personTable;

    public TablePersonView() throws HeadlessException {
        super();
        setLayout(new BorderLayout(10, 10));
        personTable = new JTable();
        personTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        personTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(personTable), BorderLayout.CENTER);

        setLocation(400, 300);

        pack();
    }
}
