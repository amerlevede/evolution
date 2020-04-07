package crossover.permutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import crossover.CrossoverRule;
import fitness.TravellingSalesman;
import genome.integer.IntegerGenome;
import genome.integer.Permutation;

/**
 * Variation on {@link PerfectEdgeCross} whose offspring are guaranteed to have the highest possible fitness.
 */
public class EdgePreservingOptimalCross {

	/** @see #crossover(fitness.TravellingSalesman.DistanceFunction) */
	public static <G extends IntegerGenome<G>> int performAndGetN(TravellingSalesman.DistanceFunction distf, IntUnaryOperator trialsf, Random rng, G a, G b) {
		int size = a.size();
		if (b.size() != size) throw new IllegalArgumentException();

		Permutation ea = a.permutationView().edgeTransform();
		Permutation eb = b.permutationView().edgeTransform();
		Permutation mapping = Permutation.action(size, eb, ea.inverse());

		int[] cycles = new int[size];
		int cyclesN = mapping.nonSingletonCyclesAndGetN(cycles);

		double[] cycleCostA = cycleCost(distf, size, cycles, cyclesN, ea);
		double[] cycleCostB = cycleCost(distf, size, cycles, cyclesN, eb);
		PerfectEdgeCross.UpdatingSubsets cycleChoices = new OrderedChoices(cycleCostA, cycleCostB);

		int len=0;
		int tryCounter=0;
		int trials = trialsf.applyAsInt(cyclesN);
		while (len<size) {
			tryCounter++;
			if (tryCounter <= trials) {
				cycleChoices.next();
				len = PerfectEdgeCross.overwriteGenomeWithReverseEdgeTransform(a, ea, eb, cycles, cycleChoices::test);
			} else {
				boolean bestParentIsA = DoubleStream.of(cycleCostA).sum() < DoubleStream.of(cycleCostB).sum();
				PerfectEdgeCross.overwriteGenomeWithReverseEdgeTransform(a, ea, eb, cycles, i->bestParentIsA);
				break;
			}
		}
		return tryCounter;
	}

	/** @see #crossover(fitness.TravellingSalesman.DistanceFunction, IntUnaryOperator) */
	public static <G extends IntegerGenome<G>> void perform(TravellingSalesman.DistanceFunction distf, IntUnaryOperator trials, Random rng, G a, G b) {
		performAndGetN(distf, trials, rng, a, b);
	}

	/**
	 * Variant of the crossover that reports run time statistics to stderr.
	 * Max. number of trials is 10 000.
	 * @see #crossover(fitness.TravellingSalesman.DistanceFunction, IntUnaryOperator)
	 */
	public static <G extends IntegerGenome<G>> CrossoverRule<G> loudCrossover(TravellingSalesman.DistanceFunction distf) {
		return (rng) -> (a, b) -> {
			G aref = a.copy();
			int trials = performAndGetN(distf, i->10000, rng, a, b);
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
	 * Optimal edge preserving crossover.
	 * Given two parent permutations, modify the first one so that it has (.) only edges that appear in a or b, and (.) the highest fitness of all possible offspring following this constraint.
	 * @see PerfectEdgeCross#crossover()
	 * @param distf - The distance function of the problem being solved. This is necessary to identify the optimal offspring.
	 * @param trialsf - The number of trials before giving up and returning a copy of the fittest parent. Can be a function of the number of cycles.
	 */
	public static <G extends IntegerGenome<G>> CrossoverRule<G> crossover(TravellingSalesman.DistanceFunction distf, IntUnaryOperator trials) {
		return (rng) -> (a, b) -> perform(distf, trials, rng, a, b);
	}

	/**
	 * For a given partitioning of the edges in a solution to the travelling salesman problem, calculate the corresponding partitioning of the fitness function.
	 * @param distf - The distance function in the TSP
	 * @param size - The size of the permutation a
	 * @param cycles - Partitioning of the edges in a, in the form as returned by {@link Permutation#cycles()} ()
	 * @param numOfCycles - maximum vaule in cycles array
	 * @param edgeperm - The original permutation. The ith edge (corresponding to cycles[i]) is given by a(i-1) -> a(i) (with a(-1)=a(size-1))
	 */
	static <G extends IntegerGenome<G>> double[] cycleCost(TravellingSalesman.DistanceFunction distf, int size, int[] cycles, int numOfCycles, Permutation edgeperm) {
		double[] result = new double[numOfCycles];

		for (int i=0; i<size; i++) result[cycles[i]-1] += distf.distance(i, edgeperm.get(i));

		return result;
	}

	/**
	 * An UpdatingSubsets which will traverse all the possible binary choices in order of increasing total cost.
	 * @see OrderedSubsets
	 */
	static class OrderedChoices extends PerfectEdgeCross.UpdatingSubsets {
		final boolean[] comp;
		final OrderedSubsets underlying;

		OrderedChoices(double[] costChoiceA, double[] costChoiceB) {
			int n = costChoiceA.length;
			double[] weights = new double[n];
			this.comp = new boolean[n];
			for (int i=0; i<n; i++) {
				comp[i] = costChoiceA[i] < costChoiceB[i];
				weights[i] = Math.abs(costChoiceA[i] - costChoiceB[i]);
			}
			underlying = new OrderedSubsets(weights);
		}
		@Override
		boolean test(int i) {
			return underlying.test(i) ^ comp[i];
		}
		@Override
		void next() {
			underlying.next();
		}
	}

	/**
	 * An UpdatingSubsets which will return all the subsets of a set in order of increasing value of their sum.
	 * The weight of each element in the set is expected to be non-negative. The given array does not have to be sorted.
	 */
	static class OrderedSubsets extends PerfectEdgeCross.UpdatingSubsets {
		final double[] weights;
		final int n;
		final int[] considering;
		final List<boolean[]> subsets; // NOTE This could be improved by forgetting subsets before the first element in considering
		final List<Double> sizes;
		final Permutation weightOrdering;
		boolean[] currentSet;
		int currentIndex = -1;

		OrderedSubsets(double[] weights) {
			this.weights = DoubleStream.of(weights).sorted().toArray();
			this.weightOrdering = Permutation.fromOrdering(weights).inverse();
			this.n = weights.length;
			this.considering = new int[n];
			this.subsets = new ArrayList<>(List.of(new boolean[n]));
			this.sizes = new ArrayList<>(List.of(0.));
		}

		boolean elongate() {
			// Find next smallest subset (which will be one of the items added to its currently considering subset)
			// NOTE This could be improved by using a priority queue instead
			Optional<Integer> argminMaybe = IntStream.range(0,n)
					.filter(i->considering[i]!=-1)
					.boxed()
					.collect(Collectors.minBy(Comparator.comparing(
							i -> sizes.get(considering[i])+weights[i]
							)));

			// Finish if no item is currently considering anything
			if (argminMaybe.isEmpty()) {
				return false;

			} else {
				int argmin = argminMaybe.get();

				// Add new subset
				boolean[] newsubset = Arrays.copyOf(subsets.get(considering[argmin]), n);
				newsubset[argmin] = true;
				this.subsets.add(newsubset);

				double newsubsetsize = sizes.get(considering[argmin]) + weights[argmin];
				this.sizes.add(newsubsetsize);

				// Updating considering subset to next set that does not already have larger elements (if any)
				considering[argmin] = IntStream
						.range(considering[argmin]+1, subsets.size()-1)
						.filter(subset -> ! IntStream.range(argmin, n)
								.anyMatch(i->subsets.get(subset)[i]))
						.findFirst()
						.orElse(-1);

				return true;
			}
		}

		@Override
		boolean test(int i) {
			return this.currentSet[this.weightOrdering.get(i)];
		}
		@Override
		void next() {
			this.elongate();
			this.currentSet = this.subsets.get(++currentIndex);
		}
	}

}
