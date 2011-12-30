/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.UIManager;
import org.open2jam.gui.Interface;
import org.open2jam.screen2d.Open2jamGame;
import org.open2jam.utils.Logger;


/**
 *
 * @author CdK
 */
public class Open2jam implements Runnable {

    public static void main(String []args)
    {       
	loadLwjgl();
	
	Config.openDB();
	
	trySetLAF();
	
        EventQueue.invokeLater(new Open2jam());
    }
    
    @Override
    public void run() {
        new Interface().setVisible(true);
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
    
    public static void loadLwjgl()
    {
	GdxNativesLoader.load();
	if (GdxNativesLoader.isWindows) {
		GdxNativesLoader.extractLibrary("OpenAL32.dll", "OpenAL64.dll");
		GdxNativesLoader.extractLibrary("lwjgl.dll", "lwjgl64.dll");
	} else if (GdxNativesLoader.isMac) {
		GdxNativesLoader.extractLibrary("openal.dylib", "openal.dylib");
		GdxNativesLoader.extractLibrary("liblwjgl.jnilib", "liblwjgl.jnilib");
	} else if (GdxNativesLoader.isLinux) {
		GdxNativesLoader.extractLibrary("libopenal.so", "libopenal64.so");
		GdxNativesLoader.extractLibrary("liblwjgl.so", "liblwjgl64.so");
	}
	System.setProperty("org.lwjgl.librarypath", GdxNativesLoader.nativesDir.getAbsolutePath());
    }
}
