package main.permutation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import crossover.CrossoverOp;
import genome.integer.IntGenome;

public class Timing extends PermutationCommandLineInterface<IntGenome> {

	public Timing(String[] args) {
		super(args);
	}

	@Override
	public void run(boolean dryrun) {
		Random rng = this.rng.read();
		CrossoverOp<IntGenome> crossover = this.crossover.read().apply(rng);
		int cycles = this.cycles.read();
		int size = this.size.read();

		if (!dryrun) {
			List<IntGenome> as = new LinkedList<>();
			List<IntGenome> bs = new LinkedList<>();
			for (int i=0; i<cycles; i++) {
				as.add(IntGenome.getRandomPermutation(rng, size));
				bs.add(IntGenome.getRandomPermutation(rng, size));
			}
			Iterator<IntGenome> aIter = as.iterator();
			Iterator<IntGenome> bIter = bs.iterator();
			long startTime = System.nanoTime();
			for (int i=0; i<cycles; i++) {
				crossover.accept(aIter.next(), bIter.next());
			}
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);

			System.out.println(duration / cycles);
		}
	}

}
