/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.parsers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.logging.Level;
import org.open2jam.parsers.utils.ByteHelper;
import org.open2jam.parsers.utils.Compressor;
import org.open2jam.parsers.utils.Logger;

/**
 * This class let us parse a SNP file
 *
 * @author CdK
 */
public class SNPParser {

    /** the signature , "VDISK1.0" in little endian */
    private static final int SNP2_SIGNATURE = 0x302E314B;
    //private static final int SNP_SIGNATURE = 0x302E314B53494456;
    
    private static final int VDISK_HEADER = 24;
    private static final int FILE_HEADER = 145;
    
    public static class SNPFileHeader{
	final byte isDir;
	final String file_name;
	final int size_original;
	final int size_packed;
	final long file_offset;

	public SNPFileHeader(byte isDir, String name, int size_original, int size_packed, long file_offset) {
	    this.isDir = isDir;
	    this.file_name = name;
	    this.size_original = size_original;
	    this.size_packed = size_packed;
	    this.file_offset = file_offset;
	}
	
	public static SNPFileHeader readHeader(ByteBuffer buffer, long file_offset){
	    byte is_dir = buffer.get();
	    byte name[] = new byte[128];
	    buffer.get(name);
	    String fname = ByteHelper.toString(name).trim();
	    int sizeo = buffer.getInt();
	    int sizep = buffer.getInt();
	    
	    return new SNPFileHeader(is_dir, fname, sizeo, sizep, file_offset);
	}
    }
    
    public static boolean canRead(File file)
    {
        return file.getName().toLowerCase().endsWith(".snp");
    }
    
    public static ChartList parseFile(File file)
    {
	ByteBuffer buffer;
	RandomAccessFile f;
	
	HashMap<String, SNPFileHeader> file_index = new HashMap<String, SNPFileHeader>();
	
        try{
            f = new RandomAccessFile(file.getAbsolutePath(),"r");
            buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, VDISK_HEADER); //header 24
        }catch(IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading SNP file {0}", file.getName());
            return null;
        }
	
	buffer.order(ByteOrder.LITTLE_ENDIAN);

	//TODO FIX THIS
	buffer.getInt();
	
	if(buffer.getInt() != SNP2_SIGNATURE)
	{
            Logger.global.log(Level.WARNING, "This isn't a snp file you! {0}", file.getName());
            return null;	    
	}
	
	ChartList list = new ChartList();
	
	try {
	    long pointer = VDISK_HEADER;
	    
	    while(pointer < f.length())
	    {
		buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, pointer, FILE_HEADER);

		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		SNPFileHeader fh = SNPFileHeader.readHeader(buffer, pointer);
		
		if(fh.isDir < 1) //DO NOT WANT DIRS D:
		    file_index.put(fh.file_name, fh); //add the file
		
		
		if(fh.size_packed > 0 && fh.file_name.trim().endsWith(".xnt"))
		{		    
		    XNTChart chart = new XNTChart();
		    //TODO read the krazyrain.xml file and get the info so we can
		    //fill with all the data
		    list.add(chart);
		} 
		
		pointer += FILE_HEADER+fh.size_packed; //header + packed bits
	    }    
	} catch (IOException ex) {
            Logger.global.log(Level.WARNING, "Fuck :_ {0}", file.getName());
            return null;
	}
	
	//let's add the offsets of the files and forget about redo it later
	for(int i = 0; i< list.size(); i++)
	{
	    XNTChart chart = (XNTChart)list.get(i);
	    chart.file_index = file_index;
	    chart.snp_file = file;
	}
	
        try {
            f.close();
        } catch (IOException ex) {
            Logger.global.log(Level.WARNING, "Error closing the file (lol?) {0}", ex);
        }
	return list;
    }
    
    public static ByteBuffer extract(SNPFileHeader fh, RandomAccessFile f) throws IOException
    {
	ByteBuffer b =
		f.getChannel().map(FileChannel.MapMode.READ_ONLY, fh.file_offset+FILE_HEADER, fh.size_packed);
	b.order(ByteOrder.LITTLE_ENDIAN);
	
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	
	bout.write(Compressor.decompress(b).array());
	
	return ByteBuffer.wrap(bout.toByteArray());
    }
}
