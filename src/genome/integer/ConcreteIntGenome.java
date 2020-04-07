package genome.integer;

import java.util.Arrays;
import java.util.stream.IntStream;

import util.Assert;

/**
 * Concrete implementation of {@link IntegerGenome}, as well as {@link Permutation}.
 * The fields and methods in this class are not implemented in IntegerGenome because they do not apply to Views.
 * This class is not accessible outside this package, so will be interfaced with through one of those two public APIs.
 * Members of this class are only expected to adhere to the {@link Permutation} contract when they are accessed through that interface (see {@link #isPermutation()} and {@link IntegerGenome#permutationView()}).
 * Using the same underlying implementation for {@link IntGenome} and {@link Permutation} avoids some code duplication and potential data copying.
 */
class ConcreteIntGenome extends IntGenome implements Permutation {

	int[] bits;
	int size;

	ConcreteIntGenome(int size, int[] bits) {
        this.size = size;
        this.bits = bits;
    }

    /**
     * Create a new BitGenome of zeroes, preserving the given number of bits in the internal representation.
     */
    static ConcreteIntGenome preserve(int prelen, int size) {
        Assert.length1(size);
        return new ConcreteIntGenome(size, new int[prelen]);
    }

	@Override
	public int get(int i) {
		Assert.index(this, i);
		return this.bits[i];
	}

	@Override
	public void set(int i, int val) {
		Assert.index(this, i);
		this.bits[i] = val;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Permutation permutationView() {
		return this;
	}

    @Override
    public void shiftRight(int n) {
        if (n < 0) {
            this.shiftLeft(-n);
        } else if (n > 0) {
            if (n < this.size()) System.arraycopy(this.bits, 0, this.bits, n, this.size()-n);
            Arrays.fill(this.bits, 0, Math.min(this.size, n), 0);
        }
    }

    @Override
    public void shiftLeft(int n) {
        if (n < 0) {
            this.shiftRight(-n);
        } else if (n > 0) {
            if (n < this.size()) System.arraycopy(this.bits, n, this.bits, 0, this.size()-n);
            Arrays.fill(this.bits, Math.max(0, this.size()-n), this.size(), 0);
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
    public void insert(int index, IntGenome g, int inclusiveStart, int exclusiveEnd) {
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
        int[] newbits;
        if (newsize > this.bits.length) {
            newbits = new int[2*newsize];
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
    public void paste(int index, IntGenome g, int inclusiveStart, int exclusiveEnd) {
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
        int[] newbits;
        if (newsize > this.bits.length) {
            newbits = new int[2*newsize];
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
	public IntStream stream() {
		return super.stream();
	}

	@Override
	public String toString() {
		return IntegerGenome.toString(this);
	}
}
