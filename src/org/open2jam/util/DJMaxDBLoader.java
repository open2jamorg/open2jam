package org.open2jam.util;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class DJMaxDBLoader {

    private static String db_path = "/resources/DJMAX_ONLINE.csv";

    private static HashMap<String, ArrayList<String>> database;

    private static HashMap<String, ArrayList<String>> readDB() throws IOException
    {
        return readDB(db_path);
    }
    
    /** Read the cvs file and return the result */
    private static HashMap<String, ArrayList<String>> readDB(String path) throws IOException
    {
        db_path = path;
        URL url = DJMaxDBLoader.class.getResource(db_path);
        BufferedReader r = null;
        if(url == null)
        {
            File file = new File(db_path);
            if(!file.exists()) return null;
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }
        else
        {
            r = new BufferedReader(new InputStreamReader(url.openStream()));
        }

        if(r == null) return null;
        
        database = new HashMap<String, ArrayList<String>>();

        String line;

        r.readLine(); //we don't need the header

        while((line = r.readLine()) != null)
        {
            line = line.replace("\"", "");

            ArrayList<String> al = new ArrayList<String>();

            for(String s : line.split("(?=;)"))
            {
                if(s.equals(";")) s = "99";
                al.add(s.replace(";", "").trim());
            }

            database.put(al.remove(0), al);
        }
        return database;
    }

    public static HashMap<String, ArrayList<String>> getDB() throws IOException
    {
        if(database == null)
            return readDB();
        else
            return database;
    }
}
