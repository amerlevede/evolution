package crossover;

import util.Assert;

/**
 * Utility class implementing the cloning crossover.
 * This is a trivial crossover operator which does not perform any recombination.
 * @author adriaan
 */
public final class CloningCross {

    private CloningCross(){
    	Assert.utilityClass();
    }

	/**
	 * @see CrossoverRule.N
	 * @see {@link #distinctN(int)} */
	public static <G> CrossoverRule<G> of(CrossoverRule.N n) {
		return crossover();
	}

    /**
     * A trivial crossover operator, which does not perform any recombination.
     */
    public static <G> CrossoverRule<G> crossover() {
        return (rng) -> (a,b) -> perform(a, b);
    }

    public static <G> void perform(G a, G b) {

    }

}
