package org.open2jam.parser;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.open2jam.util.CharsetDetector;

public class OJNParser
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
        ByteBuffer buffer = null;
        RandomAccessFile f = null;
        try{
            f = new RandomAccessFile(file.getAbsolutePath(),"r");
            buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 300);
        }catch(IOException e){
            logger.log(Level.WARNING, "IO exception on reading OJN file {0}", file.getName());
        }

        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        OJNChart easy = new OJNChart();
        OJNChart normal = new OJNChart();
        OJNChart hard = new OJNChart();

        int songid = buffer.getInt();
        int signature = buffer.getInt();
        if(signature != OJN_SIGNATURE)throw new BadFileException("Not a OJN file");

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

        easy.note_count = buffer.getInt();
        normal.note_count = buffer.getInt();
        hard.note_count = buffer.getInt();

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
        String str_title = bytes2string(title);
        easy.title = str_title;
        normal.title = str_title;
        hard.title = str_title;

        byte artist[] = new byte[32];
        buffer.get(artist);
        String str_artist = bytes2string(artist);
        easy.artist = str_artist;
        normal.artist = str_artist;
        hard.artist = str_artist;

        byte noter[] = new byte[32];
        buffer.get(noter);
        String str_noter = bytes2string(noter);
        easy.noter = str_noter;
        normal.noter = str_noter;
        hard.noter = str_noter;

        byte ojm_file[] = new byte[32];
        buffer.get(ojm_file);
        File sample_file = new File(file.getParent(), bytes2string(ojm_file));
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
        buffer = null;

        try {
            f.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public static List<Event> parseChart(OJNChart chart)
    {
        ArrayList<Event> event_list = new ArrayList<Event>();
        try{
                RandomAccessFile f = new RandomAccessFile(chart.getSource().getAbsolutePath(), "r");

                int start = chart.note_offset;
                int end = chart.note_offset_end;

                ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start, end - start);
                buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                readNoteBlock(event_list, buffer);
                f.close();
        }catch(java.io.FileNotFoundException e){
            logger.log(Level.WARNING, "File {0} not found !!", chart.getSource().getName());
        } catch (IOException e){
            logger.log(Level.WARNING, "IO exception on reading OJN file {0}", chart.getSource().getName());
        }
        return event_list;
    }

    private static void readNoteBlock(List<Event> event_list, ByteBuffer buffer) throws IOException
    {
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
                case 2:channel = Event.Channel.NOTE_P1_1;break;
                case 3:channel = Event.Channel.NOTE_P1_2;break;
                case 4:channel = Event.Channel.NOTE_P1_3;break;
                case 5:channel = Event.Channel.NOTE_P1_4;break;
                case 6:channel = Event.Channel.NOTE_P1_5;break;
                case 7:channel = Event.Channel.NOTE_P1_6;break;
                case 8:channel = Event.Channel.NOTE_P1_7;break;
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
                    float volume = (volume_pan & 0xF) / 16f;
                    if(volume == 0)volume = 1;

                    // LEFT 1 ~ 8 CENTER 8 ~ 15 RIGHT, special: 0 = 8
                    float pan = (volume_pan & 0xF0) >> 4;
                    if(pan == 0)pan = 8;
                    pan -= 8;
                    pan /= 16;

                    value--;
                    if(type == 0){
                            event_list.add(new Event(channel,measure,position,value,Event.Flag.NONE,volume, pan));
                    }
                    else if(type == 2){
                        event_list.add(new Event(channel,measure,position,value,Event.Flag.HOLD,volume, pan));
                    }
                    else if(type == 3){
                        event_list.add(new Event(channel,measure,position,value,Event.Flag.RELEASE,volume, pan));
                    }
                    else if(type == 4){ // M### auto-play
                        event_list.add(new Event(channel,measure,position,1000+value,Event.Flag.RELEASE,volume, pan));
                    }
                }
            }
        }
        Collections.sort(event_list);
    }

    private static String bytes2string(byte[] ch)
    {
        int i = 0;
        while(ch[i]!=0 && i<ch.length)i++; // find \0 terminator
        String charset = CharsetDetector.analyze(ch);
        try {
            return new String(ch,0,i,charset);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.WARNING, "Encoding [{0}] not supported !", charset);
            return new String(ch,0,i);
        }
    }
}
