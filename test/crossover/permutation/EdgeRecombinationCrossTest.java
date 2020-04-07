package crossover.permutation;

import crossover.CrossoverOp;
import genome.integer.IntGenome;

class EdgeRecombinationCrossTest extends PermutationCrossoverTest {

	@Override
	public CrossoverOp<IntGenome> crossover() {
		return EdgeRecombinationCross.<IntGenome>crossover().apply(rng);
	}

}
