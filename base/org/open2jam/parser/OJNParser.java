package org.open2jam.parser;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.Toolkit;

public class OJNParser
{
	private ByteBuffer buffer = null;
	private RandomAccessFile f = null;

	private static String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

	private Chart chart;
	private OJNHeader header;

	public ChartHeader parseFileHeader(String file)
	{
		header = new OJNHeader();
		try{
			f = new RandomAccessFile(file,"r");
			buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 300);
			buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			header.source_file = file;
			readHeader();
                        buffer = null;
			f.close();
		}catch(Exception e){ die(e); }
		return header;
	}

	private void readHeader() throws Exception
	{
		int songid = buffer.getInt();
		byte signature[] = new byte[4];
		buffer.get(signature);
		byte encoder_value[] = new byte[4];
		buffer.get(encoder_value);
		int genre = buffer.getInt();
		float bpm = buffer.getFloat();
		short level[] = new short[4];
		level[0] = buffer.getShort();
		level[1] = buffer.getShort();
		level[2] = buffer.getShort();
		level[3] = buffer.getShort();
		int event_count[] = new int[3];
		event_count[0] = buffer.getInt();
		event_count[1] = buffer.getInt();
		event_count[2] = buffer.getInt();
		int note_count[] = new int[3];
		note_count[0] = buffer.getInt();
		note_count[1] = buffer.getInt();
		note_count[2] = buffer.getInt();
		int measure_count[] = new int[3];
		measure_count[0] = buffer.getInt();
		measure_count[1] = buffer.getInt();
		measure_count[2] = buffer.getInt();
		int package_count[] = new int[3];
		package_count[0] = buffer.getInt();
		package_count[1] = buffer.getInt();
		package_count[2] = buffer.getInt();
		short unk_id[] = new short[2];
		unk_id[0] = buffer.getShort();
		unk_id[1] = buffer.getShort();
		byte unk_oldgenre[] = new byte[20];
		buffer.get(unk_oldgenre);
		int bmp_size = buffer.getInt();
		short unk_a[] = new short[2];
		unk_a[0] = buffer.getShort();
		unk_a[1] = buffer.getShort();
		byte title[] = new byte[64];
		buffer.get(title);
		byte artist[] = new byte[32];
		buffer.get(artist);
		byte noter[] = new byte[32];
		buffer.get(noter);
		byte ojm_file[] = new byte[32];
		buffer.get(ojm_file);
		int cover_size = buffer.getInt();
		int time[] = new int[3];
		time[0] = buffer.getInt();
		time[1] = buffer.getInt();
		time[2] = buffer.getInt();
		int note_offsets[] = new int[4];
		note_offsets[0] = buffer.getInt();
		note_offsets[1] = buffer.getInt();
		note_offsets[2] = buffer.getInt();
		note_offsets[3] = buffer.getInt();

                try{
                    buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, note_offsets[3], cover_size);
                    byte cv_data[] = new byte[cover_size];
                    buffer.get(cv_data);

                    java.awt.Image cover_image = Toolkit.getDefaultToolkit().createImage(cv_data);
                    header.cover = cover_image;
                }catch(Exception e){}

		header.level = level;
		header.title = bytes2string(title);
		header.artist = bytes2string(artist);
		header.genre = genre_map[(genre<0||genre>10)?10:genre];
		header.bpm = bpm;
		header.note_count = note_count;
		
		header.duration = time;

		//ojn specific fields
		header.note_offsets = note_offsets;
	}

	public Chart parseFile(OJNHeader header, int rank)
	{
		this.header = header;
		chart = new Chart(header);
		try{
			f = new RandomAccessFile(header.getSourceFile(), "r");

			int start = header.getNoteOffsets()[rank];
			int end = header.getNoteOffsets()[rank+1];

			buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start, end - start);
			buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			readNoteBlock();
			f.close();
		}catch(Exception e){ die(e); }
		return chart;
	}

	private void readNoteBlock() throws Exception
	{
		while(buffer.hasRemaining())
		{
			int measure = buffer.getInt();
			short channel = buffer.getShort();
			int events_count = buffer.getShort();

			for(double i=0;i<events_count;i++)
			{
				double position = i / events_count;
				if(channel == 0 || channel == 1) // fractional measure or BPM event
				{
					float v = buffer.getFloat();
					if(v == 0)continue;
					chart.add(new Event(channel,measure,position,v,0,0));
				}else{ // note event
					short value = buffer.getShort();
					int unk = buffer.get();
					int type = buffer.get();
					if(value == 0)continue; // ignore value=0 events
					chart.add(new Event(channel,measure,position,value,unk,type));
				}
			}
		}
		chart.finalize();
	}

	private static String bytes2string(byte[] ch)
	{
		int i; for(i=0;i<ch.length-1||ch[i]!=0;i++); // find \0 terminator
		try{
			return new String(ch,0,i);
		}catch(Exception e){die(e);}
		return null;
	}

	private static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		final java.io.PrintWriter pw = new java.io.PrintWriter(r);
		e.printStackTrace(pw);
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}