package bel.learn._25_interantionalisation;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Handles loading the ressource files and provides local Strings by:
 * 1) import static bel.learn._25_interantionalisation.InternationalisationDelegate.tr;
 * 2) then just call String localString = tr("the key string")
 *
 * You get the MessagesBundle by
 * 1) placing them in the class path? Fucking detail...
 * 2) Creating a locale, e.g. Locale l = new Locale("de", "DE"); and passing it to setLocale(...)
 * 3) Naming a MessageBundle file like: MessgeBundle_de_DE.properties
 */
public class InternationalisationDelegate {

    private static ResourceBundle messages;

    public static void setLocale(Locale locale) {
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
    }

    public static String tr(String key) {
        return messages.getString(key);
    }
}
