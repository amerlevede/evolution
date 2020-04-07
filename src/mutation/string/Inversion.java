package mutation.string;

import java.util.Optional;
import java.util.function.Function;

import genome.LinearGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Inversion mutation operator.
 * Takes a subsequence and inverts its direction on the genome.
 * 
 * @author adriaan
 *
 */
public final class Inversion {

	private Inversion() {
		Assert.utilityClass();
	}

	public static final MutationType TYPE = new MutationType("Inversion");

	public static <G extends LinearGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
		return (rng) -> (g,stats) -> {
			int len = lendist.apply(g).applyAsInt(rng);
			while (len >= g.size()) len = lendist.apply(g).applyAsInt(rng);
			int start = DiscreteDistribution.getUniform(rng, 0, g.size()-len+1);
			Inversion.perform(stats, g, start, len);
		};
	}

	public static <G extends LinearGenome<G>> void perform(Optional<MutationStats> stats, G g, int start, int len) {
		for (int i=0; i<len/2; i++) {
			g.swap(start+i, start+len-i-1);
		}
		stats.ifPresent(s->s.add(Inversion.TYPE, len));
	}

}
