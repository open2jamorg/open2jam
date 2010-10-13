package org.open2jam.parser;

import java.util.ArrayList;
import java.util.List;

public class OJNParser implements ChartParser
{
	private LEFile f = null;

	protected int songid;
	protected byte signature[];
	protected byte encoder_value[];
	protected int genre;
	protected float bpm;
	protected short level[];
	protected int event_count[];
	protected int note_count[];
	protected int measure_count[];
	protected int package_count[];
	protected short unk_id[];
	protected byte unk_oldgenre[];
	protected int bmp_size;
	protected short unk_a[];
	protected byte title[];
	protected byte artist[];
	protected byte noter[];
	protected byte ojm_file[];
	protected int cover_size;
	protected int time[];
	protected int note_offset[];
	protected java.awt.Image cover_image;

	private static String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

	private Chart chart;
	private ChartHeader header;

	public ChartHeader parseFileHeader(String file, int rank)
	{
		try{
			f = new LEFile(file,"r");
			readHeader();

			header = new ChartHeader(file, rank, "OJN");
			header.level = level[rank];
			header.title = bytes2string(title);
			header.artist = bytes2string(artist);
			header.genre = genre_map[genre>10?10:genre];
			header.bpm = bpm;
			header.noteCount = note_count[rank];
			header.cover = cover_image;
			header.duration = time[rank];

			f.close();
		}catch(Exception e){ die(e); }
		return header;
	}

	private void readHeader() throws Exception
	{
		songid = f.readINT();
		signature = f.readBYTE(4);
		encoder_value = f.readBYTE(4);
		genre = f.readINT();
		bpm = f.readFLOAT();
		level = f.readSHORT(4);
		event_count = f.readINT(3);
		note_count = f.readINT(3);
		measure_count = f.readINT(3);
		package_count = f.readINT(3);
		unk_id = f.readSHORT(2);
		unk_oldgenre = f.readBYTE(20);
		bmp_size = f.readINT();
		unk_a = f.readSHORT(2);
		title = f.readBYTE(64);
		artist = f.readBYTE(32);
		noter = f.readBYTE(32);
		ojm_file = f.readBYTE(32);
		cover_size = f.readINT();
		time = f.readINT(3);
		note_offset = f.readINT(4);

		f.seek(note_offset[3]);
		byte cv_data[] = new byte[cover_size];
		f.read(cv_data,0,cover_size);
		cover_image = new javax.swing.ImageIcon(cv_data).getImage();
	}

	public Chart parseFile(ChartHeader header)
	{
		this.header = header;

		chart = new Chart(header);

		try{
			readNoteBlock(header.getSourceFile(), header.getRank());
		}catch(Exception e){ die(e); }
		return chart;
	}

	private void readNoteBlock(String file, int rank) throws Exception
	{
		f = new LEFile(file, "r");
		f.seek(note_offset[rank]);
		int endpos = note_offset[rank+1];

		NoteEvent ln_buffer[] = new NoteEvent[7];
		while(f.getFilePointer() < endpos)
		{
			int measure = f.readINT();
			short channel = f.readSHORT();
			int events_count = f.readSHORT();
			if(channel >= 1 && channel <= 8) // known channels until now
			{
				for(int i=0;i<events_count;i++)
				{
					double sub_measure = measure + (i / events_count);
					if(channel == 1) // BPM event
					{
						float bpm = f.readFLOAT();
						if(bpm == 0)continue;
						chart.add(new BPMEvent(sub_measure,channel,bpm));
					}else{ // note event
						short value = f.readSHORT();
						char unk = (char)f.readBYTE();
						char note_type = (char)f.readBYTE();
						if(value == 0)continue; // ignore value=0 events

						NoteEvent ne = new NoteEvent(sub_measure,channel,value);

						if(note_type == 2){ // long note start
							ln_buffer[channel-2] = ne;
						}else if(note_type == 3){ // long note end
							LongNoteEvent lne = new LongNoteEvent(ln_buffer[channel-2], sub_measure);
							chart.add(lne);
						}else{
							chart.add(ne);
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
		try{
			return new String(ch,"GB18030").replaceAll("\0","");
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