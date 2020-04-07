package crossover.permutation;

import crossover.CloningCross;
import crossover.CrossoverOp;
import genome.integer.IntGenome;

class CloningCrossTest extends PermutationCrossoverTest {

	@Override
	public CrossoverOp<IntGenome> crossover() {
		return CloningCross.<IntGenome>crossover().apply(rng);
	}

}
