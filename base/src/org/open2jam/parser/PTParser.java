package org.open2jam.parser;

import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.open2jam.util.DJMaxDBLoader;
import org.open2jam.util.Logger;

class PTParser
{
    private static final int EZTR = 0x52545A45;
    private static final int HEADER_OFFSET = 0x18;
    private static final int SMP_BLOCK = 0x42;
    private static final int EZTR_HEADER = 0x4E;

    public static boolean canRead(File f)
    {
        return f.getName().toLowerCase().endsWith(".pt");
    }

    public static ChartList parseFile(File file)
    {
        ChartList list = new ChartList();
        list.source_file = file;
        System.out.println(file.toString());
        PTChart chart;
        try {
            chart = parsePTheader(file);
            if (chart != null) list.add(chart);
        } catch (IOException ex) {
            Logger.global.log(Level.WARNING, "Problem opening the file :/ {0}", ex);
        }


        Collections.sort(list);
        if (list.isEmpty()) return null;
        return list;
    }

    private static PTChart parsePTheader(File f) throws IOException
    {
        /*
         * There is nothing in the pt files nor pak ones that indicates the title,
         * artist, noter, etc. So we need to search in the db i made to get it.
         * The ideal filename is:
         * ai_normal_7k.pt or ai_hd_5key.pt
         * Now:
         * ai -> it's the key we will look for in the db, also it's the name of the pak file. Ex. ai.pak
         * normal, hd -> the difficulty, can be easy(ez), normal(nm), hard(hd), mx, sc
         * 7k, 5key -> the keys
         */
        HashMap<String, ArrayList<String>> hm = null;
        try
        {
            hm = DJMaxDBLoader.getDB();
        }
        catch(IOException e)
        {
            Logger.global.log(Level.WARNING, "Can't open the djmax db :/ {0}", e);
            return null;
        }
        if(hm.isEmpty()) return null;
        String[] splits = f.getName().toLowerCase().split("([^(a-zA-Z0-9)])");
        String name = "";
        int level = 6;
        int keys = 2;
        for(String s : splits)
        {
            if(hm.containsKey(s))
                name = s;
            if(s.equals("easy") || s.equals("ez"))
                level = 2;
            if(s.equals("normal") || s.equals("nm"))
                level = 4;
            if(s.equals("hard") || s.equals("hd"))
                level = 6;
            if(s.equals("mx"))
                level = 8;
            if(s.equals("sc"))
                level = 10;

            if(s.matches("(5k.*)"))
                keys = 1;
        }
        if(!hm.containsKey(name)) return null;
        ArrayList<String> al = hm.get(name);

        PTChart chart = new PTChart();
        File sample_file = new File(f.getParent(), name+".pak");
        chart.sample_file = sample_file;
        chart.source = f;
        chart.title = al.get(0);
        chart.artist = al.get(1);
        chart.genre = al.get(2);
        // this is a bit hack, we know that the position in the arraylist will be:
        // 3 for ez5 4 for ez7 etc
        // so level(easy) 2 + keys(5) 1 will be 3 ez5 :D and so on... XD
        // the level isn't the actual level, it's the value in the arraylist
        level = level+keys;
        chart.level = Integer.parseInt(al.get(level));
        chart.keys = keys == 1 ? 5 : 7;

        chart.sample_files = getSampleNames(chart);

        return chart;
    }

    public static List<Event> parseChart(PTChart chart)
    {
        ArrayList<Event> event_list = new ArrayList<Event>();
        try{
                RandomAccessFile f = new RandomAccessFile(chart.getSource().getAbsolutePath(), "r");

                int start = chart.eztr_start;
                int offset = 0;
                int eztr_block = 0;
                for(int counter = 0; counter < 64; counter++)
                {
                    ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start+eztr_block, EZTR_HEADER);
                    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    int eztr = buffer.getInt();
                    byte name[] = new byte[66];
                    buffer.get(name);
                    int unk1 = buffer.getInt();
                    offset = buffer.getInt();

                    eztr_block += EZTR_HEADER;
                    int note_block = 11;

                    for(int i = 0; i < offset/note_block; i++)
                    {
                        buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start+eztr_block+(i*note_block), note_block);
                        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                        switch(counter)
                        {
                            case 0:
                                readChunk(event_list, Event.Channel.BPM_CHANGE, buffer);
                            break;
                            case 3:
                                readChunk(event_list, Event.Channel.NOTE_1, buffer);
                            break;
                            case 4:
                                readChunk(event_list, Event.Channel.NOTE_2, buffer);
                            break;
                            case 5:
                                readChunk(event_list, Event.Channel.NOTE_3, buffer);
                            break;
                            case 6:
                                readChunk(event_list, Event.Channel.NOTE_4, buffer);
                            break;
                            case 7:
                                readChunk(event_list, Event.Channel.NOTE_5, buffer);
                            break;
                            case 8:
                            case 10:
                                readChunk(event_list, Event.Channel.NOTE_6, buffer);
                            break;
                            case 9:
                            case 11:
                                readChunk(event_list, Event.Channel.NOTE_7, buffer);
                            break;
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 30:
                            case 31:
                                readChunk(event_list, Event.Channel.AUTO_PLAY, buffer);
                            break;
                        }
                    }
                    eztr_block += offset;
                }
                f.close();
        }catch(java.io.FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", chart.getSource().getName());
        } catch (IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading PT file {0}", chart.getSource().getName());
        }
        return event_list;
    }

    private static HashMap<String, Integer> getSampleNames(PTChart chart)
    {
        HashMap<String, Integer> samples = new HashMap<String, Integer>();
        try{
                RandomAccessFile f = new RandomAccessFile(chart.getSource().getAbsolutePath(), "r");
                int offset = HEADER_OFFSET;
                for(int i = 0; i < 255 ;i++) // I think the maximum samples are 255
                {
                    offset = HEADER_OFFSET+(SMP_BLOCK*i);
                    ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, SMP_BLOCK);
                    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    if(buffer.getInt() == EZTR) //if EZTR stop it
                    {
                        chart.eztr_start = offset;
                        break;
                    }
                    buffer.rewind();
                    int id = buffer.get();
                    int bgm = buffer.get(); //not needed
                    byte name_byte[] = new byte[64];
                    buffer.get(name_byte);
                    String name = bytes2string(name_byte);
                    samples.put(name, id);
                }  
                f.close();
        }catch(java.io.FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", chart.getSource().getName());
        } catch (IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading OJN file {0}", chart.getSource().getName());
        }
        return samples;
    }

    private static void readChunk(List<Event> event_list, Event.Channel channel, ByteBuffer buffer) {
        while(buffer.hasRemaining())
        {
            double pos = buffer.getInt();
            int unk1 = buffer.get();
            // the measure is the int part of (pos/192) the position is the fractional one
            int measure = (int)pos/192;
            double position = (pos/192)-measure;
            if(channel == Event.Channel.BPM_CHANGE)
            {
                float bpm = buffer.getFloat();
                short unk2 = buffer.getShort();
                if(bpm < 0) continue;
                event_list.add(new Event(channel,measure,position,bpm,Event.Flag.NONE));
                System.out.println("BPM CHANGE @ "+measure+" : "+position+" VALUE "+bpm);
            }
            else
            {
                double sample_id = buffer.get();
                float vol = buffer.get();
                float pan = buffer.get();
                int unk2 = buffer.get();
                int length = buffer.get();
                int unk3 = buffer.get();

                if(vol == 0x7F) //127 = 1f
                    vol = 1.0f;
                else
                    vol /= 0x7F;
                if(pan == 0x40) //64 = 0.5f
                    pan = 0.5f;
                else
                    pan /= 0x7F;

                if(length < 6) continue;
                System.out.println("EVENT @ "+measure+" : "+position+" VALUE "+sample_id+" LENGTH "+length+" VOL/PAN "+vol+"/"+pan);
                if(length == 6)
                {
                    event_list.add(new Event(channel,measure,position,sample_id,Event.Flag.NONE,vol, pan));
                }
                else if(length > 6)
                {
                    event_list.add(new Event(channel,measure,position,sample_id,Event.Flag.HOLD,vol, pan));
                    pos = pos+length;
                    measure = (int)pos/192;
                    position = (pos/192)-measure;
                    event_list.add(new Event(channel,measure,position,sample_id,Event.Flag.RELEASE,vol, pan));
                }
            }
        }
        Collections.sort(event_list);
    }

    private static String bytes2string(byte[] ch)
    {
        int i = 0;
        while(i<ch.length && ch[i]!=0)i++; // find \0 terminator
        return new String(ch,0,i);

    }

}
