

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Main
{
	static String JAR_PATH = "lib"+File.separator+"jar";
	static String LIB_PATH = "lib"+File.separator+"native";
	static String CLASS_PATH = ".";
	static String MAIN_CLASS = "org.open2jam.render.Render";

	public static void main(String []args)
	{
		LIB_PATH += File.separator + getOS();
		try{
			File lib_path = new File(LIB_PATH);
			
			// add the classes dir and each jar in lib to a List of URLs.
			List<URL> urls = new ArrayList<URL>();
			urls.add(new File(CLASS_PATH).toURI().toURL());
			for (File f : new File(JAR_PATH).listFiles())urls.add(f.toURI().toURL());

			System.setProperty("java.library.path",LIB_PATH);

			// feed your URLs to a URLClassLoader!
			ClassLoader classloader =
				new URLClassLoader(urls.toArray(new URL[0]),
					ClassLoader.getSystemClassLoader().getParent());

			// relative to that classloader, find the main class
			Class<?> mainClass = classloader.loadClass(MAIN_CLASS);
			Class<?> thisClass = args.getClass();
			Method main = mainClass.getMethod("main",new Class[]{thisClass});

			// we need to reset sys_paths to force java look for it again
			// because we changed java.library.path at runtime
			java.lang.reflect.Field field = ClassLoader.class.getDeclaredField("sys_paths");
			field.setAccessible(true);
			field.set(ClassLoader.class, null);
			field.setAccessible(false);
			
			// well-behaved Java packages work relative to the
			// context classloader.  Others don't (like commons-logging)
			Thread.currentThread().setContextClassLoader(classloader);

			// pass the args as the "real" args to your main
			main.invoke(null, new Object[] { args });

		}catch(Throwable t){
			final java.io.Writer r = new java.io.StringWriter();
			final java.io.PrintWriter pw = new java.io.PrintWriter(r);
			t.printStackTrace(pw);
			javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
				javax.swing.JOptionPane.ERROR_MESSAGE);
			System.exit(1);
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


