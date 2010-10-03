import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.HashMap;

public class Chart
{
	private BufferedImage img;
	private Graphics2D g;

	private int width, height;
	private static final Font mono = new Font(Font.MONOSPACED,Font.PLAIN,14);
	public static final int NOTEPAD = 100; // left side padding
	public static final int NOTEHEIGHT = 7; // vertical size of a note
	public static final int NOTEWIDTH = 20; // horizontal size of a note
	public static final int CSIZE = 260; // comments space size
	public static final int BEATSIZE = 48; // beat spacing, speed related ???

	private static final HashMap<Integer,Color> colors;
	static
	{
		colors = new HashMap<Integer,Color>();
		colors.put( 0,new Color(255,255,255));  // white
		colors.put( 1,new Color(241,241,243));  // white
		colors.put( 2,new Color( 68,212,246));  // cyan
		colors.put( 3,new Color(254,204, 83));  // yellow
		colors.put( 4,new Color( 50, 50, 50));  // dark gray
		colors.put( 5,new Color(120,120,120));  // gray
		colors.put( 6,new Color(255,150,150));  // pink
		colors.put( 7,new Color(180,180,180));  // light gray
		colors.put(11,new Color(193,193,194));  // light gray
		colors.put(12,new Color( 54,170,197));  // light blue
		colors.put(13,new Color(203,163, 66));  // light brown
		colors.put(21,new Color(248,243,247));  // white
		colors.put(22,new Color(130,231,241));  // light cyan
		colors.put(23,new Color(255,246,157));  // light yellow
		colors.put(31,new Color(198,194,198));  // light gray
		colors.put(32,new Color(104,185,193));  // cyan
		colors.put(33,new Color(204,197,126));  // light yellow
	}

	public Chart(RawNotes v)
	{
		makeChart(v);
	}
	
	public BufferedImage getImage()
	{
		return img;
	}
	private void makeChart(RawNotes v)
	{
		width = (NOTEWIDTH * 7) + 2 * NOTEPAD;
		height = (v.getMaxBeat() * BEATSIZE) +  CSIZE;
		img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		g = img.createGraphics();
		g.setFont(mono);

		// vertical lines, delimiter the note space
		for(int i=0;i<8;i++)
		{
			int x = NOTEPAD + (i * NOTEWIDTH);
			line(x, 0, x, height, colors.get(4));
		}
		line(NOTEPAD, 0, NOTEPAD, height, colors.get(5)); 
		line(NOTEPAD+(7*NOTEWIDTH),0,NOTEPAD+(7*NOTEWIDTH),height, colors.get(5));

		// horizontal lines
		for(int i=0;i<=v.getMaxBeat();i++)
		{
			int x2 = NOTEPAD + (7 * NOTEWIDTH);
			int y  = height - (BEATSIZE * i) + NOTEHEIGHT;
			line(NOTEPAD, y, x2, y, colors.get(4));
			if (i % 4 == 0)
			{
				line(NOTEPAD,y-1,x2,y-1,colors.get(5));
				string("#"+(i/4),NOTEPAD+4+(7*NOTEWIDTH),y-10,colors.get(5));
			}
		}

		boolean longch[] = new boolean[7];
		int chbeat[] = new int[7];

		for(int i=0;i<v.size();i++)
		{
			int channel = v.get(i).channel - 2; // mapping 1~8 --> -1~6
			if(channel == -1) // bpm changing
			{
				int y = height - (BEATSIZE * v.get(i).beat) + NOTEHEIGHT;
				String txt = v.get(i).bpm+"";
				string(txt,NOTEPAD-(txt.length()*9)-4,y-10,colors.get(6));
			}else{
				int c = 1;
				if(channel % 2 == 1)c = 2;        
				if(channel % 10 == 3)c = 3; 
				if(!v.get(i).longnote) // tap note
				{
					int x = NOTEHEIGHT + (channel * NOTEWIDTH);
					int y = height - (BEATSIZE * v.get(i).beat);
					rect(x,y,NOTEWIDTH-1,NOTEHEIGHT-1,colors.get(c));
					line(x,y+NOTEHEIGHT-1,x+NOTEWIDTH-1,y+NOTEHEIGHT-1,colors.get(10+c));
					line(x,y,x+NOTEWIDTH-1,y,colors.get(10+c));
				}else{ // long note
					if(longch[channel])
					{
						longch[channel] = false;
						int x = NOTEPAD + ((channel - 10) * NOTEWIDTH);
						int y = height - (BEATSIZE * v.get(i).beat);
						int z = height - (BEATSIZE * chbeat[channel]);
						rect(x,y,NOTEWIDTH-1,(z-y)+NOTEHEIGHT-1,colors.get(20+c));
						rect(x+1,y,(NOTEWIDTH*0.15)-1,(z-y)+NOTEHEIGHT-1,colors.get(0));

						rect(x+(NOTEWIDTH*0.85)-1,y,(NOTEWIDTH*0.15)-2,
							(z-y)+NOTEHEIGHT-1,colors.get(30+c));

						line(x,z+NOTEHEIGHT-1,x+NOTEWIDTH-1,z+NOTEHEIGHT-1,colors.get(30+c));
						line(x,y,x+NOTEWIDTH-1,y,colors.get(30+c));
					}else{
						longch[channel] = true;
						chbeat[channel] = v.get(i).beat;
					}
				}
			}
		}
	}

	private void line(int x1, int y1, int x2, int y2, Color color)
	{
		Color oldc = g.getColor();
		g.setColor(color);
		g.drawLine(x1,y1,x2,y2);
		g.setColor(oldc);
	}
	private void rect(double x, double y,double wid,double hei, Color color)
	{
		Color oldc = g.getColor();
		g.setColor(color);
		g.fillRect((int)Math.round(x),(int)Math.round(y),(int)Math.round(wid),(int)Math.round(hei));
		g.setColor(oldc);
	}
	private void string(String s, int x, int y, Color color)
	{
		Color oldc = g.getColor();
		g.setColor(color);
		g.drawString(s,x,y);
		g.setColor(oldc);
	}
}