/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.parsers.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.open2jam.parsers.XNTChart;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author CdK
 */
public class KrazyRainDB extends DefaultHandler {
    
    private static final URL resources_xml = KrazyRainDB.class.getResource("/resources/KrazyRain.xml");
    
    private enum Keyword {
        MusicInfo, Music, Note, Compressor
    }

    private ArrayDeque<Keyword> call_stack;
    private ArrayDeque<Map<String,String>> atts_stack;
    
    private HashMap<String,ArrayList<XNTChart>> chart_map;
    private ArrayList<XNTChart> charts;
    
    private KrazyRainDB() {
	call_stack = new ArrayDeque<Keyword>();
        atts_stack = new ArrayDeque<Map<String,String>>();
	chart_map = new HashMap<String, ArrayList<XNTChart>>();
	charts = new ArrayList<XNTChart>();
	
	load();
    }
    
    public static KrazyRainDB getInstance() {
	return KrazyRainDBHolder.INSTANCE;
    }
    
    public ArrayList<XNTChart> getCharts(String filename)
    {
	if(chart_map.containsKey(filename)) return chart_map.get(filename);
	Logger.global.log(Level.WARNING, "Can''t find a KrazyRain chartList with the id {0}", filename);
	return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
	HashMap<String,String> atts_map = new HashMap<String,String>(atts.getLength());
        for(int i=0;i<atts.getLength();i++)
                atts_map.put(atts.getQName(i), atts.getValue(i));

        Keyword k = getKeyword(qName);
        call_stack.push(k);
        atts_stack.push(atts_map);
	
	switch(k)
	{
	    case Music:
		charts = new ArrayList<XNTChart>();
	    break;
	    case Note:
		XNTChart c = new XNTChart();
		if(atts_map.containsKey("level")) c.setLevel(Integer.parseInt(atts_map.get("level")));
		if(atts_map.containsKey("key")) c.setKeys(Integer.parseInt(atts_map.get("key")));
		if(atts_map.containsKey("note")) c.setNoteCount(Integer.parseInt(atts_map.get("note")));
		if(atts_map.containsKey("xnt_file")) c.setXNTFile(atts_map.get("xnt_file"));
		if(atts_map.containsKey("xne_file")) c.setXNEFile(atts_map.get("xne_file"));
		charts.add(c);
	    break;
	}
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
	Keyword k = call_stack.pop();
        Map<String,String> atts = atts_stack.pop();
	
	switch(k)
	{
	    case Music:
		String filename = "";
		String title = "";
		String genre = "";
		String artist = "";
		String noter = "";
		double bpm = 130d;
		int duration = 0;
		if(atts.containsKey("id")) filename = "C"+atts.get("id")+".snp";
		if(atts.containsKey("path")) filename = atts.get("path")+".snp";
		if(atts.containsKey("title")) title = atts.get("title");
		//TODO maybe just genre? genre_sub is more specific
		if(atts.containsKey("genre_sub")) genre = atts.get("genre_sub");
		if(atts.containsKey("bpm")) bpm = Double.parseDouble(atts.get("bpm"));
		if(atts.containsKey("Composer")) artist = atts.get("Composer");
		if(atts.containsKey("Pattern")) noter = atts.get("Pattern");
		if(atts.containsKey("playtime")) {
		    String[] t = atts.get("playtime").split(":");
		    duration = Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]);
		}
		
		for(XNTChart c : charts)
		{
		    c.setTitle(title);
		    c.setArtist(artist);
		    c.setBPM(bpm);
		    c.setDuration(duration);
		    c.setGenre(genre);
		    c.setNoter(noter);
		}
		
		filename = filename.toUpperCase();
		if(chart_map.containsKey(filename))
		    charts.addAll(chart_map.get(filename));
		
		chart_map.put(filename, charts);
	    break;
	}
    }
    
    private Keyword getKeyword(String s)
    {
        try{
            return Keyword.valueOf(s);
        }catch(IllegalArgumentException e){
            Logger.global.log(Level.WARNING, "Unknown keyword [{0}] in resources.xml.", s);
        }
        return null;
    }
    
    private void load() {
	try {
	    try {
		SAXParserFactory.newInstance().newSAXParser().parse(resources_xml.openStream(), this);
	    } catch (ParserConfigurationException ex) {
		java.util.logging.Logger.getLogger(KrazyRainDB.class.getName()).log(Level.SEVERE, "{0}", ex);
	    }
	} catch (SAXException ex) {
	    java.util.logging.Logger.getLogger(KrazyRainDB.class.getName()).log(Level.SEVERE, "{0}", ex);
	} catch (IOException ex) {
	    java.util.logging.Logger.getLogger(KrazyRainDB.class.getName()).log(Level.SEVERE, "{0}", ex);
	}	
    }
    
    private static class KrazyRainDBHolder {

	private static final KrazyRainDB INSTANCE = new KrazyRainDB();
    }
}
