package main.binary;

import java.util.Random;
import java.util.function.Function;

import genome.binary.BitGenomeWithHistory;
import mutation.MutationRule;

public abstract class BinaryWithHistoryCommandLineInterface extends BinaryCommandLineInterface<BitGenomeWithHistory> {
	
	public BinaryWithHistoryCommandLineInterface(String[] args) {
		super(args);
	}
	
	public final Option<Function<Random,BitGenomeWithHistory>> genomeA = optionWithStringDefault("genomeA", "random", optionValue -> {
		switch (optionValue) {
		case "random": {
			int size = this.genomeLength.read();
			return BitGenomeWithHistory.random(size)::apply;
		}
		default:
			throw new IllegalArgumentException("Unrecognized value for option genomeA");
		}
	});
	
	public final Option<Function<Random,Function<BitGenomeWithHistory,BitGenomeWithHistory>>> genomeB = optionWithStringDefault("genomeB", "random", optionValue -> {
		switch (optionValue) {
		case "random": {
			int size = this.genomeLength.read();
			return rng -> g -> BitGenomeWithHistory.getRandom(rng, r -> size);
		}
		case "mutated": {
			MutationRule<BitGenomeWithHistory> mutation = this.mutation.read();
			return rng -> genomeA -> {
				BitGenomeWithHistory result = genomeA.copy();
				mutation.apply(rng).mutate(result);
				return result;
			};
		}
		case "identical":
			return rng -> genomeA -> genomeA.copy();
		case "reversed":
			return rng -> genomeA -> genomeA.reversedView().copy();
		case "reversedmutated": {
			MutationRule<BitGenomeWithHistory> mutation = this.mutation.read();
			return rng -> genomeA -> {
				BitGenomeWithHistory result = genomeA.reversedView().copy();
				mutation.apply(rng).mutate(result);
				return result;
			};
		}
		default:
			throw new IllegalArgumentException("Unrecognized value for option genomeB");
		}
	});

}
