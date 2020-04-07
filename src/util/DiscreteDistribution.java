/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import genome.binary.BitGenome;

/**
 * Alias for ToIntFunction<Random>, implementing some useful ways to generate random integers.
 * @author adriaan
 */
@FunctionalInterface
public interface DiscreteDistribution extends ToIntFunction<Random> {

	@Override
	int applyAsInt(Random value);

	/**
	 * Return a stream of values generated from the supplied distribution.
	 * This implementation is different from IntStream.generate because the resulting stream is *ordered*, meaning the elements and their orders are guaranteed to be identical when given the same random seed.
	 */
	static IntStream streamOf(Random rng, ToIntFunction<Random> dist) {
		return IntStream.iterate(dist.applyAsInt(rng), (ignored) -> dist.applyAsInt(rng));
	}

	/**
	 * Convenient alias of {@link #streamOf(Random, ToIntFunction)}
	 */
	default IntStream stream(Random rng) {
		return streamOf(rng, this);
	}

	/**
	 * Calculate a numerical mean of this distribution.
	 * @return the average of n generated numbers
	 */
	static double meanOf(Random rng, int n, ToIntFunction<Random> dist) {
		return streamOf(rng, dist).limit(n).asDoubleStream().average().getAsDouble();
	}

	/**
	 * Convenient alias of {@link #meanOf(Random, int, ToIntFunction)}
	 */
	default double mean(Random rng, int n) {
		return meanOf(rng, n, this);
	}

//	public static DiscreteDistribution approximateMean(int n, DiscreteDistribution dist) {
//		return (rng) -> {
//			OptionalDouble mean = streamOf(rng, dist).limit(n).mapToDouble(Double::valueOf).average();
//			if (mean.isEmpty()) {
//				return 1;
//			} else {
//				return (int)Math.round(mean.getAsDouble());
//			}
//		};
//	}

	static DiscreteDistribution uniform(int inclusiveMin, int exclusiveMax) {
		return (rng) -> getUniform(rng, inclusiveMin, exclusiveMax);
	}

	static int getUniform(Random rng, int inclusiveMin, int exclusiveMax) {
		return rng.nextInt(exclusiveMax-inclusiveMin)+inclusiveMin;
	}
	
	static DiscreteDistribution uniform(int[] candidates) {
		return (rng) -> getUniform(rng, candidates);
	}
	
	static int getUniform(Random rng, int[] candidates) {
		return candidates[getUniform(rng, 0, candidates.length)];
	}

    static DiscreteDistribution poisson(double lambda) {
        return (rng) -> DiscreteDistribution.getPoisson(rng, lambda);
    }

    static int getPoisson(Random rng, double lambda) {
        double L = Math.exp(-lambda);

        double p = 1.0;
        int k = 0;

        do {
          k++;
          p *= rng.nextDouble();
        } while (p > L);

        return k - 1;
    }

    /**
     * Binomial distribution.
     * It describes the number of successes in n independent random trials, each with an independent probability of success of p.
     * @param n - The number of trials. (Also the maximum support of the distribution.)
     * @param p - The probability of success for each trial.
     * @return A supplier of binomial distributed values.
     */
    static DiscreteDistribution binomial(int n, double p) {
        return (rng) -> getBinomial(rng, n, p);
    }

    /** @see #binomial(int, double) **/
    // Inefficient implementation
    static int getBinomial(Random rng, int n, double p) {
        int x = 0;
        for(int i = 0; i < n; i++) {
          if(rng.nextDouble() < p)
            x++;
        }
        return x;
    }

    /**
	 * Find the highest value in the given score set.
	 * If there are multiple candidates, one is chosen at random (uniformly).
	 */
	static int getBestIndexOf(Random rng, int... scores) {
		double exaequo = 1;
		int best = 0;
		for (int i=1; i<scores.length; i++) {
			int cmp = Integer.compare(scores[i], scores[best]);
			if (cmp > 0) {
				exaequo = 1;
				best = i;
			} else if (cmp == 0) {
				exaequo++;
				if (rng.nextDouble() < 1./exaequo) best = i;
			}
		}
		return best;
	}

	/**
	 * Find the highest value in the given score set.
	 * If there are multiple candidates, one is chosen at random (uniformly).
	 */
	static int getBestIndexOf(Random rng, double... scores) {
		double exaequo = 1;
		int best = 0;
		for (int i=1; i<scores.length; i++) {
			int cmp = Double.compare(scores[i], scores[best]);
			if (cmp > 0) {
				exaequo = 1;
				best = i;
			} else if (cmp == 0) {
				exaequo++;
				if (rng.nextDouble() < 1./exaequo) best = i;
			}
		}
		return best;
	}

	static <O> O getBestOfNonempty(Random rng, Collection<O> nonemptycollection, Comparator<O> comparator) {
		Iterator<O> iter = nonemptycollection.iterator();
		if (!iter.hasNext()) return null;
		double exaequo = 1;
		O best = iter.next();
		O o;
		while (iter.hasNext()) {
			o = iter.next();
			int cmp = comparator.compare(o, best);
			if (cmp > 0) {
				exaequo = 1;
				best = o;
			} else if (cmp == 0) {
				exaequo++;
				if (rng.nextDouble() < 1./exaequo) {
					best = o;
				}
			}
		}
		return best;
	}

	/**
	 * Distribution for the number of mutations that should be performed in order to on average affect a given fraction of the genome, and assuming that each mutation is a random i.i.d. event with a certain probability.
	 * This is a binomial distribution.
	 */
	static Function<BitGenome,DiscreteDistribution> perBit(double p) {
		return (g) -> binomial(g.size(), p);
	}

	static DiscreteDistribution randomRound(double x) {
		return (rng) -> getRandomRound(rng, x);
	}

	static int getRandomRound(Random rng, double x) {
		int result = (int)x;
		if (rng.nextDouble() < x - result) result++;
		return result;
	}

	/**
	 * Distribution giving the number of mutations that should be performed in order to on average affect a given fraction of the genome, assuming that each mutation affects a number of bits according to the supplied distribution.
	 * This is a binomial distribution where the probability p is calculated from the mean of the given distribution.
	 * The mean is approximated simply by calculating a given number of values.
	 * NOTE: In the case of mutations that alter the genome size and calculate the size distribution based on the genome length, actual distribution of affected bits will be slightly different
	 * @param p - The fraction of bits that should, on average, be affected
	 * @param n - The number of times a number should be taken from the distribution in order to calculate the mean value. Low values lead to faster computation but a wider outcome distribution, especially with highly asymmetric input distributions (any reasonable argument will have this property). Calculating numbers from a distribution is cheap compared to genome operations.
	 * @param ndist - Distribution describing the number of affected bits per mutation.
	 */
	static Function<BitGenome,DiscreteDistribution> perBit(double p, int n, Function<BitGenome,DiscreteDistribution> ndist) {
		return (g) -> (rng) -> {
			double mean = ndist.apply(g).mean(rng, n);
			return getBinomial(rng, g.size(), p / mean);
		};
	}

	static int getWeighted(Random rng, int[] weights) {
		int sum = IntStream.of(weights).sum();
		return getWeightedWithTotal(rng, weights, sum);
	}

	static int getWeightedWithTotal(Random rng, int[] weights, int total) {
		int val = rng.nextInt(total);
		int i=-1;
		while (val >= 0) val -= weights[++i];
		return i;
	}

	static DiscreteDistribution weighted(int[] weights) {
		int sum = IntStream.of(weights).sum();
		return (rng) -> getWeightedWithTotal(rng, weights, sum);
	}

	static int getWeighted(Random rng, double[] weights) {
		double sum = DoubleStream.of(weights).sum();
		return getWeightedWithTotal(rng, weights, sum);
	}

	static int getWeightedWithTotal(Random rng, double[] weights, double total) {
		double val = rng.nextDouble()*total;
		int i=-1;
		while (val >= 0) val -= weights[++i];
		return i;
	}

	static DiscreteDistribution weighted(double[] weights) {
		double sum = DoubleStream.of(weights).sum();
		return (rng) -> getWeightedWithTotal(rng, weights, sum);
	}

	/**
	 * Distribute n elements randomly across containers of the given sizes.
	 * The probability of each element being distributed to a given container is proportional to the size of the container.
	 * The distribution of subsequent elements is not independent: each previously distributed element decreases the size of its container and thus alters the distribution.
	 * @throws IllegalArgumentException - if n is larger than the sum of the container sizes
	 */
	static int[] getManyWeightedWithoutReplacement(Random rng, int[] sizes, int n) {
		sizes = IntStream.of(sizes).toArray(); // Don't overwrite argument array
		int[] result = new int[sizes.length];
		int sum = IntStream.of(sizes).sum();
		if (sum < n) throw new IllegalArgumentException();
		for (int i=0; i<n; i++) {
			int container = getWeightedWithTotal(rng, sizes, sum);
			result[container]++;
			sum--;
			sizes[container]--;
		}
		return result;
	}

	/**
	 * A discrete distribution defined by the given weights; it will return a value i with a probability proportional to weights[i].
	 * There is no requirement that the sum of the weights must be 1.
	 * Zero-valued weights are allowed (but an empty list or negative weight values are not).
	 */
	static DiscreteDistribution weighted(List<Integer> weights) {
		int total = weights.stream().mapToInt(i->i).sum();
		return weightedWithTotal(weights, total);
	}

	/** @see #weighted(List) */
	static DiscreteDistribution weightedWithTotal(List<Integer> weights, int total) {
		return (rng) -> {
			int val = rng.nextInt(total);
			int i = -1;
			while (val >= 0) {
				i++;
				val -= weights.get(i);
			}
			return i;
		};
	}

	default DiscreteDistribution sumTimes(DiscreteDistribution times) {
		return (rng) -> {
			int n = times.applyAsInt(rng);
			int result = 0;
			for (int i=0; i<n; i++) {
				result += this.applyAsInt(rng);
			}
			return result;
		};
	}

	static void shuffleArray(Random rng, int[] heads) {
		for (int i=0; i<heads.length; i++) {
			int j = rng.nextInt(heads.length);
			int tmp = heads[i];
			heads[i] = heads[j];
			heads[j] = tmp;
		}
	}

//	/**
//	 *  Given the sum of n random values drawn from the same distribution D(x), where n is also random and drawn from A(x), inverseDistributionSum(D, A) returns an array of n values x1, x2, x3, ... drawn from the distributions A, D under the constraint that their sum is the given value.
//	 */
//	public static DiscreteDistribution inverseDistributionSum(DiscreteDistribution dist) {
//
//	}

}
