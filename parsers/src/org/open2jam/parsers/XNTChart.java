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
 * A class to read the XNT files from KrazyRain
 *
 * @author CdK
 */
public class XNTChart extends Chart {
    
    Map<String, SNPParser.SNPFileHeader> file_index;
    
    Map<Integer, String> samples_index;
    
    String xnt_filename = "";
    public String getXNTFile() {
	return xnt_filename;
    }
    public void setXNTFile(String name) {
	xnt_filename = name;
    }
    
    String xne_filename = "";
    public String getXNEFile() {
	return xne_filename;
    }
    public void setXNEFile(String name) {
	xne_filename = name;
    }
    
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
    public void setLevel(int level) {
	this.level = level;
    }

    int keys=0;
    @Override
    public int getKeys() {
	return keys;
    }
    public void setKeys(int keys) {
	this.keys = keys;
    }

    String title="";
    @Override
    public String getTitle() {
	return title;
    }
    public void setTitle(String title) {
	this.title = title;
    }

    String artist="";
    @Override
    public String getArtist() {
	return artist;
    }
    public void setArtist(String artist) {
	this.artist = artist;
    }

    String genre="";
    @Override
    public String getGenre() {
	return genre;
    }
    public void setGenre(String genre) {
	this.genre = genre;
    }

    String noter="";
    @Override
    public String getNoter() {
	return noter;
    }
    public void setNoter(String noter) {
	this.noter = noter;
    }

    Map<Integer, AudioData> samples;
    @Override
    public Map<Integer, AudioData> getSamples() {
	return SNPParser.getSamples(this);
    }

    double bpm=130d;
    @Override
    public double getBPM() {
	return bpm;
    }
    public void setBPM(double bpm) {
	this.bpm = bpm;
    }

    int notecount=0;
    @Override
    public int getNoteCount() {
	return notecount;
    }
    public void setNoteCount(int count) {
	this.notecount = count;
    }

    int duration=0;
    @Override
    public int getDuration() {
	return duration;
    }
    public void setDuration(int duration) {
	this.duration = duration;
    }

    File image_file;
    @Override
    public BufferedImage getCover() {
	return getNoImage();
    }

    @Override
    public List<Event> getEvents() {
	return XNTParser.parseChart(this);
    }    
}
