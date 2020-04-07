package alignment.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import alignment.Alignment;
import alignment.AlignmentOp;
import genome.binary.BitGenome;
import genome.binary.BitGenomeInit;

class AffineTest extends BitGenomeInit {

    public AlignmentOp<BitGenome> defaultsw;
    public AlignmentOp<BitGenome> defaultnw;
    
    @BeforeEach
    public void setUpDefaultAlign() {
    	defaultsw = (a, b) -> Affine.SmithWaterman.align(1, -1, -2, -1, rng, a, b);
    	defaultnw = (a, b) -> Affine.NeedlemanWunsch.align(1, -1, -2, -1, rng, a , b);
    }
    
    @Test
    public void testNeedlemanWunsch_identical_zeroes() {
        Alignment<BitGenome> a = defaultnw.apply(zeroes5, zeroes5);
        
        assertEquals(zeroes5.size(), a.score);
        assertEquals(zeroes5.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testNeedlemanWunsch_identical_random() {
        Alignment<BitGenome> a = defaultnw.apply(randomAref, randomAref);
        
        assertEquals(randomAref.size(), a.score);
        assertEquals(randomAref.size(), a.getPairs().size());
    }
    
    @Test
    public void testNeedlemanWunsch_oneflip_zeroes() {
        int i=3;
        zeroes5.flip(i);
        Alignment<BitGenome> a = defaultnw.apply(zeroes5, zeroes5ref);
        
        assertEquals(zeroes5.size()-2 ,a.score); // Missing 1 match and -1 for mismatch
        assertEquals(zeroes5.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testNeedlemanWunsch_oneflip_random() {
        int i = 12;
        randomA.flip(i);
        Alignment<BitGenome> a = defaultnw.apply(randomA, randomAref);
        
        assertEquals(randomA.size()-2, a.score); // Missing 1 match and -1 for mismatch
        assertEquals(randomA.size(), a.getPairs().size());
    }
    
    @Test
    public void testNeedlemanWunsch_onedel_fixed() {
        int i=3;
        g11011.delete(i, i+1);
        Alignment<BitGenome> a = defaultnw.apply(g11011, g11011ref);
        
        assertEquals(g11011ref.size()-3, a.score); // Missing 1 match and -2 for gap
        assertEquals(g11011.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testNeedlemanWunsch_onedel_random() {
        int i=15;
        randomA.delete(i, i+1);
        Alignment<BitGenome> a = defaultnw.apply(randomA, randomAref);
        
        assertEquals(randomAref.size()-3, a.score); // Missing 1 match and -2 for gap
        assertEquals(randomA.size(), a.getPairs().size());
    }

    @Test
    public void testSmithWaterman_identical_zeroes() {
        Alignment<BitGenome> a = defaultsw.apply(zeroes5, zeroes5);
        
        assertEquals(zeroes5.size(), a.score);
        assertEquals(zeroes5.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100) 
    public void testSmithWaterman_identical_random() {
        Alignment<BitGenome> a = defaultsw.apply(randomAref, randomAref);
        
        assertEquals(randomAref.size(), a.score);
        assertEquals(randomAref.size(), a.getPairs().size());
    }
  
    
    @RepeatedTest(100)
    public void testSmithWaterman_oneflip_random() {
        int i = 12;
        randomA.flip(i);
        Alignment<BitGenome> a = defaultsw.apply(randomA, randomAref);
        
        assertEquals(randomA.size()-2, a.score); // Missing 1 match and -1 for mismatch
        assertEquals(randomA.size(), a.getPairs().size());
    }
    
    @Test
    public void testSmithWaterman_onedel_fixed() {
        int i=3;
        g11011.delete(i, i+1);
        Alignment<BitGenome> a = defaultsw.apply(g11011, g11011ref);
        
        assertEquals(g11011ref.size()-1, a.score); // Missing 1 match (no cost for gap, local alignment)
        assertEquals(g11011.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testSmithWaterman_onedel_first_random() {
        int i=0;
        randomA.delete(i, i+1);
        Alignment<BitGenome> a = defaultsw.apply(randomA, randomAref);
        
        assertEquals(randomAref.size()-1, a.score); // Missing 1 match
        assertEquals(randomA.size(), a.getPairs().size());
    }
    
    @RepeatedTest(100)
    public void testSmithWaterman_onedel_last_random() {
        int i=randomA.size()-1;
        randomA.delete(i, i+1);
        Alignment<BitGenome> a = defaultsw.apply(randomA, randomAref);
        
        assertEquals(randomAref.size()-1, a.score); // Missing 1 match
        assertEquals(randomA.size(), a.getPairs().size());
    }
    
    @Test
    public void testSmithWaterman_onematch() {
    	zeroes5.flip(3);
    	Alignment<BitGenome> a = defaultsw.apply(zeroes5, ones5);
    	
    	assertEquals(1, a.score); // 1 because there is 1 match and no penalties
    	assertEquals(1, a.getPairs().size());
    }

}
