package alignment;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import alignment.algorithms.Affine;
import alignment.algorithms.LocalAround;
import alignment.algorithms.Gapless;
import genome.LinearGenome;
import genome.binary.BitGenome;
import util.Assert;
import util.IntPair;

/**
 * Utility class giving access to several local alignment methods.
 * Algorithmic implementations are in alignment.algorithms.
 * 
 * @author adriaan
 */
public final class Local {
	
	private Local() {
		Assert.utilityClass();
	}
	
	/**
	 * Local alignment with an affine gap scoring scheme.
	 * The local alignment differs from the global alignment in that it only aligns subsequences, ignoring any less optimal parts on the side.
	 * More negative score penalties result in a more "local" alignment.
	 * @param matchScore - change in score for each match. Should be positive.
	 * @param mismatchScore - change in score for each mismatch. Should be negative.
	 * @param gapOpenScore - change in score for each gap. Should be negative.
	 * @param gapExtendScore - change in score for each bit that is aligned as a gap (excluding the first bit opening the gap). Should be negative.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> alignmentWithAffineGapScore(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore) {
		return (rng) -> (a, b) -> alignWithAffineGapScore(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
	}
	
	/** See {@link #alignmentWithAffineGapScore(int, int, int, int)} */
	public static <G extends LinearGenome<G>> Alignment<G> alignWithAffineGapScore(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
		if (-gapOpenScore > Math.min(a.size(), b.size())*matchScore) { // If opening one gap costs more than any score that is possible to reach, cheat and use the more efficient algorithm without gaps  
			return Gapless.align(rng, matchScore, mismatchScore, a, b);
		} else {
			return Affine.SmithWaterman.align(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
		}		
	}
	
	/**
	 * Local alignment allowing no gaps.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> alignmentWithNoGaps(int matchScore, int mismatchScore) {
		return (rng) -> (a, b) -> alignWithNoGaps(matchScore, mismatchScore, rng, a, b);
	}
	
	public static <G extends LinearGenome<G>> Alignment<G> alignWithNoGaps(int matchScore, int mismatchScore, Random rng, G a, G b) {
		return Gapless.align(rng, matchScore, mismatchScore, a, b);
	}

	/**
	 * Local alignment with affine gap score.
	 * The difference between this and {@link #alignmentWithAffineGapScore(int, int, int, int)} is that it does not always return the (an) alignment with the highest score.
	 * It first takes a random point on one of the two genomes and then constrains the resulting alignment to only those that contain the chosen point.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> alignmentAroundRandomWithAffineGapScore(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore) {
		return (rng) -> (a, b) -> alignAroundRandomWithAffineGapScore(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
	}
	
	public static <G extends LinearGenome<G>> Alignment<G> alignAroundRandomWithAffineGapScore(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
		boolean targetIndexIsOnGenomeA = rng.nextDouble() < ((double)a.size())/((double)(a.size()+b.size()));
		int targetIndex = rng.nextInt((targetIndexIsOnGenomeA ? a : b).size());
		return LocalAround.align(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b, targetIndexIsOnGenomeA, targetIndex);
	}
	
	/**
	 * A local alignment that will find the longest common substring between two genomes.
	 * That is the longest set of consecutive, identical bits.
	 * This is equivalent to a local alignment with infinite mismatch and gap penalty.
	 * Not to be confused with longest common subsequence, which is a global alignment type.
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> longestCommonSubstring() {
		return (rng) -> (a, b) -> alignLongestCommonSubstring(rng, a, b);
	}
	
	public static <G extends LinearGenome<G>> Alignment<G> alignLongestCommonSubstring(Random rng, G a, G b) {
		return Gapless.align(rng, 1, -a.size()-b.size(), a, b);
	}
	
	
	/** See {@link #alignInRange(AlignmentOp, BitGenome, int, int, BitGenome, int, int)} */
	public static <G extends LinearGenome<G>> Alignment<G> alignmentInRange(AlignmentRule<G> alignrule, Random rng, G a, int aInclusiveStart, int aExclusiveEnd, G b, int bInclusiveStart, int bExclusiveEnd) {
		return alignInRange(alignrule.apply(rng), a, aInclusiveStart, aExclusiveEnd, b, bInclusiveStart, bExclusiveEnd);
	}

	/**
	 * Perform a given alignment algorithm in a restricted range of the genomes.
	 * The score of the alignment is also restricted, i.e. it will not take into account gaps in the excluded range (this does not matter for local alignments).
	 */
	public static <G extends LinearGenome<G>> Alignment<G> alignInRange(AlignmentOp<G> align, G a, int aInclusiveStart, int aExclusiveEnd, G b, int bInclusiveStart, int bExclusiveEnd) {
		Alignment<G> alignresult = align.apply(a.view(aInclusiveStart, aExclusiveEnd), b.view(bInclusiveStart, bExclusiveEnd));
		// Shift resulting pairs to fit in the frame of a and b
		UnaryOperator<IntPair> shift = (p) -> IntPair.of(p.x + aInclusiveStart, p.y + bInclusiveStart);
		SortedSet<IntPair> pairs = alignresult.getPairs().stream().map(shift).collect(Collectors.toCollection(TreeSet::new));
		// Done
		return new Alignment<>(alignresult.score, pairs, a, b);
	}

}
