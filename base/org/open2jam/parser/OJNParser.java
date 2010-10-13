package org.open2jam.parser;

import java.util.ArrayList;
import java.util.List;

public class OJNParser
{
	private LEFile f = null;

	private static String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

	private Chart chart;
	private OJNHeader header;

	public ChartHeader parseFileHeader(String file, int rank)
	{
		try{
			f = new LEFile(file,"r");

			header = new OJNHeader();
			header.source_file = file;
			header.rank = rank;
			readHeader();

			f.close();
		}catch(Exception e){ die(e); }
		return header;
	}

	private void readHeader() throws Exception
	{
		int songid = f.readINT();
		byte signature[] = f.readBYTE(4);
		byte encoder_value[] = f.readBYTE(4);
		int genre = f.readINT();
		float bpm = f.readFLOAT();
		short level[] = f.readSHORT(4);
		int event_count[] = f.readINT(3);
		int note_count[] = f.readINT(3);
		int measure_count[] = f.readINT(3);
		int package_count[] = f.readINT(3);
		short unk_id[] = f.readSHORT(2);
		byte unk_oldgenre[] = f.readBYTE(20);
		int bmp_size = f.readINT();
		short unk_a[] = f.readSHORT(2);
		byte title[] = f.readBYTE(64);
		byte artist[] = f.readBYTE(32);
		byte noter[] = f.readBYTE(32);
		byte ojm_file[] = f.readBYTE(32);
		int cover_size = f.readINT();
		int time[] = f.readINT(3);
		int note_offsets[] = f.readINT(4);

		f.seek(note_offsets[3]);
		byte cv_data[] = new byte[cover_size];
		f.read(cv_data,0,cover_size);
		java.awt.Image cover_image = new javax.swing.ImageIcon(cv_data).getImage();


		header.level = level[header.rank];
		header.title = bytes2string(title);
		header.artist = bytes2string(artist);
		header.genre = genre_map[genre>10?10:genre];
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
		f = new LEFile(header.getSourceFile(), "r");
		f.seek(header.getNoteOffsets()[header.getRank()]);
		int endpos = header.getNoteOffsets()[header.getRank()+1];

		NoteEvent ln_buffer[] = new NoteEvent[7];
		while(f.getFilePointer() < endpos)
		{
			int measure = f.readINT();
			short channel = f.readSHORT();
			int events_count = f.readSHORT();
			if(channel >= 1 && channel <= 8) // known channels until now
			{
				for(double i=0;i<events_count;i++)
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

						NoteEvent ne = new NoteEvent(sub_measure,channel-2,value);

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