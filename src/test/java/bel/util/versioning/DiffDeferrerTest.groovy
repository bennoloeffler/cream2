package bel.util.versioning

/**
 * Created 08.11.2017.
 *
 *
 */
class DiffDeferrerTest extends GroovyTestCase {

    VersionedFile vf
    DiffDeferrer dd

    void testShouldShowDiff() {
        sleep(2)
        dd = new DiffDeferrer(vf, 0)
        assert dd.shouldShowDiff()
        def diff = dd.HTMLDiff
        assert diff.contains("abc")
        assert ! diff.contains("yxz")
    }

    void testGetHTMLDiff() {
        dd = new DiffDeferrer(vf, Long.MAX_VALUE)
        assert !dd.shouldShowDiff()
        shouldFail {
            def diff = dd.HTMLDiff
        }
    }

    @Override
    void setUp() {
        vf = new VersionedFile("testDeferrer.txt")
        vf.text = "xyz"
        vf.text = "abc"
    }

    @Override
    void tearDown() {
        vf.delete()
        dd.delete()
    }
}
