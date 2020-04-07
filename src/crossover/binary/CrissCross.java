package crossover.binary;

import alignment.AlignmentRule;
import alignment.Glocal;
import crossover.CrossoverRule;
import genome.VarLengthGenome;

@Deprecated
public class CrissCross {
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(AlignmentRule<G> localAlign, int minSegmentScore, CrossoverRule.N n) {
		switch (n.type) {
		case UNIFORM:
			return nearlyUniform(localAlign, minSegmentScore);
		case VALUE:
			return distinctN(localAlign, minSegmentScore, n.value());
		default: throw new IllegalArgumentException();
		}
	}
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(AlignmentRule<G> localAlign, int minSegmentScore) {
		return GlocalAlignmentCross.Segmented.nearlyUniform(Glocal.unorderedRepeatedLocal(localAlign, minSegmentScore));
	}
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(AlignmentRule<G> localAlign, int minSegmentScore, int n) {
		return GlocalAlignmentCross.Segmented.distinctN(Glocal.unorderedRepeatedLocal(localAlign, minSegmentScore), n);
	}
	





}
