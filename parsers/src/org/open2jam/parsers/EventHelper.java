package org.open2jam.parsers;

import java.util.*;
import org.open2jam.parsers.Event.Channel;

/**
 *
 * @author User
 */
public class EventHelper {
    
    /** 
     * This method will return a map with measures and a list of events for each measure
     * @param event_list The list of events
     * @return A map with measures => list of events
     */
    public static Map<Integer, List<Event>> getEventsPerMeasure(List<Event> event_list) {
	Map<Integer, List<Event>> epm = new HashMap<Integer, List<Event>>();
	
	Integer max_measure = null;
	for(Event e : event_list) {
	    if(max_measure == null || e.getMeasure() > max_measure) {
		max_measure = e.getMeasure();
		epm.put(e.getMeasure(), new ArrayList<Event>());
	    }
	    
	    epm.get(max_measure).add(e);
	}
	
	return epm;
    }
    
    /** 
     * This method will return a map with channels and a list of events for each channel
     * @param event_list The list of events
     * @return A map with channels => list of events
     */
    public static Map<Event.Channel, List<Event>> getEventsPerChannel(List<Event> event_list) {
	Map<Event.Channel, List<Event>> epc = new EnumMap<Event.Channel, List<Event>>(Event.Channel.class);
	
	for(Event e : event_list) {
	    if(!epc.containsKey(e.getChannel()))
		epc.put(e.getChannel(), new ArrayList<Event>());
	    epc.get(e.getChannel()).add(e);
	}
	
	return epc;
    }
    
    public static List<Event> getEventsFromThisChannel(List<Event> event_list, Channel channel) {
        List<Event> eftc = new ArrayList<Event>();
        
        for(Event e : event_list) {
            if(e.getChannel().equals(channel))
                eftc.add(e);
        }
        
        return eftc;
    }
    
}
