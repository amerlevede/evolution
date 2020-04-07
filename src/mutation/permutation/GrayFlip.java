package mutation.permutation;

import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.Function;
import java.util.stream.IntStream;

import genome.LinearGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Mutation operator implementation flipping two adjacent elements in a Permutation. 
 * 
 * @author adriaan
 *
 */
public final class GrayFlip {
	
	private GrayFlip() {
		Assert.utilityClass();
	}
	
	public static final MutationType TYPE = new MutationType("GrayFlip");
	
	public static <G extends LinearGenome<G>> MutationRule<G> repeatN(Function<G,DiscreteDistribution> ndist) {
		return (rng) -> (g, stats) -> {
			if (g.size() == 1) return;
			int n = ndist.apply(g).applyAsInt(rng);
			GrayFlip.perform(stats, g, rng.ints(n, 0, g.size()-1));
		};
	}

	public static <G extends LinearGenome<G>> void perform(Optional<MutationStats> stats, G g, int i) {
		g.swap(i, i+1);
		stats.ifPresent(s->s.add(GrayFlip.TYPE, 1));
	}
	
	public static <G extends LinearGenome<G>> void perform(Optional<MutationStats> stats, G g, IntStream is) {
		OfInt iterator = is.iterator();
		int n = 0;
		while (iterator.hasNext()) {
			int i = iterator.next();
			g.swap(i, i+1);
			n++;
		}
		int finalN = n;
		stats.ifPresent(s->s.add(GrayFlip.TYPE, finalN));
	}

}
