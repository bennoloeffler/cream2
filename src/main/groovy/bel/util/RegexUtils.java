package bel.util;

import bel.en.evernote.ENHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All the common regex tasks
 */
public class RegexUtils {

    /**
     * just an  abbrevation to use regex shorter
     * @param content the content to scan
     * @param regex what to search for
     * @param matchgroup what matchgroup should be returned (depending on the regex, that may not be 0)
     * @return all matches
     */
    public static List<String> findWithRegex(String content, String regex, int matchgroup) {
        ArrayList<String> result = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String match = m.group(matchgroup);
            //if(!"".equals(match)) { // Workaround, because lookahead or lookbehind seems to produce alternating empty matches...
                result.add(ENHelper.transformENMLToNormal(match));
            //}
        }
        return result;
    }

    public static boolean matchWithRegex(String content, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        return m.find();
    }

    public static void stopIfFound(String content, String regex) {
        List<String> result = findWithRegex(content, regex, 0);
        if(result.size() > 0) {
            System.out.println("\n\nFOUND: " + regex + "  IN: " + content);
            System.out.println("");
        } else {
            //System.out.println("NO MATCH: " + regex + "  IN: " + content);
        }
    }

    /*
    Netzbetreiber	Handyvorwahlen
    Deutsche Telekom	01511 01512 01514 01515 01516 01517 0160 0170 0171 0175
    Vodafone	01520 01522 01523 01525 0162 0172 0173 0174
    E-Plus	01570 (Telogic) 01573 01575 01577 01578 0163 0177 0178
    O2	01590 0176 0179
    */
    private static final String mobilePrefixes = "(1511|151 1|1512|151 2|1514|151 4|1515|151 5|1516|151 6|1517|151 7|160|170|171|175|1520|152 0|1522|152 2|1523|152 3|1525|152 5|162|172|173|174|1570|157 0|1573|157 3|1575|157 5|1577|157 7|1578|157 8|163|177|178|1590|159 0|176|179)";
    // orig private static final String mobilePrefixes = "(1525|171|162|163|178|179|160|170|151|173|176)";

    private static String getPhoneRegex(String prefixes) {
        return"(0049\\s?|\\+\\s?49\\s?|0|\\(0)\\(?\\s?\\.?(\\(0\\))?\\s?0?"+prefixes+"\\)?[-–\\.\\s\\/]{0,3}[-–\\d ]{5,12}[\\d]";
    }

    public static List<String> findGermanMobileNumbers(String text) {
        String regex = getPhoneRegex(mobilePrefixes);
        return findWithRegex(text, regex, 0);
    }

    public static List<String> findGermanPhoneFaxNumbers(String text) {
        String regex = getPhoneRegex("[\\d ]{3,7}");
        List<String> result = findWithRegex(text, regex, 0);
        return result;
    }

    public static List<String> findEmailAdress(String text) {
        String regex = "[A-Z0-9a-z\\._%+-]+@[A-Za-z0-9\\.-]+\\.[a-zA-Z]{2,}";
        return findWithRegex(text, regex, 0);
    }

    public static List<String> findZipAndTown(String text) {
        String regex = "\\d{5}\\s[- öÖäÄüÜßa-zA-Z]*";
        return findWithRegex(text, regex, 0);
    }

    public static void main(String[] args) {
        List zipTown = findZipAndTown("49090 Osnabrück");
        System.out.println(zipTown.get(0));
    }
}
