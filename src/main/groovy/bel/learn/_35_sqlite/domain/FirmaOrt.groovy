package bel.learn._35_sqlite.domain

/**
 *
 */
class FirmaOrt {
    String firmenname
    String strasse
    String plz
    String ort
    String tags
    PersonKontakt[] pks



    public static void main(String[] args) {
        def list = [1,2,"2323"]
        List<PersonKontakt> pks = [new PersonKontakt(vorname: 'benno')]
        println(pks)
    }
}
