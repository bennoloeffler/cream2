package bel.learn._35_sqlite

import groovy.sql.DataSet
import groovy.sql.Sql

class SQLiteMain {
    static void main(String[] args) {


        Class.forName("org.sqlite.JDBC")

        def sql = Sql.newInstance("jdbc:sqlite:sample.db", "org.sqlite.JDBC")

        sql.execute("drop table if exists person")
        sql.execute("create table person (id integer, name string)")

        DataSet people = sql.dataSet("person")
        people.add(id:1, name:"leo")
        people.add(id:2,name:'yui')

        sql.eachRow("select * from person") {
            println("id=${it.id}, name= ${it.name}")
        }
    }
}
