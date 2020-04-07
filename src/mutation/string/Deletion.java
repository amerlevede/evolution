package mutation.string;

import java.util.Optional;
import java.util.function.Function;

import genome.VarLengthGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Deletion mutation operator.
 * 
 * @author adriaan
 *
 */
public final class Deletion {

	private Deletion() {
		Assert.utilityClass();
	}

	public final static MutationType TYPE = new MutationType("Deletion");

	/**
	 * Mutation rule that performs deletions using the given function to generate the length of deleted segments.
	 * The locus of the deletion is drawn randomly from all sections of the given length in the genome. As a result, bits near the middle of the genome are more likely to be deleted than those at the size (if the deleted segment length is n, the first and last bit are part of only one segment of that size, while the bits in the middle are part of n segments).
	 * If the sequence is of length 1, no deletion will occur.
	 * Otherwise, if the given distribution supplies a length that is larger than or equal to the size of the genome, the value will be redrawn.
	 * @note In most situations, 1 should be a possible return value for the length distribution, otherwise redrawing numbers may lead to an infinite loop for small genomes.
	 * @note Because the source sequence is taken from a uniform distribution across the genome, bits near the ends have lower probability of being deleted.
	 */
	public static <G extends VarLengthGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
	    return (rng) -> (g, stats) -> {
	    	if (g.size() > 1) {
	            int len = lendist.apply(g).applyAsInt(rng);
	            while (len >= g.size()) len = lendist.apply(g).applyAsInt(rng);
	            int locus = rng.nextInt(g.size()-len); // NOTE: bits near the start and end of the genome are less likely to be deleted.
	            perform(stats, g, locus, len);
	    	}
	    };
	}

	public static <G extends VarLengthGenome<G>> void perform(Optional<MutationStats> stats, G g, int locus, int length) {
		g.delete(locus, locus+length);
		stats.ifPresent(s->s.add(Deletion.TYPE,length));
	}

}
