package crossover;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import genome.Genome;
import genome.LinearGenome;
import genome.VarLengthGenome;
import genome.binary.BitGenome;
import util.Assert;
import util.IntPair;

/**
 * A functional interface describing recombination schemes for {@link Genome}s.
 * It is an alias for {@link BiConsumer}<Genome,Genome> with some static utility methods. During crossover, the parent genomes are modified to exchange genetic information.
 * @author adriaan
 */
@FunctionalInterface
public interface CrossoverOp<G> extends BiConsumer<G, G> {

	@Override void accept(G mom, G dad);

    /**
     * Perform a one-point crossover between two Genomes at the specified indices.
     * The indices ia and ib refer to positions left of a bit on Genome a and b resp.
     * This method does not allow exchanging parts of length 0, because this corner case can unexpectedly result in zero-length Genomes.
     * Therefore, ia and ib must conform to {@link BitGenome#innerSplice} of their respective genomes.
     *
     * @param a - Genome A in the crossover
     * @param ia - Index of the crossover in Genome A. All bits after this will be exchanged with bits after ib.
     * @param b - Genome B in the crossover
     * @param ib - Index of the crossover in Genome B. All bits after this will be exchanged with bits after ia.
     */
    static <G extends VarLengthGenome<G>> void performOnePoint(
            G a, int ia,
            G b, int ib) {
        Assert.notNull(a);
        Assert.notNull(b);
        Assert.innerSplice(a, ia);
        Assert.innerSplice(b, ib);

        G atail = a.copy(ia, a.size());
        a.replace(ia, a.size(), b, ib, b.size());
        b.replace(ib, b.size(), atail);
    }

    /**
     * Perform a two-point crossover between two Genomes at the specified indices.
     * This exchanges an interval of bits on Genome A with an interval (possibly of different length) of bits on Genome B.
     * This method does not allow exchanging parts of length 0, because this corner case can unexpectedly result in zero-length Genomes.
     * Therefore, ia and ib must conform to {@link BitGenome#innerSplice} of their respective genomes.
     */
    static <G extends VarLengthGenome<G>> void performTwoPoint(
            G a, int aInclusiveStart, int aExclusiveEnd,
            G b, int bInclusiveStart, int bExclusiveEnd) {
        Assert.notNull(a);
        Assert.notNull(b);
        Assert.innerSplice(a, aInclusiveStart, aExclusiveEnd);
        Assert.innerSplice(b, bInclusiveStart, bExclusiveEnd);

        G atail = a.copy(aInclusiveStart, aExclusiveEnd);
        a.replace(aInclusiveStart, aExclusiveEnd, b, bInclusiveStart, bExclusiveEnd);
        b.replace(bInclusiveStart, bExclusiveEnd, atail);
    }

    /**
     * Perform an n-point crossover between two Genomes at the specified indices.
     * Indices must be valid, see {@link #legalNPoints(SortedSet, BitGenome, BitGenome)}.
     */
    static <G extends VarLengthGenome<G>> void performNPoint(SortedSet<IntPair> indices, G a, G b) {
    	if (!CrossoverOp.legalNPoints(indices, a, b)) {
    		System.err.println(indices);
    		throw new IllegalArgumentException("Attempted to perform npoint crossover with an invalid set of indices.");
    	}

    	// Do nothing if there are no crossover points
    	if (indices.isEmpty()) return;
        Iterator<IntPair> iter = indices.iterator();

    	// Copy references
        G aref = a.copy();
        G bref = b.copy();

        // head (it's already there, so just delete rest)
        IntPair prev = iter.next();
        a.delete(prev.x, a.size());
        b.delete(prev.y, b.size());

        // body (alternatingly add pieces from reference copies)
        boolean parity = false;
        while (iter.hasNext()) {
        	IntPair p = iter.next();
        	if (parity) {
        		if (p.x - prev.x > 0) a.append(aref, prev.x, p.x);
        		if (p.y - prev.y > 0) b.append(bref, prev.y, p.y);
        	} else {
        		if (p.y - prev.y > 0) a.append(bref, prev.y, p.y);
        		if (p.x - prev.x > 0) b.append(aref, prev.x, p.x);
        	}
        	parity = !parity;
        	prev = p;
        }

        // tail
    	if (parity) {
    		a.append(aref, prev.x, aref.size());
    		b.append(bref, prev.y, bref.size());
    	} else {
    		a.append(bref, prev.y, bref.size());
    		b.append(aref, prev.x, aref.size());
    	}
    }

    /** See {@link #performNPoint(SortedSet, BitGenome, BitGenome)} */
	static <G extends VarLengthGenome<G>> void performNPoint(G a, SortedSet<Integer> ia, G b, SortedSet<Integer> ib) {
	    performNPoint(IntPair.zip(ia, ib), a, b);
	}

	/**
	 * Check if this set of index pairs appropriately defines a variable-length crossover.
	 * The points must be:
	 *  - All "a" indices must be {@link BitGenome#innerSplice} of Genome a (i.e. leaving no empty sections at the start or end).
	 *  - All "b" indices must be {@link BitGenome#innerSplice} of Genome b
	 *  - All "b" indices must have the same order as the "a" indices.
	 */
	static <G extends VarLengthGenome<G>> boolean legalNPoints(SortedSet<IntPair> indices, G a, G b) {
//		int lasta = -1;
		int lastb = -1;
		for (IntPair p : indices) {
			if (!a.innerSplice(p.x)) return false; // Check condition 1.
			if (!b.innerSplice(p.y)) return false; // Check condition 2.
//			if (p.a < lasta) return false; // Cannot be strictly smaller due to ordering
			if (p.y < lastb) return false; // Check condition 3
//			lasta = p.a;
			lastb = p.y;
		}
		return true;
	}

	/**
	 * Turn an alignment into a set of possible crossover points.
	 * The resulting set
	 *   (*) has the first element or last element removed if it is at the side of either genome ({@link #filterInnerSplice(SortedSet, BitGenome, BitGenome)}), and
	 *   (*) has the first pair of any gaps in the alignment added
	 */
	static <G extends LinearGenome<G>> SortedSet<IntPair> alignmentToCrossoverPoints(SortedSet<IntPair> indices, G a, G b) {
		SortedSet<IntPair> result = new TreeSet<>();
		for (IntPair p : indices) {
			if (a.innerSplice(p.x)   && b.innerSplice(p.y))   result.add(p);
			if (a.innerSplice(p.x+1) && b.innerSplice(p.y+1)) result.add(IntPair.of(p.x+1, p.y+1));
		}
		return result;
	}

	static <G extends VarLengthGenome<G>> int cross1segment(G appendToThis, int here, IntPair start, boolean onA, Iterator<IntPair> points, G aref, G bref) {
		while (points.hasNext()) {
			IntPair xpoint = points.next();
			int realstart = onA ? start.x : start.y;
			int realpoint = onA ? xpoint.x : xpoint.y;
			try {
				if (realpoint > realstart) appendToThis.paste(here, onA?aref:bref, realstart, realpoint);
			} catch (Exception e) {
				System.out.println(aref);
				System.out.println(bref);
				System.out.println(appendToThis);
				System.out.println(here);
				System.out.println(onA);
				System.out.println(realstart);
				System.out.println(realpoint);
				throw e;
			}
			here += realpoint - realstart;
			onA = !onA;
			start = xpoint;
			if (appendToThis.size() > aref.size()+bref.size()) throw new IllegalStateException();
		}
		return here;
	}

	static <G extends VarLengthGenome<G>> void crossNsegments(G overwriteThis, int here, boolean onA, SortedSet<SortedSet<IntPair>> sortedByA, SortedSet<SortedSet<IntPair>> sortedByB, G aref, G bref) {
		SortedSet<IntPair> segment = (onA?sortedByA:sortedByB).first();
		here = onA ? segment.first().x : segment.first().y;
		here = cross1segment(overwriteThis, here, segment.first(), onA, segment.iterator(), aref, bref);

		onA ^= segment.size() % 2 != 0;
		Iterator<SortedSet<IntPair>> iter = (onA?sortedByA:sortedByB).tailSet(segment).iterator();
		iter.next();

		while (iter.hasNext()) {
			SortedSet<IntPair> newsegment = iter.next();
			here = cross1segment(overwriteThis, here, segment.last(), onA, newsegment.iterator(), aref, bref);
			segment = newsegment;

			onA ^= segment.size() % 2 != 0;
			iter = (onA?sortedByA:sortedByB).tailSet(segment).iterator();
			iter.next();
		}

		int tailStart = onA?segment.last().x:segment.last().y;
		int tailEnd = onA?aref.size():bref.size();
		if (tailEnd>tailStart) overwriteThis.paste(here, onA?aref:bref, tailStart, tailEnd);
		here += tailEnd - tailStart;
		if (here < overwriteThis.size()) overwriteThis.delete(here, overwriteThis.size());
	}

	/**
	 * Cross over two genomes according to a not-necessarily-ordered set of crossover points generated.
	 */
	static <G extends VarLengthGenome<G>> void performUnorderedNPoint(List<SortedSet<IntPair>> crosses, G a, G b) {
//		System.err.println(crosses.size() + " - " + crosses.stream().mapToInt(Collection::size).sum());
		if (crosses.stream().allMatch(Collection::isEmpty)) return;

		SortedSet<SortedSet<IntPair>> sortedByA = new TreeSet<>(Comparator.comparing(SortedSet::first, Comparator.naturalOrder()));
		SortedSet<SortedSet<IntPair>> sortedByB = new TreeSet<>(Comparator.comparing(SortedSet::first, Comparator.comparing(IntPair::flip)));
		crosses.stream()
				.filter(segment -> !segment.isEmpty())
				.forEach(sortedByA::add);
		crosses.stream()
				.filter(segment -> !segment.isEmpty())
				.forEach(sortedByB::add);

		G aref = a.copy();
		G bref = b.copy();

		crossNsegments(a, 0, true, sortedByA, sortedByB, aref, bref);
		crossNsegments(b, 0, false, sortedByA, sortedByB, aref, bref);
	}

	/**
	 * Same as {@link #performUnorderedNPoint(List, VarLengthGenome, VarLengthGenome)}, but automatically assigns the given pairs to contiguous sections.
	 * This is necessary to avoid ambiguity in the case of overlapping points (when the same locus is coupled with two others, which should be traversed first?)
	 */
	static <G extends VarLengthGenome<G>> void performUnorderedNPoint(SortedSet<IntPair> crosses, G a, G b) {
//		performUnorderedNPoint(crosses.stream().map(pair->new TreeSet<>(List.of(pair))).collect(Collectors.toList()), a, b);

		List<SortedSet<IntPair>> segments = divideIntoSegments(crosses);
		performUnorderedNPoint(segments, a, b);

	}

	// Actually want this to be protected because it's only an implementation but Java doesn't allow that for some reason
	static List<SortedSet<IntPair>> divideIntoSegments(SortedSet<IntPair> crosses) {
		SortedSet<IntPair> sortedByB = new TreeSet<>(Comparator.comparing(IntPair::flip));
		sortedByB.addAll(crosses);

		Iterator<IntPair> iterByA = crosses.iterator();
		Iterator<IntPair> iterByB = Collections.emptyIterator();
		List<SortedSet<IntPair>> segments = new LinkedList<>();
		SortedSet<IntPair> segment = new TreeSet<>();

		while (iterByA.hasNext()) {
			IntPair nextPairByA = iterByA.next();

			if (!iterByB.hasNext() || !iterByB.next().equals(nextPairByA)) {
				if (!segment.isEmpty()) segments.add(segment);
				segment = new TreeSet<>();
				iterByB = sortedByB.iterator();
				while (!iterByB.next().equals(nextPairByA)) {};
			}

			segment.add(nextPairByA);
		}
		if (!segment.isEmpty()) segments.add(segment);

		return segments;
	}

}
