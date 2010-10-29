
import java.io.File;

import org.open2jam.parser.Chart;
import org.open2jam.parser.ChartParser;
import org.open2jam.render.Render;

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

			Chart c = ChartParser.parseFile(ChartParser.parseFileHeader(args[0],2));
			new Render(c,1);

		}catch(Exception e){
			die(e);
		}
	}

	private static void trySetLAF()
	{
		try {
			for(javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if("Nimbus".equals(info.getName())){
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
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


