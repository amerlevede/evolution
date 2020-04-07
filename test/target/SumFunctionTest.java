package target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fitness.binary.SumFunction;
import genome.LinearGenome;
import genome.RandomInit;
import genome.binary.BitGenome;
import genome.gene.Gene;
import genome.gene.Triangle;

class SumFunctionTest extends RandomInit {

	public DoubleUnaryOperator flat0;
	public DoubleUnaryOperator flat1;
	public DoubleUnaryOperator sin;

	@BeforeEach
	public void setupFunctions() {
		this.flat0 = (x) -> 0;
		this.flat1 = (x) -> 1;
		this.sin = (x) -> Math.sin(x / (2*Math.PI));
	}

	@Test
	public void testFunctionD_equal() {
		assertEquals(0., SumFunction.functionDistance(flat0, flat0));
		assertEquals(0., SumFunction.functionDistance(flat1, flat1));
		assertEquals(0., SumFunction.functionDistance(sin, sin));
	}

	@Test
	public void testFunctionD_unequal() {
		assertEquals(1., SumFunction.functionDistance(flat0, flat1));
	}

	@Test
	public void testTriangleReader() {
		LinearGenome.KMP<BitGenome> consensus = BitGenome.readUnsafe("1010101010").kmpView();
		BitGenome centerG   = BitGenome.zeroes(10);
		BitGenome widthG    = BitGenome.random(10).apply(rng);
		BitGenome heightG   = BitGenome.readUnsafe("1000000000");

		BitGenome gene = consensus.pattern.copy();
		gene.append(centerG);
		gene.append(widthG);
		gene.append(heightG);

		assertEquals(consensus.pattern.size()+centerG.size()+widthG.size()+heightG.size(),
				gene.size());

		Optional<Gene<BitGenome,Triangle>> triangle = Triangle.read(consensus, gene, 0);
		assertTrue(triangle.isPresent());

		assertEquals(0, triangle.get().getValue().getCenter());
		assertEquals(1, triangle.get().getValue().getHeight());
	}

	@Test
	public void triangleApply() {
		double center = 0;
		double width = 0.5;
		double height = 1;

		Triangle triangle = new Triangle(center, width, height);

		assertEquals(height, triangle.applyAsDouble(center)); // At peak
		assertEquals(0, triangle.applyAsDouble(center+width)); // At border
		assertEquals(0, triangle.applyAsDouble(1)); // Outside border
		assertEquals(height/2, triangle.applyAsDouble((center+width)/2)); // Halfway
	}

}
