package crossover.binary;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import alignment.AlignmentOp;
import alignment.AlignmentRule;
import alignment.Local;
import crossover.CrossoverOp;
import crossover.CrossoverRule;
import genome.VarLengthGenome;
import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;
import util.Functional;
import util.IntPair;

/**
 * Utility class implementing the synapsing variable length crossover (SVLC), and a generalization based on repeated local alignment.
 * @author adriaan
 */
public final class SynapsingCross {
	
	private SynapsingCross() {
		Assert.utilityClass();
	}
	
	public static final class Generalized {
		private Generalized() {
			Assert.utilityClass();
		}
		
		public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(AlignmentRule<G> local, int minSynapseSize, CrossoverRule.N n) {
			switch (n.type) {
			case UNIFORM:
				return SynapsingCross.Generalized.nearlyUniform(local, minSynapseSize);
			case VALUE:
				return distinctN(local, minSynapseSize, n.value());
			default: throw new IllegalArgumentException();
			}
		}

		/**
		 * A generalized version of the SVLC.
		 * In standard SVLC, synapses are identified as longest common subsequences of two (subsequences of) parent genomes.
		 * Because longest common subsequences are a special case of local alignment, this version of SVLC can use any local alignment to find synapses.
		 * Instead of synapse length, the local alignment score is used as a termination condition for recursively finding synapses.
		 * Unlike traditional SVLC, the two synapses are not required to be identical, so the choise of the actual crossover point pair within the synapse matters. This is chosen uniformly randomly from each synapse.
		 * Note that this is strongly related to {@link GlobalAlignmentCross#distinctN(AlignmentRule, java.util.function.ToIntFunction)} applied to {@link SynapsingCross#alignment(AlignmentRule, int)}, however the SVLC chooses crossover points from a different distribution (one per synapse, chosen uniformly from synapses regardless of their length, instead of choosing uniformly identically and independently from all pairs).
		 */
		public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(AlignmentRule<G> localalign, int minSynapseScore, int n) {
			return (rng) -> (a, b) -> perform(alignment.algorithms.OrderedSynapsing.Generalized.getSynapses(localalign.apply(rng), minSynapseScore, a, b)::iterator, n, rng, a, b);
		}
		
		/**
		 * A nearly-uniform version of generalized SVLC.
		 * Instead of picking a fixed number of crossover points N, the number is chosen from a binomial distribution centered around half the number of synapses.
		 * @see #generalizedDistinctN(AlignmentRule, int, int)
		 */
		public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(AlignmentRule<G> localalign, int minSynapseScore) {
			return (rng) -> {
				AlignmentOp<G> alignop = localalign.apply(rng);
				return (a, b) -> {
					List<SortedSet<IntPair>> synapses = alignment.algorithms.OrderedSynapsing.Generalized.getSynapses(alignop, minSynapseScore, a, b).collect(Collectors.toList());
					int maxN = synapses.size();
					int n = DiscreteDistribution.getBinomial(rng, maxN, 0.5);
					perform(synapses, n, rng, a, b);
				};
			};
		}
		
		/** 
		 * Perform a synapsing alignment using the given set of alignments as synapses.
		 * See {@link #generalizedDistinctN(AlignmentRule, int, int)} */
		public static <G extends VarLengthGenome<G>> void perform(Iterable<SortedSet<IntPair>> synapses, int n, Random rng, G a, G b) {
			
			Iterable<SortedSet<IntPair>> filteredSynapses = Functional.streamOfIterable(synapses)
					.map((synapse) -> CrossoverOp.alignmentToCrossoverPoints(synapse, a, b)) 
					::iterator;
			
			performWithoutAddingGapPoints(filteredSynapses, n, rng, a, b);
		}
		
		public static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints(Iterable<SortedSet<IntPair>> synapses, int n, Random rng, G a, G b) {
			List<SortedSet<IntPair>> filteredSynapses = Functional.streamOfIterable(synapses)
				.filter((set) -> !set.isEmpty())
				.collect(Collectors.toList());
			
			n = Math.min(filteredSynapses.size(), n);
			
			if (n == 0) return;
			SortedSet<IntPair> pairs = 
					CategoricalDistribution.uniformIndexed(filteredSynapses).stream(rng)
					.distinct()
					.limit(n)
					.map((synapse) -> CategoricalDistribution.getUniformUnindexed(rng, synapse))
					.collect(Collectors.toCollection(TreeSet::new));
			
			CrossoverOp.performNPoint(pairs, a, b);
		}
		
	}
	
	/** 
	 * @see CrossoverRule.N
	 * @see {@link #distinctN(int, int)} */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(int minSynapseSize, CrossoverRule.N n) {
		return SynapsingCross.Generalized.of(Local.<G>longestCommonSubstring(), minSynapseSize, n);
	}	
	/**
	 * SVLC crossover operator. The crossover first identifies the longest common subsequence of the parents as a "synapse", and then recursively applies this on the left and right hand sides of the synapse until no new synapse can be found that is longer than some given sequence.
	 * After identifying the synapses, they are used as possible locations for crossover. Because the two are identical, the exact location of the crossover point within the synapse is irrelevant.
	 * Identical to {@link #generalizedDistinctN(AlignmentRule, int, int)} applied to {@link LongestCommonSubstring#alignment()}.
	 */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(int minSynapseSize, int n) {
		return (rng) -> (a, b) -> perform(minSynapseSize, n, rng, a, b);
	}
	
	/**
	 * A nearly-uniform version of the SVLC.
	 * Instead of picking a fixed number of crossover points N, the number is chosen from a binomial distribution centered around half the number of synapses.
	 * Identical to {@link #generalizedNearlyUniform(AlignmentRule, int)} applied to {@link LongestCommonSubstring#alignment()}.
	 * @see #distinctN(int, int)
	 */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(int minSynapseSize) {
		return Generalized.nearlyUniform(Local.<G>longestCommonSubstring(), minSynapseSize);
	}
	
	/** See {@link #distinctN(int, int)} */
	public static <G extends VarLengthGenome<G>>void perform(int minSynapseSize, int n, Random rng, G a, G b) {
		Generalized.performWithoutAddingGapPoints(alignment.algorithms.OrderedSynapsing.Generalized.getSynapses(Local.<G>longestCommonSubstring().apply(rng), minSynapseSize, a, b)::iterator, n, rng, a, b);
	}

}
