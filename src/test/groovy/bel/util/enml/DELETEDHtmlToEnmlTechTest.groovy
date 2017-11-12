package bel.util.enml

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.ccil.cowan.tagsoup.Parser
import spock.lang.Specification
/**
 * Created 31.07.2017.
 *
 *
 */
class DELETEDHtmlToEnmlTechTest extends Specification {

    private def getXhtmlFromTitleAndBody(title, body) {
        """<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"><html xmlns="http://www.w3.org/1999/xhtml"><head><title>$title</title></head><body>$body</body></html>"""
    }

    private static String asXmlString( node) {
        try {/*
            Object builder = Class.forName("groovy.xml.StreamingMarkupBuilder").newInstance()
            InvokerHelper.setProperty(builder, "encoding", "UTF-8")
            Writable w = (Writable) InvokerHelper.invokeMethod(builder, "bindNode", node)
            return w.toString()
            */

            /*
            XmlUtil.serialize(new NodeChild(node, null, null), System.out)
            */
            /*
            StringWriter writer = new StringWriter()
            new XmlNodePrinter(new IndentPrinter(new PrintWriter(writer), '',
                    false)).print(node)
            String serialisedNode = writer.toString()
            */
            XmlUtil.serialize(new groovy.util.slurpersupport.Node(node, null, null))
        } catch (Exception e) {
            return "Couldn't convert node to string because: " + e.getMessage();
        }

    }

    private def makeXmlString(xmlNode) {
        def xmlOutput = new StringWriter()
        def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(xmlOutput))
        xmlNodePrinter.print(xmlNode)
        xmlOutput.toString()
    }

    void setup() {
    }

    void cleanup() {
    }


    def "read xhtml and pretty print it"() {

        setup:

            def html = getXhtmlFromTitleAndBody("the title",
                    """ ignore... <div> the text body </div> ignore also <div>second line</div> ignore this, too""")
        when:

            // Get groovy.util.Node value.
            //def xmlString = '<languages><language id="1">Groovy</language><language id="2">Java</language><language id="3">Scala</language></languages>'
            def tagsoupParser = new Parser()
            def slurper = new XmlSlurper(tagsoupParser)
            //slurper.namespaceAware = false
            GPathResult doc = slurper.parseText(html)
            // Create output with all default settings.
            //def xmlOutput = new StringWriter()
            //def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(xmlOutput))
            //def resultStr = xmlNodePrinter.print(doc)
            def resultStr = XmlUtil.serialize(doc).replace("tag0:", "").replace(":tag0", "")

        then:
            resultStr != null
            println(resultStr)
    }

    def "read xhtml and iterate the nodes"() {

        setup:

        def html = getXhtmlFromTitleAndBody("the title",
                """ ignore... <div> the text body </div> ignore also <div>second line</div> ignore this, too""")
        when:

        // parse html
        def tagsoupParser = new Parser()
        def slurper = new XmlSlurper(tagsoupParser)
        GPathResult htmlDoc = slurper.parseText(html)
        //println htmlDoc.children().last()
        def xml

        //childNodes() liefert nur <> <> echte elemente
        //children liefert auch die Strings dazwischen

        println asXmlString(htmlDoc)

        htmlDoc.childNodes()[1].children.each { tag ->
            //println tag
            //def tagStr = XmlUtil.serialize(tag)
            //def xmlTagOutput = new StringWriter()
            //def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(xmlTagOutput))
            //xmlNodePrinter.preserveWhitespace = false
            //xmlNodePrinter.expandEmptyElements = false
            //xmlNodePrinter.print(tag)
            //xml = xmlTagOutput.toString()
            if( tag.class == String ) {
                println( "STRING: " + tag)
            } else {
                //def tagStr = XmlUtil.serialize(new NodeChild(tag, null, null))
                def tagStr = asXmlString(tag)
                println( "NODE: " + tag.text() + "  ->  " + tagStr)
                //def xmlTagOutput = new StringWriter()
                //def xmlNodePrinter = new XmlNodePrinter(new PrintWriter(xmlTagOutput))
                //xmlNodePrinter.preserveWhitespace = false
                //xmlNodePrinter.expandEmptyElements = false
                //xmlNodePrinter.print(tag)
                //xml = xmlTagOutput.toString()
            }
            //println tag.text()
            //println XmlUtil.serialize(tag) // [Fatal Error] :1:2: Content ist nicht zulässig in Prolog.

        }

        then:
        xml != null
    }

    def " most simple xml parsing"() {

        given: "an xml string full of languages"
        def xml = """
<langs type='current' count='3' mainstream='true'>
  <language flavor='static' version='1.5'>Java</language>
  <language flavor='dynamic' version='1.6.0'>Groovy</language>
  <language flavor='dynamic' version='1.9'>JavaScript</language>
</langs>
"""

        when: "extracting langs"
        def langsParsed = new XmlParser().parseText(xml)
        def langsSlurped = new XmlSlurper().parseText(xml)

        then: "iterating over the elements"

        println langsParsed.attribute("count")
        langsParsed.language.each{
            println it.text()
            println XmlUtil.serialize(it).replace("""<?xml version="1.0" encoding="UTF-8"?>""", "")
        }

        println langsSlurped.@count
        langsSlurped.language.each{
            println it
        }

    }

}
