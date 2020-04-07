package crossover.permutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import crossover.CrossoverOp;
import crossover.permutation.EdgePreservingOptimalCross.OrderedChoices;
import fitness.FitnessFunction;
import fitness.TravellingSalesman;
import genome.integer.IntGenome;
import genome.integer.Permutation;
import util.ContinuousDistribution;
import util.DiscreteDistribution;
import util.DoublePair;

class EdgePreservingOptimalCrossTest extends PermutationCrossoverTest {

	TravellingSalesman.DistanceFunction distf;
	FitnessFunction<IntGenome> fitf;

	@BeforeEach
	public void setupFitness() {
		List<DoublePair> cities = TravellingSalesman.randomCities(rng, size);
		assertEquals(size, cities.size());
		distf = TravellingSalesman.DistanceFunction.fromCoords(cities);
		fitf = TravellingSalesman.cyclic(distf, 0);
	}

	@Override
	public CrossoverOp<IntGenome> crossover() {
		return EdgePreservingOptimalCross.<IntGenome>crossover(distf, i->1000000).apply(rng);
	}

	@Override
	public void otherConstraints(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
		assertTrue(EdgePreservingCrossTest.edgePreserving(a, b, aRef, bRef));

		// Check that fitness is at least that of parents (because parents are always a valid crossover this must always be true)
		double error = 0.0001;
		double fitnessOffspring = fitf.applyAsDouble(a);
		double fitnessParentA = fitf.applyAsDouble(aRef);
		double fitnessParentB = fitf.applyAsDouble(bRef);
		double fitnessBestParent = Math.max(fitnessParentA, fitnessParentB);
		assertTrue(fitnessOffspring+error >= fitnessBestParent);
	}

	@Test
	public void testOrderedSubsets_testcase() {
		double[] weights = new double[] {1,4,5,9};
		int n = 4;

		EdgePreservingOptimalCross.OrderedSubsets sets = new EdgePreservingOptimalCross.OrderedSubsets(weights);

		int countSets = 0;
		double lastSize = 0;
		do {
			countSets++;
			sets.next();
			double currentSize = IntStream.range(0, n).filter(sets::test).mapToDouble(i->weights[i]).sum();
			assertTrue(currentSize >= lastSize);
			lastSize = currentSize;
		} while (!IntStream.range(0,n).allMatch(sets::test));

		assertEquals(1<<n,countSets);
	}

	@RepeatedTest(100)
	public void testOrderedSubsets_random() {
		int n = DiscreteDistribution.getUniform(rng, 2, 10);
		double[] weights = ContinuousDistribution.uniform(0,9).stream(rng).limit(n).toArray();

		EdgePreservingOptimalCross.OrderedSubsets sets = new EdgePreservingOptimalCross.OrderedSubsets(weights);

		int countSets = 0;
		double lastSize = 0;
		do {
			countSets++;
			sets.next();
			double currentSize = IntStream.range(0, n).filter(sets::test).mapToDouble(i->weights[i]).sum();
			assertTrue(currentSize >= lastSize);
			lastSize = currentSize;
		} while (!IntStream.range(0,n).allMatch(sets::test));

		assertEquals(1<<n,countSets);
	}

	@RepeatedTest(100)
	public void testOrderedSubsets_random_withZero() {
		double error = 0.000000001;
		int n = DiscreteDistribution.getUniform(rng, 2, 10);
		double[] weights = ContinuousDistribution.uniform(0,9).stream(rng).limit(n).toArray();
		for (int i=0; i<n; i++) {
			weights[DiscreteDistribution.getUniform(rng,0,n)] = 0;
		}

		EdgePreservingOptimalCross.OrderedSubsets sets = new EdgePreservingOptimalCross.OrderedSubsets(weights);

		int countSets = 0;
		double lastSize = 0;
		do {
			countSets++;
			sets.next();
			double currentSize = IntStream.range(0, n).filter(sets::test).mapToDouble(i->weights[i]).sum();
			assertTrue(currentSize+error >= lastSize);
			lastSize = currentSize;
		} while (!IntStream.range(0,n).allMatch(sets::test));

		assertEquals(1<<n,countSets);
	}

	public void inspect(IntGenome a, IntGenome b) {
		a = a.copy();
		b = b.copy();

		System.out.println(a);
		System.out.println(b);
		System.out.println();

		Permutation ea = a.permutationView().edgeTransform();
		Permutation eb = b.permutationView().edgeTransform();
		Permutation eainv = ea.inverse();
		Permutation mapping = Permutation.action(size, eb, eainv);

		int[] cycles = new int[size];
		int cyclesN = mapping.nonSingletonCyclesAndGetN(cycles);

		double[] cycleCostA = EdgePreservingOptimalCross.cycleCost(distf, size, cycles, cyclesN, ea);
		double[] cycleCostB = EdgePreservingOptimalCross.cycleCost(distf, size, cycles, cyclesN, eb);
		PerfectEdgeCross.UpdatingSubsets cycleChoices = new OrderedChoices(cycleCostA, cycleCostB);

		System.out.println("parent fitnesses: "+fitf.applyAsDouble(a)+", "+fitf.applyAsDouble(b));
		System.out.println("sum of cycle costs: "+DoubleStream.of(cycleCostA).sum()+", "+DoubleStream.of(cycleCostB).sum());
		System.out.println("cycle costs A:" + Arrays.toString(cycleCostA));
		System.out.println("cycle costs B:" + Arrays.toString(cycleCostB));
		System.out.println();

		int len=0;
		while (len<size) {
			cycleChoices.next();
			String choice = IntStream.range(0, cyclesN).mapToObj(i->cycleChoices.test(i)?"A":"B").collect(Collectors.joining());
			double fitness = IntStream.range(0, cyclesN).mapToDouble(i->cycleChoices.test(i)?cycleCostA[i]:cycleCostB[i]).sum();
			len = PerfectEdgeCross.overwriteGenomeWithReverseEdgeTransform(a, ea, eb, cycles, cycleChoices::test);
			System.out.println("Trying subsets: "+choice);
			System.out.println("	calculated fitness:"+fitness);
		}

		System.out.println("	actual fitness"+fitf.applyAsDouble(a));
	}

}
