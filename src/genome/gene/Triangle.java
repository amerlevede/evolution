package genome.gene;

import java.util.Optional;

import fitness.MeanFunction.WeightedFunction;
import genome.LinearGenome;
import genome.binary.BinaryGenome;
import genome.binary.BitGenome;
import util.IntPair;

/**
 * A {@link Gene} that encodes a weighted radial basis function.
 * The gene contains three values: width, height, and center, and the function is a triangle shape with those properties. 
 * 
 * @author adriaan
 *
 */
public class Triangle implements WeightedFunction {

	private final double center;
	private final double width;
	private final double height;

	public double getCenter() {
		return center;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public Triangle(double center, double width, double height) {
		this.center = center;
		this.width = width;
		this.height = height;
	}

	public static final int NBITS_CENTER = 10;
	public static final int NBITS_WIDTH = 10;
	public static final int NBITS_HEIGHT = 10;

//	// Carl version
//	public static Optional<Gene<TriangleRBF>> read(Genome consensus, Genome g, int i) {
//		if (i + consensus.size() + NBITS_CENTER + NBITS_WIDTH + NBITS_HEIGHT > g.size()) {
//			return Optional.empty();
//		} else {
//			int start = i + consensus.size();
//			int end = start + NBITS_CENTER;
//			double center = ((double)g.view(start, end).decodeIntBase()) / ((double)(Math.pow(2,NBITS_CENTER)-1)); // in [0,1]
//			start = end;
//			end = start + NBITS_WIDTH;
//			double width = ((double)g.view(start, end).decodeIntBase() + 1.) / ((double)(Math.pow(2,NBITS_WIDTH))) / 2.; // in (0, 0.5]
//			start = end;
//			end = start + NBITS_HEIGHT;
//			double height = ((double)g.view(start, end).decodeIntBase()) / ((double)(Math.pow(2,NBITS_HEIGHT)-1)) * 2. - 1.; // in [-1,1)
//
//			return Optional.of(new Gene<>(
//					g.view(i, i + consensus.size() + NBITS_CENTER + NBITS_WIDTH + NBITS_HEIGHT)
//					, new TriangleRBF(center, width, height))
//					);
//		}
//	}

	/**
	 * Method implements functional interface {@link Gene.Reader#read(BitGenome, BitGenome, int)}
	 */
	public static <G extends BinaryGenome<G>> Optional<Gene<G,Triangle>> read(LinearGenome.KMP<G> consensus, G g, int i) {
		int consensusSize = consensus.pattern.size();
		if (i + consensusSize + NBITS_CENTER + NBITS_WIDTH + NBITS_HEIGHT > g.size()) {
			return Optional.empty();
		} else {
			int start = i + consensusSize;
			int end = start + NBITS_CENTER;
			int centerInt = g.view(start, end).decodeIntGray();
			start = end;
			end = start + NBITS_WIDTH;
			int widthInt = g.view(start, end).decodeIntGray();
			start = end;
			end = start + NBITS_HEIGHT;
			int heightInt = g.view(start, end).decodeIntGray();

			double center = centerFromInt(centerInt);
			double width = widthFromInt(widthInt);
			double height = heightFromInt(heightInt);

			return Optional.of(new Gene<>(
					g.view(i, i + consensusSize + NBITS_CENTER + NBITS_WIDTH + NBITS_HEIGHT)
					, i
					, new Triangle(center, width, height)
					));
		}
	}

	private static double centerFromInt(int centerInt) {
		return (centerInt) / (Math.pow(2,NBITS_CENTER)-1); // in [0,1]
	}

	private static double widthFromInt(int widthInt) {
		return (widthInt + 1.) / ((Math.pow(2,NBITS_WIDTH))) / 2.; // in [-1,1)
	}

	private static double heightFromInt(int heightInt) {
		return (heightInt) / (Math.pow(2,NBITS_HEIGHT)-1) * 2. - 1.; // in (0, 0.5]
	}

	public static <G extends BinaryGenome<G>> Optional<Gene<G,Triangle>> readWithCenterFromPosition(LinearGenome.KMP<G> consensus, G g, int i) {
		Optional<Gene<G,IntPair>> widthAndHeightMaybe = NumberGene.<G>readIntsWithDigits(NBITS_WIDTH, NBITS_HEIGHT).read(consensus, g, i);
		if (widthAndHeightMaybe.isEmpty()) {
			return Optional.empty();
		} else {
			Gene<G,IntPair> widthAndHeight = widthAndHeightMaybe.get();
			double center = ((double)i) / (double)(g.size()-consensus.pattern.size()-NBITS_WIDTH-NBITS_HEIGHT);
			double width = widthFromInt(widthAndHeight.getValue().getX());
			double height = heightFromInt(widthAndHeight.getValue().getY());

			G whg = widthAndHeight.getSequence();

			return Optional.of(new Gene<>(
					whg,
					i,
					new Triangle(center, width, height)
					));
		}
	}


	@Override
	public double valueAt(double x) {
		return this.getHeight();
	}

	@Override
	public double weightAt(double x) {
		double d = Math.abs(this.center-x);
		return d < width
				? 1. - d/this.width
				: 0;
	}

	/**
	 * Apply the function to a value (value should be between 0 and 1).
	 * The value of the function is based on the three properties of the TriangleRBF: center, width, and height.
	 * It is defined so that f(peak) = height, and the value decreases linearly until it reaches f(peak+-width) = 0. The value is 0 outside this region.
	 */
	@Override
	public double applyAsDouble(double x) {
		return this.weightAt(x) * this.valueAt(x);
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Triangle)) return false;
		Triangle that = (Triangle) obj;
		return this.center == that.center
				&& this.width == that.width
				&& this.height == that.height;
	}

	@Override
	public String toString() {
		return String.format("[x: %f, w: %f, h: %f]",this.center,this.width,this.height);
	}

}