package main.binary;

import java.util.Random;

import alignment.PerfectAlignment;
import alignment.VarOAlignment;
import genome.binary.BitGenome;
import genome.binary.BitGenomeWithHistory;
import mutation.MutationRule;
import mutation.MutationStats;

/**
 * Command line utility to inspect mutation operators and their outcomes.
 * 
 * @author adriaan
 *
 */
public class Mutate extends BinaryWithHistoryCommandLineInterface {

	public Mutate(String[] args) {
		super(args);
	}


	@Override
	public void run(boolean dryrun) {
		BitGenome genome = this.genomeA.read().apply(this.rng.read()).toBitGenome();
		long seed = this.seed.read();
		MutationRule<BitGenomeWithHistory> mutation = this.mutationWithStats.read();

		warnUnusedVariables();

		if (!dryrun) {
			BitGenomeWithHistory parent = BitGenomeWithHistory.of(genome);

			BitGenomeWithHistory offspring = parent.copy();
			MutationStats stats = mutation.apply(new Random(seed)).mutateAndGetStats(offspring).get();

			println("Mutation effects:");
			println(stats);

			println("Offspring produced:");
			println(offspring.toString());

			println("Alignment:");
			VarOAlignment<BitGenomeWithHistory> comparison = PerfectAlignment.alignFromHistory(parent, offspring); 
			println(comparison.display(90));
		}
	}
}
