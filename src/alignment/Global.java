package alignment;

import java.util.Random;

import alignment.algorithms.Affine;
import alignment.algorithms.OneGap;
import alignment.algorithms.OrderedSynapsing;
import genome.LinearGenome;
import util.Assert;

/**
 * Utility class giving access to several local alignment methods.
 * Algorithmic implementations are in alignment.algorithms.
 * 
 * @author adriaan
 */
public final class Global {
	
	private Global() {
		Assert.utilityClass();
	}
	
	/**
	 * Global alignment with an affine gap scoring scheme.
	 * Every match in the alignment gives +1 score.
	 * @param matchScore - change in score for each match. Should be positive.
	 * @param mismatchScore - change in score for each mismatch. Should be negative.
	 * @param gapOpenScore - change in score for each gap. Should be negative.
	 * @param gapExtendScore - change in score for each bit that is aligned as a gap (excluding the first bit opening the gap). Should be negative.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> alignmentWithAffineGapScore(
			int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore) 
	{
		return (rng) -> (a, b) -> alignWithAffineGapScore(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
	}
	
	/** See {@link #alignmentWithAffineGapScore(int, int, int, int)} */
	public static <G extends LinearGenome<G>> Alignment<G> alignWithAffineGapScore(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
		return Affine.NeedlemanWunsch.align(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
	}
	
	/**
	 * Global alignment that always assignes one gap (unless the genomes are the same size), in a random position.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> oneGap() {
		return (rng) -> (a, b) -> alignOneGap(rng, a, b);
	}
	
	public static <G extends LinearGenome<G>> Alignment<G> alignOneGap(Random rng, G a, G b) {
		return OneGap.align(rng, a, b);
	}
	
	/**
	 * Global alignment performed by repeating a local alignment.
	 * The algorithm will find the best local alignment of the two genomes, and continue the same procedure on the two parts to the left of the aligned region, and the two parts to the right of the aligned region.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> repeatedLocal(AlignmentRule<G> localAlign, int minScore) {
		return (rng) -> (a, b) -> alignRepeatedLocal(rng, localAlign, minScore, a, b);
	}
	
	public static <G extends LinearGenome<G>> Alignment<G> alignRepeatedLocal(Random rng, AlignmentRule<G> localAlign, int minScore, G a, G b) {
		return OrderedSynapsing.Generalized.align(localAlign.apply(rng), minScore, a, b);
	}

}