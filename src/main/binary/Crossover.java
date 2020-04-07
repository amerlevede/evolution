package main.binary;

import java.util.Random;

import alignment.Alignment;
import crossover.CrossoverRule;
import genome.binary.BitGenome;
import genome.binary.BitGenomeWithHistory;

/**
 * Command line utility to inspect crossovers.
 */
public class Crossover extends BinaryWithHistoryCommandLineInterface {

	public Crossover(String[] args) {
		super(args);
	}

//	public final Option<BitGenome> genomeA = option("genomeA", this::genomeRead);
//	public final Option<BitGenome> genomeB = option("genomeB", this::genomeRead);
	
	@Override
	public void run(boolean dryrun) {
		long seed = this.seed.read();
		Random rng = new Random(seed);
		BitGenomeWithHistory genomeA = this.genomeA.read().apply(rng);
		BitGenome genomeB = this.genomeB.read().apply(rng).apply(genomeA).toBitGenome();
		CrossoverRule<BitGenomeWithHistory> crossover = this.crossover.read();

		warnUnusedVariables();

		if (!dryrun) {
			BitGenomeWithHistory parent1 = BitGenomeWithHistory.of(genomeA);
			BitGenomeWithHistory parent2 = BitGenomeWithHistory.of(genomeB);

			BitGenomeWithHistory offspring1 = parent1.copy();
			BitGenomeWithHistory offspring2 = parent2.copy();
			crossover.apply(new Random(seed)).accept(offspring1, offspring2);

			StringBuilder offspring1str = new StringBuilder();
			offspring1str.append(offspring1);
			offspring1str.append('\n');
			for (int i=0; i<offspring1.size(); i++) offspring1str.append(parent1.homologsOf(offspring1, i).count() > 0 ? "^" : " ");

			StringBuilder offspring2str = new StringBuilder();
			offspring2str.append(offspring2);
			offspring2str.append('\n');
			for (int i=0; i<offspring2.size(); i++) offspring2str.append(parent1.homologsOf(offspring2, i).count() > 0 ? "^" : " ");

			println("Offspring A:");
			println(Alignment.splitLines(offspring1str.toString(), 90, new boolean[] {true,false}));
			println("Offspring B");
			println(Alignment.splitLines(offspring2str.toString(), 90, new boolean[] {true,false}));
		}
	}
}
