/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.open2jam.parsers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import org.open2jam.parsers.utils.SampleData;

/**
 * A class to read the XNT files from KrazyRain
 *
 * @author CdK
 */
public class XNTChart extends Chart {

    Map<String, SNPParser.SNPFileHeader> file_index;

    public XNTChart() {
	type = TYPE.XNT;
    }

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
    
    public File getSource() {
	return source;
    }
    
    public int getLevel() {
	return level;
    }
    public void setLevel(int level) {
	this.level = level;
    }
    
    public int getKeys() {
	return keys;
    }
    public void setKeys(int keys) {
	this.keys = keys;
    }
    
    public int getPlayers() {
	return players;
    }
    
    public String getTitle() {
	return title;
    }
    public void setTitle(String title) {
	this.title = title;
    }
    
    public String getArtist() {
	return artist;
    }
    public void setArtist(String artist) {
	this.artist = artist;
    }
    
    public String getGenre() {
	return genre;
    }
    public void setGenre(String genre) {
	this.genre = genre;
    }
    
    public String getNoter() {
	return noter;
    }
    public void setNoter(String noter) {
	this.noter = noter;
    }
    
    Map<Integer, SampleData> samples;
    public Map<Integer, SampleData> getSamples() {
	return SNPParser.getSamples(this);
    }
    
    public double getBPM() {
	return bpm;
    }
    public void setBPM(double bpm) {
	this.bpm = bpm;
    }
    
    public int getNoteCount() {
	return notes;
    }
    public void setNoteCount(int count) {
	this.notes = count;
    }
    
    public int getDuration() {
	return duration;
    }
    public void setDuration(int duration) {
	this.duration = duration;
    }
    
    public BufferedImage getCover() {
	return getNoImage();
    }

    public EventList getEvents() {
	return XNTParser.parseChart(this);
    }
}
