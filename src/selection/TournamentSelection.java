package selection;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

import util.CategoricalDistribution;

/**
 * Utilty class implementing tournament selection.
 * @see #comparing(int, Comparator)
 *
 * @author adriaan
 */
public class TournamentSelection {

	/**
	 * SelectorOp that applies the Tournament Selection procedure.
	 * Tournament Selection takes n organisms and returns the best one (according to the given comparator).
	 * A random one is chosen in the event of a tie (according to {@link CategoricalDistribution#getBestOf(Random, Collection, Comparator)})
	 *
	 * To use natural ordering, use {@link #bestOf(int)} or {@link #worstOf(int)}.
	 */
	public static <O> SelectorOp<O> comparing(int n, Comparator<O> comparator) {
		return (sel) -> (rng,pop) -> {
			final Selector<O> selrng = sel.apply(rng,pop);
			return Selector.generateOptional(() -> perform(n, comparator, rng, selrng));
		};
	}

	/** @see #comparing(int, Comparator) */
	public static <O extends Comparable<O>> SelectorOp<O> bestOf(int n) {
		return comparing(n, Comparator.<O>naturalOrder());
	}

	/** @see #comparing(int, Comparator) */
	public static <O extends Comparable<O>> SelectorOp<O> worstOf(int n) {
		return comparing(n, Comparator.<O>reverseOrder());
	}

	/** @see #comparing(int, Comparator) */
	public static <O> Optional<O> perform(int n, Comparator<O> comparator, Random rng, Selector<O> sel) {
		Collection<O> players = sel.takeDistinct(n);
		return CategoricalDistribution.getBestOf(rng, players, comparator);
	}

}
