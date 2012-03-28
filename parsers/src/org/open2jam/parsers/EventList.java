package org.open2jam.parsers;

import java.util.*;
import java.util.logging.Level;
import org.open2jam.parsers.Event.Channel;
import org.open2jam.parsers.utils.Logger;

/**
 * It's just an ArrayList<Event> extended to be able to make some funny things with it
 * @author CdK
 */
public class EventList extends ArrayList<Event> {
    
    /*
     * Use this only if you don't want to deal with broken longnotes or longnotes in the autoplay channel
     * If you want to write a Editor with this lib, don't use it because it changes a lot of things in the events
     */
    public void fixEventList(boolean fix_broken_longnotes, boolean fix_autoplay_longnotes) {
	List<Event.Channel> longnote_chan = new ArrayList<Event.Channel>();
	
	Iterator<Event> it = this.iterator();
	
	while(it.hasNext()) {
	    Event e = it.next();
	    Event.Channel c = e.getChannel();
	    
	    if(fix_autoplay_longnotes) {
		if(c == Event.Channel.AUTO_PLAY) {
		    if(e.getFlag() == Event.Flag.RELEASE) {
			it.remove(); //remove the release if auto_play
			continue;
		    }
		    e.flag = Event.Flag.NONE;
		}
	    }
	    
	    if(fix_broken_longnotes) {
		switch(e.getFlag()){
		    case NONE:
			if(longnote_chan.contains(c)) {
			    e.flag = Event.Flag.RELEASE;
			    longnote_chan.remove(c);
			}
		    break;
		    case HOLD:
			if(longnote_chan.contains(c)) {
			    e.flag = Event.Flag.RELEASE;
			    longnote_chan.remove(c);
			}
			longnote_chan.add(c);
		    break;  
		    case RELEASE:
			longnote_chan.remove(c);
		    break;
		}
	    }
	}
    }
    
    /** 
     * This method will return a map with measures and a list of events for each measure
     * @return A map with measures => list of events
     */
    public Map<Integer, EventList> getEventsPerMeasure() {
	Map<Integer, EventList> epm = new HashMap<Integer, EventList>();
	
	for(Event e : this) {	    
	    if(!epm.containsKey(e.getMeasure()))
		epm.put(e.getMeasure(), new EventList());
	    
	    epm.get(e.getMeasure()).add(e);
	}
	
	return epm;
    }
    
    /** 
     * This method will return a map with channels and a list of events for each channel
     * @param event_list The list of events
     * @return A map with channels => list of events
     */
    public Map<Event.Channel, EventList> getEventsPerChannel() {
	Map<Event.Channel, EventList> epc = new EnumMap<Event.Channel, EventList>(Event.Channel.class);
	
	for(Event e : this) {
	    if(!epc.containsKey(e.getChannel()))
		epc.put(e.getChannel(), new EventList());
	    epc.get(e.getChannel()).add(e);
	}
	
	return epc;
    }
    
    public EventList getOnlyNormalNotes() {
	EventList nn = new EventList();
	
	for(Event e : this) {
	    if(e.getFlag().equals(Event.Flag.NONE))
		nn.add(e);
	}
	
	return nn;
    }
    
    public EventList getOnlyLongNotes() {
	EventList ln = new EventList();
	
	for(Event e : this) {
	    if(e.getFlag().equals(Event.Flag.HOLD) || e.getFlag().equals(Event.Flag.RELEASE))
		ln.add(e);
	}
	
	return ln;
    }
    
    public EventList getEventsFromThisChannel(Channel channel) {
        EventList eftc = new EventList();
        
        for(Event e : this) {
            if(e.getChannel().equals(channel))
                eftc.add(e);
        }
        
        return eftc;
    }
    
    
    /**
    * This method will mirrorize the notes in the EventList
    * TODO ADD P2 SUPPORT
    */
    public void channelMirror()
    {
	Iterator<Event> it = this.iterator();

	while(it.hasNext())
	{
	    Event e = it.next();
	    switch(e.getChannel())
	    {
	    case NOTE_1: e.setChannel(Event.Channel.NOTE_7); break;
	    case NOTE_2: e.setChannel(Event.Channel.NOTE_6); break;
	    case NOTE_3: e.setChannel(Event.Channel.NOTE_5); break;
	    case NOTE_5: e.setChannel(Event.Channel.NOTE_3); break;
	    case NOTE_6: e.setChannel(Event.Channel.NOTE_2); break;
	    case NOTE_7: e.setChannel(Event.Channel.NOTE_1); break;
	    }
	}
    }

    /**
     * This method will shuffle the notes in the EventList
     * TODO ADD P2 SUPPORT
     */
    public void channelShuffle()
    {
        List<Event.Channel> channelSwap = new ArrayList<Event.Channel>();

        channelSwap.add(Event.Channel.NOTE_1);
        channelSwap.add(Event.Channel.NOTE_2);
        channelSwap.add(Event.Channel.NOTE_3);
        channelSwap.add(Event.Channel.NOTE_4);
        channelSwap.add(Event.Channel.NOTE_5);
        channelSwap.add(Event.Channel.NOTE_6);
        channelSwap.add(Event.Channel.NOTE_7);

        Collections.shuffle(channelSwap);

	Iterator<Event> it = this.iterator();
	
        while(it.hasNext())
        {
            Event e = it.next();
            switch(e.getChannel())
            {
            case NOTE_1: e.setChannel(channelSwap.get(0)); break;
            case NOTE_2: e.setChannel(channelSwap.get(1)); break;
            case NOTE_3: e.setChannel(channelSwap.get(2)); break;
            case NOTE_4: e.setChannel(channelSwap.get(3)); break;
            case NOTE_5: e.setChannel(channelSwap.get(4)); break;
            case NOTE_6: e.setChannel(channelSwap.get(5)); break;
            case NOTE_7: e.setChannel(channelSwap.get(6)); break;
            }
        }
    }

    /**
     * This method will randomize the notes in the EventList
     * o2jam randomize the pattern each measure unless a longnote is in between measures
     * This implementation keeps the randomization of the previous measure if that happens
     * @param buffer
     */
    public void channelRandom()
    {
        List<Event.Channel> channelSwap = new ArrayList<Event.Channel>();

        channelSwap.add(Event.Channel.NOTE_1);
        channelSwap.add(Event.Channel.NOTE_2);
        channelSwap.add(Event.Channel.NOTE_3);
        channelSwap.add(Event.Channel.NOTE_4);
        channelSwap.add(Event.Channel.NOTE_5);
        channelSwap.add(Event.Channel.NOTE_6);
        channelSwap.add(Event.Channel.NOTE_7);

        Collections.shuffle(channelSwap);

	EnumMap<Event.Channel, Event.Channel> lnMap = new EnumMap<Event.Channel, Event.Channel>(Event.Channel.class);

	int last_measure = -1;
	
	Iterator<Event> it = this.iterator();
        while(it.hasNext())
        {
            Event e = it.next();

                if(e.getMeasure() > last_measure)
                {
                    if(lnMap.isEmpty())
                        Collections.shuffle(channelSwap);
                    last_measure = e.getMeasure();
                }

            switch(e.getChannel())
            {
		case NOTE_1:
		    setRandomChannel(e, lnMap, channelSwap.get(0));
		    break;
		case NOTE_2:
		    setRandomChannel(e, lnMap, channelSwap.get(1));
		    break;
		case NOTE_3:
		    setRandomChannel(e, lnMap, channelSwap.get(2));
		    break;
		case NOTE_4:
		    setRandomChannel(e, lnMap, channelSwap.get(3));
		    break;
		case NOTE_5:
		    setRandomChannel(e, lnMap, channelSwap.get(4));
		    break;
		case NOTE_6:
		    setRandomChannel(e, lnMap, channelSwap.get(5));
		    break;
		case NOTE_7:
		    setRandomChannel(e, lnMap, channelSwap.get(6));
		    break;
            }
        }
    }

    private void setRandomChannel(Event e, EnumMap<Event.Channel, Event.Channel> lnMap, Event.Channel random)
    {
        Event.Channel c = random;

        if(e.getFlag() == Event.Flag.HOLD || e.getFlag() == Event.Flag.RELEASE)
        {
            if(!lnMap.containsKey(e.getChannel()))
                lnMap.put(e.getChannel(), c);
            else
                c = lnMap.remove(e.getChannel());
        }
        else if(e.getFlag() == Event.Flag.NONE)
            c = lnMap.containsValue(c) ? Event.Channel.NONE : c;

        if(c == null)
        {
            Logger.global.log(Level.WARNING, "FUCK THIS RANDOMNESS! I mean... channel null :/");
            c = random;
        }

        e.setChannel(c);
    }
}
