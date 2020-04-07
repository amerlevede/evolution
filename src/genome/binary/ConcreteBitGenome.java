package genome.binary;

import java.util.Arrays;
import java.util.Random;

import util.Assert;

/**
 * Concrete base implementation of {@link BitGenome}.
 * The fields and methods in this class are not implemented in BitGenome because they do not apply to Views.
 * @author adriaan
 */
class ConcreteBitGenome extends BitGenome {

    /**
     * The size / length of this genome.
     * This is kept as a separate variable because BigInteger does not care about trailing zeroes.
     * May never be zero or negative.
     */
    int size;

    /**
     * Bit sequence of this genome.
     * May be longer than this.size(). Values of bits after index this.size() are unspecified.
     */
    boolean[] bits;

    ConcreteBitGenome(int size, boolean[] bits) {
        this.size = size;
        this.bits = bits;
    }

    /**
     * Create a new BitGenome of zeroes, preserving the given number of bits in the internal representation.
     * @param prelen
     * @param size
     * @return
     */
    static ConcreteBitGenome preserve(int prelen, int size) {
        Assert.length1(size);
        return new ConcreteBitGenome(size, new boolean[prelen]);
    }


    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean get(int index) throws IndexOutOfBoundsException {
        Assert.index(this, index);
        return this.bits[index];
    }

    @Override
    public void set(int index, boolean val) {
        Assert.index(this, index);
        this.bits[index] = val;
    }

    @Override
    public void flip(int index) {
        Assert.index(this, index);
        this.bits[index] = !this.get(index);
    }

    @Override
    public void shiftRight(int n) {
        if (n < 0) {
            this.shiftLeft(-n);
        } else if (n > 0) {
            if (n < this.size()) System.arraycopy(this.bits, 0, this.bits, n, this.size()-n);
            Arrays.fill(this.bits, 0, Math.min(this.size, n), false);
        }
    }

    @Override
    public void shiftLeft(int n) {
        if (n < 0) {
            this.shiftRight(-n);
        } else if (n > 0) {
            if (n < this.size()) System.arraycopy(this.bits, n, this.bits, 0, this.size()-n);
            Arrays.fill(this.bits, Math.max(0, this.size()-n), this.size(), false);
        }
    }

    @Override
    public void delete(int inclusiveStart, int exclusiveEnd) {
        Assert.splice(this, inclusiveStart, exclusiveEnd);
        int len = exclusiveEnd - inclusiveStart;
        if (len >= this.size()) throw new IllegalArgumentException("Cannot delete whole genome");

        System.arraycopy(this.bits, exclusiveEnd, this.bits, inclusiveStart, this.size()-exclusiveEnd);
        this.size = this.size() - len;
    }

    @Override
    public void insert(int index, BitGenome g, int inclusiveStart, int exclusiveEnd) {
        Assert.splice(this, index);
        Assert.notNull(g);
        Assert.splice(g, inclusiveStart, exclusiveEnd);

        int len = exclusiveEnd-inclusiveStart;
        int newsize = this.size() + len;

        // Handle case when read and write Genomes refer to the same data
        if (this.refersTo() == g.refersTo()) {
            if (this.bits.length < newsize) {
                // Handle as usual, data won't be overwritten while reading because it has to go to a larger array
            } else {
                // Give up, just copy data.
                // May be avoided by changing order of reads and writes but is especially difficult with Views.
                this.insert(index, g.copy(inclusiveStart, exclusiveEnd));
                return;
            }
        }

        // Expand internal boolean array if not big enough to accommodate new bits
        boolean[] newbits;
        if (newsize > this.bits.length) {
            newbits = new boolean[2*newsize];
            System.arraycopy(this.bits, 0, newbits, 0, index);
        } else {
            newbits = this.bits;
        }

        // Shift tail to the right
        System.arraycopy(this.bits, index, newbits, index+len, this.size()-index);

        // Copy target bits in gap
        for (int i=0; i<len; i++) {
            newbits[index+i] = g.get(inclusiveStart+i);
        }

        this.bits = newbits;
        this.size = newsize;
    }

    @Override
    public void paste(int index, BitGenome g, int inclusiveStart, int exclusiveEnd) {
        Assert.splice(this, index);
        Assert.notNull(g);
        Assert.splice(g, inclusiveStart, exclusiveEnd);

        int len = exclusiveEnd-inclusiveStart;
        int newsize = Math.max(index+len, this.size());

        // Handle case when read and write Genomes refer to the same data
        if (this.refersTo() == g.refersTo()) {
            if (newsize > this.bits.length) {
                // Handle as usual, data won't be overwritten while reading because it has to go to a larger array
            } else if (this == g) {
                // Just write data directly
                System.arraycopy(this.bits, inclusiveStart, this.bits, index, len);
                return;
            } else {
                // Give up, just copy data.
                this.paste(index, g.copy(inclusiveStart, exclusiveEnd), 0, exclusiveEnd - inclusiveStart);
                return;
            }
        }

        // Expand internal boolean array if not big enough to accommodate new bits
        boolean[] newbits;
        if (newsize > this.bits.length) {
            newbits = new boolean[2*newsize];
            System.arraycopy(this.bits, 0, newbits, 0, index);
        } else {
            newbits = this.bits;
        }

        // Copy target bits
        for (int i=0; i<len; i++) {
            newbits[index+i] = g.get(inclusiveStart+i);
        }

        this.bits = newbits;
        this.size = newsize;
    }

	@Override
	public void insertRandom(Random rng, int index, int length) {
		BitGenome toInsert = BitGenome.getRandom(rng, (ignore) -> length);
		this.insert(index, toInsert);
	}

	@Override
	public String toString() {
		return BinaryGenome.toString(this);
	}

}
