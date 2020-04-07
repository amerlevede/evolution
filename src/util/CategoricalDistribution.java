package util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 * Alias for Function<Random,T>, implementing some useful ways to generate random elements of the given type.
 */
@FunctionalInterface
public interface CategoricalDistribution<T> extends Function<Random,T> {

	@Override T apply(Random rng);

	/**
	 * Return a stream of values generated from the supplied distribution.
	 * This implementation is different from IntStream.generate because the resulting stream is *ordered*, meaning the elements and their orders are guaranteed to be identical when given the same random seed.
	 */
	static <T> Stream<T> streamOf(Random rng, Function<Random,T> dist) {
		return Stream.iterate(dist.apply(rng), (ignored) -> dist.apply(rng));
	}

	/**
	 * Convenient alias of {@link #streamOf(Random, ToIntFunction)}
	 */
	default Stream<T> stream(Random rng) {
		return streamOf(rng, this);
	}

	static <O extends Comparable<O>> Optional<O> getBestOf(Random rng, Collection<O> collection) {
		return CategoricalDistribution.getBestOf(rng, collection, Comparator.naturalOrder());
	}

	static <O extends Comparable<O>> O getBestOfNonempty(Random rng, Collection<O> nonemptycollection) {
		return DiscreteDistribution.getBestOfNonempty(rng, nonemptycollection, Comparator.naturalOrder());
	}

	static <O> Optional<O> getBestOf(Random rng, Collection<O> collection, Comparator<O> comparator) {
		if (collection.size() > 0) {
			return Optional.of(DiscreteDistribution.getBestOfNonempty(rng, collection, comparator));
		} else {
			return Optional.empty();
		}
	}

	static <T> CategoricalDistribution<T> uniform(Collection<T> c) {
		return c instanceof List
			? uniformIndexed((List<T>)c)
			: uniformUnindexed(c);
	}

	static <T> CategoricalDistribution<T> uniformUnindexed(Collection<T> c) {
		return (rng) -> getUniformUnindexed(rng, c);
	}

	static <T> T getUniformUnindexed(Random rng, Collection<T> c) {
		if (c.size() == 0) return null;
		int j = rng.nextInt(c.size());
		int i=0;
		for (T ele : c) {
			if (i==j) return ele;
			i++;
		}
		Assert.unreachableCode();
		return null;
	}

	static <T> CategoricalDistribution<T> uniformIndexed(List<T> c) {
		return (rng) -> getUniformIndexed(rng, c);
	}

	static <T> T getUniformIndexed(Random rng, List<T> c) {
		int i = rng.nextInt(c.size());
		return c.get(i);
	}

}
