package mutation;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Functional interface for mutation operators.
 * A mutation operator is essentially a Consumer that modifies a Genome. The source of randomness is inaccessible at this layer (must be constructed through MutationRule).
 * The operator also modifies a MutationStats object, which is useful for inspecting the concrete outcome after performing a non-deterministic mutation operator.
 *
 * @author adriaan
 */
@FunctionalInterface
public interface MutationOp<G> extends BiConsumer<G, Optional<MutationStats>> {

	/**
	 * Perform this mutation, modifying a given MutationStats object.
	 */
    @Override void accept(G g, Optional<MutationStats> stats);

    default Optional<MutationStats> newStats() {
//    	return Optional.empty();
    	return Optional.of(new MutationStats());
    }

    static <G,S> MutationOp<G> withStats(MutationOp<G> op) {
    	return new MutationOp<G>() {
    		@Override
    		public void accept(G g, Optional<MutationStats> stats) {
    			op.accept(g, stats);
    		}
    		public Optional<MutationStats> newStats() {
    			return Optional.of(new MutationStats());
    		};
    	};
    }

    /**
     * Perform this mutation, and also return its effects as a MutationStats object.
     */
    default Optional<MutationStats> mutateAndGetStats(G g) {
    	Optional<MutationStats> stats = this.newStats();
    	this.accept(g, stats);
    	return stats;
    }

    default void mutate(G g) {
    	mutateAndGetStats(g);
    }
}
