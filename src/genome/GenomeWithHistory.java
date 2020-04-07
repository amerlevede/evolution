package genome;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * General interface for {@link LinearGenome}s that remember the history of each genetic element.
 * The ``history'' is initialized as a unique identifier for each genetic element. This id is retained through genome modifications and copies, to be compared at a later time.
 * @author adriaan
 */
public interface GenomeWithHistory<G extends GenomeWithHistory<G>> extends LinearGenome<G> {

	/**
	 * Check if a particular bit on this genome is homologous to a bit on the other genome.
	 * @param i - index on this genome
	 * @param g - other genome
	 * @param j - index on other genome
	 * @return this.getId(i) == g.getId(j)
	 */
	default boolean isHomologousTo(int i, G g, int j) {
		return this.getId(i) == g.getId(j);
	}

	/**
	 * Check if the target genome is homologous to this one.
	 * A homologous sequence has the exact same sequence of bit identifiers (but may have a different bit sequence, i.e. point mutations).
	 * @return true iff this.size() == g.size() and this.isHomologousTo(i, g, i) for all indices 0<=i<this.size()
	 */
	default boolean isHomologousTo(G g) {
	    if (g.size() != this.size()) return false;
	    for (int i=0; i<this.size(); i++) {
	        if (g.getId(i) != this.getId(i)) return false;
	    }
	    return true;
	}

	/**
	 * Get all indices on this genome which are homologous to the given bit on the given genome.
	 * @return this.homologs(g.getId(i))
	 * @see #homologs(long)
	 */
	default IntStream homologsOf(G g, int i) {
		return this.homologs(g.getId(i));
	}

	/**
	 * Get all indices on this genome which are homologous to the given bit on this genome.
	 * @return this.homologs(this.getId(i))
	 * @see #homologs(long)
	 */
	default IntStream homologsOf(int i) {
		return this.homologs(this.getId(i));
	}

	/**
	 * Get all indices on this genome which are homologous to the given id
	 */
	IntStream homologs(long id);

	/**
	 * Get the unique identifier of the bit at the given index
	 */
	long getId(int index);

	/**
	 * All the unique identifiers in this genome.
	 */
	default LongStream uniqueIds() {
		return IntStream.range(0, this.size()).mapToLong(this::getId).distinct();
	}

    /**
     * Check if the target genome is identical to this one.
     * @return this.isHomologousTo(g) && this.sameSequence(g);
     * @see #isHomologousTo(GenomeWithHistory)
     * @see #sameSequence(LinearGenome)
     */
    default boolean unmutated(G g) {
        return this.isHomologousTo(g) && this.sameSequence(g);
    }

}