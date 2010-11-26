package org.open2jam.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.open2jam.util.ByteBufferInputStream;
import org.open2jam.util.Logger;

public class OJNChart implements Chart
{
	/** full path to the source file of this header */
	protected File source;
	public File getSource() { return source; }

	/** an integer representing difficulty.
	*** this is the internal difficult level of the song
	*** for that rank **/
	protected short[] level;
	public int getLevel(int rank){ return level[rank]; }

	public int getMaxRank(){ return 2; }

	protected String title;
	public String getTitle() { return title; }

	protected String artist;
	public String getArtist() { return artist; }

	protected String genre;
	public String getGenre() { return genre; }

	protected String noter;
	public String getNoter(){ return noter; }

	protected File sample_file;
	public Map<Integer,Integer> getSamples(int rank){ return OJMParser.parseFile(sample_file); }

	/** the bpm as specified is the header */
	protected double bpm;
	public double getBPM(int rank) { return bpm; }

	/** the number of notes in the song */
	protected int[] note_count;
	public int getNoteCount(int rank) { return note_count[rank]; }

	/** the duration in seconds */
	protected int[] duration;
	public int getDuration(int rank) { return duration[rank]; }


        protected int cover_size;
	public BufferedImage getCover()
        {
            try{
                RandomAccessFile f = new RandomAccessFile(source, "r");
                ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, note_offsets[3], cover_size);
                buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                ByteBufferInputStream bis = new ByteBufferInputStream(buffer);
                f.close();
                return ImageIO.read(bis);
            }catch(IOException e){
                Logger.log(e.toString()+": fail map ["+source.getName()+"] from["+note_offsets[3]+"] to ["+cover_size+"]");
            }
            return null;
        }

	protected int note_offsets[];
	public int[] getNoteOffsets() { return note_offsets; }

    public List<Event> getEvents(int rank) {
        return OJNParser.parseChart(this, rank);
    }
}