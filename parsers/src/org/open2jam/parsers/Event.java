package org.open2jam.parsers;

public class Event implements Comparable<Event> {

    public enum Channel {
        NONE, //Channel to /dev/null
        //Player 1
        NOTE_1, NOTE_2, NOTE_3, NOTE_4, NOTE_5, NOTE_6, NOTE_7, NOTE_SC,
        //Player 2
        NOTE_8, NOTE_9, NOTE_10, NOTE_11, NOTE_12, NOTE_13, NOTE_14, NOTE_SC2,

        TIME_SIGNATURE, // This channel will hold all the time signature changes
        BPM_CHANGE,     // This channel will hold all the bpm changes
        STOP,           // This channel will hold all the stops
        MEASURE,        // This will be used when the velocity tree is constructed
	
	BGA,

        AUTO_PLAY(true);// Autoplay, used by the background music/sounds
        
        private boolean autoplay;
        
        private Channel() {
            this.autoplay = false;
        }

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
	
	public static Channel[] playableChannels() {
	    Channel[] playable = {
		NOTE_1, NOTE_2, NOTE_3, NOTE_4, NOTE_5, NOTE_6, NOTE_7
	    };
	    return playable;
	}
	
	public static Channel mirrorChannel(Channel c) {
	    switch(c) {
		case NOTE_1: return NOTE_7;
		case NOTE_2: return NOTE_6;
		case NOTE_3: return NOTE_5;
		case NOTE_4: return NOTE_4;
		case NOTE_5: return NOTE_3;
		case NOTE_6: return NOTE_2;
		case NOTE_7: return NOTE_1;
		default: return c;
	    }
	}
    }

    /**
     * wrapper for the sound sample
     *
     * contains the sample id for the SoundManager volume and pan
     */
    public class SoundSample {

        public final int sample_id;
        public final float volume;
        public final float pan;
        boolean bgm = false;

        public SoundSample(int sample, float vol, float pan) {
            this.sample_id = sample;
            this.volume = vol;
            this.pan = pan;
        }

        public void toBGM() {
            this.bgm = true;
        }

        public boolean isBGM() {
            return bgm;
        }
    }

    public enum Flag {
        NONE, HOLD, RELEASE, ROLL, MINE, LIFT
    };
    private Channel channel;
    private final int measure;
    private final double position;
    private final double value;
    private final double offset;
    Flag flag;
    private final SoundSample sound_sample;
    /**
     * The time to hit
     */
    private double time;

    public Event(Channel channel, int measure, double position,
            double value, Flag flag) {
        this.channel = channel;
        this.measure = measure;
        this.position = position;
        this.value = value;
        this.flag = flag;
        this.sound_sample = new SoundSample((int) value, 1, 0);
        this.offset = 0;
    }

    public Event(Channel channel, int measure, double position,
            double value, Flag flag, float vol, float pan) {
        this.channel = channel;
        this.measure = measure;
        this.position = position;
        this.value = value;
        this.flag = flag;
        this.sound_sample = new SoundSample((int) value, vol, pan);
        this.offset = 0;
    }

    public Event(Channel channel, int measure, double position,
            double value, double offset, Flag flag) {
        this.channel = channel;
        this.measure = measure;
        this.position = position;
        this.value = value;
        this.flag = flag;
        this.sound_sample = new SoundSample((int) value, 1, 0);
        this.offset = offset;
    }

    public Event(Channel channel, int measure, double position,
            double value, double offset, Flag flag, float vol, float pan) {
        this.channel = channel;
        this.measure = measure;
        this.position = position;
        this.value = value;
        this.flag = flag;
        this.sound_sample = new SoundSample((int) value, vol, pan);
        this.offset = offset;
    }

    public int compareTo(Event e) {
	double a = measure + position;
	double b = e.getMeasure() + e.getPosition();

	if (a < b) {
	    return -1;
	} else if (a == b) {
	    if (channel == Channel.STOP) {
		return 1;
	    }
	    return 0;
	} else {
	    return 1;
	}
	    
    }

    public void setChannel(Channel chan) {
        this.channel = chan;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getMeasure() {
        return measure;
    }

    public double getPosition() {
        return position;
    }
    
    public double getTotalPosition() {
	return measure+position;
    }

    public Flag getFlag() {
        return flag;
    }

    public double getValue() {
        return value;
    }

    public SoundSample getSample() {
        return sound_sample;
    }

    public double getOffset() {
        return offset;
    }

    public void setTime(double t) {
        this.time = t;
    }

    public double getTime() {
        return time;
    }

    @Override
    public String toString() {
        String s = "";
        s += "Event (" + this.flag + ", " + this.value + ")\n";
        s += "\tCHANNEL: " + this.channel + " @ " + (this.measure + this.position) + "\n";
        s += this.offset != 0f ? "\tOFFSET: " + this.offset + "\n" : "";
        s += "\tTIME: " + this.time;
        return s;
    }
}
