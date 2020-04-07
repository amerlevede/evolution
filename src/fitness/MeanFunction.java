package fitness;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import fitness.binary.SumFunction;
import genome.LinearGenome.KMP;
import genome.binary.BinaryGenome;
import genome.gene.Gene;
import util.Assert;

/**
 * Fitness function that finds genes in a genome and interprets them as radial basis functions.
 * The fitness of the genome is how well these radial basis functions approximate some target function.
 * If multiple RBFs overlap for a particular value x of the function described by a genome, their weighted mean is returned, where the weight is also encoded in the gene.
 * 
 * @author adriaan
 */
public abstract class MeanFunction {

	private MeanFunction() {
		Assert.utilityClass();
	}

	/**
	 * A function ([0,1]->R) with an attached weight ([0,1]->[0,1]).
	 */
	public interface WeightedFunction extends DoubleUnaryOperator {
		double valueAt(double x);
		double weightAt(double x);

		/**
		 * For any WeightedFunction, applyAsDouble must equal valueAt * weightAt for all arguments in [0,1].
		 */
		@Override
		default double applyAsDouble(double operand) {
			return this.valueAt(operand) * this.weightAt(operand);
		}

		static WeightedFunction of(DoubleUnaryOperator value, DoubleUnaryOperator weight) {
			return new WeightedFunction() {
				@Override
				public double valueAt(double x) {
					return value.applyAsDouble(x);
				}
				@Override
				public double weightAt(double x) {
					return weight.applyAsDouble(x);
				}
			};
		}
	}

	/**
	 * Transform a Gene.Reader for weighted functions to multiply the weight of the resulting function by an exponential factor dependent on the position of the gene in the genome.
	 */
	public static <V extends WeightedFunction, G extends BinaryGenome<G>> Gene.Reader<G,WeightedFunction> weighByPosition(double factor, Gene.Reader<G,V> reader) {
		return (consensus, g, i) -> {
			Optional<Gene<G,V>> result = reader.read(consensus, g, i);
			if (result.isPresent()) {
				WeightedFunction newF = WeightedFunction.of(
						x -> result.get().getValue().valueAt(x),
						x -> result.get().getValue().weightAt(x) * Math.pow(factor, i)
						);
				return Optional.of(new Gene<>(result.get().getSequence(), result.get().getIndex(), newF));
			} else return Optional.empty();
		};
	}

	public static <V extends WeightedFunction> DoubleUnaryOperator weightedAverageWithDefault(Iterable<V> fs, double def) {
		return (x) -> {
			double result = 0;
			double weight = 0;
			for (V f : fs) {
				double w = f.weightAt(x);
				double v = f.valueAt(x);
				weight += w;
				result += w*v;
			}
			return weight >= 1
					? result/weight
					: result + def*(1-weight);
		};
	}

	public static <V extends WeightedFunction, G extends BinaryGenome<G>> DoubleUnaryOperator genomeToWeightedAverageFunction(KMP<G> consensus, Gene.Reader<G,V> rbffactory, G g) {
		List<V> fs = Gene.Reader.scan(consensus, g, rbffactory).map(Gene::getValue).collect(Collectors.toList());
		return weightedAverageWithDefault(fs, 0);
	}

	public static <V extends WeightedFunction, G extends BinaryGenome<G>> FitnessFunction<G> of(DoubleUnaryOperator target, KMP<G> consensus, Gene.Reader<G,V> rbffactory) {
		return (g) -> -SumFunction.functionDistance(target, genomeToWeightedAverageFunction(consensus, rbffactory, g));
	}

}
