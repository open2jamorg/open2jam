package org.open2jam.parsers.utils;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

/**
 *
 * @author CdK
 */
public class ByteHelper {
    
    /**
     * Convert a byte[] to a String, also will try to get the charset (cross your fingers on this xD)
     * @param ch The byte[]
     * @return a nice String
     */
    public static String toString(byte[] ch)
    {
        int i = 0;
        while(i<ch.length && ch[i]!=0)i++; // find \0 terminator
        String charset = CharsetDetector.analyze(ch);
        try {
            return new String(ch,0,i,charset);
        } catch (UnsupportedEncodingException ex) {
            Logger.global.log(Level.WARNING, "Encoding [{0}] not supported !", charset);
            return new String(ch,0,i);
        }
    }
    
    public static byte[] intToByteArray(int i)
    {
	return new byte[] {
	    (byte) (i),
	    (byte) (i >> 8),
	    (byte) (i >> 16),
	    (byte) (i >> 24)
	};
    }
    
    public static byte[] shortToByteArray(short i)
    {
	return new byte[] {
	    (byte) (i),
	    (byte) (i >> 8)
	};
    }    
    
}
