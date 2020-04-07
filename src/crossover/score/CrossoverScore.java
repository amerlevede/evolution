package crossover.score;

import java.util.OptionalDouble;
import java.util.function.Function;

import crossover.CrossoverRule;
import mutation.MutationRule;

/**
 * Functional interface that represents a calculated property of a CrossoverRule, such as homology and linkage scores.
 * @see #apply(CrossoverRule, MutationRule, Function) 
 * @author adriaan
 */
@FunctionalInterface
public interface CrossoverScore extends Function<CrossoverOutcome, OptionalDouble>{
	
	@Override
	public OptionalDouble apply(CrossoverOutcome crossover);

}
