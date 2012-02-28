package org.open2jam.util;

import java.util.Map.Entry;
import java.util.*;

/**
 * The Node class contains the interval tree information for one single node
 * http://thekevindolan.com/2010/02/interval-tree/index.html
 * @author Kevin Dolan
 */
public class IntervalNode<K extends Comparable<K>,V> {

	private final SortedMap<Interval<K,V>, List<Interval<K,V>>> intervals;
	private K center;
	private IntervalNode<K,V> leftNode;
	private IntervalNode<K,V> rightNode;
	
	public IntervalNode() {
		intervals = new TreeMap<Interval<K,V>, List<Interval<K,V>>>();
		leftNode = null;
		rightNode = null;
	}
	
	public IntervalNode(List<Interval<K,V>> intervalList) {
		
		intervals = new TreeMap<Interval<K,V>, List<Interval<K,V>>>();
		
		SortedSet<K> endpoints = new TreeSet<K>();
		
		for(Interval<K,V> interval: intervalList) {
			endpoints.add(interval.getStart());
			endpoints.add(interval.getEnd());
		}
		
		K median = getMedian(endpoints);
		center = median;
		
		List<Interval<K,V>> left = new ArrayList<Interval<K,V>>();
		List<Interval<K,V>> right = new ArrayList<Interval<K,V>>();
		
		for(Interval<K,V> interval : intervalList) {
			if(interval.getEnd().compareTo(median) < 0)
				left.add(interval);
			else if(interval.getStart().compareTo(median) > 0)
				right.add(interval);
			else {
				List<Interval<K,V>> posting = intervals.get(interval);
				if(posting == null) {
					posting = new ArrayList<Interval<K,V>>();
					intervals.put(interval, posting);
				}
				posting.add(interval);
			}
		}

		if(left.size() > 0)
			leftNode = new IntervalNode<K,V>(left);
		if(right.size() > 0)
			rightNode = new IntervalNode<K,V>(right);
	}

	/**
	 * Perform a stabbing query on the node
	 * @param time the time to query at
	 * @return	   all intervals containing time
	 */
	public List<Interval<K,V>> stab(K time) {
		List<Interval<K,V>> result = new ArrayList<Interval<K,V>>();

		for(Entry<Interval<K,V>, List<Interval<K,V>>> entry : intervals.entrySet()) {
			if(entry.getKey().contains(time))
				for(Interval<K,V> interval : entry.getValue())
					result.add(interval);
			else if(entry.getKey().getStart().compareTo(time) > 0)
				break;
		}
		
		if(time.compareTo(center) < 0 && leftNode != null)
			result.addAll(leftNode.stab(time));
		else if(time.compareTo(center) > 0 && rightNode != null)
			result.addAll(rightNode.stab(time));
		return result;
	}
	
	/**
	 * Perform an interval intersection query on the node
	 * @param target the interval to intersect
	 * @return		   all intervals containing time
	 */
	public List<Interval<K,V>> query(Interval<K,?> target) {
		List<Interval<K,V>> result = new ArrayList<Interval<K,V>>();
		
		for(Entry<Interval<K,V>, List<Interval<K,V>>> entry : intervals.entrySet()) {
			if(entry.getKey().intersects(target))
				for(Interval<K,V> interval : entry.getValue())
					result.add(interval);
			else if(entry.getKey().getStart().compareTo(target.getEnd()) > 0)
				break;
		}
		
		if(target.getStart().compareTo(center) < 0 && leftNode != null)
			result.addAll(leftNode.query(target));
		if(target.getEnd().compareTo(center) > 0 && rightNode != null)
			result.addAll(rightNode.query(target));
		return result;
	}
	
	public K getCenter() {
		return center;
	}

	public void setCenter(K center) {
		this.center = center;
	}

	public IntervalNode<K,V> getLeft() {
		return leftNode;
	}

	public void setLeft(IntervalNode<K,V> left) {
		this.leftNode = left;
	}

	public IntervalNode<K,V> getRight() {
		return rightNode;
	}

	public void setRight(IntervalNode<K,V> right) {
		this.rightNode = right;
	}
	
	/**
	 * @param set the set to look on
	 * @return	  the median of the set, not interpolated
	 */
	private K getMedian(SortedSet<K> set) {
		int i = 0;
		int middle = set.size() / 2;
		for(K point : set) {
			if(i == middle)
				return point;
			i++;
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(center).append(": ");
		for(Entry<Interval<K,V>, List<Interval<K,V>>> entry : intervals.entrySet()) {
			sb.append("[").append(entry.getKey().getStart()).append(",").append(entry.getKey().getEnd()).append("]:{");
			for(Interval<K,V> interval : entry.getValue()) {
				sb.append("(").append(interval.getStart()).append(",").append(interval.getEnd()).append(",").append(interval.getData()).append(")");
			}
			sb.append("} ");
		}
		return sb.toString();
	}
	
}
