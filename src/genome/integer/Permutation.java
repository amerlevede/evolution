package genome.integer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import util.CategoricalDistribution;
import util.DiscreteDistribution;
import util.IntPair;

/**
 * An ordering of elements.
 * Users are expected to respect the permutation property of this Genome (see {@link #isValidPermutation(int[])}), and to refrain from modifying underlying data when implementing a Permutation that refers to it to respect the immutability. No internal checks are made in this class.
 */
public interface Permutation extends IntUnaryOperator {

	@Override
	default int applyAsInt(int i) {
		return this.get(i);
	}

	/** Identity permutation. i -> i */
	static Permutation id(int size) {
		return new FunctionPermutation(size, i->i);
	}

	/** Rotation permutation. i -> i+amt */
	static Permutation rot(int size, int amt) {
		return new FunctionPermutation(size, i->(i+amt+size)%size);
	}

	/** Rotation permutation. i -> i+1
	 * @see {@link #rot(int, int)} */
	static Permutation rot(int size) {
		return Permutation.rot(size, 1);
	}

	/** Reversal permutation. i -> size-i-1 */
	static Permutation rev(int size) {
		return new FunctionPermutation(size, i->size-1-i);
	}

	/**
	 * Get a random permutation of the given size.
	 */
	static Permutation getRandom(Random rng, int size) {
		int[] result = IntStream.range(0, size).toArray();
		DiscreteDistribution.shuffleArray(rng, result);
		return new ConcreteIntGenome(size, result);
	}

	/**
	 * A distribution of random permutations.
	 */
	static CategoricalDistribution<Permutation> random(int size) {
		return (rng) -> Permutation.getRandom(rng, size);
	}

	/**
	 * Create a permutation based on the given (immutable) function.
	 */
	static Permutation fromFunction(int size, IntUnaryOperator f) {
		return new FunctionPermutation(size, f);
	}

	/**
	 * Read an integer array as a Permutation.
	 * The caller must ensure that the supplied array defines a valid permutation before calling this.
	 * @see #isValidPermutation(int[])
	 */
	static Permutation fromImage(int[] image) {
		return new ConcreteIntGenome(image.length, image);
	}

	/**
	 * {@link #fromOrdering(IntFunction, int, Comparator)} where values are taken from the array
	 * Note that it is not required that the integers fill the range 0..n. If they do, this function is equivalent to {@link #fromImage(IntegerGenome)} followed by {@link #inverse()}.
	 */
	static Permutation fromOrdering(int[] values) {
		return Permutation.fromOrdering(i->values[i], values.length);
	}

	/** {@link #fromOrdering(IntFunction, int, Comparator)} where values are taken from the array */
	static Permutation fromOrdering(double[] values) {
		return Permutation.fromOrdering(i->values[i], values.length);
	}

	/** {@link #fromOrdering(IntFunction, int, Comparator)} where values are taken from the list */
	static <O> Permutation fromOrdering(List<O> values, Comparator<O> comp) {
		return Permutation.fromOrdering(values::get, values.size(), comp);
	}

	/** {@link #fromOrdering(IntFunction, int, Comparator)} with natural ordering */
	static <O extends Comparable<O>> Permutation fromOrdering(IntFunction<O> values, int range) {
		return Permutation.fromOrdering(values, range, Comparator.naturalOrder());
	}

	/**
	 * Safe way to read an IntegerGenome as a permutation.
	 * If it is known that the argument is a valid permutation ({@link #isValidPermutation(int[])}), it is more efficient to use {@link IntegerGenome#permutationView()} or {@link IntegerGenome#permutationCopy()}.
	 * @see #fromOrdering(IntFunction, int, Comparator)
	 */
	static <G extends IntegerGenome<G>> Permutation fromOrdering(G g) {
		return Permutation.fromOrdering(g::get, g.size());
	}

	/**
	 * A permutation based on an ordering of elements.
	 * @param values - Maps index (which will represent an element in the permutation) to the corresponding value
	 * @param range - Size of the resulting permutation (values function will be evaluated at 0 <= i < range)
	 * @param comp - Comparator that defines ordering of values
	 */
	static <O> Permutation fromOrdering(IntFunction<O> values, int range, Comparator<O> comp) {
		int[] image = IntStream
				.range(0, range)
				.boxed()
				.sorted(Comparator.comparing(values::apply, comp))
				.mapToInt(i->i)
				.toArray();
		return new ConcreteIntGenome(image.length, image);
	}

	static Permutation fromLehmerCode(int[] code) {
		List<Integer> targets = IntStream.range(0, code.length).boxed().collect(Collectors.toCollection(ArrayList::new));
		int[] result = new int[code.length];
		for (int i=0; i<code.length; i++) {
			result[i]=targets.remove(code[i]);
		}
		return Permutation.fromImage(result);
	}

	/**
	 * Check if an integer array can be used as a valid permutation image.
	 * @return True iff each integer in the range [0..image.length-1] occurs exactly once in the array.
	 * @see #isValidPermutation(IntStream, int)
	 */
	static boolean isValidPermutation(int[] image) {
		return Permutation.isValidPermutation(IntStream.of(image), image.length);
	}

	/** @see #isValidPermutation(int[]) */
	static boolean isValidPermutation(IntStream image, int size) {
		return image.filter(i->i>=0&&i<size).distinct().count() == size;
	}

	/** @see #action(int, Stream) */
	static Permutation action(int size, Permutation... factors) {
		return Permutation.action(size, Stream.of(factors));
	}

	static Permutation action(Permutation factor1, Permutation... factors) {
		return Permutation.action(factor1.size(), Stream.concat(Stream.of(factor1), Stream.of(factors)));
	}

	/**
	 * Group action of permutations.
	 * This evaluates the given permutations from left to right.
	 * @param factors - The permutations to combine
	 * @param size - The size of the output permutations. It is required that each supplied permutation has the same size.
	 * @return The product of permutations. If the given stream is empty, returns the identity permutation of the given size.
	 */
	static Permutation action(int size, Stream<Permutation> factors) {
		return Permutation.fromFunction(size, factors.reduce(Permutation.id(size), Permutation::andThen));
	}

	/**
	 * Return the edge transformed permutation.
	 * The edge transform Ea as the property Ea(a(i))=a(i+1), in words, each symbol in is mapped to its successor.
	 * @return The equivalent of Permutation.action(this.size(), this.inverse(), rot(this.size()), this);
	 */
	default Permutation edgeTransform() {
		int[] result = new int[this.size()];
		for (int i=0; i<this.size()-1; i++) result[this.get(i)] = this.get(i+1);
		result[this.get(this.size()-1)] = this.get(0);
		return new ConcreteIntGenome(this.size(), result);
	}

	/**
	 * Return the reverse edge transform permutation.
	 * Like the edge transform, except each symbol is mapped to its predecessor.
	 * @return The equivalent of this.edgeTransform().inverse()
	 */
	default Permutation inverseEdgeTransform() {
		int[] result = new int[this.size()];
		for (int i=1; i<this.size(); i++) result[this.get(i)] = this.get(i-1);
		result[this.get(0)] = this.get(this.size()-1);
		return new ConcreteIntGenome(this.size(), result);
	}

	/**
	 * @return The inverse permutation, so that Permutation.action(this.inverse(), this) === Permutation.id(this.length)
	 */
	default Permutation inverse() {
		int[] bits = new int[this.size()];
		for (int i=0; i<this.size(); i++) bits[this.get(i)] = i;
		return new ConcreteIntGenome(this.size(), bits);
	}

	/** @see #subPermutation(IntStream) */
	default Permutation subPermutation(boolean[] included) {
		if (included.length != this.size()) throw new IllegalArgumentException("Subpermutation array must have same length as permutation");
		return this.subPermutation(i -> included[i]);
	}

	/** @see #subPermutation(IntStream) */
	default Permutation subPermutation(IntPredicate included) {
		return this.subPermutation(IntStream.range(0, this.size()).filter(included));
	}

	/**
	 * Produce a new permutation whose elements are only the provided indices of this, in the same order, and rescaled to the range [0..length).
	 */
	default Permutation subPermutation(IntStream included) {
		int[] order = included.map(this::get).toArray();
		return Permutation.fromOrdering(order);
	}

	/**
	 * Get the image of the integer i under this permutation.
	 */
	int get(int i);

	/**
	 * Get the image of the integer i%this.size() under this permutation.
	 */
	default int getCyclic(int i) {
		if (i<0) {
			return (i + this.size() + (i/this.size())*this.size())%this.size(); // sloppy, this situation doesn't occur often
		} else {
			return this.get(i%this.size());
		}
	}

	/**
	 * The size of this permutation.
	 */
	int size();

	/**
	 * Partition this permutation into disjoint cycles.
	 * @return an array so that cycles()[i] == cycles()[j] iff i and j are in the same cycle according to this permutation. The values in the array are integers from 1 to n (inclusive), where n is the number of cycles.
	 */
	default int[] cycles() {
		int[] result = new int[this.size()];
		this.cyclesAndGetN(result);
		return result;
	}

	/**
	 * Modify the given array as per {@link #cycles()}, and return the number of cycles.
	 */
	default int cyclesAndGetN(int[] cycles) {
		int cycle = 0;
		for (int i=0; i<this.size(); i++) {
			if (cycles[i] == 0) cycle++;
			while (cycles[i] == 0) {
				cycles[i] = cycle;
				i = this.get(i);
			}
		}
		return cycle;
	}

	/**
	 * Partition this permutation into disjoint cycles, ignoring singleton cycles.
	 * Singletons are instead added to the cycle of a neighboring value.
	 * @return an array so that cycles()[i] == cycles()[j] iff i and j are in the same cycle of this permutation OR i is a singleton cycle OR j is a singleton cycle. Cycles are indexed starting from 1.
	 * @see #cycles()
	 */
	default int[] nonSingletonCycles() {
		int[] result = new int[this.size()];
		this.nonSingletonCyclesAndGetN(result);
		return result;
	}

	/**
	 * Modify the given array as per {@link #nonSingletonCycles()}, and return the number of cycles.
	 */
	default int nonSingletonCyclesAndGetN(int[] cycles) {
		int c = 0;
		for (int i=0; i<this.size(); i++) {
			if (cycles[i] == 0) {
				int start = i;
				i = this.get(i);
				if (i == start) {
					cycles[i] = c==0 ? 1 : c;
				} else {
					c++;
					cycles[start] = c;
					while (i != start) {
						cycles[i] = c;
						i = this.get(i);
					}
				}
			}
		}
		return c==0?1:c;
	}

	default IntStream cycle(int i) {
		return IntStream.iterate(this.get(i), this::get).takeWhile(c->c!=i);
	}

	default int[] lehmerCode() {
		Permutation inv = this.inverse();
		int[] result = new int[this.size()];
		for (int i=0; i<this.size(); i++) for (int j=i+1; j<this.size(); j++) {
			if (inv.get(i) > inv.get(j)) result[i]++;
		}
		return result;
	}

	default Stream<IntPair> edges() {
		return IntStream.range(0, this.size()).mapToObj(i->IntPair.of(this.get(i), this.getCyclic(i+1)));
	}

	/**
	 * Conjugation of permutations.
	 * Only valid if the supplied function is also a permutation!
	 */
	@Override
	default Permutation andThen(IntUnaryOperator after) {
		return Permutation.fromFunction(this.size(), i -> after.applyAsInt(this.get(i)));
	}

	/**
	 * Compute the Kendall-tau distance between two IntegerGenomes (interpreted as permutations).
	 * The distance is equal to the number of inversions, or equivalently, the number of swaps between adjacent elements would be needed to transform one genome into the other.
	 */
	static int kendallTau(Permutation a, Permutation b) {
		return (int)inversions(a, b).count();
	}

	static Stream<IntPair> inversions(Permutation a, Permutation b) {
		if (a.size() != b.size()) throw new IllegalArgumentException("Arguments for Kendall tau distance must have same length");

		return IntStream.range(0, a.size())
				.mapToObj(i ->
						IntStream.range(0, i)
						.filter(j -> a.get(i) > a.get(j) ^ b.get(i) > b.get(j))
						.mapToObj(j -> IntPair.of(i,j))
						)
				.flatMap(s->s);
	}

	default IntStream stream() {
		IntStream.Builder builder = IntStream.builder();
		for (int i=0; i<this.size(); i++) {
			builder.accept(this.get(i));
		}
		return builder.build();
	}

	default Permutation uncyclic() {
		return Permutation.fromFunction(this.size()+1, i -> i == 0 ? 0 : this.get(i-1)+1);
	}
	
	public static boolean equals(Permutation a, Permutation b) {
		if (a.size() != b.size()) return false;
		for (int i=0; i<a.size(); i++) if (a.get(i) != b.get(i)) return false;
		return true;
	}

}
