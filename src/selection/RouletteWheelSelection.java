package selection;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import population.Org;
import util.DiscreteDistribution;
import util.Functional;

/**
 * Utilty class implementing tournament selection.
 * @see #comparing(int, Comparator)
 *
 * @author adriaan
 */
public class RouletteWheelSelection {

	public static <O> SelectorRule<O> ruleWithWeight(ToDoubleFunction<O> fitness) {
		return (rng,pop) -> () -> {
			double[] weights = pop.stream().mapToDouble(fitness).toArray();
			if (weights.length == 0) return Stream.of();
			double minFitness = DoubleStream.of(weights).min().getAsDouble();
			IntStream.range(0, weights.length).forEachOrdered(i -> weights[i] -= minFitness);

			List<O> popl = pop instanceof List
					? (List<O>)pop
					: pop.stream().collect(Collectors.toList());

			return DiscreteDistribution.weighted(weights).stream(rng).mapToObj(popl::get);
		};
	}

	public static <G> SelectorRule<Org<G>> rule() {
		return RouletteWheelSelection.ruleWithWeight(Org::getFitness);
	}

	/**
	 * SelectorOp that gives weight to each selection proportional to its fitness, relative to other members of the population.
	 */
	public static <O> SelectorOp<O> diluteWithWeight(ToDoubleFunction<O> fitness) {
		return (sel) -> (rng,pop) -> () -> {
			DoubleSummaryStatistics stats = pop.stream().mapToDouble(fitness).summaryStatistics();
			double minFitness = stats.getMin();
			double totalFitness = stats.getSum() - minFitness;
			return Functional.randoms(rng)
				.map(r ->
						sel.apply(r,pop).take1()
						.filter(c -> (fitness.applyAsDouble(c) - minFitness) / totalFitness >= r.nextDouble()
					)
			).flatMap(Optional::stream);
		};
	}

	public static <G> SelectorOp<Org<G>> dilute() {
		return RouletteWheelSelection.diluteWithWeight(Org::getFitness);
	}

}
