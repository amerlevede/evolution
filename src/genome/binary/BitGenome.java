package genome.binary;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;

import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;

/**
 * Basic implementation of {@link BinaryGenome}.
 */
public abstract class BitGenome implements BinaryGenome<BitGenome> {

    protected Optional<int[]> kmp = Optional.empty();

	public static <G extends BinaryGenome<G>> BitGenome of(G g) {
        Assert.notNull(g);
        BitGenome result = ConcreteBitGenome.preserve(2*g.size(), g.size());
        for (int i=0; i<g.size(); i++) {
            result.set(i, g.get(i));
        }
        return result;
    }

    public static BitGenome of(boolean... bits) {
    	return new ConcreteBitGenome(bits.length, Arrays.copyOf(bits, bits.length*2));
    }
    
    public static Optional<BitGenome> read(String bits) {
    	BitGenome result = ConcreteBitGenome.preserve(2*bits.length(), bits.length());
    	for (int i=0; i<bits.length(); i++) {
    		char c = bits.charAt(i);
    		if (c != '0' && c != '1') return Optional.empty();
    		result.set(i, c == '1');
    	}
    	return Optional.of(result);
    }
    
    public static BitGenome readUnsafe(String bits) {
    	return read(bits).get();
    }

    public static <G extends BinaryGenome<G>> BitGenome not(G g) {
	    Assert.notNull(g);
	    BitGenome result = ConcreteBitGenome.preserve(2*g.size(), g.size());
	    for (int i=0; i<g.size(); i++) {
	        result.set(i, !g.get(i));
	    }
	    return result;
	}

    public static BitGenome getRandom(Random rng, DiscreteDistribution sizedist) {
    	int size = sizedist.applyAsInt(rng);
        BitGenome g = BitGenome.zeroes(size);
        for (int i = 0; i < size; i++) {
            g.set(i, rng.nextBoolean());
        }
        return g;
    }

	public static CategoricalDistribution<BitGenome> random(DiscreteDistribution sizedist) {
        return (rng) -> BitGenome.getRandom(rng, sizedist);
    }

    public static CategoricalDistribution<BitGenome> random(int size) {
        return BitGenome.random((ignored) -> size);
    }

    public static BitGenome zeroes(int size) {
        Assert.length1(size);
        return ConcreteBitGenome.preserve(2*size, size);
    }

    public static BitGenome ones(int size) {
    	Assert.length1(size);
    	BitGenome result = ConcreteBitGenome.preserve(2*size, size);
    	for (int i=0; i<size; i++) {
    		result.set(i, true);
    	}
    	return result;
    }

    /**
	 * Concatenate a list of genomes.
	 * May throw an error because zero-length genomes are not allowed; use {@link #append(Iterable)} as a "safe" alternative if possible.
	 * @throws IllegalArgumentException if pieces is length 0 (zero-length genomes are not allowed).
	 */
	public static BitGenome cat(Iterable<BitGenome> pieces) {
		Iterator<BitGenome> iter = pieces.iterator();
		if (!iter.hasNext()) throw new IllegalArgumentException("Concatenation must contain at least one genome");
		BitGenome result = iter.next().copy(); // Set result to copy of first element
		result.append(() -> iter); // Append other elements
		return result;
	}

	@Override
    public BitGenome toBitGenome() {
    	return this;
    }

    /**
	 * Create a Genome with bits that represent the given integer value in "reflective Gray code" representation.
	 * @return a Genome g for which g.decodeIntGray() == val % 2^len
	 * @see #encodeIntBase(int, int)
	 */
	public static BitGenome encodeIntGray(int val, int len) {
		return encodeIntBase(val ^ (val >> 1), len);
	}

	/**
	 * Create a Genome with bits that represent the given integer value in "base 2" representation.
	 * @param val - The value the resulting genome should represent.
	 * @param len - The length of the resulting genome. If len is smaller than the number of bits necessary to represent val, the "extra" bits will be ignored.
	 * @return a Genome g for which g.decodeIntBase() == val % 2^len
	 * @throws IllegalArgumentException - when len < 0 or >= 32 (the number of unsigned bits in Java ints).
	 */
	public static BitGenome encodeIntBase(int val, int len) {
		Assert.length1(len);
		if (len >= 32) throw new IllegalArgumentException("Cannot encode int to Genome longer than 32 bits");
		BigInteger bits = BigInteger.valueOf(val);
		BitGenome result = BitGenome.zeroes(len);
		for (int i=0; i<Math.min(32, len); i++) {
			result.set(i, bits.testBit(len-i-1));
		}
		return result;
	}

	/**
     * Copy a section of this Genome.
	 * Similar to {@link #view(int, int)} except this will copy the data, so the result will not expire.
     */
    @Override
	public BitGenome copy(int inclusiveStart, int exclusiveEnd) {
        return BitGenome.of(this.view(inclusiveStart, exclusiveEnd));
    }

    /**
	 * The parent Genome that this Genome refers to.
	 * I.e. the parent genome in the case of a View (or the parent's parent if this is a view referring to another view, etc.).
	 */
	@Override
	public BitGenome refersTo() {
		return this;
	}

	@Override
	public BitGenome view(int inclusiveStart, int exclusiveEnd) {
		return this.new View(inclusiveStart, exclusiveEnd, false);
	}

	@Override
	public BitGenome reversedView() {
		return this.new View(0, this.size(), true);
	}

	/**
	 * An unmodifiable view of a Genome.
	 * It allows a genome or subset of a genome to be inspected without providing access to modifying that genome or having to copy its data.
	 * Throws IllegalStateException when accessing a genome after it has been changed, because insertions and deletions could make the part of the genome being viewed undefined.
	 */
	class View extends BitGenome {

	    private final int start;
	    private final int len;
	    private final boolean reversed;

	    protected View(int inclusiveStart, int exclusiveEnd, boolean reversed) {
	        Assert.splice(BitGenome.this, inclusiveStart, exclusiveEnd);
	        this.start = inclusiveStart;
	        this.len = exclusiveEnd - inclusiveStart;
	        this.reversed = reversed;
	    }

	    @Override
	    public BitGenome view() {
	    	return this;
	    }

	    @Override
	    public BitGenome reversedView() {
	    	return BitGenome.this.new View(start, start+len, !this.reversed);
	    }

	    @Override
	    public BitGenome refersTo() {
	        return BitGenome.this.refersTo();
	    }

	    @Override
	    public int size() {
	        return len;
	    }

	    @Override
	    public boolean get(int index) {
	        Assert.index(this, index);
	        return reversed
	    		? BitGenome.this.get(start+len-index-1)
	    		: BitGenome.this.get(start+index);
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
	    public void swap(int i, int j) {
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
	    public void insert(int index, BitGenome g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void insertRandom(Random rng, int index, int length) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void append(BitGenome g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void paste(int index, BitGenome g, int inclusiveStart, int exclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	    @Override
	    public void replace(int thisInclusiveStart, int thisExclusiveEnd, BitGenome g, int gInclusiveStart,	int gExclusiveEnd) {
	    	Assert.unmodifiable();
	    }

	}

}