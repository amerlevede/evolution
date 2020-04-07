package crossover.binary;

import crossover.CrossoverOp;
import crossover.CrossoverRule.N;
import genome.binary.BitGenomeWithHistory;

class CloningCrossTest extends BinaryCrossoverTest {

	@Override
	public CrossoverOp<BitGenomeWithHistory> crossover(N n) {
		return SynapsingCross.<BitGenomeWithHistory>of(10, n).apply(rng);
	}

}
