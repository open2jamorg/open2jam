package org.open2jam.parser;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class OJNParser
{

	private static String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

        public static boolean canRead(File f)
        {
            if(f.isDirectory())return false;
            try{
                FileInputStream fis = new FileInputStream(f);
                byte[] sig = new byte[4];
                fis.read(sig); // jump songid
                fis.read(sig);
                fis.close();
                String s = new String(sig);
                return s.equals("ojn\0");
            }catch(IOException e){
                e.printStackTrace();
            }
            return false;
        }

	public static ChartHeader parseFileHeader(File file)
	{
		OJNHeader header = new OJNHeader();
		try{
			RandomAccessFile f = new RandomAccessFile(file.getAbsolutePath(),"r");
                        byte[] sig = new byte[4];
                        f.read(sig); // jump songid
                        f.read(sig);
                        String s = new String(sig);
                        if(!s.equals("ojn\0"))throw new BadFileException("Not a OJN file");

			ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 300);
			buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			header.source = file;
			readHeader(header, buffer, f);
                        buffer = null;
			f.close();
		}catch(IOException e){ die(e); }
		return header;
	}

	private static void readHeader(OJNHeader header, ByteBuffer buffer, RandomAccessFile f) throws BadFileException
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
			byte[] cv_data;
			if(buffer.hasArray()){
				cv_data = buffer.array();
			}else{
				cv_data = new byte[cover_size];
				buffer.get(cv_data);
			}
			header.cover = Toolkit.getDefaultToolkit().createImage(cv_data);
		}catch(Exception e){}

		header.level = level;
		header.title = bytes2string(title);
		header.artist = bytes2string(artist);
		header.genre = genre_map[(genre<0||genre>10)?10:genre];
		header.bpm = bpm;
		header.note_count = note_count;
		header.noter = bytes2string(noter);
		header.duration = time;
		header.sample_file = bytes2string(ojm_file);

		//ojn specific fields
		header.note_offsets = note_offsets;
	}

	public static Chart parseFile(OJNHeader header, int rank)
	{
		Chart chart = new Chart(header, rank);
		try{
			RandomAccessFile f = new RandomAccessFile(header.getSource().getAbsolutePath(), "r");

			int start = header.getNoteOffsets()[rank];
			int end = header.getNoteOffsets()[rank+1];

			ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start, end - start);
			buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			readNoteBlock(chart, buffer);
			f.close();
		}catch(Exception e){ die(e); }
		return chart;
	}

	private static void readNoteBlock(Chart chart, ByteBuffer buffer) throws Exception
	{
		while(buffer.hasRemaining())
		{
			int measure = buffer.getInt();
			short channel = buffer.getShort();
			short events_count = buffer.getShort();

			for(double i=0;i<events_count;i++)
			{
				double position = i / events_count;
				if(channel == 0 || channel == 1) // fractional measure or BPM event
				{
					float v = buffer.getFloat();
					if(v == 0)continue;
					chart.add(new Event(channel,measure,position,v,Event.Flag.NONE));
				}else{ // note event
					short value = buffer.getShort();
					int unk = buffer.get();
					int type = buffer.get();
					if(value == 0)continue; // ignore value=0 events

					if(type == 0){
						chart.add(new Event(channel,measure,position,value*(unk+1),Event.Flag.NONE));
					}
					else if(type == 2){
						chart.add(new Event(channel,measure,position,value*(unk+1),Event.Flag.HOLD));
					}
					else if(type == 3){
						chart.add(new Event(channel,measure,position,value*(unk+1),Event.Flag.RELEASE));
					}
				}
			}
		}
		chart.finalize();
	}

	private static String bytes2string(byte[] ch)
	{
		int i; for(i=0;i<ch.length&&ch[i]!=0;i++); // find \0 terminator
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