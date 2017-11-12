package bel.learn._xx_sampleData;

import java.util.HashMap;

/**
 * Just creates funny data
 */
public class SampleDataSource {

    private static String[] names ={"Leo", "Hubert", "Kevin", "Franz", "Xaver", "Michael", "Stephan", "Robert", "Sabine", "Benno", "Paul", "Ulrich", "Thorsten", "Manuel", "Albert", "Hannes", "Siegbert"};

    public static String nextName() {
        return names [(int)(Math.random() * names.length)];
    }

    public static String nextMultipleName() {
        int hm = (int)(Math.random() * 3);
        StringBuffer result = new StringBuffer();
        for(int i=0; i<=hm; i++) {
            if(result.length() != 0) result.append("-");
            result.append(nextName());
        }
        return result.toString();
    }

    public static String nextNumber() {
        return Long.toString((long)(Math.random() * Long.MAX_VALUE));
    }

    public static String nextSample() {
        int choice = (int)(Math.random() * 3);
        if(choice == 1) {
            return nextMultipleName();
        } else if(choice == 2) {
            return nextNumber();
        } else {
            return nextName();
        }
    }

    public static HashMap<String, String> hugeHashMap(int numberOfKeyValueEntries) {
        HashMap<String, String> result = new HashMap<>();
        do {
            result.put(nextNumber(), nextSample());
        } while (--numberOfKeyValueEntries > 0);
        return result;
    }

}
