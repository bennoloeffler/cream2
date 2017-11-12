package bel.learn._04_lomlok;

import lombok.extern.java.Log;
import lombok.val;

/**
 * @see <a href="https://projectlombok.org/">lombok</a>
 * Aber gegen alle Ratschläge habe ich die lomlok jar in den classpath gehängt.
 * Erst dann gings.
 */
@Log
public class Main {

    public static void main(String[] args) {
        PersonLombok personLombok = new PersonLombok();
        personLombok.setMoney(30000); // this is a miracle!
        log.info("money: " + personLombok.getMoney()); // this too!

        PersonLombok p = new PersonLombok(false, "Hugo Hülsensack", 47, new int[] {1,2,3,4,5} , 2354.78 );
        log.info("Person: " + p);

        try {
            PersonLombok pNonNullDemo = new PersonLombok(false, null, 47, new int[] {1,2,3,4,5} , 2354.78 );
        } catch (Exception e) {
            System.out.println(e);
        }

        val miracleLocal = new PersonLombok(); // val is keyword and derives type from assigned type;
        log.info("person: " + miracleLocal);

        val x = 5;

        PublicBath b = PublicBath.builder().swimMaster("Bernard").nameOfBath("Heslach"). build();

    }
}
