package htv.springboot.utils;

import htv.springboot.browsers.Browsers;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HyperlinkMouseListener extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Element h = getHyperlinkElement(e);
            if (h != null) {
                Object attribute = h.getAttributes().getAttribute(HTML.Tag.A);
                if (attribute instanceof AttributeSet set) {
                    String href = (String) set.getAttribute(HTML.Attribute.HREF);
                    if (href != null) {
                        Browsers.openUrl(href);
                    }
                }
            }
        }
    }

    private Element getHyperlinkElement(MouseEvent event) {
        JTextPane textPane = (JTextPane) event.getSource();
        int pos = textPane.getUI().viewToModel2D(
                textPane, event.getPoint(), new Position.Bias[] {Position.Bias.Forward});
        if (pos >= 0 && textPane.getDocument() instanceof HTMLDocument hdoc) {
            Element elem = hdoc.getCharacterElement(pos);
            if (elem.getAttributes().getAttribute(HTML.Tag.A) != null) {
                return elem;
            }
        }
        return null;
    }
}