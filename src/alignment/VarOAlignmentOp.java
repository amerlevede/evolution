package alignment;

import java.util.function.BiFunction;

import genome.LinearGenome;

/**
 * Alignment operator. Functional interface whose instances represent a way to align two genomes. 
 * @author adriaan
 */
@FunctionalInterface
public interface VarOAlignmentOp<G extends LinearGenome<G>> extends BiFunction<G,G,VarOAlignment<G>> {

    @Override
    public VarOAlignment<G> apply(G a, G b);
    
}
