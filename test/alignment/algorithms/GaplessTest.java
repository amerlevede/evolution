package alignment.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import alignment.Alignment;
import alignment.AlignmentOp;
import genome.binary.BitGenome;
import genome.binary.BitGenomeInit;

class GaplessTest extends BitGenomeInit {
	
	public AlignmentOp<BitGenome> global;
	public AlignmentOp<BitGenome> local;
	
    @BeforeEach
    public void setUpDefaultAlign() {
    	local = Gapless.<BitGenome>alignment(1, -2).apply(rng);
    }
    
	@Test
    public void testLocal_identical_zeroes() {
        Alignment<BitGenome> a = local.apply(zeroes5, zeroes5);
        
        assertEquals(zeroes5.size(), a.score);
        assertEquals(zeroes5.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testLocal_identical_random() {
        Alignment<BitGenome> a = local.apply(randomAref, randomAref);
        
        assertEquals(randomAref.size(), a.score);
        assertEquals(randomAref.size(), a.getPairs().size());
    }
    
    @Test
    public void testLocal_oneflip_zeroes() {
        int i=2;
        zeroes5.flip(i);
        Alignment<BitGenome> a = local.apply(zeroes5, zeroes5ref);
        
        assertEquals(zeroes5.size()-3, a.score); // Missing 1 match and -2 for mismatch
//        assertEquals(zeroes5.size(), a.getPairs().size()); // Can align either full or part for same score
    }
    
    @RepeatedTest(100)
    public void testLocal_oneflip_random() {
        int i = 12;
        randomA.flip(i);
        Alignment<BitGenome> a = local.apply(randomA, randomAref);
        
        assertEquals(randomA.size()-3, a.score); // Missing 1 match and -2 for mismatch
        assertEquals(randomA.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testLocal_oneflip_start() {
    	int i=0;
    	randomA.flip(i);
        Alignment<BitGenome> a = local.apply(randomA, randomAref);
        
        assertEquals(randomA.size()-1, a.score); // Missing 1 match
        assertEquals(randomA.size()-1, a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testLocal_oneflip_end() {
    	int i=randomA.size()-1;
    	randomA.flip(i);
        Alignment<BitGenome> a = local.apply(randomA, randomAref);
        
        assertEquals(randomA.size()-1, a.score); // Missing 1 match
        assertEquals(randomA.size()-1, a.getPairs().size());
    }
    
    @Test
    public void testLocal_empty() {
    	Alignment<BitGenome> a = local.apply(zeroes5, ones5);
    	
    	assertEquals(0, a.score);
    	assertEquals(0, a.getPairs().size());
    }
    
    @Test
    public void testLocal_onematch_sameposition() {
    	int i=3;
    	zeroes5.flip(i);
    	Alignment<BitGenome> a = local.apply(zeroes5, ones5);
    	
    	assertEquals(1, a.score); // 1 because there is 1 match and no penalties
    	assertEquals(1, a.getPairs().size());
    	assertEquals(i, a.getPairs().first().x);
    }
    
    @Test
    public void testLocal_onematch_differentposition() {
    	BitGenome a = BitGenome.readUnsafe("01");
    	BitGenome b = BitGenome.readUnsafe("10");
    	Alignment<BitGenome> ali = local.apply(a, b);
    	
    	assertEquals(1, ali.score); // 1 because there is 1 match and no penalties
    	assertEquals(1, ali.getPairs().size());
    }
    
    @Test
    public void testLocal_someexample() {
    	// This test case was isolated as a rare error of a previous implementation.
    	BitGenome lcss = BitGenome.readUnsafe(                       "11101101100");
    	BitGenome a = BitGenome.readUnsafe("01011001000110010001110110111011011001000001000100001110001011000111001011");
    	BitGenome b = BitGenome.readUnsafe("11011000000110010111110110110001011101000000000100001010001111000111101010");
//    	Genome lcss = Genome.of(                "11101101100");
    	
    	Alignment<BitGenome> ali = Gapless.align(rng, 1, -1000, a, b);
    	
    	assertEquals(lcss.size(), ali.getScore());
    }
    
    @RepeatedTest(1000)
    public void testLocal_compareToAffine() {
    	int infinity = randomA.size() + randomB.size();
    	Alignment<BitGenome> withGapless = local.apply(randomA, randomB);
    	Alignment<BitGenome> withAffine = Affine.SmithWaterman.align(1, -2, -infinity, 0, rng, randomA, randomB);
    	 
    	assertEquals(withAffine.getScore(), withGapless.getScore());
    	// Can't guarantee equality of alignment because randomness if multiple equivalent choices
    }

}
