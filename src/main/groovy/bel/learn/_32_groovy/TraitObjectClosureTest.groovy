package bel.learn._32_groovy

import groovy.transform.ToString

trait Adder {
    def add(other) {
        "ADDED: " + this + ", Other: " + other
    }

    def plus(other) {
        this.add(other)
    }
}

@ToString(includePackage = false, includeFields = true, includeNames = true)
class TraitTest implements Adder{
    int age
}

println("starting...") 
println new TraitTest(age: 2) + "Benno"


def addMult = {x,y ->
    x*y+x+y
}

assert addMult(2, 1) == 5
assert addMult("X", 2) == "XXX2"

def addSquare = {x,y ->
    (x+y)**2
}

assert addSquare(3,2) == 25

println "groovy version:  " + GroovySystem.version