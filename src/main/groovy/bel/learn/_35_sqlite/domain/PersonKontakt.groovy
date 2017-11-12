package bel.learn._35_sqlite.domain

import groovy.transform.ToString

/**
 * Created 16.10.2017.
 *
 *
 */
@ToString(includeNames = false)
class PersonKontakt {
    String titel
    String vorname
    String nachname
    String funktion
    String email
    String tel
    String mobile

    //FirmaOrt firmaOrt
}
