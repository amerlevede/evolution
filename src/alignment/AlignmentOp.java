package alignment;

import genome.LinearGenome;

/**
 * Alignment operator. Functional interface whose instances represent ways to align genomes.
 */
@FunctionalInterface
public interface AlignmentOp<G extends LinearGenome<G>> extends VarOAlignmentOp<G> {

    @Override
    public Alignment<G> apply(G a, G b);
    
    public default double score(G a, G b) {
    	return this.apply(a, b).score;
    }
    
}
