package org.open2jam.parser;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.open2jam.render.lwjgl.SampleLoader;

import org.lwjgl.openal.AL10;

public class OJMParser
{
	public static HashMap<Integer,Integer> parseFile(String file)
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
				throw new RuntimeException("["+signature+"] not supported");
			}
		}catch(Exception e) {
			die(e);
		}
		return null;
	}

	private static HashMap<Integer,Integer> parseM30(RandomAccessFile f) throws Exception
	{
		ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 4, 28);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		// header
		byte[] unk_fixed = new byte[8];
		buffer.get(unk_fixed);
		short sample_count = buffer.getShort();
		byte[] unk_fixed2 = new byte[6];
		buffer.get(unk_fixed2);
		int payload_size = buffer.getInt();
		int unk_zero2 = buffer.getInt();

		buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 28, payload_size);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();


		for(int i=0; i<sample_count; i++)
		{
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
			nami_xor(sample_data);

			int id = SampleLoader.newBuffer(
				new OggInputStream(new java.io.ByteArrayInputStream(sample_data))
			);
			samples.put(ref * (unk_sample_type+1), id);
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

	private static void die(Exception e)
	{
		final java.io.Writer r = new java.io.StringWriter();
		final java.io.PrintWriter pw = new java.io.PrintWriter(r);
		e.printStackTrace(pw);
		javax.swing.JOptionPane.showMessageDialog(null, r.toString(), "Fatal Error", 
			javax.swing.JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	public static void main(String args[]) throws Exception
	{
		HashMap<Integer,Integer> samples = parseFile(args[0]);

		int source = SampleLoader.newSource();

		int play;
		for(Integer s : samples.values()){
			SampleLoader.bindSource(source, s);
			AL10.alSourcePlay(source);

			do{
				play = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
			}while(play == AL10.AL_PLAYING);
		}
	}
}
