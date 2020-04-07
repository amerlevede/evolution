package crossover.permutation;

import java.util.Random;

import crossover.CrossoverRule;
import genome.integer.IntegerGenome;
import genome.integer.Permutation;
import util.Assert;

/**
 * Implementation of Cycle crossover.
 * 
 * @see Oliver et al. (1987)
 */
public final class CycleCross {

	private CycleCross() {
		Assert.utilityClass();
	}

	public static <G extends IntegerGenome<G>> CrossoverRule<G> crossover() {
		return (rng) -> (a, b) -> perform(rng, a, b);
	}

	public static <G extends IntegerGenome<G>> void perform(Random rng, G a, G b) {
		int size = a.size();
		if (b.size() != size) throw new IllegalArgumentException();

		Permutation mappingPerm = Permutation.action(size, b.permutationView(), a.permutationView().inverse());
		int[] cycles = new int[size];
		int ncycles = mappingPerm.cyclesAndGetN(cycles);
		boolean[] swapcycle = new boolean[ncycles];
		for (int i=0; i<ncycles; i++) swapcycle[i] = rng.nextBoolean();

		for (int i=0; i<size; i++) {
			if (swapcycle[cycles[i]-1]) {
				int swp = a.get(i);
				a.set(i, b.get(i));
				b.set(i, swp);
			}
		}
	}

}
