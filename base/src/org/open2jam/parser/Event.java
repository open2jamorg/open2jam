package org.open2jam.parser;

public class Event implements Comparable<Event>
{
 	public enum Channel {
            NONE,
            TIME_SIGNATURE,
            BPM_CHANGE,
            NOTE_P1_1,                  //P1-NOTE_1
            NOTE_P1_2,                  //P1-NOTE_2
            NOTE_P1_3,                  //P1-NOTE_3
            NOTE_P1_4,                  //P1-NOTE_4
            NOTE_P1_5,                  //P1-NOTE_5
            NOTE_P1_6,                  //P1-NOTE_6
            NOTE_P1_7,                  //P1-NOTE_7
            NOTE_P1_SC,                 //P1-NOTE_SC / P1-NOTE_8
            NOTE_P2_1,                  //P2-NOTE_1
            NOTE_P2_2,                  //P2-NOTE_2
            NOTE_P2_3,                  //P2-NOTE_3
            NOTE_P2_4,                  //P2-NOTE_4
            NOTE_P2_5,                  //P2-NOTE_5
            NOTE_P2_6,                  //P2-NOTE_6
            NOTE_P2_7,                  //P2-NOTE_7
            NOTE_P2_SC,                 //P2-NOTE_SC / P2-NOTE_8

            AUTO_PLAY;

            @Override
            public String toString(){
                switch(this){
	            case NOTE_P1_1:return "NOTE_P1_1";
	            case NOTE_P1_2:return "NOTE_P1_2";
	            case NOTE_P1_3:return "NOTE_P1_3";
	            case NOTE_P1_4:return "NOTE_P1_4";
	            case NOTE_P1_5:return "NOTE_P1_5";
	            case NOTE_P1_6:return "NOTE_P1_6";
	            case NOTE_P1_7:return "NOTE_P1_7";
                    case NOTE_P1_SC:return "NOTE_P1_SC";
	            case NOTE_P2_1:return "NOTE_P2_1";
	            case NOTE_P2_2:return "NOTE_P2_2";
	            case NOTE_P2_3:return "NOTE_P2_3";
	            case NOTE_P2_4:return "NOTE_P2_4";
	            case NOTE_P2_5:return "NOTE_P2_5";
	            case NOTE_P2_6:return "NOTE_P2_6";
	            case NOTE_P2_7:return "NOTE_P2_7";
                    case NOTE_P2_SC:return "NOTE_P2_SC";
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

            protected boolean bgm = false;

            public SoundSample(int sample, float vol, float pan){
                this.sample_id = sample;
                this.volume = vol;
                this.pan = pan;
            }

            public void toBGM()
            {
                this.bgm = true;
            }

            public boolean isBGM()
            {
                return bgm;
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
            return ((measure+position) < (e.getMeasure()+e.getPosition())) ? -1 : 1;
	}

	public void setChannel(Channel chan) { this.channel = chan; }
	public Channel getChannel() { return channel; }
	public int getMeasure() { return measure; }
	public double getPosition() { return position; }
	public Flag getFlag() { return flag; }
	public double getValue() { return value; }
        public SoundSample getSample(){ return sound_sample; }
}
