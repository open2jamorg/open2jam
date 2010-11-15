package org.open2jam.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.open2jam.Util;

public class BMSChart implements Chart
{
    protected int lntype;

    protected Map<Integer, Integer> rank_map;

    protected File source;
    protected File[] bms;
    public File getSource() { return source; }

    protected int[] level;
    public int getLevel(int rank) { return level[rank_map.get(rank)]; }

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
        return BMSParser.loadSamples(this, rank_map.get(rank));
    }

    protected int[] bpm;
    public double getBPM(int rank) {
        return bpm[rank_map.get(rank)];
    }

    public int getNoteCount(int rank) { return 0; }

    public int getDuration(int rank) { return 0; }

    protected File image_cover;
    public BufferedImage getCover() {
        try {
            return ImageIO.read(image_cover);
        } catch (IOException ex) {Util.warn(ex);}
        return null;
    }

    public String getNoter() { return ""; }

    public List<Event> getEvents(int rank) {
        return BMSParser.parseChart(this, rank_map.get(rank));
    }
}
