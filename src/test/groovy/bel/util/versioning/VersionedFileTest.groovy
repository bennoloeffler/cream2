package bel.util.versioning

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 */
class VersionedFileTest extends Specification {

    VersionedFile vf

    void setup() {

    }

    void cleanup() {
        vf?.delete()
        vft?.delete()
    }

    def "empty initial version"() {
        given:
        vf = new VersionedFile("test.txt")
        when:
        def x = vf.text // just use it
        then:
        vf.versions.size() == 1
        !vf.versions[0].changed
        vf.versions[0].text == ""
        vf.versions[0].author == null
        vf.versions[0].version == VersionedFile.INITIAL_VERSION
        x == ""
    }

    def "empty initial and identical second"() {
        given:
        vf = new VersionedFile("test.txt", "")
        when:
        vf.text = ""
        then:
        vf.versions.size() == 2

        !vf.versions[0].changed
        vf.versions[0].text == ""
        vf.versions[0].author == null
        vf.versions[0].version == VersionedFile.INITIAL_VERSION

        !vf.versions[1].changed
        vf.versions[1].text == ""
        vf.versions[1].author == null
        vf.versions[1].version == null
    }

    def "initial version"() {
        given:
        vf = new VersionedFile("initial.txt", "y", "bel")
        when:
        vf.text = "z"
        then:
        vf.versions*.changed == [true, true]
        vf.versions*.author == ["bel", "bel"]
    }

    @Unroll
    def "changes #idx"(idx, changed, text, author, version) {
        given:
        vf = new VersionedFile("test.txt", "")

        when:
        vf.text = ""

        vf.version = "named"
        vf.author = "bel"
        vf.text = "x"

        vf.text = ""

        vf.text = ""

        then:
        vf.versions[idx].changed == changed
        vf.versions[idx].text == text
        vf.versions[idx].author == author
        vf.versions[idx].version == version

        where:
        idx | changed | text | author | version
        0   | false   | ""   | null   | VersionedFile.INITIAL_VERSION
        1   | false   | ""   | null   | null
        2   | true    | "x"  | "bel"  | "named"
        3   | true    | ""   | "bel"  | null
        4   | false   | ""   | "bel"  | null
    }

    /*
    def "changes2"(vfm, closure, changeds, texts, authors, versionTags) {
        expect:
        closure() == true

        vfm.versions*.changed == changeds


        where:
        vfm                                   | closure                | changeds            | texts     | authors          | versionTags
        new VersionedFile("x.txt")   | {true}                          | [false]            | [""]         | [null]         | [VersionedFile.INITIAL_VERSION]
        new VersionedFile("x.txt")   | {vf.text = "z"; true}           |[false, true]       | ["", "z"]    | [null, null]   | [VersionedFile.INITIAL_VERSION, null]
    }*/

    @Shared VersionedFile vft
    def testData(create, doIt, changeds) {
        expect:
        create()
        doIt()
        vft.versions*.changed == changeds

        where:
        create                                                                      | doIt                      | changeds
        {it->vft = new VersionedFile('x.txt')}                              | {vft.text="x"}            | [false, true]
        {it->vft = new VersionedFile('x.txt', "x")}             | {vft.text="x"}            | [true, false]
        //new VersionedFile('x.txt', '')  |{} | 2   | [1,2]
    }

    def 'dir does not exist'() {
        setup:
        def path = "abc\\def\\yxz\\test.txt"
        when:
        vf = new VersionedFile(path)
        vf.text = "new text"
        then:
        // no exception - because it was created...
        new File(path).exists()

    }

    def deleteWorks() {
        setup:
        vf = new VersionedFile("toDelete.txt")
        vf.text = "wrote something"

        when:
        vf.delete()

        then:
        ! vf.dataFile.exists()
        ! vf.versionsFile.exists()
    }

    def diffHtml() {
        setup:
        vf = new VersionedFile("diffHtml")

        when:
        vf.text = """die erste Version - mit ein paar Änderungen\nund ein paar Zeilen\nund noch eine"""
        vf.text = """die zweite Version - mit einer großen Änderungen\nund ein paar Zeilen mehr\nund noch eine\nund noch eine ganz, ganz, ganz lange"""
        vf.text = """die dritte Version - mit ganz wenig..."""

        then:
        List<File> files = []
        (0..3).each {files[it] = new File("diffHtmlTest${it}.html"); files[it].text=vf.versions[it].getHTMLDiff()}
        (1..3).each {assert files[it].size() > 0}
        (0..3).each {files[it].delete()}
    }

    def diffHtmlToLatest() {
        setup:
        vf = new VersionedFile("diffHtml")

        when:
        vf.text = """die erste Version"""
        vf.text = """die zweite Version - mit einer großen Änderungen\nund ein paar Zeilen mehr\nund noch eine\nund noch eine ganz, ganz, ganz lange"""
        vf.text = """die dritte- mit ganz wenig..."""

        def html = vf.versions[1].getHTMLDiffToLatest()
        def f = new File("diffHtmlTail.html")
        f.text = html

        then:
        f.size() > 0
        f.delete()
        }

}
