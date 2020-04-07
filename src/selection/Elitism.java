package selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import genome.Genome;
import population.Org;

public class Elitism {

	/**
	 * Modify a selector so that the best n organisms are never returned
	 */
	public static <O> SelectorOp<O> spare(int n, Comparator<O> comp) {
		return (sel) -> (rng,pop) -> () -> {
			List<O> sortme = new ArrayList<>(pop);
			Collections.sort(sortme, comp);
			List<O> best = sortme.subList(sortme.size()-n, sortme.size());
			return sel.apply(rng, pop).getExcluding(best);
		};
	}

	public static <G extends Genome<G>> SelectorOp<Org<G>> spare(int n) {
		return Elitism.spare(n, Comparator.<Org<G>>naturalOrder());
	}

	/**
	 * Modify a selector so that the first returned organisms are always the best prob %.
	 */
	public static <O> SelectorOp<O> ensure(int n, Comparator<O> comp) {
		return (sel) -> (rng,pop) -> () -> {
			List<O> sortme = new ArrayList<>(pop);
			Collections.sort(sortme, comp);
			List<O> best = sortme.subList(sortme.size()-n, sortme.size());
			return Stream.concat(best.stream(), sel.apply(rng,pop).get());
		};
	}

	public static <G extends Genome<G>> SelectorOp<Org<G>> ensure(int n) {
		return Elitism.spare(n, Comparator.<Org<G>>naturalOrder());
	}

}
