package mutation.binary;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import genome.VarLengthGenome;
import genome.binary.BinaryGenome;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.MutationStats.MutationType;
import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;

public final class Insertion {
	
	private Insertion() {
		Assert.utilityClass();
	}

	public static final MutationType TYPE = new MutationType("Insertion");
	
	//    // Carl version
	//    public static MutationRule duplicationWithSize(Function<Genome,DiscreteDistribution> ndist) {
	//    	return InDel.insertionWithSequence((g) -> (rng) -> {
	//    		int len = ndist.apply(g).applyAsInt(rng);
	//    		while (len > g.size()) len = ndist.apply(g).applyAsInt(rng);
	//			int start = len == g.size() ? 0 : rng.nextInt(g.size()-len);
	//			return Genome.of(g.view(start, start+len));
	//    	});
	//    }
	    
    public static <G extends VarLengthGenome<G>> void perform(Optional<MutationStats> stats, G g, int locus, G seq) {
		g.insert(locus, seq);
		stats.ifPresent(s->s.add(Insertion.TYPE,seq.size()));
	}

	

	//    // Carl version
	//    public static MutationRule duplicationWithSize(Function<Genome,DiscreteDistribution> ndist) {
	//    	return InDel.insertionWithSequence((g) -> (rng) -> {
	//    		int len = ndist.apply(g).applyAsInt(rng);
	//    		while (len > g.size()) len = ndist.apply(g).applyAsInt(rng);
	//			int start = len == g.size() ? 0 : rng.nextInt(g.size()-len);
	//			return Genome.of(g.view(start, start+len));
	//    	});
	//    }
	    
	/**
	 * Mutation rule that performs insertions using the given function to generate new inserted sequences.
	 * The locus of the segments is drawn randomly from all possible bits in the target genome.
	 */
	public static <G extends VarLengthGenome<G>> MutationRule<G> withSequence(Function<G,CategoricalDistribution<? extends G>> seqdist) {
	    return (rng) -> (g, stats) -> {
	    	G seq = seqdist.apply(g).apply(rng);
	        int locus = rng.nextInt(g.size()+1);
	        perform(stats, g, locus, seq);
	    };
	}

	/**
	 * Mutation rule that performs insertions using the given function to generate the length of newly inserted sequences.
	 * The locus of the segments is drawn randomly from all possible bits in the target genome.
	 * The bit sequence of the inserted region will be uniformly random from all possible genomes of that size.
	 */
	public static <G extends BinaryGenome<G>> MutationRule<G> withSize(Function<G,DiscreteDistribution> lendist) {
		return (rng) -> (g, stats) -> {
			int len = lendist.apply(g).applyAsInt(rng);
			int locus = rng.nextInt(g.size()+1);
			perform(rng, stats, g, locus, len);
		};
	}

	public static <G extends BinaryGenome<G>> void perform(Random rng, Optional<MutationStats> stats, G g, int locus, int len) {
		g.insertRandom(rng, locus, len);
		stats.ifPresent(s->s.add(Insertion.TYPE,len));
	}

}
