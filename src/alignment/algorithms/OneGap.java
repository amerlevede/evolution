package alignment.algorithms;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import alignment.Alignment;
import alignment.AlignmentRule;
import genome.LinearGenome;
import util.Assert;
import util.IntPair;

/**
 * OneGap alignment.
 * If the genomes are different lengths, a single gap will be inserted in a random location to make up for the length difference.
 * 
 * @author adriaan
 *
 */
public class OneGap {
	
	private OneGap() {
		Assert.utilityClass();
	}

	/**
	 * A one-gap alignment: align two sequences by adding a single gap on the shortest sequence in a random location.
	 * Identical to affine with no gap extend penalty, and equal score for match or mismatch.
	 * NOTE: extremely inefficiently implemented
	 */
	public static <G extends LinearGenome<G>> AlignmentRule<G> alignment() {
		return (rng) -> (a, b) -> OneGap.align(rng, a , b);
	}

	/** See {@link oneGap} */
	public static <G extends LinearGenome<G>> Alignment<G> align(Random rng, G a, G b) {
		boolean aIsShortest = a.size() < b.size();
		int court = Math.min(a.size(), b.size());
		int longue = Math.max(a.size(), b.size());
		
		if (court == 1) {
			return Alignment.empty(a,b);
		} else {
			int gapsize = longue-court;
			int gaplocus = rng.nextInt(court);
			
			IntFunction<IntPair> indexPairing =
					aIsShortest
					? (i) -> IntPair.of(i, i>gaplocus ? i+gapsize : i)
					: (i) -> IntPair.of(i>gaplocus ? i+gapsize : i, i);
			
			SortedSet<IntPair> pairs = IntStream.range(1,court).mapToObj(indexPairing).collect(Collectors.toCollection(TreeSet::new));
			return new Alignment<>(court, pairs, a, b);
		}
	}
	
	

}
