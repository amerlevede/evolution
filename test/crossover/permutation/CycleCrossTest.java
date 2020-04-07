package crossover.permutation;

import crossover.CrossoverOp;
import genome.integer.IntGenome;

class CycleCrossTest extends PermutationCrossoverTest {

	@Override
	public CrossoverOp<IntGenome> crossover() {
		return CycleCross.<IntGenome>crossover().apply(rng);
	}

}
