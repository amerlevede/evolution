package fitness.binary;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fitness.FitnessFunction;
import genome.LinearGenome.KMP;
import genome.binary.BinaryGenome;
import genome.gene.Gene;
import genome.gene.Triangle;
import util.Assert;
import util.DoublePair;

/**
 * Utility class implementing the Triangles fitness function.
 *
 * @author adriaan
 */
public abstract class SumFunction {

	private SumFunction() {
		Assert.utilityClass();
	}

	/**
	 * Fitness function that reads the genome as a function and compares it to the target function.
	 * Identical to {@link SumFunction#of(DoubleUnaryOperator, G, GeneReader)}, but optimized for triangleRBFs.
	 */
	public static <G extends BinaryGenome<G>, V extends Triangle> FitnessFunction<G> triangles(DoubleUnaryOperator target, KMP<G> consensus) {
		return (g) -> {
			// Preprocess the genome to a list of (x, y) pairs, where y represents a change in the derivative of the function represented by the triangle RBF sum
			SortedSet<DoublePair> deriv2 = Gene.Reader
				.<G,Triangle>scan(consensus, g, Triangle::read).map(Gene::getValue)
				.flatMap((triangle) ->
					Stream.of(
						DoublePair.of(triangle.getCenter() - triangle.getWidth(),    triangle.getHeight()/triangle.getWidth()),
						DoublePair.of(triangle.getCenter()                      , -2*triangle.getHeight()/triangle.getWidth()),
						DoublePair.of(triangle.getCenter() + triangle.getWidth(),    triangle.getHeight()/triangle.getWidth())))
				.collect(Collectors.toCollection(TreeSet::new));

			// Loop over the genome, accumulating the derivative and error
			double x =0, y=0, deriv=0, err=0;
			for (DoublePair derivchange : deriv2) {
				if (derivchange.x > 1) break;
				while (x < derivchange.x) {
					y += deriv*PRECISION;
					err += Math.pow(target.applyAsDouble(x) - y, 2);
					x += PRECISION;
				}
				deriv += derivchange.y;
			}
			while (x <= 1) {
				y += deriv*PRECISION;
				err += Math.pow(target.applyAsDouble(x) - y, 2);
				x += PRECISION;
			}

			// Done
			return -err;
		};
	}

	/**
	 * Read a genome as a function.
	 * The function is the sum of all genes tagged by the given consensus sequence and converted to functions using the given reader.
	 */
	public static <V extends DoubleUnaryOperator, G extends BinaryGenome<G>> DoubleUnaryOperator genomeToSumFunction(KMP<G> consensus, Gene.Reader<G, V> rbfReader, G g) {
		final List<Gene<G,? extends DoubleUnaryOperator>> rbfs = Gene.Reader.scan(consensus, g, rbfReader).collect(Collectors.toList());
		return (x) -> rbfs.stream().collect(Collectors.summingDouble(f -> f.getValue().applyAsDouble(x)));
	}

	/**
	 * A fitness function that reads the genome as a function and compares it to the target function.
	 * @see #genomeToSumFunction(G, GeneReader, G)
	 * @see #functionDistance(DoubleUnaryOperator, DoubleUnaryOperator)
	 */
	public static <G extends BinaryGenome<G>, V extends DoubleUnaryOperator> FitnessFunction<G> of(DoubleUnaryOperator target, KMP<G> consensus, Gene.Reader<G,V> rbffactory) {
		return (g) -> -functionDistance(target, genomeToSumFunction(consensus, rbffactory, g));
	}

	/**
	 * Function difference, i.e. numerical integration of the absolute value of the difference between two real functions.
	 * The integration is executed over the unit interval.
	 */
	public static double functionDistance(DoubleUnaryOperator f, DoubleUnaryOperator g) {
		double err = 0;
		for (double x=0; x<1; x+=PRECISION) {
			err += Math.pow(f.applyAsDouble(x) - g.applyAsDouble(x), 2);
		}
		return err * PRECISION;
	}

	public static final double PRECISION = 1./1024.;

}
