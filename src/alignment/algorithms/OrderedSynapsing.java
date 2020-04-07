package alignment.algorithms;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.stream.Stream;

import alignment.Alignment;
import alignment.AlignmentOp;
import alignment.AlignmentRule;
import alignment.Local;
import crossover.binary.SynapsingCross;
import genome.LinearGenome;
import util.Assert;
import util.IntPair;

/**
 * Global alignment implemented by repeated local alignment.
 * That is, a local alignment is performed; then, the procedure is repeated to align the parts of the parents to the left of the local alignment, and also the parts to the right of the local alignment. This is repeated recursively until the next synapse is below a threshold alignment score.
 * This is an implementation of synapsing variable-length crossover (SVLC) as an alignment.
 * In SVLC, the local alignment method is the longest common substring. {@link Generalised} generalises this to arbitrary local alignment methods. 
 * 
 * @author adriaan
 */
public final class OrderedSynapsing {
	
	private OrderedSynapsing() {
		Assert.utilityClass();
	}
	
	/**
	 * Generalisation of synapsing variable-length crossover (SVLC) using any local alignment, not just longest common substring.
	 * 
	 * @author adriaan
	 */
	public static class Generalized {
		
		private Generalized() {
			Assert.utilityClass();
		}

		public static <G extends LinearGenome<G>> Stream<SortedSet<IntPair>> getSynapses(AlignmentOp<G> localAlign, int minSynapseScore, G a, G b) {
			return Generalized.alignSynapses(localAlign, minSynapseScore, a, b).stream()
					.map(Alignment::getPairs);
		}

		/**
		 * A global alignment executed by repeating local alignment in a divide-and-conquer fashion.
		 * That is, the algorithm will first find the best local alignment, and then repeat itself on the sections to the left and to the right. The recursion terminates when given a sequence smaller than the given minSynapseSize or if the local alignment on that sequence has fewer matches than minSynapseScore.
		 * This style of alignment is inspired by the Synapsing Variable Length Crossover (SVLC), which can be seen as applying this style of global alignment, where the local alignment the special case of longest common subsequence search. (However the actual SLVC has a slightly different distribution of crossover points than a general AlignmentCross applied in this way.)
		 * @see SynapsingCross 
		 * @param localAlign - The local alignment rule to use.
		 * @param minSynapseScore - The minimum length requirement of a synapse (in number of matches, *not* in distance between first and last match).
		 */
		public static <G extends LinearGenome<G>> AlignmentRule<G> alignment(AlignmentRule<G> localAlign, int minSynapseScore) {
			return (rng) -> (a, b) -> Generalized.align(localAlign.apply(rng), minSynapseScore, a, b);
		}

		public static <G extends LinearGenome<G>> Alignment<G> align(AlignmentOp<G> localalign, int minSynapseScore, G a, G b) {
			return Alignment.collate(Generalized.alignSynapses(localalign, minSynapseScore, a, b), a, b);
		}

		public static <G extends LinearGenome<G>> List<Alignment<G>> alignSynapses(AlignmentOp<G> localalign, int minSynapseScore, G a, G b) {
			return Generalized.alignSynapsesInRange(localalign, minSynapseScore, a, 0, a.size(), b, 0, b.size());
		}

		public static <G extends LinearGenome<G>> List<Alignment<G>> alignSynapsesInRange(AlignmentOp<G> localAlign, int minSynapseScore, G a, int aInclusiveStart, int aExclusiveEnd, G b, int bInclusiveStart, int bExclusiveEnd) {
			List<Alignment<G>> result = new LinkedList<>();
			if (aExclusiveEnd - aInclusiveStart >= 1 && bExclusiveEnd - bInclusiveStart >= 1) { // For non-exotic scoring schemes, could also compare to minSynapseScore 
				// Create new synapse
				Alignment<G> synapse = Local.alignInRange(localAlign, a, aInclusiveStart, aExclusiveEnd, b, bInclusiveStart, bExclusiveEnd);
				if (synapse.score >= minSynapseScore && synapse.getPairs().size() > 0) {
					result.add(synapse);
					
					// Recurse to the left
					int leftastart = aInclusiveStart;
					int leftbstart = bInclusiveStart;
					int leftaend = synapse.getPairs().first().x;
					int leftbend = synapse.getPairs().first().y;
					if (leftaend - leftastart >= 1 && leftbend - leftbstart >= 1) {
						List<Alignment<G>> leftRecurse = alignSynapsesInRange(localAlign, minSynapseScore, a, leftastart, leftaend, b, leftbstart, leftbend); 
						result.addAll(0, leftRecurse);
					}
					
					// Recurse to the right
					int rightastart = synapse.getPairs().last().x+1;
					int rightbstart = synapse.getPairs().last().y+1;
					int rightaend = aExclusiveEnd;
					int rightbend = bExclusiveEnd;
					if (rightaend - rightastart >= 1 && rightbend - rightbstart >= 1) {
						List<Alignment<G>> rightRecurse = alignSynapsesInRange(localAlign, minSynapseScore, a, rightastart, rightaend, b, rightbstart, rightbend);
						result.addAll(rightRecurse);
					}
				}
			}
			return result;
		}
		
		
	}

	public static <G extends LinearGenome<G>> AlignmentRule<G> alignment(int minSynapseSize) {
		return (rng) -> (a, b) -> align(rng, minSynapseSize, a, b);
	}

	public static <G extends LinearGenome<G>> Alignment<G> align(Random rng, int minSynapseSize, G a, G b) {
		AlignmentRule<G> localAlign = Local.longestCommonSubstring();
		return Generalized.align(localAlign.apply(rng), minSynapseSize, a ,b);
	}
	
	

}
