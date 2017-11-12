package bel.util.versioning

import bel.util.DiffMatchPatch
import bel.util.Util

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

import static org.codehaus.groovy.tools.Utilities.eol
/**
 *  Create file with initial data and author.
 *  An initial version entry does NOT contain the diff string - because that is the whole initial data...
 *  But then, every diff is recorded, so that you can go back in version history.
  */
class VersionedFile {

    @Delegate File dataFile
    final static def V_FILE_SUFFIX = ".versions.txt"
    final static def INITIAL_VERSION = "INIT"
    final File versionsFile
    String author
    String version
    final List<VersionEntry> versions

    VersionedFile(String fileName, String initialDataText="", String theDefaultAuthor=null) {
        versions = []
        author = theDefaultAuthor
        dataFile = new File(fileName)
        assert "datafile $fileName exists: NO INIT ALLOWED! delete before",
                dataFile.exists() ?
                initialDataText==null && versionsFile.exists() :
                initialDataText!=null && !versionsFile.exists()

        versionsFile = new File(fileName+V_FILE_SUFFIX)
        if(initialDataText || !dataFile.exists()) {
            makeSureParentDirExists(dataFile)
            assert dataFile.createNewFile()
            assert  versionsFile.createNewFile()
            versions << new VersionEntry (author: theDefaultAuthor, patch: null, version: INITIAL_VERSION, time: Instant.now())
            assert versions.size() == 1
            dataFile.text = initialDataText
            versionsFile.text = versions[0].versionString()
        } else {
            assert dataFile.exists()
            assert  versionsFile.exists()
            parseVersionEntries()
        }
    }

    private def makeSureParentDirExists(File file) {
        Path pathToFile = Paths.get(file.getAbsolutePath())
        Path pathToParent = pathToFile.getParent()
        def dir = new File(pathToParent.toString())
        if(!dir.exists()) {
            Files.createDirectories(Paths.get(dir.getAbsolutePath()))
            assert(dir.exists())
            assert(dir.isDirectory())
        }
    }
/**
     * @param text
     * @return
     */
    //@Override
    def setText(String text) {
        def e = calcVersionEntry(text, dataFile.text, author, version)
        dataFile.text = text
        versionsFile << e.versionString()
        versions << e
        version = null // reset it...
    }

    /**
     */
    //@Override
    def getText() {
        dataFile.text
    }


    //@Override
    void append(String text) {
        def oldText = dataFile.text
        dataFile << text
        def e = calcVersionEntry(dataFile.text, oldText, author, version)
        versionsFile << e.versionString()
        versions << e
        version = null // reset it...
    }


    //@Override
    boolean delete() {
        def v = versionsFile.delete()
        def d = dataFile.delete()
        //assert v & d
        v & d
    }


    private def parseVersionEntries() {
        //versions = []
        def lines = versionsFile.readLines()
        assert lines.size() % 3 == 0
        //println lines.size()
        (0..lines.size()-1).step(3) {
            //println (" it is: $it")
            def timeAuthorVersion = lines[it]
            def patch = lines[it+1]
            patch = patch.replace("patch: ", "").trim()
            if( patch == "NO_PATCH") patch = null
            def empty = lines[it+2]
            assert empty.trim() == ""
            def split = timeAuthorVersion.split("(time: |author: |version: )")
            split = split*.trim()
            split = split.findAll{ it.size() > 0} // remove empty ones
            assert split.size() == 3
            def time = Util.utcTimeFrom(split[0])
            def author = split[1]
            if(author == "NO_AUTHOR") author = null
            def version = split[2]
            if( version == "NO_VERSION") version = null
            versions << new VersionEntry(author: author, time: time, patch: patch, version: version)
        }
    }


    private VersionEntry calcVersionEntry(String newDataText,
                                          String oldDataText,
                                          String theAuthor,
                                          String versionTag) {
        List<DiffMatchPatch.Diff> diffs = DiffMatchPatch.get().diff_main(newDataText, oldDataText)
        String patchTextLine = DiffMatchPatch.get().diff_toDelta(diffs)
        //println ("patch: '$patchTextLine'")
        new VersionEntry(author: theAuthor, patch: patchTextLine, version: versionTag, time: Instant.now() )

    }

    class VersionEntry {

        String author
        Instant time
        String patch
        String version

        String versionString() {
            def result ="""time: ${Util.utcTimeFrom(time)}   author: ${author?:"NO_AUTHOR"}   version: ${version?:"NO_VERSION"}${eol()}patch: ${patch?:"NO_PATCH"}${eol() * 2}"""
            return result
        }

        //@Memoized
        String getText() {
            String currentText = VersionedFile.this.dataFile.text
            def versionEntries = VersionedFile.this.versions
            def currentVersionEntry = versionEntries.size()-1
            while (true) {
                if(versionEntries[currentVersionEntry] == this) {break}
                def patch = versionEntries[currentVersionEntry].patch
                LinkedList<DiffMatchPatch.Diff> diffs = DiffMatchPatch.get().diff_fromDelta(currentText, patch)
                currentText = DiffMatchPatch.get().diff_text2(diffs)
                currentVersionEntry -= 1
            }
            return currentText
        }

        private List<String> getOldAndCurrentVersion() {
            def result = []
            def oldText
            def currentText
            def ves = VersionedFile.this.versions
            assert ves.contains(this)
            def idx = ves.indexOf(this)
            if( idx > 0 ) {
                currentText = ves[idx].text
                oldText =  ves[idx-1].text
            } else {
                currentText = ves[idx].text
                oldText = ""
            }
            result << oldText
            result << currentText
            return result
        }


        boolean isChanged() { // this could be much faster by "=232" or "NO_PATCH"
            def oldText
            def currentText
            (oldText, currentText) = getOldAndCurrentVersion()
            return oldText != currentText
        }

        String getHTMLDiffToLatest() {
            def ves = VersionedFile.this.versions
            def currentText = this.getText()
            def headText = ves[ves.size()-1].getText()
            return htmlDiff(currentText, headText)

        }

        String getHTMLDiff() {
            def oldText
            def currentText
            (oldText, currentText) = getOldAndCurrentVersion()
            return htmlDiff(oldText, currentText)
            //return diffAsHTML
        }

        private GString htmlDiff(String fromText, String currentText) {
            LinkedList<DiffMatchPatch.Diff> diffs = DiffMatchPatch.get().diff_main(fromText, currentText)
            DiffMatchPatch.get().diff_cleanupSemantic(diffs)
            def diffAsHTML = DiffMatchPatch.get().diff_prettyHtml(diffs)
            return """
<html lang="de">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aenderung der Uebersicht</title>
    <style>
        body {
            background-color: white; 
            font-family: Verdana, Arial, Helvetica, sans-serif; 
            font-size: 100%; 
            line-height: 180%;
            } 

        h1   {color: orange;} 
        h2   {color: gray;} 
        h3   {color: black;} 

        div {color: grey;}
    </style>
  </head>
  <body>
    $diffAsHTML
  </body>
</html>
"""
        }
    }

    /*
    def static delete(String fileName) {
        new File(fileName).delete()
        new File(fileName+V_FILE_SUFFIX).delete()
        return fileName
    }*/

    /**
     * Tests
     * @param args
     */
    static void main(String[] args) {

        // show usage of diff package...
        String s0 = ""
        String s1 =  "String 1"
        String s2 =  s1+" und noch was dazu"

        // test empty one
        List<DiffMatchPatch.Diff> diffs = DiffMatchPatch.get().diff_main(s0, s0)
        String empty = DiffMatchPatch.get().diff_toDelta(diffs)
        assert empty == "" // the delta-file is completely empty...

        diffs = DiffMatchPatch.get().diff_main(s1, s1)
        String almostEmpty = DiffMatchPatch.get().diff_toDelta(diffs)
        assert almostEmpty == "=8" // the delta-file is completely empty...


        diffs = DiffMatchPatch.get().diff_main(s1, s0)
        String patchTextLineS1_S0 = DiffMatchPatch.get().diff_toDelta(diffs)

        diffs = DiffMatchPatch.get().diff_main(s2, s1)
        String patchTextLineS2_S1 = DiffMatchPatch.get().diff_toDelta(diffs)

        diffs = DiffMatchPatch.get().diff_fromDelta(s2, patchTextLineS2_S1)
        String shouldBeS1 = DiffMatchPatch.get().diff_text2(diffs)
        assert shouldBeS1 == s1
        diffs = DiffMatchPatch.get().diff_fromDelta(shouldBeS1, patchTextLineS1_S0)
        String shouldBeS0 = DiffMatchPatch.get().diff_text2(diffs)
        assert shouldBeS0 == s0



        // prepare test
        def FILE_NAME = delete("testfile.txt")

        // no version. just one initial, empty one.
        VersionedFile vf = [FILE_NAME]
        VersionedFile other = [FILE_NAME]
        assert other.versions.size() == 1
        assert other.versions[0].version == INITIAL_VERSION
        assert other.versions[0].author == null
        assert other.versions[0].patch == null
        assert other.versions[0].time != null

        vf.text = "jetzt kommt was dazu..."
        assert vf.versions.size() == 2
        assert vf.versions[0].version == INITIAL_VERSION
        assert vf.versions[1].patch != null

        vf.author = "BEL"
        vf.version = "Happy new year"
        vf.text = "jetzt kommt NOCH WAS was dazu..."
        assert vf.versions.size() == 3
        assert vf.versions[2].getText() == "jetzt kommt NOCH WAS was dazu..."
        assert vf.versions[1].getText() == "jetzt kommt was dazu..."
        assert vf.versions[0].getText() == ""

        vf.author = "Nicole Tietz"
        vf.version = "Happy new Halloween"
        vf.append(" hinten dran")
        assert vf.versions.size() == 4
        assert vf.versions[3].text == "jetzt kommt NOCH WAS was dazu... hinten dran"
        assert vf.versions[3].author == "Nicole Tietz"
        assert vf.versions[3].version == "Happy new Halloween"

        vf.text = "jetzt kommt was weg..." // auch die version geht nur einmal!
        def cve = vf.versions.last()
        assert cve.author == "Nicole Tietz"
        assert cve.version == null // LOST IT
        assert vf.versions[3].text == "jetzt kommt NOCH WAS was dazu... hinten dran" // test adding text from patch
        assert vf.versions[4].text == "jetzt kommt was weg..." // test adding text from patch


        // prepare test
        def FILE_NAME_INIT = delete("testfileInitial.txt")

        def ti = "das ist ein initialer text. Und noch ein wenig mehr..."
        def vfi = new VersionedFile(FILE_NAME_INIT, ti, "BEL")
        vfi.text = "das ist ein ganz anderer text. Und noch ein wenig weniger..."
        assert vfi.versions.size() == 2
        assert vfi.versions[0].text == "das ist ein initialer text. Und noch ein wenig mehr..."
        assert vfi.versions[1].text == "das ist ein ganz anderer text. Und noch ein wenig weniger..."
        assert vfi.versions[0].version == INITIAL_VERSION
        assert vfi.versions[1].version == null

        // identical ... so zero change...
        vfi.text = "das ist ein ganz anderer text. Und noch ein wenig weniger..."
        def ve = vfi.versions[2]
        assert ve.author == "BEL"
        assert ve.text == "das ist ein ganz anderer text. Und noch ein wenig weniger..."
        assert ve.patch == "=60"


        String htmlDiff = vfi.versions[1].getHTMLDiff()
        new File("htmlDiff.html").text = htmlDiff


        // prepare test
        def FILE_NAME_EMPTY = delete("testfileEmpty.txt")

        def vfe = new VersionedFile(FILE_NAME_EMPTY, "", "BEL")
        vfe.text = ""
        assert vfe.versions.size() == 2
        assert vfe.versions[0].text == ""
        assert vfe.versions[1].text == ""
        assert vfe.versions[0].version == INITIAL_VERSION
        assert vfe.versions[1].version == null
        vfe.text = "text"
        vfe.text = "text"
        vfe.text = ""
        vfe.text = ""
        vfe.text = ""
        assert vfe.versions[6].text == ""
        assert vfe.versions[6].patch == ""
        assert vfe.versions[5].text == ""
        assert vfe.versions[5].patch == ""
        assert vfe.versions[4].text == ""
        assert vfe.versions[4].patch != null
        assert vfe.versions[3].text == "text"
        assert vfe.versions[3].patch == "=4"
        assert vfe.versions[2].text == "text"
        assert vfe.versions[2].patch != ""
        assert vfe.versions[1].text == ""
        assert vfe.versions[1].patch == ""
        assert vfe.versions[0].text == ""
        assert vfe.versions[0].patch == null
        assert !vfe.versions[0].isChanged()
        assert !vfe.versions[1].isChanged()
        assert vfe.versions[2].isChanged()

        // prepare test
        FILE_NAME_EMPTY = delete("testfileEmpty.txt")

        vfe = new VersionedFile(FILE_NAME_EMPTY, "abc", "BEL")
        vfe.text = "abc"
        vfe.text = "abc"
        assert vfe.versions.size() == 3
        assert vfe.versions[0].text == "abc"
        assert vfe.versions[1].text == "abc"
        assert vfe.versions[2].text == "abc"
        assert vfe.versions[0].isChanged()

        htmlDiff = vfe.versions[0].getHTMLDiff()
        //new File("emptyHtmlDiff0.html").text = htmlDiff
        htmlDiff = vfe.versions[1].getHTMLDiff()
        assert htmlDiff == "<span>abc</span>" // no change...
        //new File("emptyHtmlDiff1.html").text = htmlDiff
        htmlDiff = vfe.versions[2].getHTMLDiff()
        //new File("emptyHtmlDiff2.html").text = htmlDiff

        // cleanup
        delete("testfileEmpty.txt")
        delete("testfileInitial.txt")
        delete("testfile.txt")

        println("YIPPIEEEE. All tests ran.")

/*
        assert
        def versions = vf.allVersions
        assert versions.size() == 1
        assert versions[0].author == "bel"
        assert versions[0].diffString == null
        assert versions[0].text == ""

        // one change.

        vf = ["oneChangeTwoVersions", "bel"]
        //vf.save("the initial text", "nit")
        //versions = vf.allVersions
        assert versions.size() == 2
        assert versions[0].author == "bel"
        assert versions[0].diffString == null
        assert versions[0].text == ""

        assert versions[1].author == "bel"
        assert versions[1].diffString == null
        assert versions[1].text == "the initial text"
*/

    }
}
