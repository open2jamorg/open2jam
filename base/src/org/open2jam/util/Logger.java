
package org.open2jam.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author fox
 */
public class Logger
{
    public static void die(Exception e)
    {
        warn(e, "Dead: ["+e.toString()+"]");
        System.exit(1);
    }

    public static void warn(Exception e) {warn(e,"WARNING");}
    public static void warn(String txt) {warn(txt,"WARNING");}

    public static void warn(Exception e, String title)
    {
        String s = getStackTrace(e);
        log(s);
        warn(s, title);
    }

    public static void warn(String string, String title) {
        JDialog d = new JOptionPane( string, JOptionPane.ERROR_MESSAGE).createDialog(null, title);
        d.setAlwaysOnTop(true);
        d.setVisible(true);
    }

    public static void log(Exception e) { e.printStackTrace(); }

    public static void log(String s) { System.err.println(s); }

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
