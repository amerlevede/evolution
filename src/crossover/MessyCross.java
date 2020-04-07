package crossover;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import genome.VarLengthGenome;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Utility class implementing the messy crossover algorithm.
 * @author adriaan
 */
public final class MessyCross {
	
	private MessyCross() {
		Assert.utilityClass();
	}
	
	/** 
	 * @see CrossoverRule.N
	 * @see {@link #distinctN(DiscreteDistribution)} */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(CrossoverRule.N n) {
		switch (n.type) {
		case UNIFORM:
			return nearlyUniform();
		case VALUE:
			return distinctN(n.value());
		default:
			throw new IllegalArgumentException();
		}
	}
    
    /**
     * A crossover operator that picks random points on the two Genomes to cross.
     * The number of crossover points is selected from the given distribution.
     * If the produced number exceeds the maximum of possible crossover points (i.e. the length of the shortest genome minus the two ends, at which crossover is not allowed), the highest possible number is used.
     */
    public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(DiscreteDistribution ndist) {
        return (rng) -> (a, b) -> {
            int n = ndist.applyAsInt(rng);
            MessyCross.perform(n, rng, a, b);
        };
    }
    
    /** See {@link #distinctN(ToIntFunction)} */
    public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(int n) {
        return MessyCross.distinctN((ignored) -> n);
    }

    /**
     * Nearly-uniform version of the messy cross.
     * The number of crossover point pairs N is chosen from a binomial distribution centered around half the length of the shortest genome. 
     */
    public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform() {
        return (rng) -> (a, b) -> {
            int n = DiscreteDistribution.getBinomial(rng, maxN(a, b), 0.5);
            MessyCross.perform(n, rng, a, b);
        };
    }
    
    /** See {@link #distinctN(int)} */
    public static <G extends VarLengthGenome<G>> void perform(int n, Random rng, G a, G b) {
		// Truncate n if necessary
		n = Math.min(n, maxN(a, b));
		if (n > 0) {
			// Find random distinct locations to cross over
			// NOTE: This implementation may take a long time to find random points when n is close to #maxN.
		    SortedSet<Integer> ia = rng.ints(1, a.size()).distinct().limit(n).boxed().collect(Collectors.toCollection(TreeSet::new));
		    SortedSet<Integer> ib = rng.ints(1, b.size()).distinct().limit(n).boxed().collect(Collectors.toCollection(TreeSet::new));
		    // Do n-point crossover
		    CrossoverOp.performNPoint(a, ia, b, ib);
		}
	}

	/**
	 * The maximum number of possible crossover points in a Messy crossover with the given Genomes.
	 * It equals the length of the smallest genome, excluding the two ends.
	 */
	public static <G extends VarLengthGenome<G>> int maxN(G a, G b) {
		return Math.min(a.size(), b.size())-1;
	}
    
}
