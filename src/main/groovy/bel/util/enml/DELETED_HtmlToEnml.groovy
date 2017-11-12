package bel.util.enml

import groovy.xml.MarkupBuilder
import org.ccil.cowan.tagsoup.Parser
/**
 * Takes html.
 * - JTidy it.
 * - Transform to VALID ENML (including TODO-Tags)
 * - validates it with the enml.dtd in order to make sure, that it is
 *
 */


@Singleton
class DELETED_HtmlToEnml {

    String transform(String html) {

        // create enml
        def xmlOutput = new StringWriter()
        def enmlOut = new MarkupBuilder(xmlOutput)

        // parse html
        def tagsoupParser = new Parser()
        def slurper = new XmlSlurper(tagsoupParser)
        def htmlDoc = slurper.parseText(html)
        //println htmlDoc.children().last()
        htmlDoc.body.childNodes().each { tag ->
            //println tag
            //def tagStr = XmlUtil.serialize(tag)
            def xmlTagOutput = new StringWriter()
            def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(xmlTagOutput))
            xmlNodePrinter.print(tag)
            println xmlTagOutput.toString()
        }

        def enmlTitle = htmlDoc.head.title

        enmlOut.enml {
            //htmlDoc.each { elem ->
            //    println elem
            //}

            htmlDoc.body.childNodes().each { tag ->
                //switch (tag.name()) {
                //    case "specialTag":
                //        break
                //    default: // just take the tag as it is... just pretty-print it
                        //def tagOutput = new StringWriter()
                        //def tagStr = XmlUtil.serialize(tag)
                        //println tagStr
                        //mkp.yieldUnescaped tagStr
                        //"${tag.name()}" tag.text()
                //}
            }
            switch(enmlTitle) {
                case("abc"):
                    break
                default:
                    def dynAttrib = 'div'
                    "${dynAttrib}" "this is a text"
                    mkp.yieldUnescaped "<div> another text unescaped </div>"
            }


        }

        xmlOutput.toString()

    }

    boolean validate() {

    }




}
