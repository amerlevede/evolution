package mutation;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import util.DiscreteDistribution;
import util.Functional;

/**
 * Wrapper for MutationOp encapsulating the dependence on random number generation.
 * @see MutationOp
 * @author adriaan
 */
@FunctionalInterface
public interface MutationRule<G> extends Function<Random, MutationOp<G>> {

    @Override MutationOp<G> apply(Random t);

    default MutationRule<G> repeatN(Function<G,DiscreteDistribution> ndist) {
        return (rng) -> {
            MutationOp<G> thisWithRng = this.apply(rng);
            return (g, stats) -> {
            	int n = ndist.apply(g).applyAsInt(rng);
                for (int i=0; i<n; i++) {
                    thisWithRng.accept(g, stats);
                }
            };
        };
    }

    static <G> MutationRule<G> withStats(MutationRule<G> rule) {
    	return (rng) -> MutationOp.withStats(rule.apply(rng));
    }

    default MutationRule<G> repeatN(int n) {
        return this.repeatN((g) -> (rng) -> n);
    }

    static <G,S> MutationRule<G> compose(Iterable<MutationRule<G>> fs) {
        return (rng) -> {
            List<MutationOp<G>> mutations = Functional.streamOfIterable(fs).map((f) -> f.apply(rng)).collect(Collectors.toList());
            return (g, stats) -> {
            	mutations.stream().forEachOrdered(mut -> mut.accept(g, stats));
            };
        };
    }
}
