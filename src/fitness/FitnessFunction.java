package fitness;

import java.util.function.ToDoubleFunction;

import genome.LinearGenome;

/**
 * Functional interface whose instances represent a fitness function.
 * This interface is basically an alias for ToDoubleFunction. 
 * 
 * @author adriaan
 *
 * @param <G>
 */
@FunctionalInterface
public interface FitnessFunction<G> extends ToDoubleFunction<G> {
	
	@Override
	double applyAsDouble(G value);
	
	/**
	 * Limit the length of the genome by applying a linear fitness penalty when the length exceeds a threshold.
	 * For positive fitness values, the penalty factor is 2-size/limit (<1).
	 * For negative fitness values, the penalty factor is size/limit (>1).
	 */
	public static <G extends LinearGenome<G>> FitnessFunction<G> limitLength(int limit, FitnessFunction<G> fitnessfunction) {
		return (g) -> {
			double rawfitness = fitnessfunction.applyAsDouble(g);
			double correctionfactor = rawfitness > 0
					? Math.min(1, Math.max(0, 2 - ((double)g.size())/((double)limit)))
					: Math.max(1, ((double)g.size())/((double)limit));
			return rawfitness * correctionfactor;
		};
	}

}
