package org.open2jam.gui;

import java.io.File;
import javax.swing.UIManager;


public class Main
{
	static final String LIB_PATH = 
		System.getProperty("user.dir") + File.separator + 
		"lib" + File.separator + 
		"native" + File.separator + 
		getOS();

	public static void main(String []args)
	{
		try{
			trySetLAF();

			System.setProperty("org.lwjgl.librarypath", LIB_PATH);

                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                 new Interface().setVisible(true);
                            }
                        });

		}catch(Exception e){
			die(e);
		}
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
		} catch (Exception e) {}
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

	public static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		e.printStackTrace(new java.io.PrintWriter(r));
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}


