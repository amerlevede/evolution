package crossover.permutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.RepeatedTest;

import crossover.CrossoverOp;
import genome.RandomInit;
import genome.integer.IntGenome;
import genome.integer.IntGenomeInit;
import mutation.permutation.GrayFlip;
import mutation.permutation.RandomFlip;
import util.DiscreteDistribution;

public abstract class PermutationCrossoverTest extends IntGenomeInit {

	public abstract CrossoverOp<IntGenome> crossover();

	public void otherConstraints(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
	};

	@RepeatedTest(100)
	public void testCross_mini() {
		IntGenome a = IntGenome.permutationOf("abc");
		IntGenome b = IntGenome.permutationOf("cba");

		crossover().accept(a, b);

		assertEquals(3, a.size());
		assertEquals(3, b.size());

		assertTrue(a.isPermutation());
		assertTrue(b.isPermutation());

		otherConstraints(a, b, IntGenome.permutationOf("abc"), IntGenome.permutationOf("cba"));
	}

	@RepeatedTest(100)
	public void testCross_permutationPreserving_special() {
		if (size >= 7) {
			IntGenome special7 = IntGenome.of(new int[] {2,5,6,3,0,1,4});
			IntGenome special7Ref = special7.copy().view();
			IntGenome a = IntGenome.range(7);
			IntGenome aRef = a.copy().view();

			crossover().accept(a, special7);

			assertEquals(special7Ref.size(), special7.size());
			assertTrue(special7.isPermutation());

			otherConstraints(a, special7, aRef, special7Ref);
		}
	}

	@RepeatedTest(1000)
	public void testCross_identical() {
		IntGenome id = perm.copy();

		crossover().accept(id, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(permRef.size(), id.size());

		assertTrue(perm.isPermutation());
		assertTrue(id.isPermutation());

		otherConstraints(id, perm, permRef, permRef);
	}

	@RepeatedTest(1000)
	public void testCross_identical_reversed() {
		IntGenome id = perm.reversedView().copy();

		crossover().accept(id, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(permRef.size(), id.size());

		assertTrue(perm.isPermutation());
		assertTrue(id.isPermutation());

		otherConstraints(id, perm, permRef.reversedView(), permRef);
	}

	@RepeatedTest(1000)
	public void testCross_grayflips() {
		IntGenome grayflips = perm.copy();
		GrayFlip.<IntGenome>repeatN(g->DiscreteDistribution.uniform(1, size/2)).apply(RandomInit.rng).mutate(grayflips);
		IntGenome grayflipsRef = grayflips.copy().view();

		crossover().accept(grayflips, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(grayflipsRef.size(), grayflips.size());

		assertTrue(perm.isPermutation());
		assertTrue(grayflips.isPermutation());

		otherConstraints(grayflips, perm, grayflipsRef, permRef);
	}

	@RepeatedTest(1000)
	public void testCross_grayflips_reverse() {
		IntGenome grayflips = perm.copy();
		GrayFlip.<IntGenome>repeatN(g->DiscreteDistribution.uniform(1, size/2)).apply(RandomInit.rng).mutate(grayflips);
		grayflips = grayflips.reversedView().copy();
		IntGenome grayflipsRef = grayflips.copy().view();

		crossover().accept(grayflips, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(grayflipsRef.size(), grayflips.size());

		assertTrue(perm.isPermutation());
		assertTrue(grayflips.isPermutation());

		otherConstraints(grayflips, perm, grayflipsRef, permRef);
	}

	@RepeatedTest(1000)
	public void testCross_randflips() {
		IntGenome randflips = perm.copy();
		RandomFlip.<IntGenome>repeatN(g->DiscreteDistribution.uniform(1, size/2)).apply(RandomInit.rng).mutate(randflips);
		IntGenome randflipsRef = randflips.copy().view();

		crossover().accept(randflips, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(randflipsRef.size(), randflips.size());
		assertTrue(perm.isPermutation());
		assertTrue(randflips.isPermutation());

		otherConstraints(randflips, perm, randflipsRef, permRef);
	}

	@RepeatedTest(1000)
	public void testCross_randflips_reverse() {
		IntGenome randflips = perm.copy();
		RandomFlip.<IntGenome>repeatN(g->DiscreteDistribution.uniform(1, size/2)).apply(RandomInit.rng).mutate(randflips);
		randflips = randflips.reversedView().copy();
		IntGenome randflipsRef = randflips.copy().view();

		crossover().accept(randflips, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(randflipsRef.size(), randflips.size());
		assertTrue(perm.isPermutation());
		assertTrue(randflips.isPermutation());

		otherConstraints(randflips, perm, randflipsRef, permRef);
	}

	@RepeatedTest(1000)
	public void testCross_random() {
		IntGenome rand = IntGenome.getRandomPermutation(RandomInit.rng, size);
		IntGenome randRef = rand.copy().view();

		crossover().accept(rand, perm);

		assertEquals(permRef.size(), perm.size());
		assertEquals(randRef.size(), rand.size());
		assertTrue(perm.isPermutation());
		assertTrue(rand.isPermutation());

		otherConstraints(rand, perm, randRef, permRef);
	}

}
