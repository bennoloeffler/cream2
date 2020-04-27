package bel.learn._32_groovy

import groovy.transform.CompileStatic
import groovy.transform.ToString



// inspired by:
s1 = "http://dl.finebook.ir/book/7e/11492.pdf"
s2 = "https://learnxinyminutes.com/docs/groovy/"
s3 = "http://developers-club.com/posts/122127/"
s4 = "https://kousenit.org/2014/10/14/spaceships-elvis-and-groovy-inject/"
s5 = "http://mrhaki.blogspot.de/2015/03/groovy-goodness-swapping-elements-in.html" // really many cool examples
s6 = "http://docs.groovy-lang.org/docs/groovy-2.4.11/html/"

/**
 * just make it really simple to print
 * p x 
 * will print the value of variable x
 */
void p(s){ // script global method without type declaration for argument...
    println s
}

// test it
p "BELs 'groovy in 15 minutes'"
p "inspired by: $s1"
p "and        : $s2"
p "and        : $s3"
p "and        : $s4"
p "and        : $s5"
p "and        : $s6"
p "Thanks a lot!"


/**
 * prints a header
 * @param s
 */
@SuppressWarnings("GroovyAssignabilityCheck")
void h(s) {
    def a = 50
    println ("\n\n\n" + '-' * (a-s.size()/2) + "   " + s + "   " + '-' * (a-s.size()/2))
}


//-------------------------------------------------------------------------------------------------------------
//
h "dynamic typing, TODO: coercion (or how is it called? ;-)) "
//
//

x=1407
p x // seems to work...
p x.class // EVERYTHING is an object: this x is not an int, but an Integer
x = -3.1499392 // dynamic change of type...
p x.class
x = 'java'
p x.class

assert 9.intdiv(4) == 2
assert 9 / 4 == 2.25
assert 9 % 4 == 1
assert --9 == 8

//-------------------------------------------------------------------------------------------------------------
//
h "coercion and type conversion"
//
// https://e.printstacktrace.blog/groovy-dynamic-types-coercion-and-promotion-you-have-been-warned/
// Kapitel 12: https://groovy-lang.org/differences.html

def strToNum = "12" as int
assert strToNum == 12

// prepare your own class for coercion: method: asType(Class target)
@groovy.transform.TupleConstructor
@groovy.transform.ToString
def class Line {
    double x1,y1, x2,y2 // two points
    def asType (Class target) {
        println(target)
        if (target == java.lang.Double.class) {
            def len = Math.sqrt((x1 - x2)**2 + (y1 - y2)**2) // len of line
            return len
        } else {
            throw new RuntimeException("not supported: " + target);
        }


    }
}

line = new Line(2,2,1,1)
len = line as Double
assert Math.abs(len)-1.414 < 0.001


//
// amazing automatic coercion
//
Line l = [3,4,5,6] // List to Object...
int strToIntAutomatic = "123"   

//-------------------------------------------------------------------------------------------------------------
//
h "groovy truth"
//
//
assert "y".toBoolean()
assert 'TRUE'.toBoolean()
assert '  trUe  '.toBoolean()
assert " y".toBoolean()
assert "1".toBoolean()

assert ! 'other'.toBoolean()
assert ! '0'.toBoolean()
assert ! 'no'.toBoolean()
assert ! '  FalSe'.toBoolean()

assert ![] // empty list is false
assert ["element"]
assert !"" // empty string?

def s
assert s?:15
assert !0

//-------------------------------------------------------------------------------------------------------------
//
h "strings"
//
//

javaString = 'java'
j = '${javaString}' // does not work in javastring String!
p j.class
groovyString = "${javaString}" // only works in GString
p groovyString.class
bigGroovyString = """
${javaString} 
${groovyString} 
${j} 
${2 + 2}"""

p bigGroovyString

assert "this is a text with Paul in between" - "Paul" == "this is a text with  in between"

//-------------------------------------------------------------------------------------------------------------
//
h "lists and maps - adding and removing, iterating, sorting, etc"
//
//

//Creating an empty list
def technologies = []

/*** Adding a elements to the list ***/

// As with Java
technologies.add("Grails")

// Left shift adds, and returns the list
technologies << "Groovy"

// Add multiple elements
technologies.addAll(["Gradle","Griffon"])

p technologies

/*** Removing elements from the list ***/

// As with Java
technologies.remove("Griffon")

// Subtraction works also
technologies -= 'Grails' // -= and += seems to work here, too...

assert technologies == ["Groovy", "Gradle"]

/*** Iterating Lists ***/

// Iterate over elements of a list with it as implicit variable
technologies.each { print "Technology: $it  "}
technologies.eachWithIndex { it, i -> print "$i: $it  "}

/*** Checking List contents ***/

//Evaluate if a list contains element(s) (boolean)
contained = technologies.contains( 'Groovy' )

// Or
contained = 'Groovy' in technologies

// Check for multiple contents
technologies.containsAll(['Groovy','Grails'])

/*** Sorting Lists ***/

// Sort a list (mutates original list)
technologies.sort()

// To sort without mutating original, you can do:
sortedTechnologies = technologies.sort( false )

/*** Manipulating Lists ***/

//Replace all elements in the list
Collections.replaceAll(technologies, 'Gradle', 'gradle')

//Shuffle a list
Collections.shuffle(technologies, new Random())

//Clear a list
technologies.clear()

//Creating an empty map
def devMap = [:]

//Add values
devMap = ['name':'Roberto', 'framework':'Grails', 'language':'Groovy']
devMap.put('lastName','Perez')

//Iterate over elements of a map
devMap.each { println "$it.key: $it.value" }
devMap.eachWithIndex { it, i -> println "$i: $it"}

//Evaluate if a map contains a key
assert devMap.containsKey('name')

//Evaluate if a map contains a value
assert devMap.containsValue('Roberto')

//Get the keys of a map
println devMap.keySet()

//Get the values of a map
println devMap.values()


//-------------------------------------------------------------------------------------------------------------
//
h "functions and closures"
//
//

// java like (without def, with return). 'def' Makes the scope global!
// @see http://stackoverflow.com/questions/184002/groovy-whats-the-purpose-of-def-in-def-x-0
@SuppressWarnings("GrMethodMayBeStatic")
double calc0(double a, double b) {
    return a / (b*b);
}

a = calc0(1,2)
//noinspection GrEqualsBetweenInconvertibleTypes
assert a == 0.25
b = calc0 9, 3
//noinspection GrEqualsBetweenInconvertibleTypes
assert b == 1

// without return and with def - groovy style
@SuppressWarnings("GrMethodMayBeStatic")
def int calc(int a, int b){
    a*b
    a+b // last expression will be returned
}
assert calc(2,3) == 5

@SuppressWarnings("GrMethodMayBeStatic")
def int calc2(a, b){ // without type declaration
    a*b
}
assert calc2(2,3) == 6

// functions can be used as parameters (closures in java)
def int printAndCalc(a, b, closure){
    //p closure.metaClass.classNode.getDeclaredMethods("doCall")[0].code.text
    //p closure.metaClass.getDeclaredMethods("doCall")[0].code.text
    //p closure.metaClass.closureMethods.getDeclaredMethods("doCall")[0].code.text
    p a + ", " + b + " -> " + closure.metaClass.getMethods()[-1] // print the declaration of the last method which is the closure method
    closure a, b // this will call a function with a and b as parameters x(y,z)
}
assert printAndCalc(100, 12, {x,y -> x*y}) == 1200

bigNum = 1000
// closures have access to non-final variables in contrast to java
assert printAndCalc(100, 12, {x,y -> x*y*bigNum}) == 1200000
bigNum = 100

def timesTen = {int x, int y -> 10 * x * y} // with declaration of types
assert printAndCalc(100,12, timesTen) == 12000

def closureOfClosure = {a,b -> //noinspection GroovyAssignabilityCheck
    12000 / timesTen (a,b) } // without declaration
assert printAndCalc(100,12, closureOfClosure) == 1

/**
 *
 * @return List<String> BUT, this declaration does only make sense
 * 1) if there is really! only Stings in there and
 * 2) if there is java code calling that function.
 * Because without List<String> it will be a hassle to call that from java.
 */
//@TypeChecked
def List<String> createList(){
    return ['Paul', 'is', 17] // without @TypeChecked THIS WORKS!
}

strList = createList()
p "OHHHH.. an List<Strings>? Groovy ignores that... Its just for documentation and for interoperability with java" + strList


// for this example, see: https://www.youtube.com/watch?v=URkFOLywex4&feature=youtu.be
class Mailer {

    public static send(closure) {
        Mailer m = new Mailer()
        m.with closure
        println("closure with delegate... Enables un-noisy user code")
    }
    def from(sender) { println "from: $sender" }
    def to(receiver) { println "to: $receiver" }
    def msg(msg) { println "msg: $msg" }

}

// now this goes without any noise (repetition)
Mailer.send {
    from('loeffler@v-und-s.de')
    to('sabine.kiefer@gmx.de')
    msg('bussi')
}


//-------------------------------------------------------------------------------------------------------------
//
h "closures with multithreading"
//
//

/*
  Groovy can memoize closure results [1][2][3]
*/
def cl = {a, b ->
    sleep(3000) // simulate some time consuming processing
    a + b
}

mem = cl.memoize()

def callClosure(a, b) {
    def start = System.currentTimeMillis()
    mem(a, b)
    println "Inputs(a = $a, b = $b) - took ${System.currentTimeMillis() - start} msecs."
}

callClosure(1, 2)
callClosure(1, 2)
callClosure(2, 3)
callClosure(2, 3)
callClosure(3, 4)
callClosure(3, 4)
callClosure(1, 2)
callClosure(2, 3)
callClosure(3, 4)



//-------------------------------------------------------------------------------------------------------------
//
h "if else, save navigation, ternary op, elvis"
//   ***   if else   ***
//

z=null

if (!z) { // false and null is usable and invertable
    p "!z is"
} else {
    p "z not (null or false)"
}

if(! (z?.attribute==1)) { // ?. save navigation. this will prevent a NullPointerException, instead will deliver null 
    p "z null or attribute != 1"
} else if(a) {
    p "a"
} else {
    p "not a"
}

//Groovy also supports the ternary operator:
def y = 10
def x = (y > 1) ? "worked" : "failed"
assert x == "worked"

//Groovy supports 'The Elvis Operator' too!
//Instead of using the ternary operator:

MetaClass user = null
//noinspection GroovyUnusedAssignment
displayName = user?.class ? user?.class : 'Anonymous'

//We can write it:
displayName = user?.class ?: 'Anonymous'
assert 'Anonymous' == displayName

a = "a"
def b = a ?: "b" // Elvis Operator: b = a!=null?a:"b" short: a?:"b" (if a is null or false then default value else a)
assert b == "a"

a = null
b = a ?: "b"
assert b == "b"


//-------------------------------------------------------------------------------------------------------------
//
h "looping, iterating"
//
//

a="numbers "
10.times{
    //noinspection GrReassignedInClosureLocalVar
    a = a + it
}
assert a == "numbers 0123456789"
p a

100.upto 120, {
    //noinspection GrReassignedInClosureLocalVar
    a = a + " " + it
}
assert a == "numbers 0123456789 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 117 118 119 120"
p a

a=100
b=200
s=0
a.upto b, {//noinspection GrReassignedInClosureLocalVar
    s=s+it
}
p s

List list=[];
for (i in 0..9) { list << i }
for (int i = 0; i <= 9; ++i) { list << i }
for (Integer i : 0..9) { list << i }
assert list.size() == 30

list = list[(1..8).step(2)]
assert list == [1, 3, 5, 7]
list17 = list.collect{it -> it.toString()}
assert list17 == ["1", "3", "5", "7"]

list.clear()
while(a>0) {
    list << a
    a-=1
}
assert list == (100..1).collect()
["Benno", "Löffler"].each{p it}


//Iterate over a list
x3 = 0
for( i in [5,3,2,1] ) {
    x3 += i
}

//Iterate over an array
array = (0..20).toArray()
x4 = 0
for (i in array) {
    x4 += i
}

//Iterate over a map
def map = ['name':'Roberto', 'framework':'Grails', 'language':'Groovy']
x5 = 0
for ( e in map ) {
    x5 += e.value
}


//-------------------------------------------------------------------------------------------------------------
//
h "slicing"
//
//

// first of all slicing with a GString
a = "0123456789"
assert a[1..4] == "1234"
assert a[1..-1] == "123456789" // - means from the right side 
assert a[-1..0] == "9876543210" // BACKWARDS!
assert a[1..<9] == "12345678" // excluding
//noinspection GroovyAssignabilityCheck
assert a[1, 3, 5] == "135"
range = 1..5
p range.class
assert a[range] == "12345" // cool.. range as variable
ss = a[0..2, 4..5, 7..9]
assert ss == "01245789"

List names = "Benno Sabine Benno Paul Leo".split() // this implicitly transforms an array to a list!
assert names == ["Benno","Sabine", "Benno", "Paul", "Leo"]
assert names[0] == "Benno"
assert names[-1] == "Leo" // count from behind
//noinspection GroovyAssignabilityCheck
assert names[1,3,4,0] == ["Sabine", "Paul","Leo", "Benno"]
assert names[1..3] == ["Sabine", "Benno", "Paul"] // a range
assert names[-2..1] == ["Paul","Benno","Sabine"] // even backwards

assert names - ["Benno", "Leo"] == ["Sabine",  "Paul"]


//-------------------------------------------------------------------------------------------------------------
//
h "advanced lists, maps: each, findAll, collect, inject, sort, sum, ..."
//
//

p  "MAPS:  http://docs.groovy-lang.org/latest/html/groovy-jdk/java/util/Map.html"
p  "LISTS: http://docs.groovy-lang.org/latest/html/groovy-jdk/java/util/List.html"
p  "COLL:  http://docs.groovy-lang.org/latest/html/groovy-jdk/java/util/Collection.html"

assert names.unique() == ["Benno","Sabine", "Paul", "Leo"]

// https://kerflyn.wordpress.com/2010/06/25/list-comprehension-in-groovy/

(1..4).inject() { result, i ->
    println "$result + $i = ${result + i}"
    result + i
}

List strings = 'this is a list of strings'.split()

strings.sort { s1, s2 -> //noinspection GrUnresolvedAccess
    s1.size() <=> s2.size()  // starship operator returns -1 0 1
}
//noinspection GrUnresolvedAccess
assert strings*.size() == [1, 2, 2, 4, 4, 7]

// this time even more sophisticated: if same, then alphabetically
strings = 'this is a list of strings'.split()
strings.sort { s1, s2 ->
    //noinspection GrUnresolvedAccess
    s1.size() <=> s2.size() ?: s2 <=> s1
}

List upper = strings.collect {String it -> it * 2 }      // double them all
        .findAll { it.contains("t") } // find those containing a t
        .collect {it.toUpperCase()}                   // add them up
assert upper == ["THISTHIS", "LISTLIST", "STRINGSSTRINGS"]

// create a map with letter:pos mapping
def letterList = 'A'..'J'
letterMap = (1..letterList.size()).inject([:]) { m, pos -> //noinspection GroovyAssignabilityCheck
    m [ letterList[pos-1] ] = pos; m }
p letterMap

class User {
    String name
    String aloud() { name.toUpperCase() }
    String toString() { name }
}

def users = [new User(name: 'mrhaki'), new User(name: 'hubert')]

assert ['mrhaki', 'hubert'] == users*.toString()
assert ['MRHAKI', 'HUBERT'] == users*.aloud()
assert ['mrhaki', 'hubert'] == users.collect { it.toString() }
assert ['MRHAKI', 'HUBERT'] == users.collect { it.aloud() }


// push and pop at the end...

def plist = ['Groovy', 'is', 'great!']

// Remove last item from list
// with pop().
assert plist.pop() == 'great!'
assert plist == ['Groovy', 'is']

// Remove last item
// which is now 'is'.
plist.pop()

// Add new item to end of
// the list (equivalent for add()).
plist.push('rocks!')

assert plist == ['Groovy', 'rocks!']

// swapping

def saying = ['Groovy', 'is', 'great']
def yodaSays = saying.swap(2, 1).swap(0, 1)
assert yodaSays.join(' ') == 'great Groovy is'

// joining to Strings, with maps injecting in n array

def abc = ['a', 'b', 'c']
assert 'abc' == abc.join()
assert 'a::b::c' == abc.join('::')

def numbers = [0, 1, 2, 3, 4, 5] as Integer[]
assert '012345' == numbers.join()
assert '0 x 1 x 2 x 3 x 4 x 5' == numbers.join(' x ')
assert '0 1 2 3 4 5' == numbers.join(' ')

def objects = [new URL('http://www.mrhaki.com'), 'mrhaki', new Expando(name: 'mrhaki'), new Date(109, 10, 10)]
assert 'http://www.mrhaki.com,mrhaki,{name=mrhaki},Tue Nov 10 00:00:00 UTC 2009' == objects.join(',')

// Also great for creating URL query parameters.
map = [q: 'groovy', maxResult: 10, start: 0, format: 'xml']
def params = map.inject([]) { result, entry ->
    result << "${entry.key}=${URLEncoder.encode(entry.value.toString())}"
}.join('&')
assert 'q=groovy&maxResult=10&start=0&format=xml' == params


//-------------------------------------------------------------------------------------------------------------
//
h "files"
//
//

// write
def pw = new File('textfile.txt').newPrintWriter()
pw.println("new line")
pw.close() // should not be neccessary? is there a with notation?

// find
new File('.').eachFile { println it }

// read
new File('textfile.txt').eachLine { println it }
def linesTextFile = new File('textfile.txt').readLines()
assert linesTextFile.size() == 1
assert linesTextFile[0] == "new line"

// delete
assert new File('textfile.txt').delete()


// Normal way of creating file objects.
def file1 = new File('groovy1.txt')
def file2 = new File('groovy2.txt')
def file3 = new File('groovy3.txt')

// Writing to the files with the write method:
file1.write 'Working with files the Groovy way is easy.\n'

// Using the leftShift operator:
file1 << 'See how easy it is to add text to a file.\n'

// Using the text property:
file2.text = '''We can even use the text property of
a file to set a complete block of text at once.'''

// Or a writer object:
file3.withWriter('UTF-8') { writer ->
    writer.write('We can also use writers to add contents.')
}

// Reading contents of files to an array:
def lines = file1.readLines()
assert 2 == lines.size()
assert 'Working with files the Groovy way is easy.' == lines[0]

// Or we read with the text property:
assert 'We can also use writers to add contents.' == file3.text

// Or with a reader:
count = 0
file2.withReader { reader ->
    while (line = reader.readLine()) {
        switch (count) {
            case 0:
                assert 'We can even use the text property of' == line
                break
            case 1:
                assert 'a file to set a complete block of text at once.' == line
                break
        }
        count++
    }
}

// We can also read contents with a filter:
sw = new StringWriter()
file1.filterLine(sw) { it =~ /Groovy/ }
assert 'Working with files the Groovy way is easy.\r\n' == sw.toString()

// We can look for files in the directory with different methods.
// See for a complete list the File GDK documentation.
files = []
new File('.').eachFileMatch(~/^groovy.*\.txt$/) { files << it.name }
assert ['groovy1.txt', 'groovy2.txt', 'groovy3.txt'] == files

// Delete all files:
files.each { new File(it).delete() }

//-------------------------------------------------------------------------------------------------------------
//
h "expando"
//
//

p "http://docs.groovy-lang.org/latest/html/gapi/groovy/util/Expando.html"

def userEx = new Expando(username: 'mrhaki')
assert 'mrhaki' == userEx.username

// Add an extra property.
userEx.email = 'email@host.com'
assert 'email@host.com' == userEx.email

// Assign closure as method. The closure can
// take parameters.
userEx.printInfo = { writer ->
    writer << "Username: $username"
    writer << ", email: $email"
}

def sw = new StringWriter()
userEx.printInfo(sw)
assert 'Username: mrhaki, email: email@host.com' == sw.toString()
p userEx


//-------------------------------------------------------------------------------------------------------------
//
h "Metaprogramming (MOP)"
//
//

//Using ExpandoMetaClass to add behaviour
String.metaClass.testAdd = {
    println "we added this"
}

String x7 = "test"
x7?.testAdd()

//Intercepting method calls
class Test23 implements GroovyInterceptable {
    def sum(Integer x, Integer y) { x + y }

    def invokeMethod(String name, args) {
        System.out.println "Invoke method $name with args: $args"
    }
}

def test = new Test23()
test?.sum(2,3)
test?.multiply(2,3)

//Groovy supports propertyMissing for dealing with property resolution attempts.
class Foo45 {
    def propertyMissing(String name) { name }
}
def f = new Foo45()

assert "boo" == f.boo


//-------------------------------------------------------------------------------------------------------------
//
h "xml"
//
//

p "http://mrhaki.blogspot.de/2009/10/groovy-goodness-reading-xml.html"

def xml = '''
<books xmlns:meta="http://meta/book/info" count="3">
  <book id="1">
    <title lang="en">Groovy in Action</title>
    <meta:isbn>1-932394-84-2</meta:isbn>
  </book>
  <book id="2">
    <title lang="en">Groovy Programming</title>
    <meta:isbn>0123725070</meta:isbn>
  </book>
  <book id="3">
    <title>Groovy &amp; Grails</title>
    <!--Not yet available.-->
  </book>
  <book id="4">
    <title>Griffon Guide</title>
  </book>
</books>
'''

def books = new XmlSlurper().parseText(xml).declareNamespace([meta:'http://meta/book/info'])
assert books instanceof groovy.util.slurpersupport.GPathResult
assert 4 == books.book.size()
assert 11 == books.breadthFirst().size()
assert 'Groovy in Action' == books.book[0].title.text()
assert 'Groovy Programming' == books.book.find { it.@id == '2' }.title as String
assert [1, 2, 3] == books.book.findAll { it.title =~ /Groovy/ }.'@id'*.toInteger()
assert ['1-932394-84-2', '0123725070'] == books.book.'meta:isbn'*.toString()

//-------------------------------------------------------------------------------------------------------------
//
h "regex"
//
//

def finder = ('groovy' =~ /gr.*/)
assert finder instanceof java.util.regex.Matcher

if('groovy' =~ /gr.*/) {
    p "found gr.*"
}

def matcher = ('groovy' ==~ /gr.*/)
assert matcher instanceof Boolean

assert 'Groovy rocks!' =~ /Groovy/  // =~ in conditional context returns boolean.
assert !('Groovy rocks!' ==~ /Groovy/)  // ==~ looks for an exact match.
assert 'Groovy rocks!' ==~ /Groovy.*/

def cool = /gr\w{4}/  // Start with gr followed by 4 characters.
def findCool = ('groovy, java and grails rock!' =~ /$cool/)
assert 2 == findCool.count
assert 2 == findCool.size()  // Groovy adds size() method.
assert 'groovy' == findCool[0]  // Array-like access to match results.
assert 'grails' == findCool.getAt(1)

// With grouping we get a multidimensional array.
def group = ('groovy and grails, ruby and rails' =~ /(\w+) and (\w+)/)
assert group.hasGroup()
assert 2 == group.size()
assert ['groovy and grails', 'groovy', 'grails'] == group[0]
assert 'rails' == group[1][2]

// Use matcher methods.
assert 'Hi world' == ('Hello world' =~ /Hello/).replaceFirst('Hi')

// Groovy matcher syntax can be used in other methods.
assert ['abc'] == ['def', 'abc', '123'].findAll { it =~ /abc/ }
assert [false, false, true] == ['def', 'abc', '123'].collect { it ==~ /\d{3}/ }


//-------------------------------------------------------------------------------------------------------------
//
h "groovy bean"
//
//

/*
  Groovy Beans

  GroovyBeans are JavaBeans but using a much simpler syntax

  When Groovy is compiled to bytecode, the following rules are used.

    * If the name is declared with an access modifier (public, private or
      protected) then a field is generated.

    * A name declared with no access modifier generates a private field with
      public getter and setter (i.e. a property).

    * If a property is declared final the private field is created final and no
      setter is generated.

    * You can declare a property and also declare your own getter or setter.

    * You can declare a property and a field of the same name, the property will
      use that field then.

    * If you want a private or protected property you have to provide your own
      getter and setter which must be declared private or protected.

    * If you access a property from within the class the property is defined in
      at compile time with implicit or explicit this (for example this.foo, or
      simply foo), Groovy will access the field directly instead of going though
      the getter and setter.

    * If you access a property that does not exist using the explicit or
      implicit foo, then Groovy will access the property through the meta class,
      which may fail at runtime.

*/

// http://mrhaki.blogspot.de/2011/04/groovy-goodness-easy-tostring-creation.html
//@ToString(includeNames=true, includeFields=true, excludes='active,likes')
@ToString(includeNames=true)
class FooBar {
    // read only property
    final String name = "Roberto"

    // read only property with public getter and protected setter
    String language
    protected void setLanguage(String language) { this.language = language }

    // dynamically typed property
    def lastName
}

fb = new FooBar(language: "spanish", lastName: "Rodriguez")
p fb.toString()


//-------------------------------------------------------------------------------------------------------------
//
h "TypeChecked and CompileStatic"
//
//

/*
  Groovy, by nature, is and will always be a dynamic language but it supports
  typechecked and compilestatic

  More info: http://www.infoq.com/articles/new-groovy-20
*/
//TypeChecked
void testMethod() {}

/*
@TypeChecked
void test() {
    testMeethod()

    def name = "Roberto"

    println naameee

}
*/

//Another example:
/*
@TypeChecked
Integer test() {
    Integer num = "1"

    Integer[] numbers = [1,2,3,4]

    Date date = numbers[1]

    return "Test"

}*/

//CompileStatic example:
@CompileStatic
int sum(int x, int y) {
    x + y
}

assert sum(2,5) == 7
