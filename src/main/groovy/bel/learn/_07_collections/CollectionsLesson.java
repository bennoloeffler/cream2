package bel.learn._07_collections;

import bel.learn._14_timingExecution.RunTimer;
import bel.learn._xx_sampleData.SampleDataSource;

import java.util.*;

/**
 * interessting learings about collections
 */
public class CollectionsLesson {
    public static void main(String[] args) {

        List<Long> l = Collections.unmodifiableList(new ArrayList<Long>());

        TreeMap<String, String> map = new TreeMap<>();
        map.put("12","Hugo");
        map.put("34","Benno");
        map.put("xy","Leo");
        map.put("--","Sabine");
        map.put("vv","Paul");
        map.put("==","Ausnahme");
        NavigableMap<String, String> navigableMap = map.descendingMap();
        navigableMap.forEach((k,v)->System.out.println("key: "+k+"val: "+v));

        List<String> stringList = Collections.unmodifiableList(new ArrayList<>(map.values()));

        //
        // Testing the speed of decorating a large map with a list interface.
        // With ONE MILLION String pairs, the decoration takes less than 100 ms.
        // Iterating over the Strings takes less than 300 ms.
        // The population takes more than 3 sec!
        //
        RunTimer timer = new RunTimer();
        Map<String, String> largeMap = SampleDataSource.hugeHashMap(1000000);
        timer.stop("populating the large map");

        timer.go();
        List<String> largeStringList = Collections.unmodifiableList(new ArrayList<>(largeMap.values()));
        timer.stop("decorating the map with a list interface");

        timer.go();
        largeStringList.forEach(s -> s.toLowerCase());
        timer.stop("iterating all elements");

    }
}
