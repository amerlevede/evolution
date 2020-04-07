package crossover.binary;

import alignment.Global;
import crossover.CrossoverOp;
import crossover.CrossoverRule.N;
import genome.binary.BitGenomeWithHistory;

class OneGapCrossTest extends BinaryCrossoverTest {

	@Override
	public CrossoverOp<BitGenomeWithHistory> crossover(N n) {
		return GlobalAlignmentCross.<BitGenomeWithHistory>of(Global.oneGap(), n).apply(rng);
	}

}
