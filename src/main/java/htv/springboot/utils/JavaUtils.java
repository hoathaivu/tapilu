package htv.springboot.utils;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Dimension;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class JavaUtils {
    public static int createOptionWindow(String title, String msg, String[] options, int defaultOptionIndex) {
        JTextArea jta = new JTextArea(msg);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        jsp.setPreferredSize(new Dimension(480, 320));

        return JOptionPane.showOptionDialog(
                null, jsp, title, YES_NO_OPTION, QUESTION_MESSAGE, null, options, options[defaultOptionIndex]);
    }
}
