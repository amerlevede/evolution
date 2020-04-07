package util;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * Simple value class to capture sets of two doubles.
 * Comparable interface uses lexicographical ordering.
 * @author adriaan
 */
public class DoublePair implements Comparable<DoublePair> {
	
	public final double x;
	public final double y;
	
	private DoublePair(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public static DoublePair of(double x, double y) {
		return new DoublePair(x,y);
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	@Override
	public int compareTo(DoublePair that) {
		int cmpa = Double.compare(this.x, that.x);
		if (cmpa == 0) {
			return Double.compare(this.y, that.y);
		} else {
			return cmpa;
		}
	}
	
	/**
	 * Combine two sets of doubles into a set of pairs.
	 * The given sets must be of the same size, otherwise an exception is thrown.
	 */
	public static SortedSet<DoublePair> zip(SortedSet<Double> ia, SortedSet<Double> ib) {
		if (ia.size() != ib.size()) throw new IllegalArgumentException("Given sets of doubles must be of the same size to form a set of pairs.");
		
		SortedSet<DoublePair> result = new TreeSet<>();
		Iterator<Double> iait = ia.iterator();
		Iterator<Double> ibit = ib.iterator();
		while (iait.hasNext()) {
			result.add(DoublePair.of(iait.next(), ibit.next()));
		}
		
		return result;
	}
	
	/**
	 * Check if the given set of pairs is monotone, meaning that the a and b components have the same ordering.
	 * This also requires that the pairs are non-overlapping, meaning that all a-components are unique, and all b-components are unique (so technically this checks for *strictly* monotone sets).
	 */
	public static boolean isMonotone(SortedSet<DoublePair> pairs) {
		if (pairs.isEmpty()) {
			return true;
		} else {
			Iterator<DoublePair> iter = pairs.iterator();
			DoublePair p = iter.next();
			double lasta = p.x;
			double lastb = p.y;
			while (iter.hasNext()) {
				p = iter.next();
				if (p.x <= lasta) return false;
				if (p.y <= lastb) return false;
			}
			return true;
		}
	}
	
	@Override
	public String toString() {
		return "("+this.x+","+this.y+")";
	}
	
	public DoublePair map(DoubleUnaryOperator f) {
		return DoublePair.of(f.applyAsDouble(this.x), f.applyAsDouble(this.y));
	}
	
	public DoublePair apply(DoubleBinaryOperator f, DoublePair arg) {
		return DoublePair.of(f.applyAsDouble(this.x, arg.x), f.applyAsDouble(this.y, arg.y));
	}
	
	public static double distance(DoublePair a, DoublePair b) {
		return Math.hypot(a.x - b.x, a.y - b.y);
	}

}
