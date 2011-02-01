
import java.util.logging.Level;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;


public class OJMDumper
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** the xor mask used in the M30 format */
    private static final byte[] nami = new byte[]{0x6E, 0x61, 0x6D, 0x69};


    /** the M30 signature, "M30\0" in little endian */
    private static final int M30_SIGNATURE = 0x0030334D;

    /** the OMC signature, "OMC\0" in little endian */
    private static final int OMC_SIGNATURE = 0x00434D4F;

    /** the OJM signature, "OJM\0" in little endian */
    private static final int OJM_SIGNATURE = 0x004D4A4F;

    /* this is a dump from debugging notetool */
    private static final byte[] REARRANGE_TABLE = new byte[]{
    0x10, 0x0E, 0x02, 0x09, 0x04, 0x00, 0x07, 0x01,
    0x06, 0x08, 0x0F, 0x0A, 0x05, 0x0C, 0x03, 0x0D,
    0x0B, 0x07, 0x02, 0x0A, 0x0B, 0x03, 0x05, 0x0D,
    0x08, 0x04, 0x00, 0x0C, 0x06, 0x0F, 0x0E, 0x10,
    0x01, 0x09, 0x0C, 0x0D, 0x03, 0x00, 0x06, 0x09,
    0x0A, 0x01, 0x07, 0x08, 0x10, 0x02, 0x0B, 0x0E,
    0x04, 0x0F, 0x05, 0x08, 0x03, 0x04, 0x0D, 0x06,
    0x05, 0x0B, 0x10, 0x02, 0x0C, 0x07, 0x09, 0x0A,
    0x0F, 0x0E, 0x00, 0x01, 0x0F, 0x02, 0x0C, 0x0D,
    0x00, 0x04, 0x01, 0x05, 0x07, 0x03, 0x09, 0x10,
    0x06, 0x0B, 0x0A, 0x08, 0x0E, 0x00, 0x04, 0x0B,
    0x10, 0x0F, 0x0D, 0x0C, 0x06, 0x05, 0x07, 0x01,
    0x02, 0x03, 0x08, 0x09, 0x0A, 0x0E, 0x03, 0x10,
    0x08, 0x07, 0x06, 0x09, 0x0E, 0x0D, 0x00, 0x0A,
    0x0B, 0x04, 0x05, 0x0C, 0x02, 0x01, 0x0F, 0x04,
    0x0E, 0x10, 0x0F, 0x05, 0x08, 0x07, 0x0B, 0x00,
    0x01, 0x06, 0x02, 0x0C, 0x09, 0x03, 0x0A, 0x0D,
    0x06, 0x0D, 0x0E, 0x07, 0x10, 0x0A, 0x0B, 0x00,
    0x01, 0x0C, 0x0F, 0x02, 0x03, 0x08, 0x09, 0x04,
    0x05, 0x0A, 0x0C, 0x00, 0x08, 0x09, 0x0D, 0x03,
    0x04, 0x05, 0x10, 0x0E, 0x0F, 0x01, 0x02, 0x0B,
    0x06, 0x07, 0x05, 0x06, 0x0C, 0x04, 0x0D, 0x0F,
    0x07, 0x0E, 0x08, 0x01, 0x09, 0x02, 0x10, 0x0A,
    0x0B, 0x00, 0x03, 0x0B, 0x0F, 0x04, 0x0E, 0x03,
    0x01, 0x00, 0x02, 0x0D, 0x0C, 0x06, 0x07, 0x05,
    0x10, 0x09, 0x08, 0x0A, 0x03, 0x02, 0x01, 0x00,
    0x04, 0x0C, 0x0D, 0x0B, 0x10, 0x05, 0x06, 0x0F,
    0x0E, 0x07, 0x09, 0x0A, 0x08, 0x09, 0x0A, 0x00,
    0x07, 0x08, 0x06, 0x10, 0x03, 0x04, 0x01, 0x02,
    0x05, 0x0B, 0x0E, 0x0F, 0x0D, 0x0C, 0x0A, 0x06,
    0x09, 0x0C, 0x0B, 0x10, 0x07, 0x08, 0x00, 0x0F,
    0x03, 0x01, 0x02, 0x05, 0x0D, 0x0E, 0x04, 0x0D,
    0x00, 0x01, 0x0E, 0x02, 0x03, 0x08, 0x0B, 0x07,
    0x0C, 0x09, 0x05, 0x0A, 0x0F, 0x04, 0x06, 0x10,
    0x01, 0x0E, 0x02, 0x03, 0x0D, 0x0B, 0x07, 0x00,
    0x08, 0x0C, 0x09, 0x06, 0x0F, 0x10, 0x05, 0x0A,
    0x04, 0x00};

    public static String getType(File file) throws java.io.FileNotFoundException, java.io.IOException
    {
        RandomAccessFile f = null;
        f = new RandomAccessFile(file,"r");
        ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, 4);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        byte[] signature = new byte[3];
        buffer.get(signature);
        return new String(signature);
    }

    public static void dumpFile(File file, File output_dir)
    {
        RandomAccessFile f = null;
        try{
            f = new RandomAccessFile(file,"r");
            ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, 4);
            buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
            int signature = buffer.getInt();

            switch(signature)
            {
                case M30_SIGNATURE:
                    parseM30(f, file, output_dir);
                    break;

                case OMC_SIGNATURE:
                case OJM_SIGNATURE:
                    parseOMC(f, output_dir);
                    break;
            }
            f.close();
        }catch(IOException e) {
            logger.log(Level.WARNING, "IO expeption on file {0} : {1}", new Object[]{file.getName(), e.getMessage()});
        }
    }

    private static void parseM30(RandomAccessFile f, File file, File out_dir) throws IOException
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

        for(int i=0; i<sample_count; i++)
        {
            // reached the end of the file before the samples_count
            if(buffer.remaining() < 52){
                logger.log(Level.INFO, "Wrong number of samples on OJM header : {0}", file.getName());
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

            int value = ref;
            if(unk_sample_type == 0){
                value = 1000 + ref;
            } else if(unk_sample_type != 5){
               logger.log(Level.WARNING, "Unknown sample id type [{0}] on OJM : {1}", new Object[]{unk_sample_type, file.getName()});
            }

            String filename = new String(sample_name).replaceAll("\\W", "").concat("_ref("+value+").ogg");
            FileOutputStream file_out = new FileOutputStream(new File(out_dir, filename));
            file_out.write(sample_data);
            file_out.close();
        }
        f.close();

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

    private static void parseOMC(RandomAccessFile f, File out_dir) throws IOException
    {
       ByteBuffer buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 4, 16);
       buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

       short unk1 = buffer.getShort();
       short unk2 = buffer.getShort();
       int wav_start = buffer.getInt();
       int ogg_start = buffer.getInt();
       int filesize = buffer.getInt();

       int file_offset = 20;
       int sample_id = 0; // wav samples use id 0~999

       // reset global variables
       acc_keybyte = 0xFF;
       acc_counter = 0;

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

           if(chunk_size == 0){ sample_id++; continue; }

           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, chunk_size);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += chunk_size;

           byte[] buf = new byte[buffer.remaining()];
           buffer.get(buf);

           buf = rearrange(buf);
           buf = acc_xor(buf);

           ByteBuffer out_buffer = ByteBuffer.allocate(chunk_size+44);
           out_buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           out_buffer.put("RIFF".getBytes());
           out_buffer.putInt(chunk_size+36);
           out_buffer.put("WAVE".getBytes());
           out_buffer.put("fmt ".getBytes());
           out_buffer.putInt(0x10); // PCM format
           out_buffer.putShort(audio_format);
           out_buffer.putShort(num_channels);
           out_buffer.putInt(sample_rate);
           out_buffer.putInt(bit_rate);
           out_buffer.putShort(block_align);
           out_buffer.putShort(bits_per_sample);
           out_buffer.put("data".getBytes());
           out_buffer.putInt(chunk_size);
           out_buffer.put(buf);

           String filename = new String(sample_name).replaceAll("\\W", "").concat("_ref("+sample_id+").wav");
           FileOutputStream file_out = new FileOutputStream(new File(out_dir, filename));
           file_out.write(out_buffer.array());
           file_out.close();

           sample_id++;
       }
       sample_id = 1000; // ogg samples use id 1000~?
       byte[] tmp_buffer = new byte[1024];
       while(file_offset < filesize) // OGG data
       {
           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, 36);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += 36;

           byte[] sample_name = new byte[32];
           buffer.get(sample_name);

           int sample_size = buffer.getInt();

           if(sample_size == 0){ sample_id++; continue; }

           buffer = f.getChannel().map(java.nio.channels.FileChannel.MapMode.READ_ONLY, file_offset, sample_size);
           buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
           file_offset += sample_size;

           String filename = new String(sample_name).replaceAll("\\W", "").concat("_ref("+sample_id+").ogg");
            FileOutputStream file_out = new FileOutputStream(new File(out_dir, filename));
            ByteBufferInputStream in = new ByteBufferInputStream(buffer);
            while(true) {
                    int r = in.read(tmp_buffer);
                    if (r == -1) break;
                    file_out.write(tmp_buffer,0,r);
            }
            file_out.close();
           sample_id++;
       }
    }

    /**
     * fuck the person who invented this, FUCK YOU!... but with love =$
     */
    private static byte[] rearrange(byte[] buf_encoded)
    {
        int length = buf_encoded.length;
        int key = ((length % 17) << 4) + (length % 17);

        int block_size = length / 17;

        // Let's fill the buffer
        byte[] buf_plain = new byte[length];
        System.arraycopy(buf_encoded, 0, buf_plain, 0, length);

        for(int block=0;block<17;block++) // loopy loop
        {
            int block_start_encoded = block_size * block;	// Where is the start of the enconded block
            int block_start_plain = block_size * REARRANGE_TABLE[key];	// Where the final plain block will be
            System.arraycopy(buf_encoded, block_start_encoded, buf_plain, block_start_plain, block_size);

            key++;
        }
        return buf_plain;
    }

    /** some weird encryption */
    private static int acc_keybyte = 0xFF;
    private static int acc_counter = 0;
    private static byte[] acc_xor(byte[] buf)
    {
        int temp = 0;
        byte this_byte = 0;
        for(int i=0;i<buf.length;i++)
        {
            temp = this_byte = buf[i];

            if(((acc_keybyte << acc_counter) & 0x80)!=0){
                this_byte = (byte) ~this_byte;
            }

            buf[i] = this_byte;
            acc_counter++;
            if(acc_counter > 7){
                acc_counter = 0;
                acc_keybyte = temp;
            }
        }
        return buf;
    }

}
