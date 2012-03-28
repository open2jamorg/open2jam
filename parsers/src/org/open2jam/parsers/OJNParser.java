package org.open2jam.parsers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.logging.Level;
import org.open2jam.parsers.utils.ByteHelper;
import org.open2jam.parsers.utils.Logger;

class OJNParser
{
    private static final String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
                    "Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

    /** the signature that appears at offset 4, "ojn\0" in little endian */
    private static final int OJN_SIGNATURE = 0x006E6A6F;

    public static boolean canRead(File file)
    {
        return file.getName().toLowerCase().endsWith(".ojn");
    }

    public static ChartList parseFile(File file)
    {
        ByteBuffer buffer;
        RandomAccessFile f;
        try{
            f = new RandomAccessFile(file.getAbsolutePath(),"r");
            buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 300);
        }catch(IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading OJN file {0}", file.getName());
            return null;
        }

        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        OJNChart easy = new OJNChart();
        OJNChart normal = new OJNChart();
        OJNChart hard = new OJNChart();

        int songid = buffer.getInt();
        int signature = buffer.getInt();
        if(signature != OJN_SIGNATURE){
            Logger.global.log(Level.WARNING, "File [{0}] isn't a OJN file !", file);
            return null;
        }

        float encode_version = buffer.getFloat();
        
        int genre = buffer.getInt();
        String str_genre = genre_map[(genre<0||genre>10)?10:genre];
        easy.genre = str_genre;
        normal.genre = str_genre;
        hard.genre = str_genre;
        
        float bpm = buffer.getFloat();
        easy.bpm = bpm;
        normal.bpm = bpm;
        hard.bpm = bpm;

        easy.level = buffer.getShort();
        normal.level = buffer.getShort();
        hard.level = buffer.getShort();
        buffer.getShort(); // 0, always

        int event_count[] = new int[3];
        event_count[0] = buffer.getInt();
        event_count[1] = buffer.getInt();
        event_count[2] = buffer.getInt();

        easy.notes = buffer.getInt();
        normal.notes = buffer.getInt();
        hard.notes = buffer.getInt();

        int measure_count[] = new int[3];
        measure_count[0] = buffer.getInt();
        measure_count[1] = buffer.getInt();
        measure_count[2] = buffer.getInt();
        int package_count[] = new int[3];
        package_count[0] = buffer.getInt();
        package_count[1] = buffer.getInt();
        package_count[2] = buffer.getInt();
        short old_encode_version = buffer.getShort();
        short old_songid = buffer.getShort();
        byte old_genre[] = new byte[20];
        buffer.get(old_genre);
        int bmp_size = buffer.getInt();
        int file_version = buffer.getInt();

        byte title[] = new byte[64];
        buffer.get(title);
        String str_title = ByteHelper.toString(title);
        easy.title = str_title;
        normal.title = str_title;
        hard.title = str_title;

        byte artist[] = new byte[32];
        buffer.get(artist);
        String str_artist = ByteHelper.toString(artist);
        easy.artist = str_artist;
        normal.artist = str_artist;
        hard.artist = str_artist;

        byte noter[] = new byte[32];
        buffer.get(noter);
        String str_noter = ByteHelper.toString(noter);
        easy.noter = str_noter;
        normal.noter = str_noter;
        hard.noter = str_noter;

        byte ojm_file[] = new byte[32];
        buffer.get(ojm_file);
        File sample_file = new File(file.getParent(), ByteHelper.toString(ojm_file));
        easy.sample_file = sample_file;
        normal.sample_file = sample_file;
        hard.sample_file = sample_file;

        int cover_size = buffer.getInt();
        easy.cover_size = cover_size;
        normal.cover_size = cover_size;
        hard.cover_size = cover_size;
        
        easy.duration = buffer.getInt();
        normal.duration = buffer.getInt();
        hard.duration = buffer.getInt();

        easy.note_offset = buffer.getInt();
        normal.note_offset = buffer.getInt();
        hard.note_offset = buffer.getInt();
        int cover_offset = buffer.getInt();

        easy.note_offset_end = normal.note_offset;
        normal.note_offset_end = hard.note_offset;
        hard.note_offset_end = cover_offset;

        easy.cover_offset = cover_offset;
        normal.cover_offset = cover_offset;
        hard.cover_offset = cover_offset;

        easy.source = file;
        normal.source = file;
        hard.source = file;

        ChartList list = new ChartList();
        list.add(easy);
        list.add(normal);
        list.add(hard);

        list.source_file = file;
	buffer.clear();

        try {
            f.close();
        } catch (IOException ex) {
            Logger.global.log(Level.WARNING, "Error closing the file (lol?) {0}", ex);
        }
        return list;
    }

    public static EventList parseChart(OJNChart chart)
    {
        EventList event_list = new EventList();
        try{
	    RandomAccessFile f = new RandomAccessFile(chart.getSource().getAbsolutePath(), "r");

	    int start = chart.note_offset;
	    int end = chart.note_offset_end;

	    ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start, end - start);
	    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
	    readNoteBlock(event_list, buffer);
	    
	    f.close();
        }catch(java.io.FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", chart.getSource().getName());
        } catch (IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading OJN file {0}", chart.getSource().getName());
        }
        return event_list;
    }

    private static void readNoteBlock(EventList event_list, ByteBuffer buffer) {
        while(buffer.hasRemaining())
        {
            int measure = buffer.getInt();
            short channel_number = buffer.getShort();
            short events_count = buffer.getShort();

            Event.Channel channel;
            switch(channel_number)
            {
                case 0:channel = Event.Channel.TIME_SIGNATURE;break;
                case 1:channel = Event.Channel.BPM_CHANGE;break;
                case 2:channel = Event.Channel.NOTE_1;break;
                case 3:channel = Event.Channel.NOTE_2;break;
                case 4:channel = Event.Channel.NOTE_3;break;
                case 5:channel = Event.Channel.NOTE_4;break;
                case 6:channel = Event.Channel.NOTE_5;break;
                case 7:channel = Event.Channel.NOTE_6;break;
                case 8:channel = Event.Channel.NOTE_7;break;
                default:
                channel = Event.Channel.AUTO_PLAY;
            }

            for(double i=0;i<events_count;i++)
            {
                double position = i / events_count;
                if(channel == Event.Channel.BPM_CHANGE || channel == Event.Channel.TIME_SIGNATURE) // fractional measure or BPM event
                {
                    float v = buffer.getFloat();
                    if(v == 0)continue;

                    event_list.add(new Event(channel,measure,position,v,Event.Flag.NONE));
                }else{ // note event
                    short value = buffer.getShort();
                    int volume_pan = buffer.get();
                    int type = buffer.get();
                    if(value == 0)continue; // ignore value=0 events

                    // MIN 1 ~ 15 MAX, special 0 = MAX
                    float volume = ((volume_pan >> 4) & 0x0F) / 16f;
                    if(volume == 0)volume = 1;

                    // LEFT 1 ~ 8 CENTER 8 ~ 15 RIGHT, special: 0 = 8
                    float pan = (volume_pan & 0x0F);
                    if(pan == 0)pan = 8;
                    pan -= 8;
                    pan /= 8f; //TODO or maybe 7f? (15-8) / 8 = 7 / 8 = 0.875 and it should be 1, right?
		    
                    value--; // make zero-based ( zero was the "ignore" value )
		    	    
		    // A lot of fixes here are done thanks to keigen shu. He's stealing my protagonism D:
		    Event.Flag f = Event.Flag.NONE;

		    if(type%8 > 3)
			value += 1000;
		    type %= 4;
		    
		    switch(type)
		    {
			case 0:
			    f = Event.Flag.NONE;
			break;
			case 1:
			    //Unused (#W Normal displayed in NoteTool)
			break;
			case 2:
			    //fix for autoplay longnotes, convert them to normal notes (it doesn't matter but... still xD)
			    f = Event.Flag.HOLD;
			break;
			case 3:
			    f = Event.Flag.RELEASE;
			break;
		    }
		    
		    event_list.add(new Event(channel,measure,position,value,f,volume, pan));
                }
            }
        }
        Collections.sort(event_list);
    }
}
