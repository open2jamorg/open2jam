package org.open2jam.parser;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

public class OJNParser
{
	private LERandomAccessFile f = null;

	private static String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

	private Chart chart;
	private OJNHeader header;

	public ChartHeader parseFileHeader(String file, int rank)
	{
		header = new OJNHeader();
		try{
			f = new LERandomAccessFile(file,"r");
			header.source_file = file;
			header.rank = rank;
			readHeader();
			f.close();
		}catch(Exception e){ die(e); }
		return header;
	}

	private void readHeader() throws Exception
	{
		int songid = f.readInt();
		byte signature[] = new byte[4];
		f.read(signature);
		byte encoder_value[] = new byte[4];
		f.read(encoder_value);
		int genre = f.readInt();
		float bpm = f.readFloat();
		short level[] = new short[4];
		level[0] = f.readShort();
		level[1] = f.readShort();
		level[2] = f.readShort();
		level[3] = f.readShort();
		int event_count[] = new int[3];
		event_count[0] = f.readInt();
		event_count[1] = f.readInt();
		event_count[2] = f.readInt();
		int note_count[] = new int[3];
		note_count[0] = f.readInt();
		note_count[1] = f.readInt();
		note_count[2] = f.readInt();
		int measure_count[] = new int[3];
		measure_count[0] = f.readInt();
		measure_count[1] = f.readInt();
		measure_count[2] = f.readInt();
		int package_count[] = new int[3];
		package_count[0] = f.readInt();
		package_count[1] = f.readInt();
		package_count[2] = f.readInt();
		short unk_id[] = new short[2];
		unk_id[0] = f.readShort();
		unk_id[1] = f.readShort();
		byte unk_oldgenre[] = new byte[20];
		f.read(unk_oldgenre);
		int bmp_size = f.readInt();
		short unk_a[] = new short[2];
		unk_a[0] = f.readShort();
		unk_a[1] = f.readShort();
		byte title[] = new byte[64];
		f.read(title);
		byte artist[] = new byte[32];
		f.read(artist);
		byte noter[] = new byte[32];
		f.read(noter);
		byte ojm_file[] = new byte[32];
		f.read(ojm_file);
		int cover_size = f.readInt();
		int time[] = new int[3];
		time[0] = f.readInt();
		time[1] = f.readInt();
		time[2] = f.readInt();
		int note_offsets[] = new int[4];
		note_offsets[0] = f.readInt();
		note_offsets[1] = f.readInt();
		note_offsets[2] = f.readInt();
		note_offsets[3] = f.readInt();

		f.seek(note_offsets[3]);
		byte cv_data[] = new byte[cover_size];
		f.read(cv_data);

		java.awt.Image cover_image = Toolkit.getDefaultToolkit().createImage(cv_data);

		header.level = level[header.rank];
		header.title = bytes2string(title);
		header.artist = bytes2string(artist);
		header.genre = genre_map[(genre<0||genre>10)?10:genre];
		header.bpm = bpm;
		header.note_count = note_count[header.rank];
		header.cover = cover_image;
		header.duration = time[header.rank];

		//ojn specific fields
		header.note_offsets = note_offsets;
	}

	public Chart parseFile(OJNHeader header)
	{
		this.header = header;
		chart = new Chart(header);
		try{
			readNoteBlock();
		}catch(Exception e){ die(e); }
		return chart;
	}

	private void readNoteBlock() throws Exception
	{
		f = new LERandomAccessFile(header.getSourceFile(), "r");
		f.seek(header.getNoteOffsets()[header.getRank()]);
		int endpos = header.getNoteOffsets()[header.getRank()+1];

		NoteEvent ln_buffer[] = new NoteEvent[7];
		while(f.getFilePointer() < endpos)
		{
			int measure = f.readInt();
			short channel = f.readShort();
			int events_count = f.readShort();
			if(channel > 0 && channel < 9) // 1~8 known channels until now
			{
				for(double i=0;i<events_count;i++)
				{
					double sub_measure = measure + (i / events_count);
					if(channel == 1) // BPM event
					{
						float bpm = f.readFloat();
						if(bpm == 0)continue;
						chart.add(new BPMEvent(sub_measure,channel,bpm));
					}else{ // note event
						short value = f.readShort();
						int unk = f.read();
						int note_type = f.read();
						if(value == 0)continue; // ignore value=0 events

						if(note_type == 3){ // long note end
							chart.add(new LongNoteEvent(ln_buffer[channel-2], sub_measure));
						}else if(note_type == 2){
							ln_buffer[channel-2] = new NoteEvent(sub_measure,channel-2,value);
						}else{
							chart.add(new NoteEvent(sub_measure,channel-2,value));
						}
					}
				}
			}else{
				f.skipBytes(4*events_count); // skipping channels I don't known
			}
		}
		chart.finalize();
		f.close();
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