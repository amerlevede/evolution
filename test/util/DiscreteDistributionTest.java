package util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.RepeatedTest;

import genome.RandomInit;

class DiscreteDistributionTest extends RandomInit {
	
	@RepeatedTest(100)
	public void testWeighted_integers() {
		int[] weights = new int[] {0, 1, 2, 3, 0, 15}; // sum = 1
		int total = 100000;
		
		double[] histogram = new double[weights.length];
		DiscreteDistribution.weighted(weights).stream(rng).limit(total).forEach(val -> histogram[val]+=1./((double)total));
		
		double weightsum = IntStream.of(weights).sum();
		double[] targetHistogram = IntStream.of(weights).mapToDouble(Double::valueOf).map(i->i/weightsum).toArray();
		
		assertArrayEquals(targetHistogram, histogram, 0.01); // Failure statistically unlikely but possible (did not happen once in 10000 tests)
	}
	
	@RepeatedTest(100)
	public void testWeighted_doubles() {
		double[] weights = new double[] {0, 1./5., 0., 1./3., 1./15., 2./5., 0}; // sum = 1
		int total = 100000;
		
		double[] histogram = new double[weights.length];
		DiscreteDistribution.weighted(weights).stream(rng).limit(total).forEach(val -> histogram[val]+=1./((double)total));
		
		assertArrayEquals(weights, histogram, 0.01); // Failure statistically unlikely but possible (did not happen once in 10000 tests)
	}

}
