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
	    NOTE_SC,
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
		    case NOTE_SC:return "NOTE_SC";
	            case TIME_SIGNATURE:return "TIME_SIGNATURE";
	            case BPM_CHANGE:return "BPM_CHANGE";
	        }
	        return super.toString();
	    }
        };

        /**
         * wrapper for the sound sample
         *
         * contains the sample id for the SoundManager
         * volume and pan */
        public class SoundSample
        {
            public int sample_id;
            public float volume;
            public float pan;

            public SoundSample(int sample, float vol, float pan){
                this.sample_id = sample;
                this.volume = vol;
                this.pan = pan;
            }
        }

        
	public enum Flag { NONE, HOLD, RELEASE };

	private Channel channel;
	private int measure;
	private double position;
	
	private double value;
	protected Flag flag;
        private final SoundSample sound_sample;

	public Event(Channel channel, int measure, double position,
			double value, Flag flag) {
            this.channel = channel;
            this.measure = measure;
            this.position = position;
            this.value = value;
            this.flag = flag;
            this.sound_sample = new SoundSample((int)value, 1, 0);
	}

        public Event(Channel channel, int measure, double position,
                    double value, Flag flag, float vol, float pan) {
            this.channel = channel;
            this.measure = measure;
            this.position = position;
            this.value = value;
            this.flag = flag;
            this.sound_sample = new SoundSample((int) value,vol, pan);
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
        public SoundSample getSample(){ return sound_sample; }
}
