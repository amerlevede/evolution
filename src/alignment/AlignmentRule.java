package alignment;

import java.util.Random;

import genome.LinearGenome;

/**
 * Wrapper for AlignmentOp encapsulating random number generation.
 * @see AlignmentOp
 * @author adriaan
 * @note This is basically a Monad, but it turns out this design pattern is not so useful in Java.
 */
public interface AlignmentRule<G extends LinearGenome<G>> extends VarOAlignmentRule<G> {
	
	@Override
	AlignmentOp<G> apply(Random t);

}
