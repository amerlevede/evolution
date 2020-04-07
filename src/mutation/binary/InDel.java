package mutation.binary;

import java.util.function.Function;

import genome.binary.BinaryGenome;
import mutation.MutationOp;
import mutation.MutationRule;
import mutation.string.Deletion;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Utility class giving access to insertion/deletion mutation operators.
 * @author adriaan
 */
public final class InDel {
    
    private InDel(){
    	Assert.utilityClass();
    }
    
    /**
     * Perform a deletion followed by an equally sized insertion.
     * This version of balacnedWithSize always performs the mutation, and thus has double the mutation strength of {@link #balancedSometimesWithSize(Function)} or #InDel{@link #withSize(Function)}, but is more predictable in outcome.
     */
    public static <G extends BinaryGenome<G>> MutationRule<G> balancedAlwaysWithSize(Function<G,DiscreteDistribution> lendist) {
    	return (rng) -> (g, stats) -> {
    		int len = lendist.apply(g).applyAsInt(rng);
            while (len >= g.size()) len = lendist.apply(g).applyAsInt(rng);
            
            int delLocus = rng.nextInt(g.size()-len); // NOTE: bits near the start and end of the genome are less likely to be deleted.
            Deletion.perform(stats, g, delLocus, len);
            
            int inLocus = rng.nextInt(g.size()+1);
            Insertion.perform(rng, stats, g, inLocus, len);
    	};
    }
    
    /** {@link #balancedAlwaysWithSize(Function)} with constant length **/
    public static <G extends BinaryGenome<G>> MutationRule<G> balancedAlwaysWithSize(int n) {
    	return InDel.balancedAlwaysWithSize(g_ignore -> rng_ignore -> n);
    }
    
    /**
     * Perform a deletion followed by an equally sized insertion.
     * This version of balancedWithSize only performs the mutation half of the time, to achieve simiular mutation strengh as when using {@link InDel#withSize(Function)}, which produces either in or del.
     * @see #balancedAlwaysWithSize(Function)
     */
    public static <G extends BinaryGenome<G>> MutationRule<G> balancedSometimesWithSize(Function<G,DiscreteDistribution> lendist) {
    	return (rng) -> (g, stats) -> {
    		if (rng.nextBoolean()) { // randomly choose to do both or neither in and del
	    		int len = lendist.apply(g).applyAsInt(rng);
	            while (len >= g.size()) len = lendist.apply(g).applyAsInt(rng);
	            
	            int delLocus = rng.nextInt(g.size()-len); // NOTE: bits near the start and end of the genome are less likely to be deleted.
	            Deletion.perform(stats, g, delLocus, len);
	            
	            int inLocus = rng.nextInt(g.size()+1);
	            Insertion.perform(rng, stats, g, inLocus, len);
    		}
    	};
    }
    
    /** {@link #balancedSometimesWithSize(Function)} with constant length **/
    public static <G extends BinaryGenome<G>> MutationRule<G> balancedSometimesWithSize(int n) {
    	return InDel.balancedSometimesWithSize(g_ignore -> rng_ignore -> n);
    }
    
    /**
     * A balanced indel mutation operator that performs either deletion OR insertion with some size distribution.
     * @see #withSize(Function)
     * @see #withSize(Function)
     */
    public static <G extends BinaryGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
    	final MutationRule<G> in = Insertion.withSize(lendist);
    	final MutationRule<G> del = Deletion.withSize(lendist);
    	return (rng) -> {
    		final MutationOp<G> in_rng = in.apply(rng);
    		final MutationOp<G> del_rng = del.apply(rng);
    		return (g, stats) -> {
    			if (rng.nextBoolean()) {
    				in_rng.accept(g, stats);
    			} else {
    				del_rng.accept(g, stats);
    			}
    		};
    	};
    }
    
    public static <G extends BinaryGenome<G>> MutationRule<G> withSize(int n) {
    	return InDel.withSize(g_ignore -> rng_ignore -> n);
    }
    
//    // Carl version
//    public static MutationRule duplicationWithSize(Function<Genome,DiscreteDistribution> ndist) {
//    	return InDel.insertionWithSequence((g) -> (rng) -> {
//    		int len = ndist.apply(g).applyAsInt(rng);
//    		while (len > g.size()) len = ndist.apply(g).applyAsInt(rng);
//			int start = len == g.size() ? 0 : rng.nextInt(g.size()-len);
//			return Genome.of(g.view(start, start+len));
//    	});
//    }

    
}
