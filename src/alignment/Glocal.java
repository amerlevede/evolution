package alignment;

import java.util.Random;

import alignment.algorithms.UnorderedSynapsing;
import genome.LinearGenome;

/**
 * Utility class giving access to glocal alignment.
 * Algorithmic implementations are in alignment.algorithms.
 * 
 * @author adriaan
 */
public class Glocal {

	public static <G extends LinearGenome<G>> VarOAlignment<G> alignUnorderedRepeatedLocal(Random rng, AlignmentRule<G> localAlign, int minScore, G a, G b) {
		return UnorderedSynapsing.align(rng, localAlign.apply(rng), minScore, a, b);
	}

	/**
	 * Global alignment performed by repeating a local alignment in an unordered manner.
	 * The algorithm will find the best local alignment of the two genomes, and continue the same procedure on the four parts to the left and right of the aligned region.
	 * Unlike {@link #repeatedLocal(AlignmentRule, int)}, the next alignments can be found in any combination of parts from the two genomes (as long as they are not on the same genome, that is), without regard for order. 
	 */
	public static <G extends LinearGenome<G>> VarOAlignmentRule<G> unorderedRepeatedLocal(AlignmentRule<G> localAlign, int minScore) {
		return (rng) -> (a, b) -> alignUnorderedRepeatedLocal(rng, localAlign, minScore, a, b);
	}

}
