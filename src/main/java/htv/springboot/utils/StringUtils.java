package htv.springboot.utils;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class StringUtils {
    public static String windowsFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static String toString(Object[] arr) {
        return Arrays
                .stream(arr)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    //https://stackoverflow.com/a/12053940/5207195
    public static String escape(String s) {
        StringBuilder builder = new StringBuilder();
        boolean previousWasASpace = false;
        for( char c : s.toCharArray() ) {
            if( c == ' ' ) {
                if( previousWasASpace ) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch(c) {
                case '<': builder.append("&lt;"); break;
                case '>': builder.append("&gt;"); break;
                case '&': builder.append("&amp;"); break;
                case '"': builder.append("&quot;"); break;
                case '\n': builder.append("<br>"); break;
                // We need Tab support here, because we print StackTraces as HTML
                case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;
                default:
                    if( c < 128 ) {
                        builder.append(c);
                    } else {
                        builder.append("&#").append((int)c).append(";");
                    }
            }
        }

        return builder.toString();
    }

    public static String getPlainText(String htmlText) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor.traverse(formatter, Jsoup.parse(htmlText)); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }

    //copied with modification from
    // https://github.com/jhy/jsoup/blob/master/src/main/java/org/jsoup/examples/HtmlToPlainText.java
    private static class FormattingVisitor implements NodeVisitor {
        private final StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        @Override
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append("  ");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");
        }

        // hit when all of the node's children (if any) have been visited
        @Override
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n");
            else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder
        private void append(String text) {
            if (text.equals(" ") &&
                    (accum.isEmpty() || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            accum.append(text);
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
}
