package util;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Simple value class to capture sets of two integers.
 * Comparable interface uses lexicographical ordering.
 * @author adriaan
 */
public class IntPair implements Comparable<IntPair> {
	
	public final int x;
	public final int y;
	
	private IntPair(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public static IntPair of(int x, int y) {
		return new IntPair(x,y);
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	@Override
	public int compareTo(IntPair that) {
		int cmpa = Integer.compare(this.x, that.x);
		if (cmpa == 0) {
			return Integer.compare(this.y, that.y);
		} else {
			return cmpa;
		}
	}
	
	public static int compare(IntPair thisone, IntPair thatone) {
		return thisone.compareTo(thatone);
	}
	
	/**
	 * Combine two sets of integers into a set of pairs.
	 * The given sets must be of the same size, otherwise an exception is thrown.
	 */
	public static SortedSet<IntPair> zip(SortedSet<Integer> ia, SortedSet<Integer> ib) {
		if (ia.size() != ib.size()) throw new IllegalArgumentException("Given sets of integers must be of the same size to form a set of pairs.");
		
		SortedSet<IntPair> result = new TreeSet<>();
		Iterator<Integer> iait = ia.iterator();
		Iterator<Integer> ibit = ib.iterator();
		while (iait.hasNext()) {
			result.add(IntPair.of(iait.next(), ibit.next()));
		}
		
		return result;
	}
	
	/**
	 * Check if the given set of pairs is monotone, meaning that the a and b components have the same ordering.
	 * This also requires that the pairs are non-overlapping, meaning that all a-components are unique, and all b-components are unique (so technically this checks for *strictly* monotone sets).
	 */
	public static boolean isMonotone(SortedSet<IntPair> pairs) {
		if (pairs.isEmpty()) {
			return true;
		} else {
			Iterator<IntPair> iter = pairs.iterator();
			IntPair p = iter.next();
			int lasta = p.x;
			int lastb = p.y;
			while (iter.hasNext()) {
				p = iter.next();
				if (p.x <= lasta) return false;
				if (p.y <= lastb) return false;
			}
			return true;
		}
	}
	
	public IntPair flip() {
		return IntPair.of(this.y, this.x);
	}
	
	@Override
	public String toString() {
		return "("+this.x+","+this.y+")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntPair) {
			IntPair that = (IntPair) obj;
			return this.x == that.x && this.y == that.y;
		} else return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 23;
		hash = hash * 31 + this.x;
		hash = hash * 31 + this.y;
		return hash;
	}

}
