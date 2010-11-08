package org.open2jam.parser;

import java.awt.Image;
import java.io.File;
import java.util.Map;
import org.open2jam.parser.ChartParser.Formats;

public class BMSHeader implements ChartHeader
{

    protected int lntype;

    public Formats getSourceType() { return Formats.BMS; }

    protected File source;
    protected File[] bms;
    public File getSource() { return source; }

    protected int[] level;
    public int getLevel(int rank) { return level[rank]; }

    protected int max_rank;
    public int getMaxRank() {
        return max_rank;
    }

    protected String title;
    public String getTitle() {
        return title;
    }

    protected String artist;
    public String getArtist() {
        return artist;
    }

    protected String genre;
    public String getGenre() {
        return genre;
    }

    protected Map<Integer, File> sample_files;
    public Map<Integer,Integer> getSamples(int rank) {
        return BMSParser.loadSamples(this, rank);
    }

    protected int[] bpm;
    public double getBPM(int rank) {
        return bpm[rank];
    }

    public int getNoteCount(int rank) { return 0; }

    public int getDuration(int rank) { return 0; }

    public Image getCover() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNoter() { return ""; }
}
