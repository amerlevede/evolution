package alignment.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import alignment.VarOAlignment;
import alignment.VarOAlignmentRule;
import genome.binary.BitGenome;
import genome.binary.BitGenomeInit;

class CrissCross2Test extends BitGenomeInit {
	
	public VarOAlignmentRule<BitGenome> defaultalign;
	
	@BeforeEach
	public void setupAlign() {
		this.defaultalign = GreedyGlocal.alignment(1, -20, 5);
	}
	
	@Test
	public void testAlign_identical_zeroes() {
		VarOAlignment<BitGenome> ali = defaultalign.apply(rng).apply(zeroes5, zeroes5);
		
		assertEquals(5, ali.getPairs().size());
		assertEquals(1, ali.getSegments().size());
	}
	
	@Test
	public void testAlign_identical_mixed() {
		VarOAlignment<BitGenome> ali = defaultalign.apply(rng).apply(g11011, g11011);
		
		assertEquals(5, ali.getPairs().size());
		assertEquals(1, ali.getSegments().size());
	}
	
	@RepeatedTest(100)
	public void testAlign_identical_random() {
		VarOAlignment<BitGenome> ali = defaultalign.apply(rng).apply(randomA, randomA);
		
		assertEquals(randomA.size(), ali.getPairs().size());
		assertEquals(1, ali.getSegments().size());
	}
	
	@Test
	public void testAlign_twoparts_zeroesandones() {
		// Test if it recognizes two unordered homologous segments but without overlap correction
		zeroes5.append(ones5ref);
		ones5.append(zeroes5ref);
		
		VarOAlignment<BitGenome> ali = defaultalign.apply(rng).apply(zeroes5, ones5);
		
		assertEquals(10, ali.getPairs().size());
		assertEquals(2, ali.getSegments().size());
	}
	
//	@Test
//	public void testAlign_twoparts_looksLikeASNP() {
//		// The genome contains two identical parts in different orders, but the first alignment can reach a higher score by aligning a mismatch unless the mismatch penalty is more than three
//		Genome a = Genome.of("1100001000");
//		Genome b = Genome.of("0100011000");
//		
//		VarOAlignment ali = defaultalign.apply(rng).apply(a, b);
//	
//	assertEquals(2, ali.getSegments().size()); // This is not true when the two segments are identical
//	assertEquals(10, ali.getPairs().size());
//	}
	
//	@RepeatedTest(100)
//	public void testAlign_twoparts_longestIsMisleading() {
//		// This alignment fails because the initial local alignment with the highest score is of length 6, and the remaining parts are not long enough to take back the sections on the side.
//		Genome a = Genome.of(  "1101101100");
//		Genome b = Genome.of("0110011011");
//		
//		VarOAlignment ali = defaultalign.apply(rng).apply(a, b);
//		
//		assertEquals(2, ali.getSegments().size()); // This is not true when the two segments are identical
//		assertEquals(10, ali.getPairs().size());
//	}
		
//	@RepeatedTest(100)
//	public void testAlign_twoparts_randomshort() {
//		// Test if it recognizes two unordered homologous segments with overlap correction
//		random5A.append(random5Bref);
//		random5B.append(random5Aref);
//		
//		VarOAlignment ali = defaultalign.apply(rng).apply(random5A, random5B);
//		
//		if (ali.getSegments().size() != 2) {
//			System.err.println(random5Aref);
//			System.err.println(random5Bref);
//			System.err.println();
//		}
//		
//		assertEquals(2, ali.getSegments().size()); // This is not true when the two segments are identical
//		assertEquals(10, ali.getPairs().size());
//	}
	
//	@RepeatedTest(1)
//	public void testAlign_twoparts_stuck() {
//		long seed = 1802076490569538221L;
//		System.out.println(seed);
//		rng.setSeed(seed);
//		
//		Genome a = Genome.of("111111111010");
//		Genome b = Genome.of("111010111111");
//		
//		VarOAlignment ali = defaultalign.apply(rng).apply(a, b);
//		
//		assertEquals(2, ali.getSegments().size());
//		assertEquals(12, ali.getPairs().size());
//	}
	
	@RepeatedTest(100)
	public void testAlign_twoparts_random_differentSize() {
		// This only works because randomA and randomB are large enough that a "misleading" alignment is very unlikely.
		randomA.append(randomBref);
		randomB.append(randomAref);
		
		VarOAlignment<BitGenome> ali = defaultalign.apply(rng).apply(randomA, randomB);
		
		assertEquals(2, ali.getSegments().size());
		assertEquals(randomAref.size()+randomBref.size(), ali.getPairs().size());
	}

}
