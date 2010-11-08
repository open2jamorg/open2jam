
package org.open2jam;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author fox
 */
public class Util
{
    public static void die(Exception e)
    {
        warn(e, "Dead:" + e.toString());
        System.exit(1);
    }

    public static void warn(Exception e) {warn(e,"WARNING");}
    public static void warn(String txt) {warn(txt,"WARNING");}

    public static void warn(Exception e, String title)
    {
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        warn(sw.toString(), title);
    }

    public static void warn(String string, String title) {
        JDialog d = new JOptionPane( string, JOptionPane.ERROR_MESSAGE).createDialog(null, title);
        d.setAlwaysOnTop(true);
        d.setVisible(true);
    }
}
