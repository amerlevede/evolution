package algorithm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import crossover.CrossoverOp;
import crossover.CrossoverRule;
import genome.Genome;
import mutation.MutationOp;
import mutation.MutationRule;
import population.Org;
import population.OrganismOp;
import population.Population;
import selection.Selector;
import selection.SelectorRule;
import util.CategoricalDistribution;
import util.DiscreteDistribution;
import util.Functional;

/**
 * Encapsulates glue logic of the evolutionary algorithm.
 * Various components of the algorithm are delegated to subclasses:
 * ({@link SelectorRule}) Selection of individuals from the population , which will be based on a {@link FitnessFunction};
 * ({@link MutationRule}) Mutation logic;
 * ({@link CrossoverRule}) Recombination logic;
 * ({@link OrganismOp}) Data structure logic, particularly how to create new individuals;
 * ({@link CategoricalDistribution}) How to generate random new individuals for initialisation. 
 * 
 * 
 * @author adriaan
 *
 * @param <G> - The type of data structure ({@link Genome}) being evolved
 */
public class GeneticAlgorithm<G extends Genome<G>> implements PopulationOptimizationAlgorithm<G> {

	public static class Settings<G extends Genome<G>> {
		public SelectorRule<Org<G>> selectGood;
		public SelectorRule<Org<G>> selectBad;
		public MutationRule<G> mutationOperator;
		public CrossoverRule<G> crossoverOperator;
		public OrganismOp<G> organismFactory;
		public double crossoverProbability;
		public int initialPopulationSize;
		public CategoricalDistribution<G> initialPopulationSupplier;
		public int lambda;

		public boolean hasNullFields() throws IllegalAccessException {
		    for (Field f : getClass().getDeclaredFields())
		        if (f.get(this) == null)
		            return true;
		    return false;
		}

		public Settings<G> copy() {
			Settings<G> set = new Settings<>();
			for (Field f : getClass().getDeclaredFields()) {
				try {
					f.set(set, f.get(this));
				} catch (IllegalAccessException e) {
				}
			}
			return set;
		}
	}

	protected final Selector<Org<G>> selectGood;
	protected final Selector<Org<G>> selectBad;
	protected final MutationOp<G> mutationOperator;
	protected final CrossoverOp<G> crossoverOperator;
	protected final OrganismOp<G> organismFactory;
	protected final int lambda;

	public GeneticAlgorithm(Settings<G> settings, Random rng) {
		try {
			if (settings.hasNullFields()) throw new IllegalArgumentException("Must initialize all fields for GA settings");
		} catch (IllegalAccessException e) {
			// Will result in NullPointerException below
		}

		this.rng = rng;

		this.pop = new Population<>();

		this.selectGood        = settings.selectGood.apply(rng, this.pop.asCollection());
		this.selectBad         = settings.selectBad.apply(rng, this.pop.asCollection());
		this.mutationOperator  = settings.mutationOperator.apply(rng);
		this.crossoverOperator = settings.crossoverOperator.apply(rng);
		this.organismFactory   = settings.organismFactory;
		this.crossprob         = settings.crossoverProbability;
		this.lambda            = settings.lambda;

		// Initialize population
		Functional.randoms(rng)
			.limit(settings.initialPopulationSize)
			.map(settings.initialPopulationSupplier)
			.map(settings.organismFactory)
			.forEach(this.pop::add);
	}

	protected int generation = 0;
	protected final Population<G> pop;

	protected Random rng;
	protected double crossprob;

	@Override
	public Population<G> getPopulation() {
		return this.pop;
	}

	@Override
	public int getGeneration() {
		return this.generation;
	}

	@Override
	public Org<G> getBestOrganism() {
		return this.pop.stream().max(Comparator.naturalOrder()).orElseThrow();
	}

//	// Carl version
//	public void next() {
//		generation++;
//
//		{ // Carl version
//			boolean doingSex = rng.nextDouble() < crossprob;
//			if (doingSex) {
//				Org loser, winner1, winner2;
//				do {
//					loser = selectBad.take1().get();
//					winner1 = selectGood.take1().get();
//					winner2 = selectGood.take1().get();
//				} while (loser.equals(winner1) || loser.equals(winner2) || winner1.equals(winner2));
//				pop.kill(loser);
//				pop.copulate(crossoverOperator, organismFactory, winner1, winner2);
//			} else {
//				Org loser, winner;
//				do {
//					loser = selectBad.take1().get();
//					winner = selectGood.take1().get();
//				} while (loser.equals(winner));
//				pop.kill(loser);
//				pop.vegetate(mutationOperator, organismFactory, winner);
//			}
//		}
//	}

	@Override
	public void next() {
		generation++;

		Set<Org<G>> kill = selectBad.takeDistinct(lambda);

		int doingSex = DiscreteDistribution.getBinomial(rng, lambda, crossprob);
		List<Org<G>> born = new LinkedList<>();
		List<Org<G>> lucky = selectGood.get().limit(lambda+doingSex).collect(Collectors.toCollection(ArrayList::new));

		for (int i=0; i<doingSex; i++) {
			Org<G> child = pop.copulateAndGet(crossoverOperator, organismFactory, lucky.get(2*i), lucky.get(2*i+1)).get();
			pop.kill(child);
			born.add(child);
		}

		for (int i=2*doingSex; i<lambda+doingSex; i++) {
			Org<G> child = pop.vegetateAndGet(mutationOperator, organismFactory, lucky.get(i)).get();
			pop.kill(child);
			born.add(child);
		}

		pop.killAll(kill);
		pop.addAll(born);

	}

}
