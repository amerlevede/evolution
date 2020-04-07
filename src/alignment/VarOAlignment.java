package alignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import genome.LinearGenome;
import util.IntPair;

/**
 * Alignment object where homologous pairs are not expected to be present in sequence.
 * The alignment is memorized as a list of "segments" that are sequential on their own.
 * @see Alignment
 * @author adriaan
 */
public class VarOAlignment<G extends LinearGenome<G>> {

	private final List<SortedSet<IntPair>> segments;
	private final G a;
	private final G b;

	// TODO implement some kind of check of soundness for the segments
	public VarOAlignment(List<SortedSet<IntPair>> segments, G a, G b) {
		this.a = a;
		this.b = b;
		segments = segments.stream().filter((pairs) -> !pairs.isEmpty()).collect(Collectors.toList());
		segments.sort(Comparator.comparing(SortedSet::first));
		this.segments = Collections.unmodifiableList(segments);
	}

	public G getA () {
		return this.a;
	}

	public G getB() {
		return this.b;
	}

	/**
	 * Return a simplified version of this alignment.
	 * This means that any consecutive segments (with no other segments in between on either of the two genomes) will be concatenated.
	 */
	public VarOAlignment<G> simplify() {
		List<Integer> bOrder = IntStream.range(0, this.segments.size()).boxed().collect(Collectors.toCollection(ArrayList::new));
		bOrder.sort((i,j) -> IntPair.compare(this.segments.get(i).first().flip(), this.segments.get(j).first().flip()));

		List<SortedSet<IntPair>> newsegments = new LinkedList<>();
		int i=0;
		while (i<this.segments.size()) {
			int indexOnB = bOrder.indexOf(i);

			SortedSet<IntPair> newsegment = new TreeSet<>();
			int j=0;
			while (indexOnB+j < this.segments.size() && bOrder.get(indexOnB+j) == i+j) {
				newsegment.addAll(this.segments.get(i+j));
				j++;
			}
			newsegments.add(newsegment);

			i += j;
		}

		return new VarOAlignment<>(newsegments, this.getA(), this.getB());
	}

	/**
	 * Get all pairs of this alignment.
	 */
	public SortedSet<IntPair> getPairs() {
		if (this.segments.size() == 1) {
			return Collections.unmodifiableSortedSet(this.segments.get(0));
		} else  {
			SortedSet<IntPair> result = new TreeSet<>();
			for (SortedSet<IntPair> segment : this.getSegments()) {
				result.addAll(segment);
			}
			return result;
		}
	}

	/**
	 * Get all segments of this alignment.
	 * The segments should be individually ordered but not (necessarily) mutually.
	 * Every segment has a length > 0.
	 */
	public List<SortedSet<IntPair>> getSegments() {
		return this.segments;
	}

	private static final char[] SEGMENT_NAMES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%&*+".toCharArray();

	public String displayA() {
		StringBuilder aStr = new StringBuilder(),
				      bStr = new StringBuilder();

		int ai = 0;
		int segmentIndex = -1;
		for (SortedSet<IntPair> segment : this.getSegments()) {
			segmentIndex++;
			char segmentid = segmentIndex < SEGMENT_NAMES.length ? SEGMENT_NAMES[segmentIndex] : '^';
			int bi = segment.first().y;
			// Print head or part between segments
    		while (ai < segment.first().x) {
    			aStr.append(this.getA().view1(ai));
    			bStr.append(' ');
    			ai++;
    		}
    		// Print segment
			for (IntPair pair : segment) {
	    		while (ai < pair.x) {
	    			aStr.append(this.getA().view1(ai));
	    			bStr.append('-');
	    			ai++;
	    		}
	    		while (bi < pair.y) {
	    			aStr.append('-');
	    			bStr.append(this.getB().view1(bi));
	    			bi++;
	    		}
	    		try {
	    			aStr.append(this.getA().view1(ai));
	    			bStr.append(this.getB().view1(bi).sameSequence(this.getA().view1(ai)) ? segmentid : (this.getB().view1(bi)));
	    		} catch (Exception e) {
	    			System.err.println(pair);
	    			throw e;
	    		}

	    		ai++;
	    		bi++;
			}
		}
		// Print tail
		while (ai < a.size()) {
			aStr.append(this.getA().view1(ai));
			bStr.append(' ');
			ai++;
		}

		return aStr + "\n" + bStr;
	}

	public String displayB() {
		StringBuilder aStr = new StringBuilder(),
			          bStr = new StringBuilder();

		List<SortedSet<IntPair>> bOrderedSegments = new ArrayList<>(this.getSegments());
		bOrderedSegments.sort(Comparator.comparing(pairs -> pairs.first().flip()));

		int bi = 0;
		for (SortedSet<IntPair> segment : bOrderedSegments) {
			int segmentIndex = this.getSegments().indexOf(segment);
			char segmentid = segmentIndex < SEGMENT_NAMES.length ? SEGMENT_NAMES[segmentIndex] : '^';
			int ai = segment.first().x;
			// Print head or part between segments
			while (bi < segment.first().y) {
				aStr.append(' ');
				bStr.append(this.getB().view1(bi));
				bi++;
			}
			// Print segment
			for (IntPair pair : segment) {
	  		while (ai < pair.x) {
	  			aStr.append(this.getA().view1(ai));
	  			bStr.append('-');
	  			ai++;
	  		}
	  		while (bi < pair.y) {
	  			aStr.append('-');
	  			bStr.append(this.getB().view1(bi));
	  			bi++;
	  		}
	  		aStr.append(this.getA().view1(ai).sameSequence(this.getB().view1(bi)) ? segmentid : this.getA().view1(ai));
	  		bStr.append(this.getB().view1(bi));
	  		ai++;
	  		bi++;
			}
		}
		// Print tail
		for (; bi < b.size(); bi++) {
			aStr.append(' ');
			bStr.append(this.getB().view1(bi));
		}

		return aStr + "\n" + bStr;
	}

	public String displayA(int linelength) {
    	return Alignment.splitLines(this.displayA(), linelength, new boolean[] {true,false});
	}

	public String displayB(int linelength) {
		return Alignment.splitLines(this.displayB(), linelength, new boolean[] {false,true});
	}

	public String display() {
		return "A:\n" + this.displayA() + "\n" + "B:\n" + this.displayB();
	}

	public String display(int linelength) {
		return "A:\n" + this.displayA(linelength) + "\n" + "B:\n" + this.displayB(linelength);
	}

    @Override
    public String toString() {
    	return this.display();
    }

}
