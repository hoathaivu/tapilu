package htv.springboot.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import java.awt.Dimension;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class JavaUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    public static int createOptionWindow(String title, String msg, String[] options) {
        if (msg.length() > 500) {
            return createOptionWindow(title, msg, options, 0, 1000, 600);
        } else {
            return createOptionWindow(title, msg, options, 0, 600, 266);
        }
    }

    public static int createOptionWindow(
            String title, String msg, String[] options, int defaultOptionIndex, int width, int height) {
        LOGGER.trace("Preparing to display");
        JTextPane jtp = new JTextPane();
        jtp.setContentType("text/html; charset=UTF-8");
        //if not set, ChangedCharSetException will be thrown silently if the HTML in msg has the meta tag with
        // attributes http-equiv="content-type" and content!="text/html" and content!="text/plain"
        jtp.getStyledDocument().putProperty("IgnoreCharsetDirective", true);
        jtp.setEditable(false);
        jtp.addMouseListener(new HyperlinkMouseListener());
        jtp.setText(msg);

        JScrollPane jsp = new JScrollPane(jtp);
        jsp.setPreferredSize(new Dimension(width, height));
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JFrame jFrame = new JFrame();
        jFrame.setLocationRelativeTo(null);
        jFrame.setAlwaysOnTop(true);
        jFrame.setVisible(false);

        return JOptionPane.showOptionDialog(
                jFrame, jsp, title, YES_NO_OPTION, QUESTION_MESSAGE, null, options, options[defaultOptionIndex]);
    }
}
