package bel.en.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;

/**
 * Show version, help / docu and licens infos
 */
public class HelpAndLicenseForm {
    private JLabel versionLabel;
    private JButton editHelpButton;
    private JSplitPane helpAndLicensesSplitPane;
    private JEditorPane helpEditorPane;
    private JEditorPane licensesEditorPane;
    private JPanel rootPanel;

    public HelpAndLicenseForm() {
        setHtmlHelp(getHtmlHelp());
        setHtmlCredits(getHtmlCredits());
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel.setText("CREAM-Version: " + versionLabel);
    }

    public void setHtmlHelp(String help) {
        helpEditorPane.setText(help);
    }

    public void setHtmlCredits(String creditsLicenses) {
        licensesEditorPane.setText(creditsLicenses);
    }


    public Component getPanel() {
        return rootPanel;
    }

    public String getHtmlHelp() {
        return "<html><head></head><body>" +
                "<b>Hilfe</b><br/>" +
                "Wenn du sie schreiben willst, dann bewirb Dich<br/>" +
                "</body></html>";
    }

    public String getHtmlCredits() {
        return "<html><head></head><body>" +
                "<b>Licenses:</b><br/>" +
                "Apache License, Version 2.0, January 2004, http://www.apache.org/licenses/<br/>" +
                "commons-lang3-3.5<br/>" +
                "</body></html>";
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        versionLabel = new JLabel();
        versionLabel.setText("hier aus Main.version überschreiben");
        rootPanel.add(versionLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editHelpButton = new JButton();
        editHelpButton.setText("Hilfe bearbeiten");
        rootPanel.add(editHelpButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        helpAndLicensesSplitPane = new JSplitPane();
        helpAndLicensesSplitPane.setContinuousLayout(true);
        helpAndLicensesSplitPane.setDividerLocation(500);
        helpAndLicensesSplitPane.setDividerSize(15);
        helpAndLicensesSplitPane.setDoubleBuffered(true);
        helpAndLicensesSplitPane.setOneTouchExpandable(true);
        helpAndLicensesSplitPane.setOrientation(0);
        rootPanel.add(helpAndLicensesSplitPane, new GridConstraints(1, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        helpAndLicensesSplitPane.setLeftComponent(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        helpEditorPane = new JEditorPane();
        helpEditorPane.setContentType("text/html");
        helpEditorPane.setEditable(false);
        helpEditorPane.setText("<html>\r\n  <head>\r\n    \r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      Hilfe\r\n    </p>\r\n  </body>\r\n</html>\r\n");
        scrollPane1.setViewportView(helpEditorPane);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        helpAndLicensesSplitPane.setRightComponent(panel2);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        licensesEditorPane = new JEditorPane();
        licensesEditorPane.setContentType("text/html");
        licensesEditorPane.setText("<html>\r\n  <head>\r\n    \r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      License\r\n    </p>\r\n  </body>\r\n</html>\r\n");
        scrollPane2.setViewportView(licensesEditorPane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
