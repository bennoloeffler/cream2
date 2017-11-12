package bel.util.versioning

import spock.lang.Specification

/**
 *
 *
 */
class FileVersionTest extends Specification {


    def "SaveCurrentVersion"() {
        setup:
            def fv = new FileWithVersions()
        when:
            fv.save('test_file', "original text", "author")
            def versions = fv.getAllVersions('test_file')
        then:
            versions.size == 1
    }

    def "ReadVersion"() {
    }

    def "ReadVersion1"() {
    }

    def "GetAllVersions"() {
    }
}
