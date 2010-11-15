package org.open2jam.parser;

public class Event implements Comparable<Event>
{
 	public enum Channel {
            NONE,
            TIME_SIGNATURE,
            BPM_CHANGE,
            NOTE_1,
            NOTE_2,
            NOTE_3,
            NOTE_4,
            NOTE_5,
            NOTE_6,
            NOTE_7,
            AUTO_PLAY;

            @Override
            public String toString(){
                switch(this){
	            case NOTE_1:return "NOTE_1";
	            case NOTE_2:return "NOTE_2";
	            case NOTE_3:return "NOTE_3";
	            case NOTE_4:return "NOTE_4";
	            case NOTE_5:return "NOTE_5";
	            case NOTE_6:return "NOTE_6";
	            case NOTE_7:return "NOTE_7";
	            case TIME_SIGNATURE:return "TIME_SIGNATURE";
	            case BPM_CHANGE:return "BPM_CHANGE";
	        }
	        return super.toString();
	    }
        };

        public static Channel[] note_channels = new Event.Channel[]{
                Channel.NOTE_1,
                Channel.NOTE_2,
                Channel.NOTE_3,
                Channel.NOTE_4,
                Channel.NOTE_5,
                Channel.NOTE_6,
                Channel.NOTE_7
        };

	public enum Flag { NONE, HOLD, RELEASE };

	private Channel channel;
	private int measure;
	private double position;
	
	private double value;
	private Flag flag;

	public Event(Channel channel, int measure, double position,
			double value, Flag flag) {
		this.channel = channel;
		this.measure = measure;
		this.position = position;
		this.value = value;
		this.flag = flag;
	}

	public int compareTo(Event e)
	{
		return (int) ((measure+position) - (e.getMeasure()+e.getPosition()));
	}

	public Channel getChannel() { return channel; }
	public int getMeasure() { return measure; }
	public double getPosition() { return position; }
	public Flag getFlag() { return flag; }
	public double getValue() { return value; }
}
