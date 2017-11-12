package bel.learn._36_gstorm_ORM

import groovy.sql.Sql
import gstorm.Gstorm
/**
 * Created 15.10.2017.
 *
 *
 */

class GSPerson {
    String name, project, age
    long company
}       // this is your model class

class GSCompany {
    String name, street, town
}


class GStormMain {

    // Problem with groovy 2.4: https://github.com/kdabir/gstorm/issues/14
    // github: https://github.com/kdabir/gstorm.git
    static void main(String[] args) {

        Class.forName("org.sqlite.JDBC")

        def sql = Sql.newInstance("jdbc:sqlite:sample.db", "org.sqlite.JDBC")

        println (sql.connection.catalog)

        def g = new Gstorm(sql)
        g.stormify(GSPerson)                          // table automatically gets created for this class

        def person = new GSPerson(name: "benno", project: "löffler")

        person.save()                               // which saves object to db

        def result = GSPerson.where("name = 'kunal'") // pass any standard SQL where clause
        println result

        println "all records -> ${GSPerson.all}"      // get all objects from db

        person.name = "kunal XXX dabir"
        person.save()                               // saves the object back to db

        println GSPerson.get(person.id)               // loads the object by id

        //person.delete()
    }
}
