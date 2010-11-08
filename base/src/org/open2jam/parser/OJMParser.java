package org.open2jam.parser;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.open2jam.render.lwjgl.SoundManager;
import org.open2jam.Util;


public class OJMParser
{
	public static HashMap<Integer,Integer> parseFile(File file)
	{
		try{
			RandomAccessFile f = new RandomAccessFile(file,"r");
			byte[] sig = new byte[4];
			f.read(sig);
			String signature = new String(sig);
			if(signature.equals("M30\0")) {
				return parseM30(f);
			}
			else {
				f.close();
				throw new UnsupportedOperationException("["+signature+"] not supported");
			}
		}catch(Exception e) {
			Util.warn(e);
		}
		return new HashMap<Integer,Integer>();
	}

	private static HashMap<Integer,Integer> parseM30(RandomAccessFile f) throws Exception
	{
		ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 4, 28);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		// header
		byte[] unk_fixed = new byte[4];
		buffer.get(unk_fixed);
                byte nami_encoded = buffer.get();
                byte[] unk_fixed2 = new byte[3];
                buffer.get(unk_fixed2);
		short sample_count = buffer.getShort();
		byte[] unk_fixed3 = new byte[6];
		buffer.get(unk_fixed3);
		int payload_size = buffer.getInt();
		int unk_zero2 = buffer.getInt();

		buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 28, payload_size);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();


		for(int i=0; i<sample_count; i++)
		{
                        if(buffer.remaining() < 52){
                            Util.warn("Wrong number of samples on OJM header");
                            break;
                        }
			byte[] sample_name = new byte[32];
			buffer.get(sample_name);
			int sample_size = buffer.getInt();
			byte unk_sample_type = buffer.get();
			byte unk_off = buffer.get();
			short fixed_2 = buffer.getShort();
			int unk_sample_type2 = buffer.getInt();
			short ref = buffer.getShort();
			short unk_zero = buffer.getShort();
			byte[] unk_wut = new byte[3];
			buffer.get(unk_wut);
			byte unk_counter = buffer.get();

			byte[] sample_data = new byte[sample_size];
			buffer.get(sample_data);
			if(nami_encoded > 0)nami_xor(sample_data);

			int id = SoundManager.newBuffer(
				new OggInputStream(new java.io.ByteArrayInputStream(sample_data))
			);
                        int value = ref;
                        if(unk_sample_type == 0){
                                value = 1000 + ref;
                        }
                        else if(unk_sample_type != 5){
                            System.out.println("! WARNING ! unknown sample id type ["+unk_sample_type+"]");
                        }
			samples.put(value, id);
		}
		f.close();
		return samples;
	}

	private static byte[] nami = new byte[]{0x6E, 0x61, 0x6D, 0x69};
	private static void nami_xor(byte[] array)
	{
		for(int i=0;i+3<array.length;i+=4)
		{
			array[i+0] ^= nami[0];
			array[i+1] ^= nami[1];
			array[i+2] ^= nami[2];
			array[i+3] ^= nami[3];
		}
	}

}
