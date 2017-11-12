package bel.learn._06_swing;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.jgoodies.looks.plastic.theme.SkyBluer;

import javax.swing.*;
import java.awt.*;

/**
 */
public class TestApp {
    public static void main(String[] args) {

        //
        // a proper main
        //
        SwingUtilities.invokeLater(() -> {
            try {
                initLookAndFeel();
                JFrame frame = new JFrame();
                frame.setTitle("los, klick den Button und die Checkbox. Oft. Öfter!");
                frame.setContentPane(new BigTablePanel());
                frame.setPreferredSize(new Dimension(1000, 800));
                frame.pack();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @lombok.SneakyThrows()
    private static void initLookAndFeel() {
        System.setProperty("swing.aatext", "true");
        /*
        FontSet fontSet = FontSets.createDefaultFontSet(
                new Font("Tahoma", Font.PLAIN, 14),    // control font
                new Font("Tahoma", Font.PLAIN, 14),    // menu font
                new Font("Tahoma", Font.BOLD, 14)     // completeString font
        );
        FontPolicy fixedPolicy = FontPolicies.createFixedPolicy(fontSet);
        Plastic3DLookAndFeel.setFontPolicy(fixedPolicy);
        */
        Plastic3DLookAndFeel laf = new Plastic3DLookAndFeel();
        Plastic3DLookAndFeel.setCurrentTheme(new SkyBluer());
        Options.setPopupDropShadowEnabled(true);
        ExperienceBlue eb = new ExperienceBlue();
        PlasticLookAndFeel.setPlasticTheme(eb);
        UIManager.setLookAndFeel(laf);
    }

    //
    // find the JFrame EVERYWHERE - if you have a component
    //
    public void createDialog(Component rootFinder) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getRoot(rootFinder));
    }
}
