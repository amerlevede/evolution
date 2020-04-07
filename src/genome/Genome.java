package genome;

import genome.binary.BinaryGenome;
import genome.integer.IntegerGenome;

/**
 * General class of mutable data structures that are intended for use as a genome.
 * Any further properties are encapsulated in separate interfaces (e.g. {@link LinearGenome}, {@link VarLengthGenome}, {@link BinaryGenome}, {@link IntegerGenome}, {@link GenomeWithHistory}).
 * @author adriaan
 * @param <G> The implementing class.
 */
public interface Genome<G extends Genome<G>> {

	/**
	 * Create a copy of this genome.
	 */
	G copy();

	G view();

	G refersTo();

}