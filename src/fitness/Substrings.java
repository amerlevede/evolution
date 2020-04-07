package fitness;

import java.util.Collection;

import genome.LinearGenome.KMP;
import genome.binary.BinaryGenome;
import util.Assert;

/**
 * Utility class implementing the Substrings fitness function.
 * The fitness function counts the number of substrings (in a given target set) that are contained in the genome.
 *
 * @author adriaan
 */
public abstract class Substrings {

	private Substrings() {
		Assert.utilityClass();
	}

	/**
	 * Fitness function implementing the Substrings problem.
	 * Simply counts the number of substrings a genome contains (more substrings = higher fitness).
	 */
	public static <G extends BinaryGenome<G>> FitnessFunction<G> of(Collection<KMP<G>> targets) {
		return (g) -> calculate(targets, g);
	}

	public static <G extends BinaryGenome<G>> FitnessFunction<G> ordered(Collection<KMP<G>> targets) {
		return (g) -> calculateOrdered(targets, g);
	}

	public static <G extends BinaryGenome<G>> double calculate(Collection<KMP<G>> targets, G g) {
		return targets.stream().filter(t->t.containedIn(g)).count();
	}

	public static <G extends BinaryGenome<G>> double calculateOrdered(Collection<KMP<G>> targets, G g) {
		Iterable<Integer> iter = targets.stream().map(t->t.findFirstIn(g)).filter(i->i!=-1)::iterator;
		int result = 0;
		int max = -1;
		for (int i : iter) {
			result++;
			if (i > max) {
				max = i;
				result++;
			}
		}
		return result;
	}

}
