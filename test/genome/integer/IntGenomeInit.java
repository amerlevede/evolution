package genome.integer;

import org.junit.jupiter.api.BeforeEach;

import genome.RandomInit;
import util.DiscreteDistribution;

public class IntGenomeInit extends RandomInit {

	public int size;

	public IntGenome range;
	public IntGenome rangeRef;

	public IntGenome perm;
	public IntGenome permRef;

	@BeforeEach
	public void intGenomeSetup() {
		size = DiscreteDistribution.getUniform(rng, 5, 500);

		range = IntGenome.range(size);
		rangeRef = range.copy().view();

		perm = IntGenome.getRandomPermutation(rng, size);
		permRef = perm.copy().view();

	}

}
