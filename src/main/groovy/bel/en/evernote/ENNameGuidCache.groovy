package bel.en.evernote

/**
 * buffer guids, so searching can be avoided...
 */
class ENNameGuidCache {

    static File path

    static setBasePath(String basePath) {
        path = new File(basePath)
        if(!path.exists()) {
            assert(path.mkdirs())
        }
    }

    static String getGuid(String name) {
        assert( path.exists())
        File f = new File(path, "${name}.guid.txt")
        if(f.exists()) {
            def guid = new File(path, "${name}.guid.txt").text
            return guid
        } else {
            return null
        }
    }

    static setGuid(String name, String guid) {
        assert( path.exists())
        new File(path, "${name}.guid.txt").text = guid
    }

}
