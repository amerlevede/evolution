package genome.integer;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;

/**
 * Minimal implementation of {@link IntegerGenome}.
 */
public abstract class IntGenome implements IntegerGenome<IntGenome> {

	public static <G extends IntegerGenome<G>> IntGenome of(G g) {
		IntGenome result = ConcreteIntGenome.preserve(g.size()*2, g.size());
		for (int i=0; i<g.size(); i++) result.set(i, g.get(i));
		return result;
	}

	public static IntGenome of(int... vals) {
		int[] valscopy = Arrays.copyOf(vals, vals.length*2);
		IntGenome g = new ConcreteIntGenome(vals.length, valscopy);
		return g;
	}

	public static IntGenome of(IntStream stream) {
		return IntGenome.of(stream.toArray());
	}

	public static IntGenome range(int size) {
		return IntGenome.of(IntStream.range(0, size));
	}

	/**
	 * Read the given string as a permutation, using the ascii ordering of characters.
	 */
	public static IntGenome permutationOf(String str) {
		return IntGenome.of(Permutation.fromOrdering(str::charAt, str.length()).stream());
//		if (str.chars().distinct().count() != str.length()) throw new IllegalArgumentException(str);
//		char[] chars = str.toCharArray();
//		Arrays.sort(chars);
//		int[] order = new int[str.length()];
//		for (int i=0; i<str.length(); i++) {
//			int o = Arrays.binarySearch(chars, str.charAt(i));
//			order[i] = o;
//		}
//		IntGenome g = IntGenome.of(order);
//		return g;
	}

	public static IntGenome getRandomPermutation(Random rng, int size) {
		int[] bits = IntStream.range(0, size).toArray();
		DiscreteDistribution.shuffleArray(rng, bits);
		return IntGenome.of(bits);
	}

	public static CategoricalDistribution<IntGenome> randomPermutation(int size) {
		return (rng) -> IntGenome.getRandomPermutation(rng, size);
	}

	/**
     * Copy a section of this Genome.
	 * Similar to {@link #view(int, int)} except this will copy the data, so the result will not expire.
     */
    @Override
	public IntGenome copy(int inclusiveStart, int exclusiveEnd) {
        return IntGenome.of(this.view(inclusiveStart, exclusiveEnd));
    }

    /**
	 * The parent Genome that this Genome refers to.
	 * I.e. the parent genome in the case of a View (or the parent's parent if this is a view referring to another view, etc.).
	 */
	@Override
	public IntGenome refersTo() {
		return this;
	}

	@Override
	public IntGenome view(int inclusiveStart, int exclusiveEnd) {
		return this.new View(inclusiveStart, exclusiveEnd, false);
	}

	@Override
	public IntGenome reversedView() {
		return this.new View(0, this.size(), true);
	}

	/**
	 * An unmodifiable view of a Genome.
	 * It allows a genome or subset of a genome to be inspected without providing access to modifying that genome or having to copy its data.
	 * Throws IllegalStateException when accessing a genome after it has been changed, because insertions and deletions could make the part of the genome being viewed undefined.
	 */
	class View extends IntGenome {

	    private final int start;
	    private final int len;
	    private final boolean reversed;

	    protected View(int inclusiveStart, int exclusiveEnd, boolean reversed) {
	        Assert.splice(IntGenome.this, inclusiveStart, exclusiveEnd);
	        this.start = inclusiveStart;
	        this.len = exclusiveEnd - inclusiveStart;
	        this.reversed = reversed;
	    }

	    @Override
	    public IntGenome view() {
	    	return this;
	    }

	    @Override
	    public IntGenome reversedView() {
	    	return IntGenome.this.new View(start, start+len, !this.reversed);
	    }

	    @Override
	    public IntGenome refersTo() {
	        return IntGenome.this.refersTo();
	    }

	    @Override
	    public int size() {
	        return len;
	    }

	    @Override
	    public int get(int index) {
	        Assert.index(this, index);
	        return reversed
	    		? IntGenome.this.get(start+len-index-1)
	    		: IntGenome.this.get(start+index);
	    }

	    @Override
	    public Permutation permutationView() {
	    	if (this.start == 0 && this.len == IntGenome.this.size()) {
	    	 	if (reversed) {
	    	 		return new FunctionPermutation(this.size(), this::get);
	    	 	} else {
	    	 		return IntGenome.this.permutationView();
	    	 	}
	    	} else {
	    		throw new UnsupportedOperationException("Cannot convert partial view to permutation");
	    	}
	    }

	    @Override
	    public void set(int index, int val) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void shiftLeft(int n) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void shiftRight(int n) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void delete(int index, int length) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void insert(int index, IntGenome g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void append(IntGenome g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void paste(int index, IntGenome g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void replace(int thisInclusiveStart, int thisExclusiveEnd, IntGenome g, int gInclusiveStart,	int gExclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	}

}
