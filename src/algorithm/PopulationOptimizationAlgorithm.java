package algorithm;

import java.util.Comparator;

import genome.Genome;
import population.Org;
import population.Population;

/**
 * Interface for {@link GeneticAlgorithm}.
 */
public interface PopulationOptimizationAlgorithm<G extends Genome<G>> {

	int getGeneration();

	void next();

	Population<G> getPopulation();

	default Org<G> getBestOrganism() {
		return this.getPopulation().stream().max(Comparator.naturalOrder()).orElseThrow();
	}

	default String report() {
		Org<G> winner = this.getBestOrganism();
		return this.getGeneration()
				+ "\t" + winner.getFitness()
				+ "\t" + winner.getGenome()
				;
	}

		default void next(int n) {
		for (int i=0; i<n; i++) next();
	}

}