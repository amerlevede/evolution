package crossover.binary;

import crossover.CloningCross;
import crossover.CrossoverOp;
import crossover.CrossoverRule.N;
import genome.binary.BitGenomeWithHistory;

class SynapsingCrossTest extends BinaryCrossoverTest {

	@Override
	public CrossoverOp<BitGenomeWithHistory> crossover(N n) {
		return CloningCross.<BitGenomeWithHistory>crossover().apply(rng);
	}

}
