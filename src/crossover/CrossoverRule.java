package crossover;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import mutation.MutationRule;
import util.Assert;

/**
 * Wrapper for CrossoverOp encapsulating the dependence on random number generation.
 * @see CrossoverOp.
 * @author adriaan
 */
@FunctionalInterface
public interface CrossoverRule<G> extends Function<Random, CrossoverOp<G>> {

    @Override
    CrossoverOp<G> apply(Random t);

    public static class N {
    	public enum Type {
    		UNIFORM, VALUE;
    	}

    	public final Optional<Integer> val;
    	public final Type type;

    	private N(Type type, Optional<Integer> val) {
    		this.type = type;
    		this.val = val;
    	}

    	public final int value() {
    		if (this.type == Type.VALUE) {
    			return this.val.get();
    		} else {
    			throw new IllegalStateException();
    		}
    	}

    	public static final N UNIFORM = new N(Type.UNIFORM, Optional.empty());
    	public static N value(int val) {
    		return new N(Type.VALUE, Optional.of(val));
    	}

    	public static N valueOf(String str) {
    		switch (str) {
    		case ("uniform"):
    			return N.UNIFORM;
			default:
    			return value(Integer.valueOf(str));
    		}
    	}

    	@Override
    		public String toString() {
    		switch (this.type) {
    		case UNIFORM:
    			return "uniform";
    		case VALUE:
    			return String.valueOf(val.get());
    		default:
    			Assert.unreachableCode();
    			return "";
    		}
    		}
    }

    default CrossoverRule<G> andThenMutate(MutationRule<G> mutation) {
    	return (rng) -> (g1, g2) -> {
    		this.apply(rng).accept(g1, g2);
    		mutation.apply(rng).mutate(g1);
    		mutation.apply(rng).mutate(g2);
    	};
    }

}
