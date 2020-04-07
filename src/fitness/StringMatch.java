package fitness;

import java.util.function.ToDoubleBiFunction;

import alignment.AlignmentOp;
import genome.binary.BitGenome;
import util.Assert;

/**
 * Utility class implementing the StringMatch fitness function.
 * Fitness is equal to the distance between the genome and a target linear sequence.
 * 
 * @author adriaan
 */
public abstract class StringMatch {
	
	private StringMatch() {
		Assert.utilityClass();
	}
	
	/**
	 * Fitness function implementing StringMatch.
	 * The fitness of a genome is simply the alignment score obtained by comparing it to a target sequence.
	 * @param target - The target sequence
	 * @param align - The alignment operator used to compare the given genome and target
	 */
	public static <G, T> FitnessFunction<G> of(T target, ToDoubleBiFunction<G,T> distance) {
		return (g) -> calculate(target, distance, g);
	}
	
	/** @see #of(BitGenome, AlignmentOp) */
	public static <G, T> double calculate(T target, ToDoubleBiFunction<G,T> distance, G g) {
		return distance.applyAsDouble(g, target);
	}

}
