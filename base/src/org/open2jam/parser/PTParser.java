package org.open2jam.parser;

import java.util.logging.Level;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.open2jam.util.Logger;

class PTParser
{

    private static final FileFilter pt_filter = new FileFilter(){
        public boolean accept(File f){
            String s = f.getName().toLowerCase();
            return (!f.isDirectory()) && (s.endsWith(".pt"));
        }
    };

    public static boolean canRead(File f)
    {
        if(!f.isDirectory())return false;

        File[] pt = f.listFiles(pt_filter);
        return pt.length > 0;
    }

    public static ChartList parseFile(File file)
    {
        ChartList list = new ChartList();
        list.source_file = file;

        File[] pt_files = file.listFiles(pt_filter);

        for (File pt_file : pt_files) {
            try {
                PTChart chart = parsePTheader(pt_file);
                if (chart != null) list.add(chart);
            } catch (Exception e) {
                Logger.global.log(Level.WARNING, "{0}", e);
            }
        }
        Collections.sort(list);
        if (list.isEmpty()) return null;
        return list;
    }

    private static PTChart parsePTheader(File f) throws IOException
    {
        return null;
    }

    public static List<Event> parseChart(PTChart chart)
    {
        return null;
    }

}
