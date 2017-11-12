package bel.util.versioning
/**
 * Makes sure, that the diff is not sent too often...
 */
class DiffDeferrer {

    private VersionedFile versionedFile
    private File last
    private long lastSent = 0
    private int idx = 0
    private long minimumMSecs

    DiffDeferrer(VersionedFile theVersionedFile, long theMinimumMSecs) {
        versionedFile = theVersionedFile
        last = new File (versionedFile.getPath() + ".lastTime.txt")
        minimumMSecs = theMinimumMSecs
    }

    boolean shouldShowDiff() {
        if(last.exists()) {
            def millisAndIdx = last.text.split(" ")
            assert millisAndIdx.size() == 2
            lastSent = Long.parseLong(millisAndIdx[0].trim())
            idx = Integer.parseInt(millisAndIdx[1].trim())
            assert idx >= 0
            assert idx < versionedFile.versions.size()
        }
        long duration = System.currentTimeMillis() - lastSent
        return duration > minimumMSecs
    }

    def getHTMLDiff() {
        assert shouldShowDiff()
        last.text = Long.toString(System.currentTimeMillis()) + " " + Integer.toString(versionedFile.versions.size()-1)
        versionedFile.versions[idx].HTMLDiffToLatest
    }

    void delete() {
        last.delete()
    }
}
