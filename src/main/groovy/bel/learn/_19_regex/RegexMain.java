package bel.learn._19_regex;

import bel.util.RegexUtils;

import java.util.List;

/**
 * @see <a href="http://regex101.com">regex101.com</a>
 * @see bel.util.RegexUtils
 */
public class RegexMain {
    public static void main(String[] args) {
        List hits = RegexUtils.findWithRegex("This is the shit to find. SHIT. Shitting shit.", "(s|S)hit", 0);
        System.out.println(hits);
    }
}
