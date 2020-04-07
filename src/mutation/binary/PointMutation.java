package mutation.binary;

import java.util.function.Function;

import genome.binary.BinaryGenome;
import genome.binary.BitGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Utility class giving access to mutation operators that change values of single bits.
 * @author adriaan
 */
public final class PointMutation {
    
    private PointMutation(){
    	Assert.utilityClass();
    }
    
    public final static MutationType TYPE = new MutationType("PointMutation");
    
    public static <G extends BinaryGenome<G>> MutationRule<G> distinctN(Function<G,DiscreteDistribution> ndist) {
    	return (rng) -> (g, stats) -> {
    		int n = Math.min(g.size(), ndist.apply(g).applyAsInt(rng));
            
            rng.ints(0, g.size()).distinct().limit(n).forEach(g::flip);
            
            stats.ifPresent(s->s.add(PointMutation.TYPE,n));
    	};
    }
    
    public static <G extends BinaryGenome<G>> MutationRule<G> distinctN(int n) {
        return PointMutation.distinctN((g) -> (rng) -> n);
    }
    
    /**
     * Perform a number of point mutations drawn from given random distributions.
     * Points to flip are chosen with replacement.
     * @param ndist
     * @return 
     */
    public static <G extends BinaryGenome<G>> MutationRule<G> repeatN(Function<G,DiscreteDistribution> ndist) {
        return (rng) -> (g, stats) -> {
        	int n = ndist.apply(g).applyAsInt(rng);
            rng.ints(0, g.size()).limit(n).forEach(g::flip);
            
            stats.ifPresent(s->s.add(PointMutation.TYPE,n));
        };
    }
    
    public static <G extends BinaryGenome<G>> MutationRule<G> repeatN(int n) {
        return PointMutation.repeatN((g) -> (rng) -> n);
    }

	public static void perform(MutationStats stats, BitGenome g, int i) {
		g.flip(i);
		stats.add(PointMutation.TYPE,1);
	}
    
}
