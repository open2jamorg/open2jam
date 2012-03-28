package org.open2jam.parsers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import org.open2jam.parsers.SNPParser.SNPFileHeader;
import org.open2jam.parsers.utils.ByteHelper;
import org.open2jam.parsers.utils.Logger;

/**
 *
 * @author CdK
 */
public class XNTParser {
    
    /** The *.xnt files have "XNOT" as signature */
    private final static int XNT_SIGNATURE = 0x544F4E58;
    
    public static EventList parseChart(XNTChart chart)
    {
	EventList list = new EventList();
	
	ByteBuffer buffer;
        RandomAccessFile f;
	SNPFileHeader fh;
        try{
            f = new RandomAccessFile(chart.source.getAbsolutePath(),"r");
	    //First, the xne file
	    if(!chart.file_index.containsKey(chart.xne_filename))
	    {
		Logger.global.log(Level.WARNING, "Where is my xne file? {0}", chart.source.getName());
		return null;
	    }
	    
	    fh = chart.file_index.get(chart.xne_filename);
	    buffer = SNPParser.extract(fh, f);  
	    String xne = new String(buffer.array());
	    String tempo = xne.toLowerCase().split("tempo=\"")[1].split("\"")[0];
	    //this is the real BPM, the xml bpm isn't it :/
	    list.add(new Event(Event.Channel.BPM_CHANGE,0,0,Float.parseFloat(tempo),Event.Flag.NONE));
	    
	    //Then, the xnt file
	    if(!chart.file_index.containsKey(chart.xnt_filename))
	    {
		Logger.global.log(Level.WARNING, "Where is my xnt file? {0}", chart.source.getName());
		return null;
	    }

	    fh = chart.file_index.get(chart.xnt_filename);
            buffer = SNPParser.extract(fh, f);
        }catch(IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading SNP file {0}", chart.source.getName());
            return null;
        }
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
	
	int signature = buffer.getInt();
	short unk1    = buffer.getShort();
	int segments  = buffer.getInt();
	byte unk2    = buffer.get();
	
	if(signature != XNT_SIGNATURE)
	{
	    Logger.global.log(Level.WARNING, "This isn't a XNT file :/ {0}", fh.file_name);
            return null;	    
	}
	
	if(segments == 3)
	    readBPMChange(list, buffer);
	
	readNoteBlock(list, buffer, false); //first key sounds
	readNoteBlock(list, buffer, true);  //then bgm sounds
	    
	chart.sample_index = readSamples(buffer);
	
	Collections.sort(list);
	return list;
    }
    
    private static void readNoteBlock(EventList list, ByteBuffer buffer, boolean bgm)
    {
	byte[] junk = new byte[12]; //skip this, idk what it is 
	buffer.get(junk);
	int number_events = buffer.getInt();

	for(int i = 0; i < number_events; i++)
	{	
	    byte  zero     = buffer.get();
	    short measure  = buffer.getShort();
	    float position = buffer.getFloat();
	    short chan     = buffer.get();
	    short sample_id= buffer.getShort();
	    float hold     = buffer.getFloat();
	    
	    Event.Channel channel = Event.Channel.AUTO_PLAY;
	    
	    if(!bgm)
	    {
		switch(chan)
		{
		    case 1: channel = Event.Channel.NOTE_1; break;
		    case 2: channel = Event.Channel.NOTE_2; break;
		    case 3: channel = Event.Channel.NOTE_3; break;
		    case 4: channel = Event.Channel.NOTE_4; break;
		    case 5: channel = Event.Channel.NOTE_5; break;
		    case 6: channel = Event.Channel.NOTE_6; break;
		    case 7: channel = Event.Channel.NOTE_7; break;
		    default: Logger.global.log(Level.WARNING, "A unknown channel here {0}", chan);
		}
	    }
	    
	    
	    if(hold > 0)
	    {
		list.add(new Event(channel, measure, position, sample_id, Event.Flag.HOLD));
		
		float release = measure+position+hold;
		measure = (short)release;
		position = release-measure;
		
		list.add(new Event(channel, measure, position, sample_id, Event.Flag.RELEASE));
	    }
	    else
	    {
		list.add(new Event(channel, measure, position, sample_id, Event.Flag.NONE));
	    }
	    
	}
    }
    
    private static void readBPMChange(EventList list, ByteBuffer buffer)
    {
	byte[] junk = new byte[12]; //skip this, idk what it is 
	buffer.get(junk);
	int number_events = buffer.getInt();
	
	for(int i = 0; i < number_events; i++)
	{
	    byte  zero     = buffer.get();
	    short measure  = buffer.getShort();
	    float position = buffer.getFloat();
	    byte[] skip    = new byte[3]; buffer.get(skip); // yes my lord
	    float bpm      = buffer.getFloat();
	    
	    list.add(new Event(Event.Channel.BPM_CHANGE, measure, position, bpm, Event.Flag.NONE));
	}
    }
    
    private static HashMap<Integer, String> readSamples(ByteBuffer buffer)
    {
	HashMap<Integer, String> samples = new HashMap<Integer, String>();
	
	int number_events = buffer.getInt();
	
	for(int i = 0; i < number_events; i++)
	{
	    short id  = buffer.getShort();
	    short unk = buffer.getShort();
	    int n_len = buffer.getInt();
	    byte[] bname = new byte[n_len];
	    buffer.get(bname);
	    String name = ByteHelper.toString(bname);
	    
	    samples.put((int)id, name);
	}
	
	return samples;
    }
    
}
