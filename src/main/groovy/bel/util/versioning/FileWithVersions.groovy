package bel.util.versioning

import groovy.time.TimeCategory
import groovy.util.logging.Log4j2

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
/**
 * This API helps to keep track of versions of a file.
 * With the help of a diff string, we can go back in history.
 * With every diff string, an author and a date are stored.
 *
 */
@Log4j2
class FileWithVersions {

    File dataFile
    File versionsFile

    FileWithVersions(String fileName,  String text = "", String author=null) {
        this(new File(fileName), text, author)
    }

    FileWithVersions(File file, String text="", String author=null) {
        dataFile = file
        if(!dataFile.exists()) {
            if(!dataFile.createNewFile()) {
                log.error("FileWithVersions cant be created: " + dataFile.getAbsolutePath())
            } else {
                initDiffFile(text, author) // do only, if data file was created
            }
        }
    }

    def save(String text, String author) {
        // ISO DATETIME: https://gist.github.com/kdabir/6bfe265d2f3c2f9b438b
        use TimeCategory, {

        }
        LocalDateTime now = LocalDateTime.now()
        String nowStr = now.format(DateTimeFormatter.ISO_DATE_TIME)
        String oldFile = dataFile.text
    }

    def readCurrentVersion() {
        return dataFile.text

    }

    List<VersionEntry> getAllVersions() {

    }

    String getFullVersion(VersionEntry ve) {
    }

    String utcDateTimeNow() {
        // https://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format-with-date-hour-and-minute
        ZonedDateTime.now(ZoneOffset.UTC).format( DateTimeFormatter.ISO_INSTANT )
    }


    class VersionEntry {
        String author
        LocalDateTime time
        String diffString
        String getText() {

        }
    }

    /**
     * Tests
     * @param args
     */
    static void main(String[] args) {

        // no version. just one initial, empty one.
        FileWithVersions fwv = ["initialVersion", "bel"]
        def versions = fwv.allVersions
        assert versions.size() == 1
        assert versions[0].author == "bel"
        assert versions[0].diffString == null
        assert versions[0].text == ""

        // one change.
        fwv = ["oneChangeTwoVersions", "bel"]
        fwv.save("the initial text", "nit")
        versions = fwv.allVersions
        assert versions.size() == 2
        assert versions[0].author == "bel"
        assert versions[0].diffString == null
        assert versions[0].text == ""

        assert versions[1].author == "bel"
        assert versions[1].diffString == null
        assert versions[1].text == "the initial text"


    }

}
