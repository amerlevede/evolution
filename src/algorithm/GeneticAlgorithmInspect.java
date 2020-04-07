package algorithm;

import java.util.Random;

import genome.Genome;
import population.Org;

@Deprecated
public class GeneticAlgorithmInspect<G extends Genome<G>> extends GeneticAlgorithm<G> {
	
	public GeneticAlgorithmInspect(Settings<G> settings, Random rng) {
		super(settings, rng);
		
		this.currentBest = this.getBestOrganism();
	}
	
	private Org<G> currentBest;
	
	@Override
	public void next() {
		generation++;

		Org<G> killthis = selectBad.take1().get();

		boolean doingSex = rng.nextDouble() < crossprob;
		if (doingSex) {
			Org<G> mom = selectGood.take1().get();
			Org<G> dad = selectGood.take1Excluding(mom).get();
			Org<G> kid = this.pop.copulateAndGet(crossoverOperator, organismFactory, mom, dad).get();
			
			if (kid.getFitness() > currentBest.getFitness()) {
				currentBest = kid;
				System.out.println();
				System.out.println(generation);
				System.out.println(mom.getGenome());
				System.out.println(dad.getGenome());
				System.out.println(kid.getGenome());
				System.out.println(kid.getFitness());
			}
		} else {
			pop.vegetate(mutationOperator, organismFactory, selectGood);
		}
		
		pop.kill(killthis);
		
	}

}
