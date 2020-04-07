package genome.integer;

import java.util.stream.IntStream;

import genome.Genome;
import genome.VarLengthGenome;

/**
 * {@link Genome} containing integer values.
 */
public interface IntegerGenome<G extends IntegerGenome<G>> extends VarLengthGenome<G> {

	int get(int i);

	void set(int i, int val);

	default IntStream stream() {
		IntStream.Builder builder = IntStream.builder();
		for (int i=0; i<this.size(); i++) {
			builder.accept(this.get(i));
		}
		return builder.build();
	}

	@Override
	default void swap(int i, int j) {
		int tmp = this.get(i);
		this.set(i, this.get(j));
		this.set(j, tmp);
	}

	/**
	 * Check if this IntegerGenome describes a valid permutation.
	 * @return true iff all values are distinct and filling the interval [0, this.size()).
	 * @see Permutation#isValidPermutation(int[])
	 */
	default boolean isPermutation() {
		return Permutation.isValidPermutation(this.stream(), this.size());
	}

	/**
	 * If this genome is a permutation, return the corresponding {@link Permutation}.
	 * Behavior when this is not a permutation is undefined.
	 * This function may or may not copy data from this genome, its behavior after this genome is modified is undefined. If the Permutation should remain the same even after modifying this, use {@link #permutationCopy()}.
	 * @see #isPermutation()
	 * @note If this is not a proper permutation, it may still be read as a permutation with {@link Permutation#fromOrdering(IntegerGenome)}
	 */
	Permutation permutationView();

	/**
	 * @return this.copy().permutationView()
	 * @see #permutationView()
	 */
	default Permutation permutationCopy() {
		return this.copy().permutationView();
	}

	@Override
    default boolean sameAt(int thisIndex, G that, int thatIndex) {
    	return this.get(thisIndex) == that.get(thatIndex);
    }

    default int findFirst1(int val) {
    	for (int i=0; i<this.size(); i++) if (this.get(i) == val) return i;
    	return -1;
    }

    static <G extends IntegerGenome<G>> String toString(G g) {
    	int len = 2+(int)Math.log10(-0.5+g.stream().max().orElse(0));
    	StringBuilder result = new StringBuilder();
    	result.append("|");
    	g.stream().forEachOrdered(i -> result.append(String.format("% "+len+"d", i)));
    	result.append(" |");
    	return result.toString();
    }

}
