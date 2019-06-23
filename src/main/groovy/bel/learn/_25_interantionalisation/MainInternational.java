package bel.learn._25_interantionalisation;


import java.net.URL;
import java.util.Locale;

import static bel.learn._25_interantionalisation.InternationalisationDelegate.tr;

/**
 * test loading the ressource file and using it
 * https://docs.oracle.com/javase/tutorial/i18n/resbundle/propfile.html
 */
public class MainInternational {
    public static void main(String[] args) {

        // make sure, that the locale file can be found be the classloeader.
        URL location = new MainInternational().getClass().getClassLoader().getResource("MessagesBundle_de_DE.properties");

        // for locales, without a fitting MessangeBundle, it deliveres the default locale. Eg in Germany de_DE
        InternationalisationDelegate.setLocale(new Locale("fr", "FR"));
        System.out.println(tr("bel/learn/_37_gpars_groovy_concurrency/test"));

        InternationalisationDelegate.setLocale(new Locale("de", "DE"));
        System.out.println(tr("bel/learn/_37_gpars_groovy_concurrency/test"));

        InternationalisationDelegate.setLocale(new Locale("en", "US"));
        System.out.println(tr("bel/learn/_37_gpars_groovy_concurrency/test"));    }
}
