package htv.springboot.utils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Dimension;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class JavaUtils {
    public static int createOptionWindow(String title, String msg, String[] options, int defaultOptionIndex) {
        JFrame jFrame = new JFrame();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(false);
        jFrame.setAlwaysOnTop(true);

        JTextArea jta = new JTextArea(msg);
        jta.setLineWrap(true);
        jta.setEditable(false);
        JScrollPane jsp = new JScrollPane(jta);
        jsp.setPreferredSize(new Dimension(600, 440));

        return JOptionPane.showOptionDialog(
                jFrame, jsp, title, YES_NO_OPTION, QUESTION_MESSAGE, null, options, options[defaultOptionIndex]);
    }
}
