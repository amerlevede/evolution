package crossover.binary;

import crossover.CrossoverOp;
import crossover.MessyCross;
import crossover.CrossoverRule.N;
import genome.binary.BitGenomeWithHistory;

class MessyCrossTest extends BinaryCrossoverTest {

	@Override
	public CrossoverOp<BitGenomeWithHistory> crossover(N n) {
		return MessyCross.<BitGenomeWithHistory>of(n).apply(rng);
	}

}
