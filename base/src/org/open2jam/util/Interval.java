package org.open2jam.util;

/**
 * The Interval class maintains an interval with some associated data
 * @author Kevin Dolan
 * http://thekevindolan.com/2010/02/interval-tree/index.html
 * @param <V> The V of data being stored
 */
public class Interval<K extends Comparable<K>,V> implements Comparable<Interval<K,V>> {

	private K start;
	private K end;
	private V data;
	
	public Interval(K start, K end, V data) {
		this.start = start;
		this.end = end;
		this.data = data;
	}

	public K getStart() {
		return start;
	}

	public void setStart(K start) {
		this.start = start;
	}

	public K getEnd() {
		return end;
	}

	public void setEnd(K end) {
		this.end = end;
	}

	public V getData() {
		return data;
	}

	public void setData(V data) {
		this.data = data;
	}
	
	/**
	 * @param time
	 * @return	true if this interval contains time (invlusive)
	 */
	public boolean contains(K time) {
                return time.compareTo(end) < 0 && time.compareTo(start) > 0;
		// return time < end && time > start;
	}
	
	/**
	 * @param other
	 * @return	return true if this interval intersects other
	 */
	public boolean intersects(Interval<K,?> other) {
                return other.getEnd().compareTo(start) > 0 && other.getStart().compareTo(end) < 0;
		//return other.getEnd() > start && other.getStart() < end;
	}
	
	/**
	 * Return -1 if this interval's start time is less than the other, 1 if greater
	 * In the event of a tie, -1 if this interval's end time is less than the other, 1 if greater, 0 if same
	 * @param other
	 * @return 1 or -1
	 */
	public int compareTo(Interval<K,V> other) {

            int i = start.compareTo(other.getStart());
            int j = end.compareTo(other.getStart());

            if(i < 0)return -1;
            else if (i > 0)return 1;
            else if (j < 0)return -1;
            else if (j > 0)return 1;
            else return 0;

                /*
		if(start < other.getStart())
			return -1;
		else if(start > other.getStart())
			return 1;
		else if(end < other.getEnd())
			return -1;
		else if(end > other.getEnd())
			return 1;
		else
			return 0;
                 */
	}
	
}
