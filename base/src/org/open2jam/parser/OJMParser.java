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
import org.open2jam.Logger;


public class OJMParser
{
	/** the xor mask used in the M30 format */
	private static final byte[] nami = new byte[]{0x6E, 0x61, 0x6D, 0x69};


        /** the M30 signature, "M30\0" in little endian */
        private static final int M30_SIGNATURE = 0x0030334D;

        /** the OMC signature, "OMC\0" in little endian */
        private static final int OMC_SIGNATURE = 0x00434D4F;

        /** the OJM signature, "OJM\0" in little endian */
        private static final int OJM_SIGNATURE = 0x004D4A4F;

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
                            case M30_SIGNATURE:
                                ret = parseM30(f);
                                break;

                            case OMC_SIGNATURE:
                            case OJM_SIGNATURE:
                                ret = parseOMC(f);
                                break;

                            default:
                                ret = new HashMap<Integer,Integer>();
                        }
                        f.close();
		}catch(IOException e) {
			Logger.warn(e);
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
                            Logger.log("Wrong number of samples on OJM header");
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
       HashMap<Integer, Integer> samples =  new HashMap<Integer, Integer>();
       
       ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 4, 16);
       buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

       short unk1 = buffer.getShort();
       short unk2 = buffer.getShort();
       int wav_start = buffer.getInt();
       int ogg_start = buffer.getInt();
       int filesize = buffer.getInt();

       int file_offset = 20;
       int sample_id = 0; // wav samples use id 0~999

       while(file_offset < ogg_start) // WAV data
       {
           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, 56);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += 56;

           byte[] sample_name = new byte[32];
           buffer.get(sample_name);

           short audio_format = buffer.getShort();
           short num_channels = buffer.getShort();
           int sample_rate = buffer.getInt();
           int bit_rate = buffer.getInt();
           short block_align = buffer.getShort();
           short bits_per_sample = buffer.getShort();
           int data = buffer.getInt();
           int chunk_size = buffer.getInt();

           if(chunk_size == 0)continue;

           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, chunk_size);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += chunk_size;

           int buffer_id = SoundManager.newBuffer(buffer, bits_per_sample, num_channels, sample_rate);
           samples.put(sample_id, buffer_id);
           sample_id++;
       }
       sample_id = 1000; // ogg samples use id 1000~?
       while(file_offset < filesize) // OGG data
       {
           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, 36);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += 36;

           byte[] sample_name = new byte[32];
           buffer.get(sample_name);

           int sample_size = buffer.getInt();

           if(sample_size == 0)continue;

           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, sample_size);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += sample_size;

           int buffer_id = SoundManager.newBuffer(new OggInputStream(new ByteBufferInputStream(buffer)));
           samples.put(sample_id, buffer_id);
           sample_id++;
       }

       return samples;
    }

    public static void main(String[] args) throws InterruptedException{
        HashMap<Integer, Integer> samples = parseFile(new File(args[0]));

        int source = SoundManager.newSource();

        for(int sample_id : samples.keySet()){
            System.out.println("sample: "+sample_id);
            SoundManager.play(source, samples.get(sample_id));

            while(SoundManager.isPlaying(source))Thread.sleep(0);
        }
    }
}
