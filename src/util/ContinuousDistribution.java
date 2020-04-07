/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * Alias for ToDoubleFunction<Random>, implementing some useful ways to generate random floating point numbers.
 * @author adriaan
 */
@FunctionalInterface
public interface ContinuousDistribution extends ToDoubleFunction<Random> {

	@Override
	double applyAsDouble(Random value);

	/**
	 * Return a stream of values generated from the supplied distribution.
	 * This implementation is different from DoubleStream.generate because the resulting stream is *ordered*, meaning the elements and their orders are guaranteed to be identical when given the same random seed.
	 */
	static DoubleStream streamOf(Random rng, ToDoubleFunction<Random> dist) {
		return DoubleStream.iterate(dist.applyAsDouble(rng), (ignored) -> dist.applyAsDouble(rng));
	}

	/**
	 * Convenient alias of {@link #streamOf(Random, ToDoubleFunction)}
	 */
	default DoubleStream stream(Random rng) {
		return streamOf(rng, this);
	}

	/**
	 * Calculate a numerical mean of this distribution.
	 * @return the average of n generated numbers
	 */
	static double meanOf(Random rng, int n, ToDoubleFunction<Random> dist) {
		return streamOf(rng, dist).limit(n).average().getAsDouble();
	}

	/**
	 * Convenient alias of {@link #meanOf(Random, int, ToDoubleFunction)}
	 */
	default double mean(Random rng, int n) {
		return meanOf(rng, n, this);
	}

	/**
	 * Round a continuous distribution.
	 * For each continuous value, the upper or lower nearest integer value is chosen with a probability of 1 - distance to the continuous value.
	 * This method of rounding does not alter the mean value of the distribution, but it is sometimes not as desired near the edges of the distribution support.
	 * Note rounded continuous distribution is often not identical to a discrete variant (e.g. discrete exponential has probability of x proportional to exp(-lambda x), rounded continuous version does not).
	 */
	static DiscreteDistribution roundOf(ToDoubleFunction<Random> dist) {
		return (rng) -> DiscreteDistribution.getRandomRound(rng, dist.applyAsDouble(rng));
	}

	static DiscreteDistribution floorOf(ToDoubleFunction<Random> dist) {
		return (rng) -> (int)dist.applyAsDouble(rng);
	}

	static DiscreteDistribution ceilOf(ToDoubleFunction<Random> dist) {
		return (rng) -> (int)Math.ceil(dist.applyAsDouble(rng));
	}

	default DiscreteDistribution round() {
		return roundOf(this);
	}

	default DiscreteDistribution floor() {
		return floorOf(this);
	}

	default DiscreteDistribution ceil() {
		return ceilOf(this);
	}

    static ContinuousDistribution exponential(double mean) {
        return (rng) -> ContinuousDistribution.getExponential(rng, mean);
    }

    static double getExponential(Random rng, double mean) {
    	if (mean == 0) {
    		return 0;
    	} else if (mean > 0) {
    		return -mean*Math.log(1 - rng.nextDouble());
    	} else {
    		throw new IllegalArgumentException("Exponential distribution mean must be positive");
    	}
    }

    /**
     * Power law distribution.
     */
    static ContinuousDistribution powerLaw(double power, int startOfRange, int endOfRange) {
        return (rng) -> ContinuousDistribution.getPowerLaw(rng, power, startOfRange, endOfRange);
    }

    /**
     * Draw a number from a power law distribution.
     * Because the power law does not have a finite integral for powers >= -2, a finite support range must be defined.
     * This function returns a rounded version of a continuous power law distribution, which is not exactly the same as a discrete power law distribution but much easier to calculate.
     * Mean of this distribution is approx. log(endOfRange)-log(startOfRange), as opposed to harmonicNumber(endOfRange)-harmonicNumber(startOfRange).
     * @param rng
     * @param power
     * @param startOfRange - inclusive start of range
     * @param endOfRange - exclusive end of range
     * @return
     */
    static double getPowerLaw(Random rng, double power, int startOfRange, int endOfRange) {
        if (startOfRange <= 0) throw new IllegalArgumentException("Power law range must be positive");
        if (endOfRange <= startOfRange) throw new IllegalArgumentException("Power law range end must be higher than start");

        if (power < -1.) {
	        double n = power+1;
	        double a = Math.pow(startOfRange, n);
	        double b = Math.pow(endOfRange-1, n);
	        double y = Math.pow(a+rng.nextDouble()*(b-a), 1/n);

	        int result = (int)y; // floor
	        if (rng.nextDouble() < y - result) result++; // Correct for decimal part
	        return result;
        } else if (power == -1.) {
        	double alpha = Math.log(endOfRange-1) - Math.log(startOfRange);
        	double a = Math.log(startOfRange) / alpha;
        	double b = Math.log(endOfRange-1) / alpha;

        	return Math.exp(alpha * (rng.nextDouble()*(b-a)+a));
        } else {
            throw new IllegalArgumentException("Illegal exponent for power law distribution");
        }
    }

	static double powerLawAvg(double n, int x0, int x1) {
		return powerLawAvgInclusiveEnd(n, x0, x1-1);
	}

	static double powerLawAvgInclusiveEnd(double n, double x0, double x1) {
		return n == -1. ? (x1-x0)/(Math.log(x1)-Math.log(x0))
			 : n == -2. ? -(Math.log(x1) - Math.log(x0)) / (1/x1 - 1/x0)
			 :            (n+1)/(n+2) * (Math.pow(x1, n+2) - Math.pow(x0, n+2)) / (Math.pow(x1, n+1) - Math.pow(x0, n+1));
	}

	static double getUniform(Random rng, double min, double max) {
		return rng.nextDouble()*(max-min)+min;
	}

	static ContinuousDistribution uniform(double min, double max) {
		return rng -> getUniform(rng, min, max);
	}

}
