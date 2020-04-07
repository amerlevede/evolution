package mutation.permutation;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import genome.integer.IntGenome;
import genome.integer.IntGenomeInit;
import genome.integer.Permutation;

class GrayFlipTest extends IntGenomeInit {

	@Test
	public void testFlip_identity() {
		if (range.size() > 1) {

			GrayFlip.<IntGenome>repeatN(g->r->1).apply(rng).mutate(range);

			assertFalse(range.sameSequence(rangeRef));
			assertEquals(1, Permutation.kendallTau(range.permutationView(), rangeRef.permutationView()));
		}
	}

}
