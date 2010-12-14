package org.open2jam.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.open2jam.util.ByteBufferInputStream;

public class OJNChart extends Chart
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** full path to the source file of this header */
    protected File source;
    public File getSource() { return source; }

    /** an integer representing difficulty.
    *** this is the internal difficult level of the song
    *** for that rank **/
    protected short level;
    public int getLevel(){ return level; }

    protected String title;
    public String getTitle() { return title; }

    protected String artist;
    public String getArtist() { return artist; }

    protected String genre;
    public String getGenre() { return genre; }

    protected String noter;
    public String getNoter(){ return noter; }

    protected File sample_file;
    public Map<Integer,Integer> getSamples(){ return OJMParser.parseFile(sample_file); }

    /** the bpm as specified is the header */
    protected double bpm;
    public double getBPM() { return bpm; }

    /** the number of notes in the song */
    protected int note_count;
    public int getNoteCount() { return note_count; }

    /** the duration in seconds */
    protected int duration;
    public int getDuration() { return duration; }

    protected int note_offset;
    protected int note_offset_end;

    protected int cover_offset;
    protected int cover_size;
    public BufferedImage getCover()
    {
        try{
            RandomAccessFile f = new RandomAccessFile(source, "r");
            ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, cover_offset, cover_size);
            buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            ByteBufferInputStream bis = new ByteBufferInputStream(buffer);
            f.close();
            return ImageIO.read(bis);
        }catch(IOException e){
            logger.log(Level.WARNING, "IO exception getting image from file {0}", source.getName());
        }
        return null;
    }

    public int getKeys() { return 7; }

    public List<Event> getEvents() {
        return OJNParser.parseChart(this);
    }
}