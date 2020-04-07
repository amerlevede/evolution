package alignment.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import alignment.Alignment;
import genome.binary.BitGenome;
import genome.binary.BitGenomeInit;

class ConstrainedTest extends BitGenomeInit {

	@Test
	public void testConstrained_identical_onA() {
		int i=2;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, zeroes5, zeroes5, true, i);
		
		assertEquals(5, constrained.getScore());
		assertEquals(5, constrained.getPairs().size());
	}
	
	@Test
	public void testConstrained_identical_onB() {
		// Not a very good test case, since they are identical it doesn't matter if the bit is on a or b
		int i=2;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, zeroes5, zeroes5, false, i);
		
		assertEquals(5, constrained.getScore());
		assertEquals(5, constrained.getPairs().size());
	}
	
	@Test
	public void testConstrained_identical_atStart() {
		int i=0;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, zeroes5, zeroes5, true, i);
		
		assertEquals(5, constrained.getScore());
		assertEquals(5, constrained.getPairs().size());
	}
	
	@Test
	public void testConstrained_identical_atEnd() {
		int i=4;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, zeroes5, zeroes5, true, i);
		
		assertEquals(5, constrained.getScore());
		assertEquals(5, constrained.getPairs().size());
	}
	
	@Test
	public void testConstrained_identical_random() {
		int target=12;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, randomA, randomA, true, target);
		
		assertEquals(randomA.size(), constrained.getScore());
		assertEquals(randomA.size(), constrained.getPairs().size());
		assertTrue(constrained.getPairs().stream().anyMatch(pair -> pair.x == target));
	}
	
	@RepeatedTest(100)
	public void testConstrained_random_onA() {
		int target=12;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, randomA, randomA, true, target);
		
		assertTrue(constrained.getPairs().stream().anyMatch(pair -> pair.x == target));
	}
	
	@RepeatedTest(100)
	public void testConstrained_random_onB() {
		int target=2;
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, randomA, randomA, false, target);
		
		assertTrue(constrained.getPairs().stream().anyMatch(pair -> pair.y == target));
	}
	
	@RepeatedTest(100)
	public void testConstrained_compareToUnconstrained_targetOnA() {
		int target = rng.nextInt(randomA.size());
		Alignment<BitGenome> unconstrained = Affine.SmithWaterman.align(1, -1, -2, -1, rng, randomA, randomB);
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, randomA, randomB, true, target);
	
		if (unconstrained.getPairs().stream().anyMatch(pair -> pair.x == target)) {
//			assertEquals(unconstrained.getPairs(), constrained.getPairs()); // Pairs not necessarily identical due to randomness
			assertEquals(unconstrained.getScore(), constrained.getScore());
		} else testConstrained_compareToUnconstrained_targetOnA();
	}
	
	@RepeatedTest(100)
	public void testConstrained_compareToUnconstrained_targetOnB() {
		int target = rng.nextInt(randomB.size());
		Alignment<BitGenome> unconstrained = Affine.SmithWaterman.align(1, -1, -2, -1, rng, randomA, randomB);
		Alignment<BitGenome> constrained = LocalAround.align(1, -1, -2, -1, rng, randomA, randomB, false, target);
	
		if (unconstrained.getPairs().stream().anyMatch(pair -> pair.y == target)) {
//			assertEquals(unconstrained.getPairs(), constrained.getPairs()); // Pairs not necessarily identical due to randomness
			assertEquals(unconstrained.getScore(), constrained.getScore());
		} else testConstrained_compareToUnconstrained_targetOnB();
	}

}
