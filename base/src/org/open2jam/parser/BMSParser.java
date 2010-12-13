package org.open2jam.parser;

import java.util.logging.Level;
import org.open2jam.util.OggInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.NoSuchElementException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.open2jam.render.lwjgl.SoundManager;

public class BMSParser
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final FileFilter bms_filter = new FileFilter(){
        public boolean accept(File f){
            String s = f.getName().toLowerCase();
            return (!f.isDirectory()) && (s.endsWith(".bms") || s.endsWith(".bme"));
        }
    };

    public static boolean canRead(File f)
    {
        if(!f.isDirectory())return false;

        File[] bms = f.listFiles(bms_filter);
        return bms.length > 0;
    }

    public static ChartList parseFile(File file)
    {
        ChartList list = new ChartList();
        list.source_file = file;

        File[] bms_files = file.listFiles(bms_filter);

        for(int i=0;i<bms_files.length;i++)
        {
            try{
                list.add(parseBMSHeader(bms_files[i]));
            }catch(UnsupportedOperationException e){continue;}
        }
        Collections.sort(list);
        return list;
    }

    private static BMSChart parseBMSHeader(File f) throws BadFileException
    {
        BMSChart chart = new BMSChart();
        BufferedReader r = null;
        try{
            r = new BufferedReader(new FileReader(f));
        }catch(FileNotFoundException e){
            logger.log(Level.WARNING, "File {0} not found !!", f.getName());
            return null;
        }

        int playlevel = 0;
        Integer rank = null;
        int player = 1;
        int bpm = 130;
        int lntype = 0;
        String title = null, artist = null, genre = null;
        String line = null;
        StringTokenizer st = null;
        Map<String,Integer> sample_files = new HashMap<String, Integer>();
        try{
        while((line = r.readLine()) != null)
        {
            line = line.trim();
            if(!line.startsWith("#"))continue;
            st = new StringTokenizer(line);

            String cmd = st.nextToken().toUpperCase();

            try{
                if(cmd.equals("#PLAYLEVEL")){
                        playlevel = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#RANK")){
                        rank = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#TITLE")){
                        title = st.nextToken("").trim();
                        continue;
                }
                if(cmd.equals("#ARTIST")){
                        artist = st.nextToken("").trim();
                        continue;
                }
                if(cmd.equals("#GENRE")){
                        genre = st.nextToken("").trim();
                        continue;
                }
                if(cmd.equals("#PLAYER")){
                        player = Integer.parseInt(st.nextToken());
                        if(player != 1)throw new UnsupportedOperationException("Not supported yet.");
                        continue;
                }
                if(cmd.equals("#BPM")){
                        bpm = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#LNTYPE")){
                        lntype = Integer.parseInt(st.nextToken());
                        continue;
                }
                if(cmd.equals("#LNOBJ")){
                        throw new UnsupportedOperationException("LNOBJ Not supported yet.");
                }
                if(cmd.equals("#STAGEFILE")){
                        chart.image_cover = new File(f.getParent(),st.nextToken("").trim());
                }
                if(cmd.startsWith("#WAV")){
                        int id = Integer.parseInt(cmd.replaceFirst("#WAV",""), 36);
                        String name = st.nextToken("").trim();
                        int idx = name.lastIndexOf('.');
                        if(idx > 0)name = name.substring(0, idx);
                        sample_files.put(name, id);
                        continue;
                }
            }catch(NoSuchElementException e){}
             catch(NumberFormatException e){ throw new BadFileException("unparsable number @ "+cmd); }
        }
        }catch(IOException e){
            logger.log(Level.WARNING, "IO exception on file parsing ! {0}", e.getMessage());
        }

        chart.level = playlevel;
        chart.title = title;
        chart.artist = artist;
        chart.genre = genre;
        chart.bpm = bpm;
        chart.sample_files = sample_files;
        chart.lntype = lntype;
        chart.source = f;
        return chart;
    }

    public static List<Event> parseChart(BMSChart chart)
    {
        ArrayList<Event> event_list = new ArrayList<Event>();
        BufferedReader r = null;
        String line = null;
        try{
            r = new BufferedReader(new FileReader(chart.source));
        }catch(FileNotFoundException e){
            logger.log(Level.WARNING, "File {0} not found !!", chart.source);
            return null;
        }

        HashMap<Integer, Double> bpm_map = new HashMap<Integer, Double>();
        HashMap<Integer, Boolean> ln_buffer = new HashMap<Integer, Boolean>();

        Pattern note_line = Pattern.compile("^#(\\d\\d\\d)(\\d\\d):(.+)$");
        Pattern bpm_line = Pattern.compile("^#BPM(\\w\\w)\\s+(.+)$");
        try {
            while ((line = r.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (!line.startsWith("#"))continue;

                Matcher matcher = note_line.matcher(line);
                if (!matcher.find()) {
                    Matcher bpm_match = bpm_line.matcher(line);
                    if (bpm_match.find()) {
                        int code = Integer.parseInt(bpm_match.group(1), 36);
                        double value = Double.parseDouble(bpm_match.group(2));
                        bpm_map.put(code, value);
                    }
                    continue;
                }
                int measure = Integer.parseInt(matcher.group(1));
                int channel = Integer.parseInt(matcher.group(2));
                if (channel == 2) {
                    // time signature
                    double value = Double.parseDouble(matcher.group(3));
                    event_list.add(new Event(Event.Channel.TIME_SIGNATURE, measure, 0, value, Event.Flag.NONE));
                    continue;
                }
                String[] events = matcher.group(3).split("(?<=\\G.{2})");
                if (channel == 3) {
                    for (int i = 0; i < events.length; i++) {
                        int value = Integer.parseInt(events[i], 16);
                        if (value == 0) {
                            continue;
                        }
                        double p = ((double) i) / events.length;
                        event_list.add(new Event(Event.Channel.BPM_CHANGE, measure, p, value, Event.Flag.NONE));
                    }
                    continue;
                } else if (channel == 8) {
                    for (int i = 0; i < events.length; i++) {
                        if (events[i].equals("00")) {
                            continue;
                        }
                        double value = bpm_map.get(Integer.parseInt(events[i], 36));
                        double p = ((double) i) / events.length;
                        event_list.add(new Event(Event.Channel.BPM_CHANGE, measure, p, value, Event.Flag.NONE));
                    }
                    continue;
                }
                Event.Channel ec;
                switch (channel)
                {
                    case 1:
                        ec = Event.Channel.AUTO_PLAY;
                        break;
                    case 11:
                    case 51:
                        ec = Event.Channel.NOTE_1;
                        break;
                    case 12:
                    case 52:
                        ec = Event.Channel.NOTE_2;
                        break;
                    case 13:
                    case 53:
                        ec = Event.Channel.NOTE_3;
                        break;
                    case 14:
                    case 54:
                        ec = Event.Channel.NOTE_4;
                        break;
                    case 15:
                    case 55:
                        ec = Event.Channel.NOTE_5;
                        break;
                    case 18:
                    case 58:
                        ec = Event.Channel.NOTE_6;
                        break;
                    case 19:
                    case 59:
                        ec = Event.Channel.NOTE_7;
                        break;
                    default:
                        continue;
                }
                for (int i = 0; i < events.length; i++) {
                    int value = Integer.parseInt(events[i], 36);
                    double p = ((double) i) / events.length;
                    if (channel > 50) {
                        Boolean b = ln_buffer.get(channel);
                        if (b != null && b == true) {
                            if (chart.lntype == 2) {
                                if (value == 0) {
                                    event_list.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
                                    ln_buffer.put(channel, false);
                                }
                            } else {
                                if (value > 0) {
                                    event_list.add(new Event(ec, measure, p, value, Event.Flag.RELEASE));
                                    ln_buffer.put(channel, false);
                                }
                            }
                        } else {
                            if (value > 0) {
                                ln_buffer.put(channel, true);
                                event_list.add(new Event(ec, measure, p, value, Event.Flag.HOLD));
                            }
                        }
                    } else {
                        if (value == 0) {
                            continue;
                        }
                        event_list.add(new Event(ec, measure, p, value, Event.Flag.NONE));
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        Collections.sort(event_list);
        return event_list;
    }

    public static HashMap<Integer,Integer> loadSamples(BMSChart h)
    {
        HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();
        for(File f : h.source.getParentFile().listFiles())
        {
            String s = f.getName();
            int idx = s.lastIndexOf('.');
            if(idx > 0)s = s.substring(0, idx);
            Integer id = h.sample_files.get(s);
            if(id == null)continue;
            try {
                int buffer = SoundManager.newBuffer(new OggInputStream(new FileInputStream(f)));
                samples.put(id, buffer);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return samples;
    }
}
