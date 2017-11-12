package bel.learn._03_lambdas;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Shows Collections with lambdas
 */
public class WithCollections {

    static void pr(Collection c) {
        System.out.print("VALUES: ");
        c.forEach((i) -> {System.out.print(i);  System.out.print(", ");});
        System.out.println();
    }

    /**
     * Custom collector - if a special implementaiton of the container is needed
     * @param <T>
     * @return
     */
    public static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return Collectors.toCollection(ArrayList::new);
    }

    public static void main(String[] args) {
        String[] data = {"erster", "zweiter", "dritter", "vierter", "fünfter", "sechster", "siebter", "achter", "neunter", "zehnter", "doppelt", "doppelt"};

        List<String> dataList = Arrays.asList(data);

        // forEach takes an Consumer... And if there is only one argument: A method reference.
        dataList.forEach(System.out::println);
        pr(dataList);

        // List transformed every entry
        dataList.replaceAll(item->item+"X");
        pr(dataList);

        // List distinct
        List<String> collect = dataList.stream().distinct().collect(toList());
        pr(collect);

        // List filtered
        collect = dataList.stream().filter(item->item.startsWith("z")).collect(toList());
        pr(collect);

        // List filtered, selfmade collector
        ArrayList<String> collectArrayList = dataList.stream().distinct().collect(toArrayList()); // get specific List... see toArrayList()
        pr(collectArrayList);

        // List to stream and collected in Map
        Map<Integer, String> m = dataList.stream().distinct().filter(i->i.contains("r")).collect(Collectors.toMap(i->i.hashCode(), i->i));
        System.out.println(m);

        // Map keySet(), values() and entrySet() delivers a stream
        String allKeys = m.keySet().stream().map(Object::toString).collect(Collectors.joining(", ")).toString();
        System.out.println(allKeys);

        // so its easy to filter a map to a map
        Map<Integer, String> newMap = m.entrySet().stream()
                .filter(e->e.getValue().contains("e"))
                .collect(Collectors.toMap(e -> Math.abs(e.getKey()/10000000), e -> e.getValue().toUpperCase()));

        System.out.println(newMap);

        // average is one reduction function... like min max findFirst...
        OptionalDouble average = dataList.stream().mapToInt(String::hashCode).map(Math::abs).average();
        if(average.isPresent()) {
            System.out.println(average);
        } else {
            System.out.printf("ooops...");
        }

        // https://docs.oracle.com/javase/tutorial/collections/streams/reduction.html
        List<Integer> collect1 = dataList.stream().map(e -> Math.abs(e.hashCode()) / 10000000).collect(toList());
        pr(collect1);

        pr(dataList);
        int sum = dataList.stream().map(e->Math.abs(e.hashCode())/10000000).reduce(0, (a,b)-> a+b);
        System.out.println(sum);

        Stream obst = Stream.of("bananas", "oranges", "apples");
        IntStream arr = IntStream.of(1, 1000);
        //String s = arr.map(Integer::toString).reduce("", String::concat);

        //Find Oldest Person
        final Comparator<String> comp = (p1, p2) -> p1.length() - p2.length();
        String longest = dataList.stream()
                .max(comp)
                .get();
        System.out.println(longest);

        //Find Youngest Person
        //  -This time instead create the Comparator as the argument to the min() method
        String shortest = dataList.stream()
                .min((p1, p2) -> Integer.compare(p1.length(), p2.length()))
                .get();
        System.out.println(shortest);
    }
}
