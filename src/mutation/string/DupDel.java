package mutation.string;

import java.util.function.Function;

import genome.VarLengthGenome;
import mutation.MutationOp;
import mutation.MutationRule;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Utility class giving access to implementations of the DupDel mutation operator.
 * DupDel is like InDel except new sequences are copied from other parts of the Genome.
 * 
 * @author adriaan
 */
public final class DupDel {
    
    private DupDel(){
    	Assert.utilityClass();
    }
    
    /**
     * A balanced dupdel mutation operator that performs either deletion OR duplication with some size distribution.
     * @see #withSize(Function)
     * @see #withSize(Function)
     */
    public static <G extends VarLengthGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
    	final MutationRule<G> in = Duplication.withSize(lendist);
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
    
}
