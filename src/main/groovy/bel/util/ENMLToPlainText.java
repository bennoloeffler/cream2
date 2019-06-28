package bel.util;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to convert HTML input to lightly-formatted
 * plain-text. That is divergent from the general goal of jsoup's .text() methods, which is to get clean data from a
 * scrape.
 * <p>
 * Note that this is a fairly simplistic formatter -- for real world use you'll want to embrace and extend.
 * </p>
 * <p>
 * To invoke from the command line, assuming you've downloaded the jsoup jar to your current directory:</p>
 * <p><code>java -cp jsoup.jar org.jsoup.examples.ENMLToPlainText url [selector]</code></p>
 * where <i>url</i> is the URL to fetch, and <i>selector</i> is an optional CSS selector.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class ENMLToPlainText {

    public static String convert(String html) throws IOException {

        // <div><br></br></div> --> <br/> otherwise, too many \n are created
        html = html.replaceAll("<div><br><\\/br><\\/div>","<br/>");
        // fetch the specified URL and parse to a HTML DOM
        Document doc = Jsoup.parse(html);

        // remove all format stuff - otherwise too many \n
        for( Element element : doc.select("span") )
        {
            element.replaceWith(new TextNode(element.text(), ""));
            //element.remove();
        }

        ENMLToPlainText formatter = new ENMLToPlainText();

        String selector = null; // TODO: Clarify waht that means... WHAT THE FUCK IS A SELECTOR?
        String plainText = null;
        if (selector != null) {
            Elements elements = doc.select(selector); // get each element that matches the CSS selector
            for (Element element : elements) {
                plainText = formatter.getPlainText(element); // format that element to plain text
            }
        } else { // format the whole doc
            plainText = formatter.getPlainText(doc);
        }
        return plainText;

    }

    /**
     * Format an Element to plain-text
     * @param element the root element to format
     * @return formatted text
     */
    public String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append(" ");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");

            //
            // SPECIAL ENML Handling
            //

            else if(name.equals("input") && node.attributes().get("type").equals("checkbox")) {
                if(node.hasAttr("checked")) {
                    append("[x] ");
                } else {
                    append("[ ] ");
                }

            } else if (name.equals("title"))
                append("TITEL: ");
            else if(name.equals("img"))
                append("{HIER war in Evernote ein BILD}");
            //else
            //    System.out.println("IGNORED HEAD: " + node);

        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "div", "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n");
            else if (name.equals("a")) {
                String href = node.absUrl("href");
                if(href != null && !href.trim().equals("")) {
                    append(String.format(" LINK:<%s>", href));
                }
            } else if (name.equals("title"))
                append("\n-----------------------\n\n");
            //else
            //    System.out.println("IGNORED TAIL: " + node);
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
}

