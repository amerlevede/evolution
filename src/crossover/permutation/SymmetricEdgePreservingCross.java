package crossover.permutation;

import java.util.Arrays;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import crossover.CrossoverRule;
import genome.integer.IntegerGenome;
import genome.integer.Permutation;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Variation of {@link PerfectEdgeCross}, using the symmetric-EAX method of finding AB-cycles.
 * @see Nagata et al. (1997)
 */
public final class SymmetricEdgePreservingCross {

	private SymmetricEdgePreservingCross() {
		Assert.utilityClass();
	}

	/**
	 * Random edge preserving crossover.
	 * Given two parent permutations, modify the first one so that it has only edges that appear in a or b. 
	 * @param trialsf - The number of trials before giving up and returning a copy of a random parent.
	 */
	public static <G extends IntegerGenome<G>> CrossoverRule<G> crossover(int trials) {
		return (rng) -> (a, b) -> perform(trials, rng, a ,b);
	}

	/** @see SymmetricEdgePreservingCross#crossover() */
	public static <G extends IntegerGenome<G>> int performAndGetN(int trials, Random rng, G a, G b) {
		int size = a.size();
		if (b.size() != size) throw new IllegalArgumentException();

		Permutation ea = a.permutationView().edgeTransform();
		Permutation eb = b.permutationView().edgeTransform();
		Permutation eainv = ea.inverse();
		Permutation ebinv = eb.inverse();

		int[] abcycles_a = new int[size];
		int[] abcycles_b = new int[size];

		int tryCounter=0;
		int len = 0;
		while (len < size) {
			tryCounter++;
			int c = partitionABcycles(rng, size, ea, eainv, eb, ebinv, abcycles_a, abcycles_b);
			
			boolean[] eset = new boolean[c+1];
			if (tryCounter >= trials) {
				boolean val = rng.nextBoolean();
				for (int i=0; i<eset.length; i++) eset[i] = val;
			} else {
				for (int i=0; i<eset.length; i++) eset[i] = rng.nextBoolean();
			}

			boolean[] used = new boolean[size];
			len = 0;
			int i=0;
			while (len<size) {
				a.set(len++, i);
				used[i] = true;

				       if (!eset[abcycles_b[i]]            && !used[eb.get(i)]) {
					i = eb.get(i);
				} else if (!eset[abcycles_b[ebinv.get(i)]] && !used[ebinv.get(i)]) {
					i = ebinv.get(i);
				} else if ( eset[abcycles_a[i]]            && !used[ea.get(i)]) {
					i = ea.get(i);
				} else if ( eset[abcycles_a[eainv.get(i)]] && !used[eainv.get(i)]) {
					i = eainv.get(i);
				} else {
					break;
				}
			}
		}

		return tryCounter;
	}
	
	/**
	 * Generate a set of ab cycles using the EAX method.
	 * @param abcycles_a - This array will be overwritten with integers identifying the ab cycles by numbers between 0 and (returned value). That is, abcycles[i] refers to the id of the cycle of which the edge in parent a that starts at i is a member.
	 * @param abcycles_b - Same as abcycles_b, but referring to edges from parent b.
	 * @return the number of cycles
	 */
	static int partitionABcycles(Random rng, int size, Permutation ea, Permutation eainv, Permutation eb, Permutation ebinv, int[] abcycles_a, int[] abcycles_b) {
		
		Arrays.fill(abcycles_a, -1);
		Arrays.fill(abcycles_b, -1);
		
		// Respectful: take out all shared edges
		int tot = 2*size;
		for (int j=0; j<size; j++) {
			if (ea.get(j) == eb.get(j)) {
				abcycles_a[j] = 0;
				abcycles_b[j] = 0;
				tot -= 2;
			} else if (ea.get(j) == ebinv.get(j)) {
				abcycles_a[j] = 0;
				abcycles_b[ebinv.get(j)] = 0;
				tot -= 2;
			}
		}
		
		int i = DiscreteDistribution.getUniform(rng, 0, size);
		int c = 0;
		
		int[] path = new int[tot+1];
		int[] lastseen = new int[size];
		
		Arrays.fill(lastseen, -1);
		Arrays.fill(path, -1);
		path[0] = i;
		
		int len = 0;
		path:
		while (len < tot) {
			// set b step
			boolean unusedFwd_b = abcycles_b[i] == -1;
			boolean unusedBwd_b = abcycles_b[ebinv.get(i)] == -1;
			
			if (!unusedFwd_b && !unusedBwd_b) {
				for (int j=0; j<size; j++) {
					if (abcycles_a[j] != -1) abcycles_a[j]++;
					if (abcycles_b[j] != -1) abcycles_b[j]++;
				}
				c++;
				int[] candidates = IntStream.range(0,size)
					.filter(j -> abcycles_b[j] == -1 || abcycles_b[ebinv.get(j)] == -1)
					.toArray();
				i = DiscreteDistribution.getUniform(rng, candidates);
				path[len] = i;
				lastseen[i] = len;
				continue path;
			}
			
			lastseen[i] = len++;
			if (unusedFwd_b && (!unusedBwd_b || rng.nextBoolean())) {
				abcycles_b[i] = 0;
				i = eb.get(i);
			} else {
				i = ebinv.get(i);
				abcycles_b[i] = 0;
			}
			
			path[len] = i;
			
			// Check if this forms a new loop ..
			if (lastseen[i] != -1 && (lastseen[i] - len) % 2 == 0) {
				int j = lastseen[i];
				boolean usedFwd_1 = path[j+1] == ea.get(path[j]) && abcycles_a[path[j]] == 0;
				boolean usedBwd_1 = path[j+1] == eainv.get(path[j]) && abcycles_a[path[j+1]] == 0;
				
				if (usedFwd_1 || usedBwd_1)	{
					c++;
					while (j < len) {
						boolean usedFwd_a = path[j+1] == ea.get(path[j]);
						boolean usedBwd_a = path[j+1] == eainv.get(path[j]);
						if (usedFwd_a && !usedBwd_a) {
							if (abcycles_a[path[j]] == 0) abcycles_a[path[j]] = c;
						} else if (usedBwd_a && !usedFwd_a) {
							if (abcycles_a[path[j+1]] == 0) abcycles_a[path[j+1]] = c;
						} else Assert.unreachableCode();
						j++;
						
						boolean usedFwd_b = path[j+1] == eb.get(path[j]);
						boolean usedBwd_b = path[j+1] == ebinv.get(path[j]);
						if (usedFwd_b && !usedBwd_b) {
							if (abcycles_b[path[j]] == 0) abcycles_b[path[j]] = c;
						} else if (usedBwd_b && !usedFwd_b) {
							if (abcycles_b[path[j+1]] == 0) abcycles_b[path[j+1]] = c;
						} else Assert.unreachableCode();
						j++;
					}
				}
			}
			
			lastseen[i] = len++;
			
			// set a step
			boolean unusedFwd_a = abcycles_a[i] == -1;
			boolean unusedBwd_a = abcycles_a[eainv.get(i)] == -1;
			
			if (unusedFwd_a && (!unusedBwd_a || rng.nextBoolean())) {
				abcycles_a[i] = 0;
				i = ea.get(i);
			} else {
				i = eainv.get(i);
				abcycles_a[i] = 0;
			}
			
			if (len == 2*size) break;
			path[len] = i;
			
			if (lastseen[i] != -1 && (lastseen[i] - len) % 2 == 0) {
				int j = lastseen[i];
				boolean usedFwd_1 = path[j+1] == eb.get(path[j]) && abcycles_b[path[j]] == 0;
				boolean usedBwd_1 = path[j+1] == ebinv.get(path[j]) && abcycles_b[path[j+1]] == 0;
				
				if (usedFwd_1 || usedBwd_1) {
					c++;
					while (j < len) {
						boolean usedFwd_b = path[j+1] == eb.get(path[j]);
						boolean usedBwd_b = path[j+1] == ebinv.get(path[j]);
						if (usedFwd_b && !usedBwd_b) {
							if (abcycles_b[path[j]] == 0) abcycles_b[path[j]] = c;
						} else if (usedBwd_b && !usedFwd_b) {
							if (abcycles_b[path[j+1]] == 0) abcycles_b[path[j+1]] = c;
						} else Assert.unreachableCode();
						j++;
						
						boolean usedFwd_a = path[j+1] == ea.get(path[j]);
						boolean usedBwd_a = path[j+1] == eainv.get(path[j]);
						if (usedFwd_a && !usedBwd_a) {
							if (abcycles_a[path[j]] == 0) abcycles_a[path[j]] = c;
						} else if (usedBwd_a && !usedFwd_a) {
							if (abcycles_a[path[j+1]] == 0) abcycles_a[path[j+1]] = c;
						} else Assert.unreachableCode();
						j++;
					}
				}
			}
		}
		
		return c;
	}


	public static <G extends IntegerGenome<G>> void perform(int trialsf, Random rng, G a, G b) {
		performAndGetN(trialsf, rng, a, b);
	}

	/**
	 * Variant of the crossover that reports run time statistics to stderr.
	 * Max. number of trials is 10 000.
	 * @see #crossover(IntUnaryOperator)
	 */
	public static <G extends IntegerGenome<G>> CrossoverRule<G> loudCrossover() {
		return (rng) -> (a, b) -> {
			G aref = a.copy();
			int trials = performAndGetN(10000, rng, a, b);
			Permutation ea = a.permutationView().edgeTransform();
			Permutation earef = aref.permutationView().edgeTransform();
			Permutation eb = b.permutationView().edgeTransform();
			boolean trivialCross = 
					Permutation.equals(ea, earef)
				 || Permutation.equals(ea, earef.inverse())
				 || Permutation.equals(ea, eb)
				 || Permutation.equals(ea, eb.inverse());
			System.err.println(trials+"\t"+trivialCross);
		};
	}


}
