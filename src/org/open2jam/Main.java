package org.open2jam;

import java.io.File;
import java.awt.EventQueue;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.open2jam.util.Logger;
import javax.swing.UIManager;
import org.open2jam.gui.Interface;

public class Main implements Runnable
{
    private static final String LIB_PATH =
        System.getProperty("user.dir") + File.separator +
        "lib" + File.separator +
        "native" + File.separator +
        getOS();

    public static void main(String []args)
    {
        System.setProperty("org.lwjgl.librarypath", LIB_PATH);
        
        Config.openDB();
        
        setupLogging();

        trySetLAF();
        
        EventQueue.invokeLater(new Main());
    }
    
    @Override
    public void run() {
        new Interface().setVisible(true);
    }

    private static void setupLogging()
    {
        //Config c = Config.get();
        //if(c.log_handle != null)Logger.global.addHandler(c.log_handle);
        for(Handler h : Logger.global.getHandlers())h.setLevel(Level.INFO);
        Logger.global.setLevel(Level.INFO);
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
            Logger.global.info(e.toString());
        }
    }

    private static String getOS()
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


