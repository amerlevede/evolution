package genome;

import java.util.stream.IntStream;

/**
 * General interface for {@link Genome} types that are organized as a linear data string.
 * Supports comparison, search, and some modification methods that do not depend on the contained type.
 * @author adriaan
 */
public interface LinearGenome<G extends LinearGenome<G>> extends Genome<G> {

	/**
	 * Copy this Genome.
	 * Equivalent to {@link #copy(int, int)} applied to the whole string.
	 */
	@Override
	default G copy() {
		return this.copy(0,this.size());
	}

	/**
	 * Copy a section of this Genome.
	 * Similar to {@link #view(int, int)} except this will copy the data, so the result will not expire.
	 */
	G copy(int inclusiveStart, int exclusiveEnd);

	/**
	 * Create a View of this Genome.
	 * Equivalent to {@link #view(int, int)} applied to this whole genome from 0 to this.size().
	 */
	@Override
	default G view() {
		return this.view(0, this.size());
	}

	default G view1(int index) {
		return this.view(index, index+1);
	}

	G view(int inclusiveStart, int exclusiveEnd);

	G reversedView();

	/**
	 * Size or length of the genome.
	 */
	int size();

	void swap(int i, int j);

	default void bubble(int srcIndex, int destIndex) {
		while (srcIndex < destIndex) {
			this.swap(srcIndex, ++srcIndex);
		}
		while (srcIndex > destIndex) {
			this.swap(srcIndex, --srcIndex);
		}
	}

	/**
	 * Shift the bits of this genome to the left without affecting size.
	 * @param n
	 */
	void shiftLeft(int n);

	/**
	 * Shift the bits of this genome to the right without affecting size.
	 */
	void shiftRight(int n);

	default void rotateLeft(int n) {
		if (n < 0) {
			this.rotateRight(-n);
		} else {
			G ref = this.copy(0, n);
			this.shiftLeft(n);
			this.paste(this.size()-n,ref);
		}
	}

	default void rotateRight(int n) {
		if (n < 0) {
			this.rotateLeft(-n);
		} else {
			G ref = this.copy(this.size()-n, this.size());
			this.shiftRight(n);
			this.paste(0,ref);
		}
	}

	/**
	 * Paste a sequence onto the genome.
	 * Equivalent to {@link #paste(int, G, int, int)} applied to the whole target sequence from 0 to g.size().
	 */
	default void paste(int index, G g) {
		this.paste(index, g, 0, g.size());
	}

	/**
	 * Paste a sequence onto the genome.
	 * Behavior when pasted section exceeds genome size depends on implementation.
	 * @param index - Index to start at in this genome
	 * @param g - Source of sequence to paste
	 * @param inclusiveStart - Start index of sequence to insert on genome g (inclusive)
	 * @param exclusiveEnd - End index of sequence to insert on genome g (exclusive)
	 */
	void paste(int index, G g, int inclusiveStart, int exclusiveEnd);

	/**
	 * Check if another Genome has the same bit sequence as this.
	 * This also requires that both genomes are the same length.
	 */
	default boolean sameSequence(G that) {
        if (this.size() != that.size()) return false;
        for (int i=0; i<this.size(); i++) {
            if (!this.sameAt(i, that, i)) return false;
        }
        return true;
	}

	boolean sameAt(int thisIndex, G that, int thatIndex);

	/**
	 * Check if an value is "inside" this genome.
	 * This requires that it is a valid reference to a "splice" between bits but also does not refer to the left or right end.
	 * This is useful because crossover at the ends is illegal (it may result in zero-length genomes).
	 */
	default boolean innerSplice(int i) {
		return i>0 && i<this.size();
	}

	/**
	 * Check if a subsequence is "inside" this genome.
	 * This requires that it refers to a subsequence and does not touch the left or right end of this genome.
	 * This is useful because crossover at the ends is illegal (it may result in zero-length genomes).
	 */
	default boolean innerSplice(int inclusiveStart, int exclusiveEnd) {
        return inclusiveStart > 0 && exclusiveEnd < this.size() && exclusiveEnd > inclusiveStart;
	}

//	/** Find all occurrences of a substring in this genome.
//	 * @see #findFirst(G)
//	 * @see #findAllNonOverlapping(G)
//	 */
//	List<Integer> findAllOverlapping(G pattern);
//
//	/**
//	 * Find all occurrences of a substring in this genome, not considering overlaps.
//	 * @see #findAllOverlapping(G)
//	 * @se {@link #findFirst(G)}
//	 */
//	List<Integer> findAllNonOverlapping(G pattern);
//
//	/**
//	 * Find the first occurrence of a substring in this genome.
//	 * @see #findAllOverlapping(G)
//	 * @see #findAllNonOverlapping(G)
//	 * @return The first index of the first occurrence of the substring if any, otherwise -1.
//	 */
//	int findFirst(G pattern);

//	/**
//	 * Check if this genome contains a given substring.
//	 * @see #findFirst(G)
//	 */
//	default boolean contains(G substr) {
//		return this.findFirst(substr) != -1;
//	}

	/**
	 * Compile this LinearGenome to a {@link KMP} array, which can be used to efficiently search another genome for this pattern.
	 * When this genome is updated, the compiled array expires. Using it after that will lead to incorrect results.
	 */
	default KMP<G> kmpView() {
		return new KMP<>(this.view());
	}

	/**
	 * Compile this LinearGenome to a {@link KMP}  array, which can be used to efficiently search another genome for this pattern.
	 * Differs from {@link #kmpView()} in that the pattern does not expire when this genome is mutated. (It also does not update! It simply refers to the old version of this genome)
	 */
	default KMP<G> kmpCopy() {
		return new KMP<>(this.copy());
	}

	default IntStream findAllOverlapping(KMP<G> pattern) {
		return pattern.findAllOverlappingIn(this.view());
	}

	default IntStream findAllNonOverlapping(KMP<G> pattern) {
		return pattern.findAllNonOverlappingIn(this.view());
	}

	default int findFirst(KMP<G> pattern) {
		return pattern.findFirstIn(this.view());
	}

	default boolean contains(KMP<G> pattern) {
		return pattern.containedIn(this.view());
	}

	default boolean sameSequence(KMP<G> pattern) {
		return pattern.sameSequence(this.view());
	}

	public static class KMP<G extends LinearGenome<G>> {

		final int[] kmp;
		public final G pattern;

		/**
		 * Compile a LinearGenome to obtain a KMP array. This can be used in the Knuth-Morris-Pratt algorithm to efficiently find this genome sequence inside another genome.
		 */
		KMP(G pattern) {
			this.pattern = pattern.view();
			kmp = new int[pattern.size()+1];
	    	kmp[0] = -1;
	    	int candidate = 0;
	    	for (int j=1; j<pattern.size(); j++) {
	    		if (pattern.sameAt(j, pattern, candidate)) {
	    			kmp[j] = kmp[candidate];
	    		} else {
	    			kmp[j] = candidate;
	    			candidate = kmp[candidate];
	    			while (candidate >= 0 && !pattern.sameAt(j, pattern, candidate)) {
	    				candidate = kmp[candidate];
	    			}
	    		}
	    		candidate++;
	    	}
	    	kmp[pattern.size()] = candidate;
		}

		/**
		 * Find all overlapping occurrences of this (compiled) pattern in the supplied genome.
		 */
		public IntStream findAllOverlappingIn(G g) {
			IntStream.Builder result = IntStream.builder();
			int i=0, j=0;
			while (i < g.size()) {
				if (g.sameAt(i, pattern, j)) {
					i++;
					j++;
					if (j == pattern.size()) {
						result.add(i-j);
						j = kmp[j];
					}
				} else {
					j = kmp[j];
					if (j < 0) {
						i++;
						j++;
					}
				}
			}

			return result.build();
		}

		/**
		 * Find all non-overlapping occurrences of this (compiled) pattern in the supplied genome.
		 */
		public IntStream findAllNonOverlappingIn(G g) {
			IntStream.Builder result = IntStream.builder();
			int i=0, j=0;
			while (i < g.size()) {
				if (g.sameAt(i, pattern, j)) {
					i++;
					j++;
					if (j == pattern.size()) {
						result.add(i-j);
						j = 0;
					}
				} else {
					j = kmp[j];
					if (j < 0) {
						i++;
						j++;
					}
				}
			}

			return result.build();
		}

		/**
		 * Find the first occurrence of this (compiled) pattern in the supplied genome, or -1 if it is absent.
		 */
		public int findFirstIn(G g) {
			int i=0, j=0;
			while (i < g.size()) {
				if (g.sameAt(i, pattern, j)) {
					i++;
					j++;
					if (j == pattern.size()) {
						return i-j;
					}
				} else {
					j = kmp[j];
					if (j < 0) {
						i++;
						j++;
					}
				}
			}

			return -1;
		}

		public boolean containedIn(G g) {
			return this.findFirstIn(g) != -1;
		}

		public boolean sameSequence(G g) {
			return g.size() == this.pattern.size() && this.containedIn(g);
		}
	}

}

