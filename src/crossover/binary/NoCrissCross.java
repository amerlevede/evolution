package crossover.binary;

import alignment.Alignment;
import alignment.AlignmentRule;
import alignment.Global;
import crossover.CrossoverRule;
import genome.VarLengthGenome;
import util.Assert;

@Deprecated
public final class NoCrissCross {
	
	private NoCrissCross() {
		Assert.utilityClass();
	}
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(AlignmentRule<G> local, int minSynapseSize, CrossoverRule.N n) {
		switch (n.type) {
		case UNIFORM:
			return nearlyUniform(local, minSynapseSize);
		case VALUE:
			return distinctN(local, minSynapseSize, n.value());
		default: throw new IllegalArgumentException();
		}
	}
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(AlignmentRule<G> localAlign, int minScore, int n) {
		return (rng) -> (a, b) -> {
			Alignment<G> ali = Global.alignRepeatedLocal(rng, localAlign, minScore, a, b);
			GlobalAlignmentCross.perform(ali.getPairs(), n, rng, a, b);
//			List<SortedSet<IntPair>> segments = Synapsing.Generalized.getSynapses(localAlign.apply(rng), minScore, a, b).collect(Collectors.toList());
//			VarOAlignment ali = new VarOAlignment(segments, a, b);
//			ali.simplify();
//			CrissCross.perform(rng, ali.getSegments(), n, a, b);
		};
	}
	
	public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(AlignmentRule<G> localAlign, int minScore) {
		return (rng) -> (a, b) -> {
			Alignment<G> ali = Global.alignRepeatedLocal(rng, localAlign, minScore, a, b);
			GlobalAlignmentCross.performNearlyUniform(ali.getPairs(), rng, a, b);
//			List<SortedSet<IntPair>> segments = Synapsing.Generalized.getSynapses(localAlign.apply(rng), minScore, a, b).collect(Collectors.toList());
//			VarOAlignment ali = new VarOAlignment(segments, a, b);
//			ali.simplify();
//			CrissCross.performNearlyUniform(rng, ali.getSegments(), a, b);
		};
	}

}
