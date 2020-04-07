package crossover.score;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import genome.binary.BitGenomeWithHistory;
import util.DiscreteDistribution;

/**
 * Linkage score.
 * Use: LinkageScore::score is a {@link CrossoverScore}.
 */
public class LinkageScore {
	
	static int distributeUniqueTags_01(Random rng, BitGenomeWithHistory a, BitGenomeWithHistory b, boolean[] inheritanceA, boolean[] inheritanceB, boolean[] uniqueA, boolean[] uniqueB) {
		int maxCrossovers = 0;

		for (boolean targetInheritance : new boolean[] {true,false}) {
		for (long family : LongStream.concat(a.uniqueIds(), b.uniqueIds()).distinct().toArray()) {
			int[] onA0 = a.homologs(family).filter(i->!a.get(i) && inheritanceA[i] == targetInheritance).toArray();
			int[] onB1 = b.homologs(family).filter(i-> b.get(i) && inheritanceB[i] != targetInheritance).toArray();
			
			int match01 = Math.min(onA0.length, onB1.length);
			if (match01 == 0) continue;
			maxCrossovers += match01;
			
			DiscreteDistribution.uniform(onA0).stream(rng).distinct()
				.limit(match01)
				.forEach(i -> uniqueA[i] = true);
			DiscreteDistribution.uniform(onB1).stream(rng).distinct()
				.limit(match01)
				.forEach(i -> uniqueB[i] = true);
		}
		}
		
		return maxCrossovers;
	}
	
	static int countUniqueSections(boolean[] inheritance, boolean[] unique) {
		final int size = inheritance.length;
		int actualCrossovers = 0;
		
		boolean firstUniqueBit = true;
		boolean lastBitIsOnOffspringA = inheritance[0];
		for (int i=0; i<size; i++) {
			if (unique[i]) {
				boolean thisBitIsOnOffspringA = inheritance[i];
				if (firstUniqueBit) {
					firstUniqueBit = false;
					lastBitIsOnOffspringA = thisBitIsOnOffspringA;
				} else if (lastBitIsOnOffspringA != thisBitIsOnOffspringA) {
					actualCrossovers++;
					lastBitIsOnOffspringA = thisBitIsOnOffspringA;
				}
			}
		}
		
		return actualCrossovers;
	}
	
	public static CrossoverScore score(Random rng) {
		return (crossover) -> {
			boolean[] inheritanceA = new boolean[crossover.parentA.size()];
			for (int i=0; i<crossover.parentA.size(); i++)
				if (crossover.offspringResetA.homologsOf(crossover.parentResetA, i).findAny().isPresent())
					inheritanceA[i] = true;
			boolean[] inheritanceB = new boolean[crossover.parentB.size()];
			for (int i=0; i<crossover.parentB.size(); i++)
				if (crossover.offspringResetA.homologsOf(crossover.parentResetB, i).findAny().isPresent())
					inheritanceB[i] = true;
			
			boolean[] uniqueA = new boolean[crossover.parentA.size()];
			boolean[] uniqueB = new boolean[crossover.parentB.size()];
			
			int maxCrossovers = -2;
			maxCrossovers += 2*distributeUniqueTags_01(rng, crossover.parentA, crossover.parentB, inheritanceA, inheritanceB, uniqueA, uniqueB);
			maxCrossovers += 2*distributeUniqueTags_01(rng, crossover.parentB, crossover.parentA, inheritanceB, inheritanceA, uniqueB, uniqueA);
			if (maxCrossovers <= 0) return OptionalDouble.empty();
			
			int actualCrossovers = 0;
			actualCrossovers += countUniqueSections(inheritanceA, uniqueA);
			actualCrossovers += countUniqueSections(inheritanceB, uniqueB);
			
			return OptionalDouble.of(((double)actualCrossovers) / ((double)maxCrossovers));
		};
	}

	
    /**
     * An older, more complicated way of calculating the linkage score.
     */
	@Deprecated
	public static OptionalDouble score_deprecated(CrossoverOutcome crossover) {
		int[] lft = calculateLinkageScoreHalf(crossover);
		int[] rgt = calculateLinkageScoreHalf(crossover.flip());

		int separatedConsecutiveMutants = lft[0] + rgt[0];
		int totalConsecutiveMutants     = lft[1] + rgt[1];

		return totalConsecutiveMutants == 0
				? OptionalDouble.empty()
				: OptionalDouble.of(((double) separatedConsecutiveMutants) / ((double) totalConsecutiveMutants));
	}

	static int[] calculateLinkageScoreHalf(CrossoverOutcome crossover) {
		// Loop over c1 bits, comparing parent of origin of consecutive mutants
		boolean thisIsTheFirstMutant = true; // Can't compare first value to previous
		boolean lastParentOfOriginIsP1 = false; // Dummy initialized value will be overwritten before first use
		int separatedConsecutiveMutants = 0;
		int totalConsecutiveMutants = 0;
		for (int i=0; i<crossover.offspringA.size(); i++) {
			// This bit is considered mutant if it has no "twins" (bits with identical value and context) on the other genome, but it does have a homologously inherited partner
			boolean thisBitIsMutant = bitIsMutant(i, crossover);

			if (thisBitIsMutant) {
				// There was (an odd number of) crossover between this and last mutant iff the bits come from different parents
				boolean parentOfOriginIsP1 = crossover.parentResetA.homologsOf(crossover.offspringResetA, i).count() > 0;
				// Skip first mutant, there is no previous value to compare to
				if (thisIsTheFirstMutant) {
					thisIsTheFirstMutant = false;
				} else {
					// Increment total mutants
					totalConsecutiveMutants++;
					// Increment separated mutants only if this is inherited from different parent from previous mutant
					if (parentOfOriginIsP1 != lastParentOfOriginIsP1) {
						separatedConsecutiveMutants++;
					}
				}
				lastParentOfOriginIsP1 = parentOfOriginIsP1;
			}
		}

		return new int[] { separatedConsecutiveMutants, totalConsecutiveMutants };
	}

	/**
	 * Check if a bit is a "mutant" (or polymorphic) for the purposes of the linkage score.
	 * A bit on offspring A is considered polymorphic iff
	 *  (1) the bit was inherited in a homologous manner: the list of homologous bits on offspring B, looking only at those inherited from a different parent, is non-empty
	 *  (2) no homolog-from-a-different-parent on the offspring B is a "twin" (has the same value (0/1) and context (historical identity of left and right positions))
	 */
	@Deprecated
	static boolean bitIsMutant(int indexOnOffspringA, CrossoverOutcome crossover) {
		List<Integer> homologsInheritedFromDifferentParent =
				crossover.offspringB.homologsOf(crossover.offspringA, indexOnOffspringA) // Homologs of this ...
				.filter((j) -> crossover.parentResetA.homologsOf(crossover.offspringResetA,  indexOnOffspringA).count()  // this side of the inequality is 1 iff thisgenome:i is inherited from "aparent"
						    != crossover.parentResetA.homologsOf(crossover.offspringResetB, j).count()) // this side of the inequality is 1 iff othergenome:j is inherited from "aparent"
				.boxed().collect(Collectors.toList());
		if (homologsInheritedFromDifferentParent.isEmpty()) {
			return false;
		} else {
			return homologsInheritedFromDifferentParent.stream().mapToInt(Integer::intValue)
				   .noneMatch((j) -> isTwinDontCheckHomology(indexOnOffspringA, j, crossover));
		}
	}

	/**
	 * Check if bits on two siblings are "twins".
	 * Two bits are twins if
	 *  (1) they have the same homologous ID,
	 *  (2) they were inherited from different parents,
	 *  (3) they have the same value (0/1), and
	 *  (4) the bits to the left and right on each genome have the same respective homologous IDs.
	 * This measure is used to identify polymorphic/mutant sites that can be meaningfully separated during crossover, which is useful to calculate the linkage score.
	 *
	 * @param offspringA - First genome
	 * @param offspringResetA - Version of thisgenome with overridden id values that inform of direct parental heritage instead of more distant homology
	 * @param indexOnOffspringA - index of bit of interest on thisgenome
	 * @param offspringB - Second genome
	 * @param offspringResetB - Version of thisgenome with overridden id values that inform of direct parental heritage instead of more distant homology
	 * @param indexOnOffspringB - index of bit of interest on othergenome
	 * @param parentResetA - Genome of one of the two parents with "fresh" id values, i.e. corresponding to thisgenomeH and othergenomeH.
	 */
	@Deprecated
	static boolean isTwin(int indexOnOffspringA, int indexOnOffspringB, CrossoverOutcome crossover) {
		return crossover.offspringA.isHomologousTo(indexOnOffspringA, crossover.offspringB, indexOnOffspringB)
			&& crossover.parentResetA.homologsOf(crossover.offspringA, indexOnOffspringA).count() != crossover.parentResetA.homologsOf(crossover.offspringB, indexOnOffspringB).count()
			&& isTwinDontCheckHomology(indexOnOffspringA, indexOnOffspringB, crossover);
	}

	/**
	 * {@link #isTwin(BitGenomeWithHistory, BitGenomeWithHistory, int, BitGenomeWithHistory, BitGenomeWithHistory, int, BitGenomeWithHistory)} without checking requirement (1) and (2).
	 */
	@Deprecated
	static boolean isTwinDontCheckHomology(int indexOnOffspringA, int indexOnOffspringB, CrossoverOutcome crossover) {
		boolean sameValue =
				crossover.offspringA.get(indexOnOffspringA) == crossover.offspringB.get(indexOnOffspringB);

		boolean sameContext =
				(indexOnOffspringA==0
					|| indexOnOffspringB==0
					|| crossover.offspringA.getId(indexOnOffspringA-1) == crossover.offspringB.getId(indexOnOffspringB-1))
			 && (indexOnOffspringA==crossover.offspringA.size()-1
			 		|| indexOnOffspringB==crossover.offspringB.size()-1
			 		|| crossover.offspringA.getId(indexOnOffspringA+1) == crossover.offspringB.getId(indexOnOffspringB+1));

		return sameValue && sameContext;
	}

}
