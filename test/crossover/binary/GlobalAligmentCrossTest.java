package crossover.binary;

import alignment.AlignmentRule;
import alignment.Global;
import crossover.CrossoverOp;
import crossover.CrossoverRule.N;
import genome.binary.BitGenomeWithHistory;

class GlobalAligmentCrossTest extends BinaryCrossoverTest {

	@Override
	public CrossoverOp<BitGenomeWithHistory> crossover(N n) {
		AlignmentRule<BitGenomeWithHistory> align = Global.alignmentWithAffineGapScore(1, -5, -20, -3);
		return GlobalAlignmentCross.<BitGenomeWithHistory>of(align, n).apply(rng);
	}

}
