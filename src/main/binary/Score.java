package main.binary;

import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;

import crossover.CrossoverRule;
import crossover.score.CrossoverOutcome;
import crossover.score.CrossoverScore;
import crossover.score.HomologyScore;
import crossover.score.LinkageScore;
import genome.binary.BitGenome;
import genome.binary.BitGenomeWithHistory;
import mutation.MutationRule;
import util.Functional;

/**
 * Command line utility to calculate crossover scores based on repeated application on many different random parents.
 * 
 * @author adriaan
 *
 */
public class Score extends BinaryCommandLineInterface<BitGenomeWithHistory> {

	public Score(String[] args) {
		super(args);
	}

	public final Option<CrossoverScore> score = option(
			"type",
			(optionValue) -> {
				switch (optionValue) {
				case "homology":
					return HomologyScore::score;
				case "linkage":
					return LinkageScore.score(rng.read());
				default:
					throw new IllegalArgumentException();
				}
			});

	public final Option<String> stat = option("stat", "average", s -> s);

	@Override
	public void run(boolean dryrun) {
		CrossoverScore scoretype    = this.score.read();
		MutationRule<BitGenomeWithHistory> mutationRule   = this.mutation.read();
		CrossoverRule<BitGenomeWithHistory> crossoverRule = this.crossover.read();
		int genomeLength            = this.genomeLength.read();
		int cycles                  = this.cycles.read();
		long seed                   = this.seed.read();
		String stat                 = this.stat.read();

		Function<Random,? extends BitGenome> ancestorFactory = BitGenome.random(genomeLength);

		warnUnusedVariables();

		if (!dryrun) {
			switch (stat) {
			case "mean":
			case "average": {
				OptionalDouble result = Functional.randoms(seed)
					.map(rng -> CrossoverOutcome.fromMutationOne(rng, crossoverRule, ancestorFactory, mutationRule))
					.map(scoretype)
					.limit(2*cycles)
					.flatMapToDouble(opt -> opt.stream())
					.limit(cycles)
					.average();
				if (result.isPresent()) println(result.getAsDouble());
				break;
			}
			case "all": {
				Random rng = new Random(seed);
				int tries = 0;
				int outputs = 0;
				while (tries < 2*cycles && outputs < cycles) {
					CrossoverOutcome crossover = CrossoverOutcome.fromMutationOne(rng, crossoverRule, ancestorFactory, mutationRule);
					OptionalDouble score = scoretype.apply(crossover);
					if (score.isPresent()) {
						println(crossover.mutationStats.get().toStringOneLine() + "\t" + score.getAsDouble());
						outputs++;
					}
					tries++;
				}
				break;
			}
			default:
				err("Illegal value for stat");
			}
		}
	}
}
