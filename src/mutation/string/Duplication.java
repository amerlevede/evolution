package mutation.string;

import java.util.Optional;
import java.util.function.Function;

import genome.VarLengthGenome;
import mutation.MutationRule;
import mutation.MutationStats.MutationType;
import mutation.binary.Insertion;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Duplication mutation operator.
 * Duplication is the insertion of a part in the genetic sequence, but the new part is identical to an existing part.
 * 
 * @author adriaan
 *
 */
public final class Duplication {

	private Duplication() {
		Assert.utilityClass();
	}

	public static final MutationType TYPE = new MutationType("Duplication");

	/**
	 * Duplication operator that inserts a sequence in the genome that is copied from a random location in the same genome.
	 * If the length of the segment given by the supplied distribution is shorter than the genome, the number will be redrawn.
	 * @note In most situations, 1 should be a possible return value for ndist, otherwise redrawing numbers may lead to an infinite loop for small genomes.
	 * @note Because the source sequence is taken from a uniform distribution across the genome, bits near the ends have lower probability of being copied .
	 */
	public static <G extends VarLengthGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
		return (rng) -> (g, stats) -> {
			int len = lendist.apply(g).applyAsInt(rng);
			while (len > g.size()) len = lendist.apply(g).applyAsInt(rng);
			int start = len == g.size() ? 0 : rng.nextInt(g.size()-len);
			G seq = g.view(start, start+len);

			Insertion.perform(Optional.empty(), g, start, seq); // ignore stats, replace with duplication
			int finalLen = len;
			stats.ifPresent(s->s.add(Duplication.TYPE,finalLen));
		};
	}

}
