package crossover.permutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;

import crossover.CrossoverOp;
import genome.RandomInit;
import genome.integer.IntGenome;
import mutation.permutation.RandomFlip;
import util.DiscreteDistribution;
import util.IntPair;

class EdgePreservingCrossTest extends PermutationCrossoverTest {

	@Override
	public CrossoverOp<IntGenome> crossover() {
		return PerfectEdgeCross.<IntGenome>crossover(i->1000000).apply(rng);
	}
	
	public static boolean edgePreserving(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
		Collection<IntPair> edgesAfter = a.permutationView().edges().collect(Collectors.toList());

		Set<IntPair> edgesBefore = new HashSet<>(2*a.size());
		aRef.permutationView().edges().forEach(edgesBefore::add);
		bRef.permutationView().edges().forEach(edgesBefore::add);

		for (IntPair edgeAfter : edgesAfter) {
			if (!edgesBefore.contains(edgeAfter)) return true;
		}
		return false;
	}
	
	@Override
	public void otherConstraints(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
		assertTrue(edgePreserving(a, b, aRef, bRef));
	}

	@RepeatedTest(100)
	public void testCross_randflips_other() {
		IntGenome randflips = range.copy();
		RandomFlip.<IntGenome>repeatN(g->DiscreteDistribution.uniform(1, size/2)).apply(RandomInit.rng).mutate(randflips);
		IntGenome randflipsRef = randflips.copy().view();

		crossover().accept(range, randflips);

		assertEquals(rangeRef.size(), range.size());
		assertEquals(randflipsRef.size(), randflips.size());
		assertTrue(range.isPermutation());
		assertTrue(randflips.isPermutation());

		otherConstraints(range, randflips, rangeRef, randflipsRef);
	}

}
