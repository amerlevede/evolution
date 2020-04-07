package main.permutation;

import java.util.Random;
import java.util.function.Function;

import crossover.CrossoverOp;
import genome.integer.IntGenome;
import mutation.MutationRule;

/**
 * Command line utility to inspect crossover outcomes.
 * 
 * @author adriaan
 *
 */
public class Crossover extends PermutationCommandLineInterface<IntGenome> {

	public Crossover(String[] args) {
		super(args);
	}

	public final Option<Function<Random,IntGenome>> genomeA = optionWithStringDefault("genomeA", "random", optionValue -> {
		switch (optionValue) {
		case "random": {
			int size = this.size.read();
			return IntGenome.randomPermutation(size);
		}
		case "range": {
			int size = this.size.read();
			return rng -> IntGenome.range(size);
		}
		default:
			throw new IllegalArgumentException("Unrecognized value for option genomeA");
		}
	});
	public final Option<Function<Random,Function<IntGenome,IntGenome>>> genomeB = optionWithStringDefault("genomeB", "random", optionValue -> {
		switch (optionValue) {
		case "random":
			return rng -> genomeA -> IntGenome.getRandomPermutation(rng, genomeA.size());
		case "range":
			return rng -> genomeA -> IntGenome.range(genomeA.size());
		case "mutated": {
			MutationRule<IntGenome> mutation = this.mutation.read();
			return rng -> genomeA -> {
				IntGenome result = genomeA.copy();
				mutation.apply(rng).mutate(result);
				return result;
			};
		}
		case "identical":
			return rng -> genomeA -> genomeA.copy();
		case "reversed":
			return rng -> genomeA -> genomeA.reversedView().copy();
		case "reversedrange":
			return rng -> genomeA -> IntGenome.range(genomeA.size()).reversedView();
		case "reversedmutated": {
			MutationRule<IntGenome> mutation = this.mutation.read();
			return rng -> genomeA -> {
				IntGenome result = genomeA.reversedView().copy();
				mutation.apply(rng).mutate(result);
				return result;
			};
		}
		default:
			throw new IllegalArgumentException("Unrecognized value for option genomeB");
		}
	});

	@Override
	public void run(boolean dryrun) {
		int cycles = this.cycles.read();
		Random rng = this.rng.read();
		CrossoverOp<IntGenome> cross = this.crossover.read().apply(rng);
		Function<Random,IntGenome> genomeAgen = this.genomeA.read();
		Function<Random,Function<IntGenome,IntGenome>> genomeBgen = this.genomeB.read();

		if (!dryrun) {
			for (int i=0; i<cycles; i++) {
				IntGenome parent1 = genomeAgen.apply(rng);
				IntGenome parent2 = genomeBgen.apply(rng).apply(parent1);

				IntGenome offspring1 = parent1.copy();
				IntGenome offspring2 = parent2.copy();
				cross.accept(offspring1, offspring2);

				println("Parent A: "+parent1);
				println("Parent B: "+parent2);
				println("Offspring A: "+offspring1);
				println("Offspring B: "+offspring2);

			}
		}
	}

}
