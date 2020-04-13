package bel.learn._39_gorm

@Grapes([
        @Grab("org.grails:grails-datastore-gorm-hibernate5:6.1.4.RELEASE"),
        @Grab("com.h2database:h2:1.4.192"),
        @Grab("org.apache.tomcat:tomcat-jdbc:8.5.0"),
        @Grab("org.apache.tomcat.embed:tomcat-embed-logging-log4j:8.5.0"),
        @Grab("org.slf4j:slf4j-api:1.7.10")
])

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity
import org.grails.orm.hibernate.HibernateDatastore

// Create the domain classes you want
@Entity
class Person implements GormEntity<Person> {

    String firstName
    String lastName

    static mapping = {
        firstName blank: false
        lastName blank: false
    }
}

//Guts of the script, do your db stuff here with the power of Gorm
gormScript {
    println "running gormScript"
    new Person(firstName: 'Dave', lastName: 'Ronald').save()
    new Person(firstName: 'Sergio', lastName: 'Happy').save()
    new Person(firstName: 'Sergio', lastName: 'del Amo').save()
    new Person(firstName: 'Monica', lastName: 'Crazy').save()

    println "People = ${Person.count()}"

    def sergio = Person.findByFirstName("Sergio")

    println "Got ${sergio.firstName} ${sergio.lastName}"
}


def gormScript(Closure exec) {
    println "running gormScript(Closure exec) exec= ${exec.toString()}"
    Map configuration = [
            'hibernate.hbm2ddl.auto':'create-drop',
            'dataSource.url':'jdbc:h2:mem:myDB'
    ]
    HibernateDatastore datastore = new HibernateDatastore( configuration, Person)

    Person.withTransaction {
        exec()
    }
}
