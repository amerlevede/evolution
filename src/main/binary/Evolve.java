package main.binary;

import java.util.Comparator;
import java.util.Random;

import algorithm.GeneticAlgorithm;
import algorithm.PopulationOptimizationAlgorithm;
import genome.binary.BitGenome;
import population.Org;
import selection.Elitism;
import selection.RouletteWheelSelection;
import selection.SelectorRule;
import selection.TournamentSelection;

/**
 * Command line utility to implement genetic algorithms.
 * 
 * @author adriaan
 *
 */
public class Evolve extends BinaryCommandLineInterface<BitGenome> {

	public Evolve(String[] args) {
		super(args);
	}

	protected PopulationOptimizationAlgorithm<BitGenome> ga;

	public final Option<Integer> tournamentSize = option("tournamentSize", 2, Integer::valueOf);
	public final Option<SelectorRule<Org<BitGenome>>> selectGood = optionWithStringDefault("selectGood", "tournament", optionValue -> readSelection(optionValue,true));
	public final Option<SelectorRule<Org<BitGenome>>> selectBad  = optionWithStringDefault("selectBad", "tournament", optionValue -> readSelection(optionValue,false));
	public final Option<Integer> elitism = option("elitism", 0, Integer::valueOf);

	SelectorRule<Org<BitGenome>> readSelection(String optionValue, boolean good) {
		switch (optionValue) {
		case "tournament": {
			int size = this.tournamentSize.read();
			int elitism = this.elitism.read();
			return SelectorRule.<BitGenome>random()
					.modify(TournamentSelection.comparing(size, Comparator.<Org<BitGenome>,Double>comparing(o -> (good?1:-1)*o.getFitness())))
					.modify(Elitism.spare(good?0:elitism));
		}
		case "roulettewheel": {
			int elitism = this.elitism.read();
			return RouletteWheelSelection.<BitGenome>rule()
					.modify(Elitism.spare(good?0:elitism));
		}
		case "list": {
			return (rng,pop) -> () -> pop.stream().sorted(good?Comparator.reverseOrder():Comparator.naturalOrder());
		}
		default:
			throw new IllegalArgumentException("Unrecognized option for selection");
		}
	}

	public final Option<Double> crossoverProbability = option("crossoverProbability", .2, Double::valueOf); // Carl version. Note: Paper erroneously says 0.15.
    public final Option<Integer> populationSize = option("populationSize", 100, Integer::valueOf);

	public final Option<GeneticAlgorithm.Settings<BitGenome>> gaSettings = autoOption(() -> {
		GeneticAlgorithm.Settings<BitGenome> settings = new GeneticAlgorithm.Settings<>();
		settings.selectGood           = this.selectGood.read();
		settings.selectBad            = this.selectBad.read();
		settings.mutationOperator     = this.mutation.read();
		settings.organismFactory      = Org.factory(this.fitness.read());
		settings.crossoverOperator    = this.crossover.read();
		settings.crossoverProbability = this.crossoverProbability.read();
		settings.initialPopulationSize = this.populationSize.read();
		settings.initialPopulationSupplier = BitGenome.random(this.genomeLength.read())::apply;
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

}
