package bel.util.enml

import bel.en.evernote.ENHelper

/**
 * Created 26.08.2017.
 *
 *
 */
class TextToEnml {

    /**
     * Simple idea:
     * [] [ ] are interpreted as open todos
     * [x] [X] (or any other (trimed) single character between braces [x ] or [ v] or  [ y ] are done todos
     *
     * Every linefeed is replaced by a pair of </div><div>
     * at the beginning of the document, there will be a <div>
     * at the very end, there will be a </div>
     *
     * @param text plain text
     * @return a part of an ENML document. Has to be inserted between <enml> and </enml> tags to make it a valid ENML document
     */
    static String transformToENMLBody(String text) {
        def disabledCheckbox = "<en-todo checked=\\\"false\\\"></en-todo>"
        def enabledCheckbox = "<en-todo checked=\\\"true\\\"></en-todo>"
        def content = "<div>"
        text = ENHelper.transformNormalToENML(text)
        content += text
        content = content.replaceFirst("TITEL:.*\\s*-----------------------\\s*","")
        content = content.replaceAll("(?<!\\n)\\n", "</div><div>")
        content = content.replaceAll("\\n", "<br/></div><div>")
        content = content.replaceAll("\\[ {0,2}]", disabledCheckbox)
        content = content.replaceAll("\\[ {0,2}[xX] {0,2}]", enabledCheckbox)
        content += "</div>"
        return content
    }

}
