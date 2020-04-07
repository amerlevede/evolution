package crossover.permutation;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import crossover.CrossoverRule;
import genome.integer.IntegerGenome;
import genome.integer.Permutation;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Implementation of perfect crossover for undirected edges.
 * 
 * @author adriaan
 *
 */
public final class PerfectEdgeCross {

	private PerfectEdgeCross() {
		Assert.utilityClass();
	}

	/**
	 * Random edge preserving crossover.
	 * Given two parent permutations, modify the first one so that it has only edges that appear in a or b. The offspring (modified a) is drawn uniformly from the distribution of all possible offspring adhering to this constraint, approximately.
	 * @param trialsf - The number of trials before giving up and returning a copy of a random parent. Can be a function of the number of cycles.
	 */
	public static <G extends IntegerGenome<G>> CrossoverRule<G> crossover(IntUnaryOperator trialsf) {
		return (rng) -> (a, b) -> perform(trialsf, rng, a ,b);
	}

	/**
	 * Iterate over a list of subsets. Each subset is identified by its member function {@link PerfectEdgeCross.UpdatingSubsets#test(int)}.
	 * Not implemented as an Iterator or Stream of IntPredicate or Boolean[] because the different subsets may represent the same underlying data that mutates with each "update".
	 */
	abstract static class UpdatingSubsets {
		/** Check if element i is part of the subset */
		abstract boolean test(int i);
		/** Update the subset, modifying the behavior of test() */
		abstract void next();
	}

	/** @see PerfectEdgeCross#crossover() */
	public static <G extends IntegerGenome<G>> int performAndGetN(IntUnaryOperator trialsf, Random rng, G a, G b) {
		int size = a.size();
		if (b.size() != size) throw new IllegalArgumentException();

		Permutation ea = a.permutationView().edgeTransform();
		Permutation eb = b.permutationView().edgeTransform();
		Permutation eainv = ea.inverse();
		Permutation mapping = Permutation.action(size, eb, eainv);

		int[] cycles = new int[size];
		int cyclesN = mapping.nonSingletonCyclesAndGetN(cycles);

		if (cyclesN <= 1) {
			return 0;
		}

		UpdatingSubsets randomCross = randomCrosses(rng, trialsf.applyAsInt(cyclesN), cyclesN);
		int len=0;
		int tryCounter = 0;
		while (len<size) {
			tryCounter++;
			randomCross.next();
			len = overwriteGenomeWithReverseEdgeTransform(a, ea, eb, cycles, randomCross::test);
		}
		return tryCounter;
	}

	public static <G extends IntegerGenome<G>> void perform(IntUnaryOperator trialsf, Random rng, G a, G b) {
		performAndGetN(trialsf, rng, a, b);
	}

	/**
	 * Variant of the crossover that reports run time statistics to stderr.
	 * Max. number of trials is 10 000.
	 * @see #crossover(IntUnaryOperator)
	 */
	public static <G extends IntegerGenome<G>> CrossoverRule<G> loudCrossover() {
		return (rng) -> (a, b) -> {
			G aref = a.copy();
			int trials = performAndGetN(i->10000, rng, a, b);
			Permutation ea = a.permutationView().edgeTransform();
			Permutation earef = aref.permutationView().edgeTransform();
			Permutation eb = b.permutationView().edgeTransform();
			boolean trivialCross = 
					Permutation.equals(ea, earef)
				 || Permutation.equals(ea, eb);
			System.err.println(trials+"\t"+trivialCross);
		};
	}

	/**
	 * UpdatingSubsets that attempts to generate possible crossovers in random order (avoiding the trivial crossovers if possible).
	 */
	static UpdatingSubsets randomCrosses(Random rng, int trials, int numOfCycles) {
		int maxval = 1<<numOfCycles;

		if (numOfCycles < 5 && trials >= maxval / 4) {
			// NOTE: now allowing parents to be crossover results to avoid peaks in try distribution
//			int[] candidates= IntStream.range(1, maxval-1).toArray();
			int[] candidates= IntStream.range(0, maxval).toArray();
			DiscreteDistribution.shuffleArray(rng, candidates);
			PrimitiveIterator.OfInt iter = IntStream.of(candidates).iterator();
			UpdatingSubsets result = new UpdatingSubsets() {
				boolean[] subset = new boolean[numOfCycles];
				@Override
				boolean test(int i) {
					return subset[i];
				}
				@Override
				void next() {
					int currentIndicator = iter.hasNext() ? iter.next() : (rng.nextBoolean()?maxval:0);
					for (int i=0; i<numOfCycles; i++) {
						subset[i] = currentIndicator%2==0;
						currentIndicator>>=1;
					}
				}
			};
			return result;

		} else if (numOfCycles < 15) {
//			PrimitiveIterator.OfInt iter = rng.ints(1, maxval-1).distinct().limit(trials).iterator();
			PrimitiveIterator.OfInt iter = rng.ints(0, maxval).distinct().limit(trials).iterator();
			UpdatingSubsets result = new UpdatingSubsets() {
				boolean[] subset = new boolean[numOfCycles];
				@Override
				boolean test(int i) {
					return subset[i];
				}
				@Override
				void next() {
					int currentIndicator = iter.hasNext() ? iter.next() : (rng.nextBoolean()?maxval:0);
					for (int i=0; i<numOfCycles; i++) {
						subset[i] = currentIndicator%2==0;
						currentIndicator>>=1;
					}
				}
			};
			return result;

		} else {
			UpdatingSubsets result = new UpdatingSubsets() {
				int counter = 0;
				boolean[] subset = new boolean[numOfCycles];
				@Override
				boolean test(int i) {
					return subset[i];
				}
				@Override
				void next() {
					counter++;
					if (counter <= trials) {
						for (int i=0; i<numOfCycles; i++) subset[i] = rng.nextBoolean();
					} else {
						boolean bool = rng.nextBoolean();
						Arrays.fill(subset, bool);
					}
				}
			};
			return result;
		}
	}

	/**
	 * Overwrite the given genome with the reverse edge transform of the combined permutations ea/eb, where each i is picked from either based on the cyclechoice.test(cycles[i]-1)
	 */
	static <G extends IntegerGenome<G>> int overwriteGenomeWithReverseEdgeTransform(G a, Permutation ea, Permutation eb, int[] cycles, IntPredicate cyclechoice) {
		int len=0;
		int i=0;
		do {
			a.set(len++, i);
			i = cyclechoice.test(cycles[i]-1) ? ea.get(i) : eb.get(i);
		} while (i != 0);
		return len;
	}

}
