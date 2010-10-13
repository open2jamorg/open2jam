
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import org.open2jam.parser.*;

public class ImageRender
{
	private static int resolution_height = 600;
	private static double viewport = 0.8 * resolution_height;

	private static int note_width = 28;
	private static int note_height = 7;
	private static int width = 7 * note_width;


	private static Color colors [] = new Color[6];

	static {
		colors[0] = new Color(241,241,243); // white
		colors[1] = new Color( 68,212,246); // cyan
		colors[2] = new Color(254,204, 83); // yellow
		colors[3] = new Color( 50, 50, 50); // dark gray
		colors[4] = new Color(120,120,120); // gray
		colors[5] = new Color(255,150,150); // pink
	}

	public static BufferedImage renderChart(Chart chart, int speed, int sub_measures)
	{
		double measure_size = 0.8 * speed * viewport;
		double height = chart.getMeasureCount() * measure_size;
		BufferedImage bi = new BufferedImage(width+100,(int) Math.ceil(height), BufferedImage.TYPE_INT_RGB);

		Graphics2D g = bi.createGraphics();

		int x = 0;
		g.setColor(colors[3]);
		for(int i=1;i<7;i++)
		{
			x += note_width;
			g.drawLine(x, 0, x, (int)Math.round(height));
		}
		g.setColor(colors[4]);
		g.drawLine(0, 0, 0, (int)Math.round(height));
		g.drawLine(x+note_width, 0, x + note_width, (int)Math.round(height));


		// measure and sub_measure marker lines
		x = 7 * note_width;
		double y;
		for(int measure=0;measure<=chart.getMeasureCount();measure++)
		{
			y = height - (measure * measure_size);
			g.drawLine(0, (int)Math.round(y), x, (int)Math.round(y));
			g.drawString(String.format("#%03d",measure),x+4,(int)Math.round(y));

			if(sub_measures>1){
				g.setColor(colors[3]);
				for(double sub_measure=1;sub_measure<sub_measures;sub_measure++)
				{
					y = height - (measure+(sub_measure/sub_measures)) * measure_size;
					g.drawLine(0, (int)Math.round(y), x, (int)Math.round(y));
				}
				g.setColor(colors[4]);
			}
		}

		double bpm = 100;
		int c;
		for(Event e : chart.getEvents())
		{
			if(e instanceof BPMEvent)
			{
				BPMEvent be = (BPMEvent) e;
				double newbpm = be.getBPM();
				x = (7 * note_width) + 4;
				y = height - (measure_size * be.getMeasure());

				g.setColor(colors[5]);
				g.drawString(String.format("BPM %.3f",newbpm),x, (int)Math.round(y-100));
				bpm = newbpm;
			}else{
				c = 0;
				if(e.getChannel() % 2 == 0)c = 0;  // white notes
				if(e.getChannel() % 2 != 0)c = 1 ; // blue notes
				if(e.getChannel() == 3)c = 2;      // yellow note

				x = e.getChannel() * note_width;
				y = height - (measure_size * e.getMeasure()) - 1;
				g.setColor(colors[c]);
				if(e instanceof LongNoteEvent)
				{
					LongNoteEvent lne = (LongNoteEvent) e;
					double y_end = height - (measure_size * lne.getEndMeasure()) - 1;
					g.fillRect(x, (int)Math.round(y), note_width, (int)Math.round(y-y_end));
				}else{
					g.fillRect(x, (int)Math.round(y - note_height), note_width, note_height); 
				}
			}
		}
		g.dispose();
		return bi;
	}
}
