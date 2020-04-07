package genome.integer;

import java.util.function.IntUnaryOperator;

/**
 * Implementation of {@link Permutation} with no internal data memory.
 * Useful to avoid unnecessarily writing data arrays for permutations such as {@link Permutation#id(int)} and {@link Permutation#action(int, Permutation...)}.
 */
class FunctionPermutation implements Permutation {

	final int size;
	final IntUnaryOperator f;

	FunctionPermutation(int size, IntUnaryOperator f) {
		this.size = size;
		this.f = f;
	}

	@Override
	public int get(int i) {
		return f.applyAsInt(i);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public String toString() {
    	int len = 2+(int)Math.log10(-0.5+this.stream().max().orElse(0));
    	StringBuilder result = new StringBuilder();
    	result.append("|");
    	this.stream().forEachOrdered(i -> result.append(String.format("% "+len+"d", i)));
    	result.append(" |");
    	return result.toString();
	}

}
