package org.open2jam;

import java.io.File;
import java.awt.EventQueue;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.open2jam.gui.NewInterface;

public class Main
{
    static final String LIB_PATH =
        System.getProperty("user.dir") + File.separator +
        "lib" + File.separator +
        "native" + File.separator +
        getOS();

    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String []args)
    {
        setupLogging();

        trySetLAF();

        System.setProperty("org.lwjgl.librarypath", LIB_PATH);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                 new NewInterface().setVisible(true);
            }
        });
    }

    private static void setupLogging()
    {
        Config c = Config.get();
        if(c.log_handle != null)logger.addHandler(c.log_handle);
        for(Handler h : logger.getHandlers())h.setLevel(c.log_level);
        logger.setLevel(c.log_level);
    }

    private static void trySetLAF()
    {
        try {
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            {
                if("Nimbus".equals(info.getName())){
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.info(e.toString());
        }
    }

    public static String getOS()
    {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("win") >= 0){
            return "windows";
        }else if(os.indexOf("mac") >= 0){
            return "macosx";
        }else if(os.indexOf("nix") >=0 || os.indexOf("nux") >=0){
            return "linux";
        }else if(os.indexOf("solaris") >= 0){
            return "solaris";
        }else{
            return os;
        }
    }
}


