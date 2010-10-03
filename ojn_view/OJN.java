import java.io.RandomAccessFile;
import javax.swing.ImageIcon;
import java.util.Vector;
import javax.swing.*;

public class OJN
{
	private RandomAccessFile f = null;

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
	byte unk_zero2[];
	boolean unk_bool;
	byte unk_k[];
	byte unk_zero3[];
	byte title[];
	byte artist[];
	byte noter[];
	byte ojm_file[];
	int cover_size;
	int time[];
	int note_offset[];

	Vector<RawNotes> note_sec;

	ImageIcon cover_image;
	ImageIcon mini_cover;

	String genre_map[] = {"Ballad","Rock","Dance","Techno","Hip-hop",
			"Soul/R&B","Jazz","Funk","Classical","Traditional","Etc"};

	public OJN(String filename)
	{
		try{
			f = new RandomAccessFile(filename,"r");
			readHeader();
			readNotes();
			readCover();
			f.close();
		}catch(Exception e){die(e);}
	}
	private void readHeader()
	{
		songid = readINT();
		signature = readBYTE(8);
		genre = readINT();
		bpm = readFLOAT();
		level = readSHORT(3);
		unk_num = readINT(3);
		unk_zero = readBYTE(2);
		note_count = readINT(3);
		unk_time = readINT(3);
		package_count = readINT(3);
		unk_id = readSHORT(2);
		unk_oldgenre = readBYTE(5);
		unk_zero2 = readBYTE(14);
		unk_bool = readBOOL();
		unk_k = readBYTE(2);
		unk_zero3 = readBYTE(6);
		title = readBYTE(64);
		artist = readBYTE(32);
		noter = readBYTE(32);
		ojm_file = readBYTE(32);
		cover_size = readINT();
		time = readINT(3);
		note_offset = readINT(4);
	}
	private void readNotes() throws Exception
	{
		note_sec = new Vector<RawNotes>(3);
		readNoteBlock(0);
		readNoteBlock(1);
		readNoteBlock(2);
	}
	private void readNoteBlock(int lvl) throws Exception
	{
		f.seek(note_offset[lvl]);
		int endpos = note_offset[lvl+1];
		note_sec.add(lvl,new RawNotes(lvl));
		int maxbeat = 0;
		while(f.getFilePointer() < endpos)
		{
			int measure = readINT();
			int channel = readSHORT();
			int length = readSHORT();
			
			if(channel >= 1 && channel <= 8)
			{
				for(int i=0;i<length;i++)
				{
					int beat = (4 * (measure + (i / length)));
					if(beat > maxbeat)maxbeat = beat;
					if(channel == 1)
					{
						float bpm = readFLOAT();
						if(bpm == 0)continue;
						note_sec.get(lvl).add(new Note(channel,beat,bpm));
					}else{
						short value = readSHORT();
						char f1 = (char)readBYTE();
						char f2 = (char)readBYTE();
						if(value == 0)continue;
						note_sec.get(lvl).add(new Note(channel,beat,value,f1,f2));
					}
				}
			}else{
				f.skipBytes(4*length); // skipping what ??
			}
		}
		note_sec.get(lvl).setMaxBeat(maxbeat);
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
	public ImageIcon getChart(int lvl)
	{
		return new ImageIcon(new Chart(note_sec.get(lvl)).getImage());
	}
	private boolean readBOOL()
	{
		int bool = readBYTE();
		return bool!=0;
	}
	private int[] readINT(int n)
	{
		int nbuf[] = new int[n];
		for(int i=0;i<n;i++)nbuf[i] = readINT();
		return nbuf;
	}
	private int readINT()
	{
		int value = 0;
		try{
			for(int shiftBy=0;shiftBy<32;shiftBy+=8)
			{
				value |= (f.read() & 0xFF) << shiftBy;
			}
		}catch(Exception e){die(e);}
		return value;
	}
	private float readFLOAT()
	{
		int accum = 0;
		try{
		for(int shiftBy=0;shiftBy<32;shiftBy+=8)
		{
			accum |= (f.read() & 0xFF) << shiftBy;
		}
		}catch(Exception e){die(e);}
		return Float.intBitsToFloat(accum);
	}

	private byte[] readBYTE(int n)
	{
		byte nbuf[] = new byte[n];
		for(int i=0;i<n;i++)nbuf[i] = readBYTE();
		return nbuf;
	}
	private byte readBYTE()
	{
		int c = 0;
		try{
			c = f.read();
		}catch(Exception e){die(e);}
		return (byte)c;
	}
	private short[] readSHORT(int n)
	{
		short nbuf[] = new short[n];
		for(int i=0;i<n;i++)nbuf[i] = readSHORT();
		return nbuf;
	}
	private short readSHORT()
	{
		int low = 0, high = 0;
		try{
			low = f.read() & 0xFF;
			high = f.read() & 0xFF;
		}catch(Exception e){die(e);}
		return(short)( high << 8 | low );
	}
	private String bytes2string(byte[] ch)
	{
		try{
			return new String(ch,"GB18030");
		}catch(Exception e){die(e);}
		return null;
	}
	private static final String HEX_CHARS = "0123456789ABCDEF";
	private String bytes2hex( byte [] raw )
	{
		StringBuilder hex = new StringBuilder(2*raw.length);
		for(byte b : raw)
		{
			hex.append(HEX_CHARS.charAt((b & 0xF0) >> 4))
				.append(HEX_CHARS.charAt((b & 0x0F))).append(' ');
		}
		return hex.toString();
	}
	void die(Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}

	public static void main(String[]args)
	{
		JDialog jd = new JDialog();
		JLabel chart = new JLabel();
		chart.setIcon(new OJN("../o2p/o2ma394.ojn").getChart(0));
		jd.getContentPane().add(new JScrollPane(chart));
		jd.setSize(600,400);
		jd.setVisible(true);
	}
}