package bel.en;

import bel.en.deamon.DeamonCream;
import bel.en.evernote.ENAuth;
import bel.en.gui.StructureNoteFormNew;
import bel.en.localstore.NoteStoreLocal;
import bel.util.Util;
import com.evernote.auth.EvernoteService;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.io.*;
import java.util.Properties;

/**
 * Starting point of CREAM gui
 */
@Log4j2
@FieldDefaults(level= AccessLevel.PRIVATE)
public class MainGUI {

    //
    // Version
    //

    //
    // NEXT TODO: m: einen Vorschlag eintragen
    //

    static int versionMajor = 0;
    static int versionMinor = 6;
    static int versionBugs = 2;
    static public boolean debugOffline = true;
    static String versionName = "Corona-XMas 2020 re-mail";
    public static final String VERSION_STRING = "\""+versionName + "\" (" + versionMajor + "." + versionMinor + "." + versionBugs + ")";


    //
    // local properties
    //

    private static StructureNoteFormNew structureNoteFormNew;

    @Getter
    public static Properties properties = new Properties();

    //@Getter
    //private static boolean deamon;

    private static String PROPERTY_FILE = NoteStoreLocal.NOTE_STORE_BASE + "\\cream_user.properties";


    public static void main(String[] args) throws Exception {

        if(args.length >= 1) {
            if(args[0].equals("-offline")) {
                log.warn("\n\nSTARTING OFFLINE DEBUGGING MODE... DO YOU REALLY WANT THAT?\n\n");
                debugOffline = true;
            }
            if (args[0].equals("-testmode")) {
                log.warn("\n\nstarting up in TESTMODE!  --> TESTDATA e.g. T_C_ALL instead of C_ALL\n\n");
                //DeamonCreamW = true
            }

        } else {
            debugOffline = false;
        }

        log.info("Starting CREAM Release and Version:  " + VERSION_STRING);

        initLookAndFeel();

        loadProperties();

        try {
            ENAuth a = ENAuth.get(properties, EvernoteService.PRODUCTION);
            a.connectToEvernote();
        } catch (Exception e) {
            log.warn("Could not connect to evernote server...");
            //log.catching(e);
        }

        structureNoteFormNew = new StructureNoteFormNew();


        SwingUtilities.invokeLater(() -> {
            try {
                SwingUtilities.invokeLater(structureNoteFormNew::loadConfigAndDataInitGuiAndSync);
            } catch (Exception e) {
                log.fatal("terminating CREAM because of exception came through swing loop: ", e);
                System.exit(-1);
            }
        });


        DeamonCream dc = DeamonCream.get();
        Timer t = new Timer(10*1000, e -> dc.start()); // 10s: just to make sure its after the loadConfigAndDataInitGuiAndSync
        t.setRepeats(false);
        t.start();


        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                log.info("saving properties and ending deamon");
                saveProperties();
                dc.releaseLockAndStop(); // at least try it...
            }
        });

    }

    /**
     * use jGoodies. Looks best :-)
     * @throws javax.swing.UnsupportedLookAndFeelException
     */
    private static void initLookAndFeel() throws UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
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
        //Plastic3DLookAndFeel.setCurrentTheme(new SkyBluer());
        //Plastic3DLookAndFeel.setCurrentTheme(new );

        Options.setPopupDropShadowEnabled(true);
        SkyPink sp = new SkyPink();
        DarkStar ds = new DarkStar();
        ExperienceBlue eb = new ExperienceBlue();
        ExperienceRoyale er = new ExperienceRoyale();
        SkyBlue sb = new SkyBlue();
        SkyBluer sbr = new SkyBluer();
        SkyKrupp sk = new SkyKrupp();
        LightGray lg = new LightGray();
        BrownSugar bs = new BrownSugar();
        DesertBlue db = new DesertBlue();
        DesertBluer dbr = new DesertBluer();
        DesertYellow dy = new DesertYellow();
        Silver si = new Silver();

        PlasticLookAndFeel.setPlasticTheme(sp);
        UIManager.setLookAndFeel(laf);

    }

    public static void loadProperties() {
        try {
            File props = new File(PROPERTY_FILE);
            if(!props.exists()) props.createNewFile();
            InputStream is = new FileInputStream(PROPERTY_FILE);
            MainGUI.properties.load(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveProperties() {
        try {
            FileOutputStream out = new FileOutputStream(PROPERTY_FILE);
            MainGUI.properties.store(out, "");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
