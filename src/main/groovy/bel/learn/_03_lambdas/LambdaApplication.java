package bel.learn._03_lambdas;

import bel.en.test.TestData;
import bel.learn._01_bean.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * look there:
 * https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
 * https://kevcodez.de/index.php/2015/07/java-8-lambda-tutorial-einstieg-in-lambda-und-streams/
 * https://blog.codecentric.de/2013/10/java-8-erste-schritte-mit-lambdas-und-streams/
 */



public class LambdaApplication {


    public static void main(String[] args) {
        LambdaApplication la = new LambdaApplication();
        la.doConverstion();
        la.doItWithPersons();
    }

    /**
     * @link https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html
     * exactly ONE abstract method in a @FunctionalInterface.
     * This is exactly the same as: Function<T, R>
     *
     * @param <F>
     * @param <T>
     */
    @FunctionalInterface
    interface DoConvertFromFtoT<F, T> {
        T convertFromFtoT(F from);
        //F doReverse(T from); // try it. Compiler error...
    }

    private void doConverstion() {
        String s = "123";
        DoConvertFromFtoT<String, Integer> con = str -> (Integer)Integer.parseInt(str);
        System.out.println("Conversion: " + con.convertFromFtoT(s));
    }

    List<Person> persons = new ArrayList<>();

    private void doItWithPersons() {

        //DoConvertFromFtoT<String, Integer> converter = (from) -> Integer.valueOf(from);
        // or even shorter:


        // nothing in...
        persons.forEach(p->System.out.println(p));


        // get some persons
        TestData.populate(persons);

        // the long version
        System.out.print("ALTER (1): ");
        persons.forEach((Person p) -> System.out.print(p.getAge() + "  "));
        System.out.println();

        // only one argument, then shorter...
        System.out.print("ALTER (2): ");
        persons.forEach(p -> System.out.print(p.getAge() + "  "));
        System.out.println();

        // now print the person completely
        System.out.println("Alle Personen:");
        persons.forEach(p -> System.out.println(p)); //the whole person!
        System.out.println("Nochmal Alle Personen:");
        persons.forEach(System.out::println); // even shorter, if the consumer consumes exactly one argument of right type

        // create a filtered stream (male persons)
        Stream<Person> filter = persons.stream().filter(p -> p.isMale());
        // and get it by a collector
        List<Person> maleList = filter.collect(Collectors.toList());
        System.out.println("Männer: " + maleList);

        //same but shorter
        maleList = persons.stream().filter(p->p.isMale()).collect(Collectors.toList());
        System.out.println("Männer: " + maleList);

        // find the minimum alpabetical
        System.out.println("Alphabetisches Min: " + persons.stream().min((p1, p2) -> p1.getFullName().compareTo(p2.getFullName())));

        // find the richest. Eiter with optional or with null element
        Optional<Person> p = persons.stream().max((p1, p2) -> Double.compare(p1.getMoney(), p2.getMoney()));
        Person person = persons.stream().max((p1, p2) -> Double.compare(p1.getMoney(), p2.getMoney())).orElse(null);
        System.out.println("Reichster (Optional): " + p);
        System.out.println("Reichster (null?): " + person);

    }

}
