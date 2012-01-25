/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.open2jam.parsers.utils.AudioData;

/**
 *
 * @author CdK
 */
public class XNTChart extends Chart {
    
    Map<String, SNPParser.SNPFileHeader> file_index;
    File snp_file;
    
    File source;
    @Override
    public File getSource() {
	return source;
    }

    int level=0;
    @Override
    public int getLevel() {
	return level;
    }

    int keys=0;
    @Override
    public int getKeys() {
	return keys;
    }

    String title="";
    @Override
    public String getTitle() {
	return title;
    }

    String artist="";
    @Override
    public String getArtist() {
	return artist;
    }

    String genre="";
    @Override
    public String getGenre() {
	return genre;
    }

    @Override
    public String getNoter() {
	return "";
    }

    Map<Integer, AudioData> samples;
    @Override
    public Map<Integer, AudioData> getSamples() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    double bpm=0d;
    @Override
    public double getBPM() {
	return bpm;
    }

    int notecount=0;
    @Override
    public int getNoteCount() {
	return notecount;
    }

    int duration=0;
    @Override
    public int getDuration() {
	return duration;
    }

    File image_file;
    @Override
    public BufferedImage getCover() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Event> getEvents() {
	return null;
	//return SNPParser.parseChart(this);
    }    
}
