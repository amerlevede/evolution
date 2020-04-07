package mutation.permutation;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import genome.LinearGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.DiscreteDistribution;
import util.Functional;
import util.IntPair;

/**
 * Mutation operator implementation swapping two random elements in a permutation. 
 * 
 * @author adriaan
 *
 */
public final class RandomFlip {

	private RandomFlip() {
		Assert.utilityClass();
	}

	public static final MutationType TYPE = new MutationType("RandomFlip");

	public static <G extends LinearGenome<G>> MutationRule<G> repeatN(Function<G,DiscreteDistribution> ndist) {
		return (rng) -> (g, stats) -> {
			int len = g.size();
			int n = ndist.apply(g).applyAsInt(rng);
			Stream<IntPair> pairs = Functional.randoms(rng).limit(n).map(r -> IntPair.of(r.nextInt(g.size()), r.nextInt(g.size())));
			RandomFlip.perform(stats, g, pairs);
			if (g.size() != len) throw new IllegalStateException("Mutation changed genome size from "+len+" to "+g.size());
		};
	}

	public static <G extends LinearGenome<G>> void perform(Optional<MutationStats> stats, G g, int i, int j) {
		g.swap(i, j);
		stats.ifPresent(s->s.add(RandomFlip.TYPE, 1));
	}

	public static <G extends LinearGenome<G>> void perform(Optional<MutationStats> stats, G g, Stream<IntPair> is) {
		Iterator<IntPair> iterator = is.iterator();
		int n = 0;
		while (iterator.hasNext()) {
			IntPair i = iterator.next();
			g.swap(i.x, i.y);
			n++;
		}
		int finalN = n;
		stats.ifPresent(s->s.add(GrayFlip.TYPE, finalN));
	}

}
