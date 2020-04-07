package genome.binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import genome.GenomeWithHistory;
import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;

/**
 * Implementation of {@link BinaryGenome} and also {@link GenomeWithHistory}.
 */
public abstract class BitGenomeWithHistory implements BinaryGenome<BitGenomeWithHistory>, GenomeWithHistory<BitGenomeWithHistory> {

    /**
     * Create a new GenomeWithHistory with the same bits as the given Genome.
     * If the parameter is also a GenomeWithHistory, its history will *not* be copied (use GenomeWithHistory.copy() for that).
     */
    public static <G extends BinaryGenome<G>> BitGenomeWithHistory of(G g) {
        Assert.notNull(g);

        List<BoolWithHistory> thisbits = new ArrayList<>();
        for (int i=0; i<g.size(); i++) {
        	thisbits.add(new BoolWithHistory(g.get(i)));
        }
        return new ConcreteBitGenomeWithHistory(thisbits);
    }

    /**
     * Create a new GenomeWithHistory from a list of bits.
     * The bit history will be fresh unique identifiers.
     */
    public static BitGenomeWithHistory of(List<Boolean> bits) {
        Assert.notNull(bits);
        Assert.length1(bits.size());

        List<BoolWithHistory> thisbits = bits.stream().map(BoolWithHistory::new).collect(Collectors.toList());
        return new ConcreteBitGenomeWithHistory(thisbits);
    }

    /** See {@link #of(List)} */
    public static BitGenomeWithHistory of(boolean... bits) {
        Assert.notNull(bits);
        Assert.length1(bits.length);

        List<BoolWithHistory> thisbits = new ArrayList<>();
        for (int i=0; i<bits.length; i++) {
            thisbits.add(new BoolWithHistory(bits[i]));
        }
        return new ConcreteBitGenomeWithHistory(thisbits);
    }

    /**
     * A GenomeWithHistory of the given size containing only zeroes.
	 * The bit history will be fresh unique identifiers.
     */
    public static BitGenomeWithHistory zeroes(int size) {
        Assert.length1(size);

        return BitGenomeWithHistory.of(new boolean[size]);
    }

    public static BitGenomeWithHistory ones(int size) {
    	Assert.length1(size);;
    	boolean[] result = new boolean[size];
    	Arrays.fill(result, true);
    	return BitGenomeWithHistory.of(result);
    }

    public static BitGenomeWithHistory getRandom(Random rng, DiscreteDistribution ndist) {
    	return BitGenomeWithHistory.of(BitGenome.random(ndist).apply(rng));
    }

    public static CategoricalDistribution<? extends BitGenomeWithHistory> random(DiscreteDistribution ndist) {
        return (rng) -> BitGenomeWithHistory.getRandom(rng, ndist);
    }

    public static CategoricalDistribution<? extends BitGenomeWithHistory> random(int n) {
        return BitGenomeWithHistory.random((ignored) -> n);
    }

    @Override
    public BitGenome toBitGenome() {
    	return BitGenome.of(this);
    }

    /**
     * Get all the identifiers in this genome
     */
    public abstract LongStream ids();

    /**
     * Get the identifier for a given index that holds information of this bit's history.
     */
    @Override
	public abstract long getId(int index);

    @Override
    public void insertRandom(Random rng, int index, int length) {
    	BitGenomeWithHistory toInsert = BitGenomeWithHistory.getRandom(rng, (ignore) -> length);
    	this.insert(index, toInsert);
    }

    @Override
    public BitGenomeWithHistory copy(int inclusiveStart, int exclusiveEnd) {
        int len = exclusiveEnd-inclusiveStart;
        ArrayList<BoolWithHistory> newbits = new ArrayList<>(len);
        for (int i=0; i<len; i++) {
            newbits.add(new BoolWithHistory(this.get(inclusiveStart+i), this.getId(inclusiveStart+i)));
        }
        return new ConcreteBitGenomeWithHistory(newbits);
    }

    @Override
    public BitGenomeWithHistory copy() {
        return this.copy(0, this.size());
    }


    /**
    * Class of boolean values with a unique identifier.
    * New instances are initialized with a new unique identifier, but the identifier can be copied.
    */
    public static class BoolWithHistory {

        boolean b;
        public final long id;

        private static long newid = 0;

        protected BoolWithHistory(boolean b, long id) {
            this.b = b;
            this.id = id;
        }

        protected BoolWithHistory(boolean b) {
            this(b, newid++);
        }

        protected BoolWithHistory copy() {
            return new BoolWithHistory(this.b, this.id);
        }

        @Override
        public String toString() {
            return this.b ? "1" : "0";
        }

    }

    @Override
	public BitGenomeWithHistory view(int inclusiveStart, int exclusiveEnd) {
	    return this.new View(inclusiveStart, exclusiveEnd, false);
	}

    @Override
    public BitGenomeWithHistory reversedView() {
    	return this.new View(0, this.size(), true);
    }

	@Override
    public abstract BitGenomeWithHistory refersTo();

	/**
	 * An unmodifiable view of a Genome.
	 * It allows a genome or subset of a genome to be inspected without providing access to modifying that genome or having to copy its data.
	 * Throws IllegalStateException when accessing a genome after it has been changed, because insertions and deletions could make the part of the genome being viewed undefined.
	 */
	class View extends BitGenomeWithHistory {

	    private final int start;
	    private final int len;
	    private final boolean reversed;

	    protected View(int inclusiveStart, int exclusiveEnd, boolean reversed) {
	        Assert.splice(BitGenomeWithHistory.this, inclusiveStart, exclusiveEnd);
	        this.start = inclusiveStart;
	        this.len = exclusiveEnd - inclusiveStart;
	        this.reversed = reversed;
	    }

	    @Override
	    public BitGenomeWithHistory view() {
	    	return this;
	    }

	    @Override
	    public BitGenomeWithHistory reversedView() {
	    	return BitGenomeWithHistory.this.new View(start, start+len, !this.reversed);
	    }

	    @Override
	    public BitGenomeWithHistory refersTo() {
	        return BitGenomeWithHistory.this.refersTo();
	    }

	    @Override
	    public int size() {
	        return len;
	    }

	    @Override
	    public boolean get(int index) {
	        Assert.index(this, index);
	        return reversed
	    		? BitGenomeWithHistory.this.get(start+len-index-1)
	    		: BitGenomeWithHistory.this.get(start+index);
	    }

	    @Override
	    public LongStream ids() {
	    	return BitGenomeWithHistory.this.ids();
	    }

        @Override
        public long getId(int index) {
            Assert.index(this, index);
            return BitGenomeWithHistory.this.getId(index + start);
        }

        @Override
        public IntStream homologs(long id) {
            return BitGenomeWithHistory.this.homologs(id)
            		.filter((i) -> i >= this.start && i < this.start+len);
        }

	    @Override
	    public void set(int index, boolean val) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void flip(int index) {
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
	    public void insert(int index, BitGenomeWithHistory g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void insertRandom(Random rng, int index, int length) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void append(BitGenomeWithHistory g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void paste(int index, BitGenomeWithHistory g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void replace(int thisInclusiveStart, int thisExclusiveEnd, BitGenomeWithHistory g, int gInclusiveStart,	int gExclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public String toString() {
	    	return BinaryGenome.toString(this);
	    }

	}

}
