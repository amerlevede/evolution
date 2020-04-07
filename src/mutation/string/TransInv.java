package mutation.string;

import java.util.Optional;
import java.util.function.Function;

import genome.VarLengthGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import util.Assert;
import util.DiscreteDistribution;

/**
 * A mutation rule that either does a translocation, an inversion, or a combination ({@link #perform(Optional, VarLengthGenome, int, int, int)}).
 *
 * @author adriaan
 */
public final class TransInv {

    private TransInv(){
    	Assert.utilityClass();
    }

    public static <G extends VarLengthGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
        return (rng) -> (g, stats) -> {
        	int len = lendist.apply(g).applyAsInt(rng);
            while (len >= g.size()) len = lendist.apply(g).applyAsInt(rng);
            int fromlocus = rng.nextInt(g.size() - len + 1); // NOTE: bits near the start and end of the genome are less likely to be affected.

        	boolean doTrans = false;
        	boolean doInv = false;
        	while (!doTrans && !doInv) {
        		doTrans = rng.nextBoolean();
        		doInv = rng.nextBoolean();
        	}

        	if (doTrans) {
	            int tolocus = rng.nextInt(g.size() - len + 1);
	            while (fromlocus == tolocus) tolocus = rng.nextInt(g.size() - len + 1);

	            if (doInv) {
	            	TransInv.perform(stats, g, fromlocus, tolocus, len);
	            } else {
	            	Translocation.perform(stats, g, fromlocus, tolocus, len);
	            }
        	} else {
        		if (doInv) {
        			Inversion.perform(stats, g, fromlocus, len);
        		} else {
        			Assert.unreachableCode();
        		}
        	}
        };
    }

    /**
     * Perform a combination translocation-inversion, where the same piece is moved an also inverted.
     */
    public static <G extends VarLengthGenome<G>> void perform(Optional<MutationStats> stats, G g, int fromlocus, int tolocus, int len) {
    	Translocation.perform(stats, g, fromlocus, tolocus, len);
    	Inversion.perform(stats, g, tolocus, len);
    }

}
