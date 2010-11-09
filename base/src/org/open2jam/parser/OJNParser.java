package org.open2jam.parser;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.open2jam.Util;

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

	public static OJNChart parseFile(File file)
	{
		OJNChart header = new OJNChart();
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
			readHeader(header, buffer, f, file.getParentFile());
                        buffer = null;
			f.close();
		}catch(IOException e){ Util.die(e); }
		return header;
	}

	private static void readHeader(OJNChart header, ByteBuffer buffer, RandomAccessFile f, File parent) throws BadFileException
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
		header.sample_file = new File(parent, bytes2string(ojm_file));

		//ojn specific fields
		header.note_offsets = note_offsets;
	}

	public static List<Event> parseChart(OJNChart header, int rank)
	{
		ArrayList<Event> event_list = new ArrayList<Event>();
		try{
			RandomAccessFile f = new RandomAccessFile(header.getSource().getAbsolutePath(), "r");

			int start = header.getNoteOffsets()[rank];
			int end = header.getNoteOffsets()[rank+1];

			ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, start, end - start);
			buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			readNoteBlock(event_list, buffer);
			f.close();
		}catch(Exception e){ Util.die(e); }
		return event_list;
	}

	private static void readNoteBlock(List<Event> event_list, ByteBuffer buffer) throws Exception
	{
            while(buffer.hasRemaining())
            {
                int measure = buffer.getInt();
                short channel_number = buffer.getShort();
                short events_count = buffer.getShort();

                Event.Channel channel;
                switch(channel_number)
                {
                    case 0:channel = Event.Channel.TIME_SIGNATURE;break;
                    case 1:channel = Event.Channel.BPM_CHANGE;break;
                    case 2:channel = Event.Channel.NOTE_1;break;
                    case 3:channel = Event.Channel.NOTE_2;break;
                    case 4:channel = Event.Channel.NOTE_3;break;
                    case 5:channel = Event.Channel.NOTE_4;break;
                    case 6:channel = Event.Channel.NOTE_5;break;
                    case 7:channel = Event.Channel.NOTE_6;break;
                    case 8:channel = Event.Channel.NOTE_7;break;
                    default:
                    channel = Event.Channel.AUTO_PLAY;
                }

                for(double i=0;i<events_count;i++)
                {
                    double position = i / events_count;
                    if(channel == Event.Channel.BPM_CHANGE || channel == Event.Channel.TIME_SIGNATURE) // fractional measure or BPM event
                    {
                        float v = buffer.getFloat();
                        if(v == 0)continue;

                        event_list.add(new Event(channel,measure,position,v,Event.Flag.NONE));
                    }else{ // note event
                        short value = buffer.getShort();
                        int unk = buffer.get();
                        int type = buffer.get();
                        if(value == 0)continue; // ignore value=0 events

                        value--;
                        if(type == 0){
                                event_list.add(new Event(channel,measure,position,value,Event.Flag.NONE));
                        }
                        else if(type == 2){
                            event_list.add(new Event(channel,measure,position,value,Event.Flag.HOLD));
                        }
                        else if(type == 3){
                            event_list.add(new Event(channel,measure,position,value,Event.Flag.RELEASE));
                        }
                        else if(type == 4){ // M### auto-play
                            event_list.add(new Event(channel,measure,position,1000+value,Event.Flag.RELEASE));
                        }
                    }
                }
            }
            Collections.sort(event_list);
	}

	private static String bytes2string(byte[] ch)
	{
		int i; for(i=0;i<ch.length&&ch[i]!=0;i++); // find \0 terminator
		try{
			return new String(ch,0,i);
		}catch(Exception e){Util.die(e);}
		return null;
	}
}