package org.open2jam.parser;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class OJN
{
	private LEFile f = null;

	int songid;
	byte signature[];
	int genre;
	float bpm;
	short level[];
	int unk_num[];
	byte unk_zero[];
	int note_count[];
	int unk_time[];
	int package_count[];
	short unk_id[];
	byte unk_oldgenre[];
	int bmp_size;
	short unk_a[];
	byte title[];
	byte artist[];
	byte noter[];
	byte ojm_file[];
	int cover_size;
	int time[];
	int note_offset[];

	List<Chart> notes_section;

	ImageIcon cover_image;
	ImageIcon mini_cover;

	static String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

	public OJN(String filename)
	{
		try{
			f = new LEFile(filename,"r");
			readHeader();
			readNotes();
			readCover();
			f.close();
		}catch(Exception e){die(e);}
	}
	private void readHeader()
	{
		songid = f.readINT();
		signature = f.readBYTE(8);
		genre = f.readINT();
		bpm = f.readFLOAT();
		level = f.readSHORT(3);
		unk_num = f.readINT(3);
		unk_zero = f.readBYTE(2);
		note_count = f.readINT(3);
		unk_time = f.readINT(3);
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
	}
	private void readNotes() throws Exception
	{
		notes_section = new ArrayList<Chart>(3);
		notes_section.add(new Chart());
		notes_section.add(new Chart());
		notes_section.add(new Chart());
		readNoteBlock(0);
		readNoteBlock(1);
		readNoteBlock(2);
	}
	private void readNoteBlock(int lvl) throws Exception
	{
		f.seek(note_offset[lvl]);
		int endpos = note_offset[lvl+1];

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
						notes_section.get(lvl).add(new BPMEvent(sub_measure,channel,bpm));
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
							notes_section.get(lvl).add(lne);
						}else{
							notes_section.get(lvl).add(ne);
						}
					}
				}
			}else{
				f.skipBytes(4*events_count); // skipping channels I don't known
			}
		}
		notes_section.get(lvl).finalize();
	}
	private void readCover() throws Exception
	{
		f.seek(note_offset[3]);
		byte cv_data[] = new byte[cover_size];
		f.read(cv_data,0,cover_size);
		cover_image = new ImageIcon(cv_data);
		mini_cover = new ImageIcon(cover_image.getImage().getScaledInstance(200,200,java.awt.Image.SCALE_SMOOTH));
	}

	public String getGenre()
	{
		return genre_map[genre];
	}
	public String getTitle()
	{
		return bytes2string(title);
	}
	public String getArtist()
	{
		return bytes2string(artist);
	}
	public String getNoter()
	{
		return bytes2string(noter);
	}
	public String getOJM()
	{
		return bytes2string(ojm_file);
	}
	public ImageIcon getMiniCover()
	{
		return mini_cover;
	}
	public ImageIcon getCover()
	{
		return cover_image;
	}
	public String getTime(int i)
	{
		return time[i]/60+":"+time[i]%60;
	}
	public String getSignature()
	{
		return bytes2hex(signature);
	}

	private static String bytes2string(byte[] ch)
	{
		try{
			return new String(ch,"GB18030");
		}catch(Exception e){die(e);}
		return null;
	}
	private static final String HEX_CHARS = "0123456789ABCDEF";
	private static String bytes2hex( byte [] raw )
	{
		StringBuilder hex = new StringBuilder(2*raw.length);
		for(byte b : raw)
		{
			hex.append(HEX_CHARS.charAt((b & 0xF0) >> 4))
				.append(HEX_CHARS.charAt((b & 0x0F))).append(' ');
		}
		return hex.toString();
	}
	static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		final java.io.PrintWriter pw = new java.io.PrintWriter(r);
		e.printStackTrace(pw);
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}