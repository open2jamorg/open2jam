package org.open2jam.parser;

import java.util.logging.Level;
import java.io.File;
import java.io.FileFilter;
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
    /** the PTFF signature in little endian */
    private static final int PTFF_SIGNATURE = 0x46465450;
    /** the long of the ptff header */
    private static final int PTFF_BLOCK = 0x18;
    /** the long of the samplename block */
    private static final int SAMPLE_BLOCK = 0x42;
    /** the EZTR signature in little endian */
    private static final int EZTR_SIGNATURE = 0x52545A45;
    /** the long of the eztr header */
    private static final int EZTR_BLOCK = 0x4E;
    /** the long of the event block */
    private static final int EVENT_BLOCK = 0xB;

    private static final FileFilter pt_filter = new FileFilter(){
        public boolean accept(File f){
            String s = f.getName().toLowerCase();
            return (!f.isDirectory()) && (s.endsWith(".pt"));
        }
    };

    public static boolean canRead(File f)
    {
        return f.getName().toLowerCase().endsWith(".pt");
    }

    public static ChartList parseFile(File file)
    {
        ChartList list = new ChartList();
        list.source_file = file;

        try {
            PTChart chart = parsePTheader(file);
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
        PTChart chart = new PTChart();
        //check if it's a pt file
        try{
            RandomAccessFile rf = new RandomAccessFile(f.getAbsolutePath(), "r");
            ByteBuffer buffer = rf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, PTFF_BLOCK);
            buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            int ptff_signature = buffer.getInt();
            int unk1 = buffer.getInt();
            float bpm = buffer.getFloat();
            int unk2 = buffer.getInt();
            int unk3 = buffer.getInt();
            int unk4 = buffer.getInt();
            if(ptff_signature != PTFF_SIGNATURE)
            {
                Logger.global.log(Level.WARNING, "File [{0}] isn't a PTFF file !", f.getName());
                return null;
            }

            chart.bpm = bpm;

        }catch(java.io.FileNotFoundException e){
            Logger.global.log(Level.WARNING, "File {0} not found !!", f.getName());
        } catch (IOException e){
            Logger.global.log(Level.WARNING, "IO exception on reading PT file {0}", f.getName());
        }

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

        HashMap<String, ArrayList<String>> db = new HashMap<String, ArrayList<String>>();
        try
        {
            db = DJMaxDBLoader.getDB();
        }
        catch(IOException e)
        {
            Logger.global.log(Level.WARNING, "Can't open the djmax db :/ {0}", e);
            return null;
        }

        String[] splits = f.getName().toLowerCase().split("([^(a-zA-Z0-9)])");
        String name = f.getName();
        int level = 6;
        int keys = 2;
        for(String s : splits)
        {
            if(db.containsKey(s))
                name = s;
            if(s.equals("easy") || s.equals("ez"))
                level = 2;
            else if(s.equals("normal") || s.equals("nm"))
                level = 4;
            else if(s.equals("hard") || s.equals("hd"))
                level = 6;
            else if(s.equals("mx"))
                level = 8;
            else if(s.equals("sc"))
                level = 10;

            if(s.matches("(5k.*)"))
                keys = 1;
        }

        if(db.containsKey(name))
        {
            ArrayList<String> al = db.get(name);
            
            File sample_file = new File(f.getParent(), name+".pak");
            chart.sample_file = sample_file;   
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
        }
        else
        {
            chart.sample_file = null;
            chart.title = name;
            chart.artist = "Unknown";
            chart.genre = "Unknown";
            chart.level = 0;
            chart.keys = 7;
        }
        
        chart.source = f;
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
                    ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start+eztr_block, EZTR_BLOCK);
                    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    int eztr = buffer.getInt();
                    byte name[] = new byte[66];
                    buffer.get(name);
                    int unk1 = buffer.getInt();
                    offset = buffer.getInt();

                    if (eztr != EZTR_SIGNATURE)
                    {
                        Logger.global.log(Level.WARNING, "Something went wrong with the parser. Iteration {0} ... OH NOES!!!!11!!one!1", counter);
                        return null;
                    }
                    
                    eztr_block += EZTR_BLOCK;

                    for(int i = 0; i < offset/EVENT_BLOCK; i++)
                    {
                        buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start+eztr_block+(i*EVENT_BLOCK), EVENT_BLOCK);
                        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                        switch(counter)
                        {
                            case 0: //BPM changes
                                readChunk(event_list, Event.Channel.BPM_CHANGE, buffer);
                            break;
                            //case 1: case 2: DUMMIES
                            case 3: // NOTE 1 p1
                                readChunk(event_list, Event.Channel.NOTE_1, buffer);
                            break;
                            case 4: // NOTE 2 p1
                                readChunk(event_list, Event.Channel.NOTE_2, buffer);
                            break;
                            case 5: // NOTE 3 p1
                                readChunk(event_list, Event.Channel.NOTE_3, buffer);
                            break;
                            case 6: // NOTE 4 p1
                                readChunk(event_list, Event.Channel.NOTE_4, buffer);
                            break;
                            case 7: // NOTE 5 p1
                                readChunk(event_list, Event.Channel.NOTE_5, buffer);
                            break;
                            case 8: // NOTE 6 p1 <- seems empty
                            case 10:// SCRATCH p1 <- seems to use it as note 6
                                readChunk(event_list, Event.Channel.NOTE_6, buffer);
                            break;
                            case 9: // NOTE 7 p1 <- seems empty
                            case 11:// PEDAL p1 <- seems to use it as note 7
                                readChunk(event_list, Event.Channel.NOTE_7, buffer);
                            break;
                            //case 12: case 13: case 14: case 15: case 16: NOTE 1 to 5 p2 <- seems not used
                            //case 17: case 18: NOTE 6 and 7 p2 <- seems not used
                            //case 19: case 20: SCRATCH and PEDAL p2 <- seems not used
                            //case 21: LIGHTS? <- seems not used
                            case 22: case 23: case 24: case 25: case 26: // BGM
                            case 27: case 28: case 29: case 30: case 31: // BGM
                                readChunk(event_list, Event.Channel.AUTO_PLAY, buffer);
                            break;
                            //case 31 to 63 UNKNOWN maybe for bga?
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
                int offset = PTFF_BLOCK;
                for(int i = 0;;i++)
                {
                    offset = PTFF_BLOCK+(SAMPLE_BLOCK*i);
                    ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, offset, SAMPLE_BLOCK);
                    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    if(buffer.getInt() == EZTR_SIGNATURE) //if EZTR stop it
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
            int unk1 = buffer.get(); //some kind of... idk status? check? just garbage? idk but if it's 2 or 4 i'm pretty sure we should skip it
                                     //because when it's 2 or 4 the values are 0 or negavite or worse xD
            if(unk1 == 0x02 || unk1 == 0x04) break;
            // the measure is the integer part of (pos/192) the position is the fractional one
            int measure = (int)pos/192;
            double position = (pos/192)-measure;
            if(channel == Event.Channel.BPM_CHANGE)
            {
                float bpm = buffer.getFloat();
                short unk2 = buffer.getShort();
                if(bpm < 0) break;
                event_list.add(new Event(channel,measure,position,bpm,Event.Flag.NONE));
//                System.out.println("BPM CHANGE @ "+measure+" : "+position+" VALUE "+bpm);
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
//                System.out.println("EVENT ["+channel.toString()+"] @ "+measure+" : "+position+" VALUE "+sample_id+" LENGTH "+length+" VOL/PAN "+vol+"/"+pan);
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
