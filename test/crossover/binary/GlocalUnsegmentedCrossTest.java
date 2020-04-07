package crossover.binary;

import alignment.AlignmentRule;
import alignment.Glocal;
import alignment.Local;
import alignment.VarOAlignmentRule;
import crossover.CrossoverOp;
import crossover.CrossoverRule.N;
import genome.binary.BitGenomeWithHistory;

class GlocalUnsegmentedCrossTest extends BinaryCrossoverTest {

	@Override
	public CrossoverOp<BitGenomeWithHistory> crossover(N n) {
		AlignmentRule<BitGenomeWithHistory> localAlign = Local.alignmentWithAffineGapScore(1, -5, -20, -3);
		VarOAlignmentRule<BitGenomeWithHistory> glocalAlign = Glocal.unorderedRepeatedLocal(localAlign, 10);
		return GlocalAlignmentCross.Unsegmented.of(glocalAlign, n).apply(rng);
	}

}
