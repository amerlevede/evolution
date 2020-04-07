package mutation.string;

import java.util.Optional;
import java.util.function.Function;

import genome.VarLengthGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.DiscreteDistribution;

/**
 * Mutation operator that moves a part of the genome to somewhere else.
 * @author adriaan
 */
public final class Translocation {

    private Translocation(){
    	Assert.utilityClass();
    }

    public final static MutationType TYPE = new MutationType("Translocation");

    public static <G extends VarLengthGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
        return (rng) -> (g, stats) -> {
        	if (g.size() > 1) {
	            int len = lendist.apply(g).applyAsInt(rng);
	            while (len >= g.size()) len = lendist.apply(g).applyAsInt(rng);

	            int fromlocus = rng.nextInt(g.size() - len + 1); // NOTE: bits near the start and end of the genome are less likely to be affected.
	            int tolocus = rng.nextInt(g.size() - len + 1);
	            while (fromlocus == tolocus) tolocus = rng.nextInt(g.size() - len + 1);

	            Translocation.perform(stats, g, fromlocus, tolocus, len);
	        }
        };
    }

    public static <G extends VarLengthGenome<G>> MutationRule<G> withSize(int n) {
    	return Translocation.withSize(g_ignore -> rng_ignore -> n);
    }

    public static <G extends VarLengthGenome<G>> void perform(Optional<MutationStats> stats, G g, int fromlocus, int tolocus, int len) {
    	G seq = g.copy(fromlocus, fromlocus+len);
        g.delete(fromlocus, fromlocus+len);
        g.insert(tolocus, seq);

    	stats.ifPresent(s->s.add(Translocation.TYPE,len));
    }

}
