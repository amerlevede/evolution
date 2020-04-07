package genome.binary;

import java.util.Random;

import genome.Genome;
import genome.VarLengthGenome;

/**
 * {@link Genome} containing a variable-length string of binary values.
 */
public interface BinaryGenome<G extends BinaryGenome<G>> extends VarLengthGenome<G> {

    /**
	 * Get the value of the genome at given index.
	 */
	boolean get(int index);

	/**
	 * Set a bit of this genome.
	 * @param index
	 * @param val
	 */
	void set(int index, boolean val);

	/**
	 * Flip (modify) the value of the genome at a given index.
	 */
	default void flip(int index) {
		this.set(index, !this.get(index));
	}

	@Override
    default void swap(int i, int j) {
    	boolean tmp = this.get(i);
    	this.set(i, this.get(j));
    	this.set(j, tmp);
    }

	/**
	 * Get a version of this BitGenome as a BitGenome (possibly a View)
	 */
	BitGenome toBitGenome();

    @Override
    default boolean sameAt(int thisIndex, G that, int thatIndex) {
    	return this.get(thisIndex) == that.get(thatIndex);
    }

    void insertRandom(Random rng, int index, int length);

	/**
     * Decode this {@link #Genome()} to an int using binary base code.
     * Bits with index higher than 31 are ignored (sign bit is never set).
     */
    default int decodeIntBase() {
    	int result = 0;
    	int len = Math.min(31, this.size());
    	for (int i=0; i<len; i++) {
    		result <<= 1;
    		if (this.get(i)) result = result + 1;
    	}
    	return result;
    }

    /**
     * Decode this {@link #Genome()} using binary reflected Gray code.
     * Bits with index higher than 31 are ignored (sign bit is never set).
     */
    default int decodeIntGray() {
    	int result = 0;
    	int len = Math.min(31, this.size());

    	boolean lastset = this.get(0);
    	if (lastset) result = 1;
    	for (int i=1; i<len; i++) {
    		result <<= 1;
    		lastset = this.get(i) ^ lastset;
    		if (lastset) result = result + 1;
    	}
    	return result;
    }

	static <G extends BinaryGenome<G>> String toString(G g) {
		StringBuilder result = new StringBuilder();
		for (int i=0; i<g.size(); i++) result.append(g.get(i)?'1':'0');
		return result.toString();
	}
}
