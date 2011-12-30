package org.open2jam.parsers;

public class Event implements Comparable<Event>
{
 	public enum Channel {
            NOTE_1,                  //P1-NOTE_1
            NOTE_2,                  //P1-NOTE_2
            NOTE_3,                  //P1-NOTE_3
            NOTE_4,                  //P1-NOTE_4
            NOTE_5,                  //P1-NOTE_5
            NOTE_6,                  //P1-NOTE_6
            NOTE_7,                  //P1-NOTE_7
            NOTE_SC,                 //P1-NOTE_SC / P1-NOTE_8
            NOTE_8,                  //P2-NOTE_1
            NOTE_9,                  //P2-NOTE_2
            NOTE_10,                 //P2-NOTE_3
            NOTE_11,                 //P2-NOTE_4
            NOTE_12,                 //P2-NOTE_5
            NOTE_13,                 //P2-NOTE_6
            NOTE_14,                 //P2-NOTE_7
            NOTE_SC2,                //P2-NOTE_SC / P2-NOTE_8
            
	    NONE,		     //
            TIME_SIGNATURE,	     //
            BPM_CHANGE,		     //
	    STOP,
            MEASURE,                 //This will be used when the velocity tree is constructed;
	    
	    AUTO_PLAY(true);	     //Autoplay, used by the background music/sounds
	    
	    private boolean autoplay = false;
	    
	    private Channel() { }
	    
	    private Channel(boolean auto) {
		this.autoplay = auto;
	    }
	    
	    public boolean isAutoplay() {
		return autoplay;
	    }
	    
	    public void enableAutoplay() {
		this.autoplay = true;
	    }
	    
	    public void disableAutoplay() {
		this.autoplay = false;
	    }

        }

        /**
         * wrapper for the sound sample
         *
         * contains the sample id for the SoundManager
         * volume and pan */
        public class SoundSample
        {
            public final int sample_id;
            public final float volume;
            public final float pan;

            boolean bgm = false;

            public SoundSample(int sample, float vol, float pan){
                this.sample_id = sample;
                this.volume = vol;
                this.pan = pan;
            }

            public void toBGM() { this.bgm = true; }

            public boolean isBGM() { return bgm; }
        }

        
	public enum Flag { NONE, HOLD, RELEASE }

        private Channel channel;
	private final int measure;
	private final double position;
	
	private final long offset;
	
	private final double value;
	Flag flag;
        private final SoundSample sound_sample;
        
        /** The time to hit */
        private long time;

	public Event(Channel channel, int measure, double position,
			double value, Flag flag) {
            this.channel = channel;
            this.measure = measure;
            this.position = position;
            this.value = value;
            this.flag = flag;
            this.sound_sample = new SoundSample((int)value, 1, 0);
	    this.offset = 0;
	}

        public Event(Channel channel, int measure, double position,
                    double value, Flag flag, float vol, float pan) {
            this.channel = channel;
            this.measure = measure;
            this.position = position;
            this.value = value;
            this.flag = flag;
            this.sound_sample = new SoundSample((int) value,vol, pan);
	    this.offset = 0;
	}
	
	public Event(Channel channel, int measure, double position,
			double value, long offset, Flag flag) {
            this.channel = channel;
            this.measure = measure;
            this.position = position;
            this.value = value;
            this.flag = flag;
            this.sound_sample = new SoundSample((int)value, 1, 0);
	    this.offset = offset;
	}

        public Event(Channel channel, int measure, double position,
                    double value, long offset, Flag flag, float vol, float pan) {
            this.channel = channel;
            this.measure = measure;
            this.position = position;
            this.value = value;
            this.flag = flag;
            this.sound_sample = new SoundSample((int) value,vol, pan);
	    this.offset = offset;
	}

	public int compareTo(Event e)
	{
            return ((measure+position) < (e.getMeasure()+e.getPosition())) ? -1 : 1;
	}

	public void setChannel(Channel chan) { this.channel = chan; }
	public Channel getChannel() { return channel; }
	public int getMeasure() { return measure; }
	public double getPosition() { return position; }
	public Flag getFlag() { return flag; }
	public double getValue() { return value; }
        public SoundSample getSample(){ return sound_sample; }
	public long getOffset() { return offset; }
        
        public void setTime(long t) { this.time = t; }
        public long getTime() { return time; }
}
