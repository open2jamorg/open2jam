package org.open2jam.parser;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.open2jam.ByteBufferInputStream;

import org.open2jam.render.lwjgl.SoundManager;
import org.open2jam.Util;


public class OJMParser
{
	/** the xor mask used in the M30 format */
	private static final byte[] nami = new byte[]{0x6E, 0x61, 0x6D, 0x69};


        /** the M30 signature, "M30\0" in little endian */
        private static final int M30_SIGNATURE = 0x0030334D;

        /** the M30 signature, "OMC\0" in little endian */
        private static final int OMC_SIGNATURE = 0x00434D4F;

	public static HashMap<Integer,Integer> parseFile(File file)
	{
                RandomAccessFile f = null;
                HashMap<Integer,Integer> ret = null;
		try{
			f = new RandomAccessFile(file,"r");
                        ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, 4);
                        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			int signature = buffer.getInt();

                        switch(signature)
                        {
                            case M30_SIGNATURE:ret = parseM30(f);break;

                            case OMC_SIGNATURE:ret = parseOMC(f);break;

                            default:ret = new HashMap<Integer,Integer>();
                        }
                        f.close();
		}catch(IOException e) {
			Util.warn(e);
		}
		return ret;
	}

	private static HashMap<Integer,Integer> parseM30(RandomAccessFile f) throws IOException
	{
		ByteBuffer buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 4, 28);
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

		buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 28, payload_size);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();


		for(int i=0; i<sample_count; i++)
		{
			// reached the end of the file before the samples_count
                        if(buffer.remaining() < 52){
                            Util.log("Wrong number of samples on OJM header");
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
				new OggInputStream(new ByteArrayInputStream(sample_data))
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

    private static HashMap<Integer, Integer> parseOMC(RandomAccessFile f) throws IOException
    {
        ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 52, 4);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        int sample_size = buffer.getInt();

        buffer = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 56, sample_size);

        int id = SoundManager.newBuffer(
                new OggInputStream(new ByteBufferInputStream(buffer))
        );
        HashMap<Integer,Integer> samples = new HashMap<Integer,Integer>();
        samples.put(1000, id);
        return samples;
    }
}
