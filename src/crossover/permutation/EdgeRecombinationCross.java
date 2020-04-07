package crossover.permutation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import crossover.CrossoverRule;
import genome.integer.IntegerGenome;
import util.Assert;
import util.CategoricalDistribution;

/**
 * Edge recombination crossover (ERX).
 * 
 * @see Whitley et al. (1989)
 */
public final class EdgeRecombinationCross {

	private EdgeRecombinationCross() {
		Assert.utilityClass();
	}

	public static <G extends IntegerGenome<G>> CrossoverRule<G> crossover() {
		return (rng) -> (a, b) -> EdgeRecombinationCross.perform(rng, a, b);
	}

	public static <G extends IntegerGenome<G>> void perform(Random rng, G a, G b) {
		int size = a.size();
		if (b.size() != size) throw new IllegalArgumentException();

		List<SortedSet<Integer>> neighbors = a.stream().mapToObj((i) -> new TreeSet<Integer>()).collect(Collectors.toList());
		neighbors.get(a.get(0)).add(a.get(size-1));
		neighbors.get(a.get(size-1)).add(a.get(0));
		neighbors.get(b.get(0)).add(b.get(size-1));
		neighbors.get(b.get(size-1)).add(b.get(0));
		for (int i=1; i<size; i++) {
			neighbors.get(a.get(i-1)).add(a.get(i));
			neighbors.get(a.get(i)).add(a.get(i-1));
			neighbors.get(b.get(i-1)).add(b.get(i));
			neighbors.get(b.get(i)).add(b.get(i-1));
		}
		SortedSet<Integer> unpicked = IntStream.range(0, size).boxed().collect(Collectors.toCollection(TreeSet::new));

		int last = a.get(0);
		for (int i=1; i<size; i++) {
			unpicked.remove(last);
//			for (SortedSet<Integer> neighs : neighbors) neighs.remove(last);
			for (int neigh : neighbors.get(last)) neighbors.get(neigh).remove(last);

			Optional<Integer> maybeNext = neighbors.get(last).stream().collect(Collectors.minBy(Comparator.comparing(v -> neighbors.get(v).size())));
			last = maybeNext.orElseGet(() -> CategoricalDistribution.getUniformUnindexed(rng, unpicked));

			a.set(i, last);
		}

		// b is left unchanged

	}

}

