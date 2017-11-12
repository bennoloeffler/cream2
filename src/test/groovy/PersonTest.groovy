import spock.lang.Specification

/**
 * Created 06.07.2017.
 *
 *
 */
class PersonTest extends Specification {
    def "ToString"() {
        setup:
        def p = new Person("X", 1)

        when:
        def s = p.toString()

        then:
        s == "Person(name=X, age=1)"
    }

    def "persons are usable with constructor lombok"() {
        setup:
        def p = new Person("Benno", 47)

        when:
        def s = p.toString()

        then:
        s == "Person(name=Benno, age=47)"
    }
}
