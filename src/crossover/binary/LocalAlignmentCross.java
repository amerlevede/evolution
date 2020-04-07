package crossover.binary;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import alignment.Alignment;
import alignment.AlignmentOp;
import alignment.AlignmentRule;
import crossover.CrossoverRule;
import genome.VarLengthGenome;
import util.Assert;
import util.IntPair;

/**
 * A crossover method that only exchanges information within a supplied local alignment.
 * This will have a lower linkage than {@link GlobalAlignmentCross#of(AlignmentRule, crossover.CrossoverRule.N)}, but avoid misalignment when there is out-of-order homology.
 * 
 * @author adriaan
 */
public class LocalAlignmentCross {
	
	private LocalAlignmentCross() {
		Assert.utilityClass();
	}
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(AlignmentRule<G> localAlign, CrossoverRule.N n) {
		switch (n.type) {
		case UNIFORM:
			return nearlyUniform(localAlign);
		case VALUE:
			return distinctN(localAlign, n.value());
		default: throw new IllegalArgumentException();
		}
	}

	public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(AlignmentRule<G> localAlign) {
		return (rng) -> {
			final AlignmentOp<G> alignOp = localAlign.apply(rng);
			return (a, b) -> {
				Alignment<G> ali = alignOp.apply(a, b);
				performNearlyUniform(ali.getPairs(), rng, a, b);
			};
		};
	}
	
	/**
	 * @param n - The number of crossovers to perform in the section. Should be even to avoid non-homologous rearrangement of the genome that comes afterthe aligned region.
	 */
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(AlignmentRule<G> localAlign, int n) {
		return (rng) -> {
			final AlignmentOp<G> alignOp = localAlign.apply(rng);
			return (a, b) -> {
				Alignment<G> ali = alignOp.apply(a, b);
				perform(ali.getPairs(), n, rng, a, b);
			};
		};
	}
	
	public static <G extends VarLengthGenome<G>> void perform(SortedSet<IntPair> segment, int n, Random rng, G a, G b) {
		GlocalAlignmentCross.Segmented.perform(rng, List.of(segment), n, a, b);
	}
	
	public static <G extends VarLengthGenome<G>> void performNearlyUniform(SortedSet<IntPair> segment, Random rng, G a, G b) {
		GlocalAlignmentCross.Segmented.performNearlyUniform(List.of(segment), rng, a, b);
	}

}
