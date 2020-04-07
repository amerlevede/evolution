package genome.integer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class IntGenomeTest extends IntGenomeInit {


	@RepeatedTest(100)
	public void testIdentical() {
		assertTrue(perm.sameSequence(perm));
		assertTrue(perm.sameSequence(permRef));

		perm.set(0, perm.get(0)+1);

		assertTrue(perm.sameSequence(perm));
		assertFalse(perm.sameSequence(permRef));
	}

	@RepeatedTest(100)
	public void testGet() {
		for (int i=0; i<range.size(); i++) {
			assertEquals(i, range.get(i));
		}
		for (int i=0; i<perm.size(); i++) {
			assertEquals(permRef.get(i), perm.get(i));
		}
	}

	@RepeatedTest(100)
	public void testKendallTau_identity() {
		assertEquals(0, Permutation.kendallTau(range.permutationView(), range.permutationView()));
		assertEquals(0, Permutation.kendallTau(perm.permutationCopy(), perm.permutationView()));
	}

	@Test
	public void testIsPermutation_case1() {
		// Case came up while debugging
		IntGenome a = IntGenome.of(9,7,2,5,0,4,6,10,1,3,8);
		IntGenome b = IntGenome.of(6,9,2,5,0,4,10,3,1,7,8);
		IntGenome c = IntGenome.of(9,7,2,4,6,3,5,0,1,10,8);
		IntGenome d = IntGenome.of(6,9,2,5,0,4,10,3,1,7,8);

		assertTrue(a.isPermutation());
		assertTrue(b.isPermutation());
		assertTrue(c.isPermutation());
		assertTrue(d.isPermutation());
	}

}
