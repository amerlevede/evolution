package algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;

import genome.Genome;
import population.Org;
import population.Population;

/**
 * Run multiple genetic algorithms in parallel, with possibility of occasional exchange.
 */
public class ParallelAlgorithms<G extends Genome<G>> implements PopulationOptimizationAlgorithm<G> {

	private final List<PopulationOptimizationAlgorithm<G>> algos;
	private int nextUpdate = -1;
	private int nextSwap = -1;
	private int swapCounter = 0;
	private final int swapN;

	private ParallelAlgorithms(int swapN) {
		this.algos = new ArrayList<>();
		this.swapN = swapN;
	}

	public static <G extends Genome<G>> ParallelAlgorithms<G> swapping(int swapN, Collection<PopulationOptimizationAlgorithm<G>> gas) {
		ParallelAlgorithms<G> result = new ParallelAlgorithms<>(swapN);
		result.algos.addAll(gas);
		return result;
	}


	@Override
	public Population<G> getPopulation() {
		throw new IllegalAccessError();
	}

	@Override
	public Org<G> getBestOrganism() {
		return algos.stream()
				.map(PopulationOptimizationAlgorithm::getBestOrganism)
				.reduce(BinaryOperator.maxBy(Comparator.naturalOrder()))
				.get();
	}

	@Override
	public int getGeneration() {
		return algos.stream()
				.mapToInt(PopulationOptimizationAlgorithm::getGeneration)
				.sum();
	}

	@Override
	public void next() {
		nextUpdate = (nextUpdate+1)%this.algos.size();
		swapCounter = (swapCounter+1)%this.swapN;

		this.algos.get(nextUpdate).next();

		if (swapCounter == 0) {
			nextSwap = (nextSwap+1)%this.algos.size();
			PopulationOptimizationAlgorithm<G> p1 = this.algos.get(nextSwap);
			PopulationOptimizationAlgorithm<G> p2 = this.algos.get((nextSwap+1)%this.algos.size());

			if (p1.getBestOrganism().compareTo(p2.getBestOrganism()) < 0) {
				PopulationOptimizationAlgorithm<G> ptmp = p1;
				p2 = p1;
				p1 = ptmp;
			}

			p2.getPopulation().replace(p2.getBestOrganism(), p1.getBestOrganism());
		}
	}

}
