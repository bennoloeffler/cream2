package bel.util;

public class StringSimilarity {

    public static double bestLastSimilarity = -1;

    /**
     * returns the substrig with the same length as the shortest of s1 s2, that fits best:
     * "this is a test" ,"tast" will deliver "test" and a bestLastSimilarity of 0.75
     * @param s1
     * @param s2
     * @return
     */
    public static String findBestSimilarityByEqualLenthMatch(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        double bestSimilarity = -1;
        String bestString = "";
        for(int i = 0; i <= longer.length()-shorter.length(); i++ ) {

            String substring = longer.substring(i, shorter.length()+i);
            double s = similarity(substring, shorter);
            if(s>bestSimilarity) {
                bestSimilarity = s;
                bestString = substring;
            }
        }
        bestLastSimilarity = bestSimilarity;
        return bestString;

    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
    /* // If you have StringUtils, you can use it to calculate the edit distance:
    return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                               (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static void printSimilarity(String s, String t) {
        System.out.println(String.format(
                "%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
    }

    public static void main(String[] args) {
        printSimilarity("", "");
        printSimilarity("1234567890", "1");
        printSimilarity("1234567890", "123");
        printSimilarity("1234567890", "1234567");
        printSimilarity("1234567890", "1234567890");
        printSimilarity("1234567890", "1234567980");
        printSimilarity("47/2010", "472010");
        printSimilarity("47/2010", "472011");
        printSimilarity("47/2010", "AB.CDEF");
        printSimilarity("47/2010", "4B.CDEFG");
        printSimilarity("47/2010", "AB.CDEFG");
        printSimilarity("The quick fox jumped", "The fox jumped");
        printSimilarity("The quick fox jumped", "The fox");
        printSimilarity("kitten", "sitting");

        String best = findBestSimilarityByEqualLenthMatch("Das ist ein Einkaufsmanager", "einkauf");
        System.out.println(best + " s: " + bestLastSimilarity);

        best = findBestSimilarityByEqualLenthMatch("Das ist ein Einkaufsmanager", "menager");
        System.out.println(best + " s: " + bestLastSimilarity);


        best = findBestSimilarityByEqualLenthMatch("tast", "this is a test");
        System.out.println(best + " s: " + bestLastSimilarity);
    }

}
