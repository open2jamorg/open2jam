
package org.open2jam.parsers.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * wrapper around the mozilla's chardet algorithm
 * @author fox
 */
public class CharsetDetector implements nsICharsetDetectionObserver
{
    private String charset;
    private nsDetector det;

    private CharsetDetector() {}

    void start(){
        charset = "US-ASCII";
        det = new nsDetector(nsPSMDetector.ALL);
        det.Init(this);
    }

    private boolean done = false;
    private boolean isAscii = true;

    /** returns true if done */
    boolean feed(byte[] b){
        if(isAscii)isAscii = det.isAscii(b,b.length);
        if(!isAscii && !done)done = det.DoIt(b,b.length, false);
        return done;
    }

    String result(){
        det.DataEnd();
        return charset;
    }

    @Override
    public void Notify(String string) {
        charset = string;
    }
    
    public static String analyze(InputStream in) throws java.io.IOException
    {
        CharsetDetector c = new CharsetDetector();
        c.start();

        BufferedInputStream imp = new BufferedInputStream(in);
        byte[] buf = new byte[1024];
        while( imp.read(buf,0,buf.length) != -1) {
            if(c.feed(buf))break;
        }
        return c.result();	
    }

    public static String analyze(File f) throws java.io.IOException
    {
	return analyze(new FileInputStream(f));
    }

    public static String analyze(byte[] buf)
    {
        CharsetDetector c = new CharsetDetector();
        c.start();
        c.feed(buf);
        return c.result();
    }
}
