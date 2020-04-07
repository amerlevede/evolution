package alignment;

import java.util.Random;
import java.util.function.Function;

import genome.LinearGenome;

/**
 * Wrapper for AlignmentOp encapsulating random number generation.
 * @see AlignmentOp
 * @author adriaan
 */
public interface VarOAlignmentRule<G extends LinearGenome<G>> extends Function<Random,VarOAlignmentOp<G>> {
	
	@Override
	public VarOAlignmentOp<G> apply(Random t);

}
