package org.open2jam.parser;

/* little endian wrapper uppon RandomAccessFile */
class LEFile extends java.io.RandomAccessFile
{
	public LEFile(java.io.File file, String mode) throws Exception
	{ super(file, mode); }
	public LEFile(String name, String mode) throws Exception
	{ super(name, mode); }
	public boolean readBOOL()
	{
		int bool = readBYTE();
		return bool!=0;
	}
	public int[] readINT(int n)
	{
		int nbuf[] = new int[n];
		for(int i=0;i<n;i++)nbuf[i] = readINT();
		return nbuf;
	}
	public int readINT()
	{
		int value = 0;
		try{
			for(int shiftBy=0;shiftBy<32;shiftBy+=8)
			{
				value |= (read() & 0xFF) << shiftBy;
			}
		}catch(Exception e){die(e);}
		return value;
	}
	public float readFLOAT()
	{
		int accum = 0;
		try{
		for(int shiftBy=0;shiftBy<32;shiftBy+=8)
		{
			accum |= (read() & 0xFF) << shiftBy;
		}
		}catch(Exception e){die(e);}
		return Float.intBitsToFloat(accum);
	}

	public byte[] readBYTE(int n)
	{
		byte nbuf[] = new byte[n];
		for(int i=0;i<n;i++)nbuf[i] = readBYTE();
		return nbuf;
	}
	public byte readBYTE()
	{
		int c = 0;
		try{
			c = read();
		}catch(Exception e){die(e);}
		return (byte)c;
	}
	public short[] readSHORT(int n)
	{
		short nbuf[] = new short[n];
		for(int i=0;i<n;i++)nbuf[i] = readSHORT();
		return nbuf;
	}
	public short readSHORT()
	{
		int low = 0, high = 0;
		try{
			low = read() & 0xFF;
			high = read() & 0xFF;
		}catch(Exception e){die(e);}
		return(short)( high << 8 | low );
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