package selection;

import java.util.Collection;
import java.util.Random;
import java.util.function.BiFunction;

import genome.Genome;
import population.Org;
import util.CategoricalDistribution;

/**
 * A SelectorRule implements a way to select organisms from a population.
 * 
 * @author adriaan
 *
 * @param <O>
 */
@FunctionalInterface
public interface SelectorRule<O> extends BiFunction<Random,Collection<O>,Selector<O>> {

	@Override Selector<O> apply(Random rng, Collection<O> pop);

	default SelectorRule<O> modify(SelectorOp<O> sel) {
		return sel.apply(this);
	}

	static <G extends Genome<G>> SelectorRule<Org<G>> random() {
		return (rng, pop) -> () -> CategoricalDistribution.uniform(pop).stream(rng);
	}

}
