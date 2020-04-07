package main.permutation;

import java.util.Comparator;
import java.util.Random;

import algorithm.GeneticAlgorithm;
import algorithm.PopulationOptimizationAlgorithm;
import genome.integer.IntGenome;
import population.Org;
import selection.Elitism;
import selection.RouletteWheelSelection;
import selection.SelectorRule;
import selection.TournamentSelection;

/**
 * Command line utility to run evolutionary algorithms.
 * 
 * @author adriaan
 *
 */
public class Evolve extends PermutationCommandLineInterface<IntGenome> {

	public Evolve(String[] args) {
		super(args);
	}

	protected PopulationOptimizationAlgorithm<IntGenome> ga;

	public final Option<Integer> tournamentSize = option("tournamentSize", 2, Integer::valueOf);
	public final Option<Integer> elitism = option("elitism", 0, Integer::valueOf);
	public final Option<Integer> lambda = option("lambda", 1, Integer::valueOf);
	public final Option<SelectorRule<Org<IntGenome>>> selectGood = optionWithStringDefault("selectGood", "tournament", optionValue -> readSelection(optionValue,true));
	public final Option<SelectorRule<Org<IntGenome>>> selectBad  = optionWithStringDefault("selectBad", "tournament", optionValue -> readSelection(optionValue,false));

	public final Option<Double> crossoverProbability = option("crossoverProbability", .2, Double::valueOf);
    public final Option<Integer> populationSize = option("populationSize", 100, Integer::valueOf);

	public final Option<GeneticAlgorithm.Settings<IntGenome>> gaSettings = autoOption(() -> {
		GeneticAlgorithm.Settings<IntGenome> settings = new GeneticAlgorithm.Settings<>();
		settings.selectGood           = this.selectGood.read();
		settings.selectBad            = this.selectBad.read();
		settings.mutationOperator     = this.mutation.read();
		settings.organismFactory      = Org.factory(this.fitness.read());
		settings.crossoverOperator    = this.crossover.read();
		settings.crossoverProbability = this.crossoverProbability.read();
		settings.initialPopulationSize = this.populationSize.read();
		settings.initialPopulationSupplier = IntGenome.randomPermutation(size.read());
		settings.lambda               = this.lambda.read();
		return settings;
	});

	public final Option<Integer> skip = option("skip", 1, Integer::valueOf);

	public void report() {
		this.println(this.ga.report());
	}

	@Override
	public void run(boolean dryrun) {
		this.ga = new GeneticAlgorithm<>(this.gaSettings.read(), new Random(this.seed.read()));
		int cycles = this.cycles.read();
		int skip = this.skip.read();

		warnUnusedVariables();

		if (!dryrun) {
			this.report();
			for (int i=0; i<cycles; i++) {
				this.ga.next(skip);
				this.report();
			}
		}
	}

	SelectorRule<Org<IntGenome>> readSelection(String optionValue, boolean good) {
		switch (optionValue) {
		case "tournament": {
			int size = this.tournamentSize.read();
			int elitism = this.elitism.read();
			return SelectorRule.<IntGenome>random()
					.modify(TournamentSelection.comparing(size, good?Comparator.<Org<IntGenome>>naturalOrder():Comparator.<Org<IntGenome>>reverseOrder()))
					.modify(Elitism.spare(good?0:elitism));
		}
		case "roulette": {
			int elitism = this.elitism.read();
			return RouletteWheelSelection.<IntGenome>rule()
					.modify(Elitism.spare(good?0:elitism));
		}
		case "list": {
			return (rng,pop) -> () -> pop.stream().sorted(good?Comparator.reverseOrder():Comparator.naturalOrder());
		}
		default:
			throw new IllegalArgumentException("Unrecognized option for selection");
		}
	}

}
