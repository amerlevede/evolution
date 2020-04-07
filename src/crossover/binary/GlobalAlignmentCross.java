package crossover.binary;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import alignment.Alignment;
import alignment.AlignmentOp;
import alignment.AlignmentRule;
import crossover.CrossoverOp;
import crossover.CrossoverRule;
import genome.VarLengthGenome;
import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;
import util.IntPair;

/**
 * A general crossover scheme guided by a global alignment.
 * These crossovers will align the genomes using the given alignment scheme, and then pick N paired bits from the alignment as crossover point pairs.
 * @author adriaan
 */
public final class GlobalAlignmentCross {
	
	private GlobalAlignmentCross() {
		Assert.utilityClass();
	}
	
	/** 
	 * @see CrossoverRule.N
	 * @see {@link #distinctN(AlignmentRule, DiscreteDistribution)} */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(AlignmentRule<G> alignrule, CrossoverRule.N n) {
		switch (n.type) {
		case UNIFORM:
			return nearlyUniform(alignrule);
		case VALUE:
			return distinctN(alignrule, n.value());
		default:
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * A crossover operator based on an alignment algorithm.
	 * It will align the two Genomes and then pick a number of crossover pairs (from given distribution) from the produced alignment.
	 * If the number of crossover points exceeds the number of possible crossover points according to the alignment (which excludes those points touching the beginning or end of one of the genomes, see {@link genome.binary.BitGenome#innerSplice}), the number will be truncated to the maximum.  
	 */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(AlignmentRule<G> alignrule, DiscreteDistribution ndist) {
		return (rng) -> {
			final AlignmentOp<G> align = alignrule.apply(rng);
			return (a, b) -> {
				int n = ndist.applyAsInt(rng);
				Alignment<G> al = align.apply(a, b);
				perform(al.getPairs(), n, rng, a, b);
			};
		};
	}
	
	/** @see #distinctN(AlignmentRule, DiscreteDistribution) */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(AlignmentRule<G> alignrule, int n) {
		return distinctN(alignrule, (ignored) -> n);
	}
		
	/**
	 * Nearly-uniform version of the alignment crossover.
	 * The number N of crossover pairs will be chosen from a binomial distribution with a mean equal to half the number of pairs in the alignment.
	 */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(AlignmentRule<G> alignrule) {
		return (rng) -> {
			final AlignmentOp<G> align = alignrule.apply(rng);
			return (a, b) -> {
				performNearlyUniform(align.apply(a, b).getPairs(), rng, a, b);
			};
		};
	}
	
	/**
	 * Pick n candidate pairs from the alignment and perform crossover.
	 */
	public static <G extends VarLengthGenome<G>> void perform(SortedSet<IntPair> alignment, int n, Random rng, G a, G b) {
		SortedSet<IntPair> candidates = CrossoverOp.alignmentToCrossoverPoints(alignment, a, b);
		performWithoutAddingGapPoints(candidates, n, rng, a, b);
	}
	
	/**
	 * Perform an alignment-based crossover with the given alignment and number of crossover points.
	 * If the alignment contains invalid crossover points (particularly, points at the zeroth or last index of a genome) they will be removed from the set.
	 * If n is greater than the number of possible crossover points the number of actual crossover points will be the maximum (i.e. crossover at all valid pairs).
	 */
	public static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints(SortedSet<IntPair> candidates, int n, Random rng, G a, G b) {
		// Cutoff n if there are not enough candidates
		n = Math.min(candidates.size(), n);
		// Take a random subset of the filtered pairs
		SortedSet<IntPair> indices =
				CategoricalDistribution.uniformUnindexed(candidates).stream(rng)
				.distinct().limit(n)
				.collect(Collectors.toCollection(TreeSet::new));
		
		// Perform cross
		CrossoverOp.performNPoint(indices, a, b);
	}
	
	public static <G extends VarLengthGenome<G>> void performNearlyUniform(SortedSet<IntPair> alignment, Random rng, G a, G b) {
		SortedSet<IntPair> candidates = CrossoverOp.alignmentToCrossoverPoints(alignment, a, b);
		int n = DiscreteDistribution.getBinomial(rng, candidates.size(), 0.5);
		performWithoutAddingGapPoints(candidates, n, rng, a, b);
	}
}
