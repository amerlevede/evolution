package genome;

import java.util.List;

import util.Assert;

/**
 * General interface for {@link LinearGenome} structures that can change in length.
 * @author adriaan
 */
public interface VarLengthGenome<G extends VarLengthGenome<G>> extends LinearGenome<G> {


	/**
	 * Delete segment from the genome.
	 * Throws an error when exclusiveEnd-inclusiveStart because the index cannot be inclusive and exclusive at the same time
	 * @throws IllegalArgumentException when attempting to delete whole genome.
	 * @throws IndexOutOfBoundsException when attempting to delete empty segment.
	 */
	void delete(int inclusiveStart, int exclusiveEnd);

	/**
	 * Insert a segment into the genome.
	 * Equivalent to {@link #insert(int, G, int, int)} applied to the full length from 0 to g.size().
	 */
	default void insert(int index, G g) {
		this.insert(index, g, 0, g.size());
	}

	/**
	 * Insert a segment into a genome.
	 * This will shift all bits to the right of the given index to the right by just enough to accommodate the new sequence.
	 * The size of this genome after the insert will be this.size()+exclusiveEnd-inclusiveStart.
	 * @param index - Index to insert at in this genome
	 * @param g - Source of sequence to insert
	 * @param inclusiveStart - Start index of sequence to insert on genome g (inclusive)
	 * @param exclusiveEnd - End index of sequence to insert on genome g (exclusive)
	 */
	void insert(int index, G g, int inclusiveStart, int exclusiveEnd);

	/**
	 * Paste a sequence onto the genome.
	 * Equivalent to {@link #paste(int, G, int, int)} applied to the whole target sequence from 0 to g.size().
	 */
	@Override
	default void paste(int index, G g) {
		this.paste(index, g, 0, g.size());
	}

	/**
	 * Paste a sequence onto the genome.
	 * This will not shift bits on this genome but overwrite them, and possibly extend the genome size if necessary.
	 * @param index - Index to start at in this genome
	 * @param g - Source of sequence to paste
	 * @param inclusiveStart - Start index of sequence to insert on genome g (inclusive)
	 * @param exclusiveEnd - End index of sequence to insert on genome g (exclusive)
	 */
	@Override
	void paste(int index, G g, int inclusiveStart, int exclusiveEnd);

	/**
	 * Replace a subsequence of this genome with another sequence, not necessarily of the same length.
	 * Equivalent to {@link #replaceUnsafe(int, int, G, int, int)} applied to the whole target sequence from 0 to g.size()
	 */
	default void replace(int thisInclusiveStart, int thisExclusiveEnd, G g) {
		this.replace(thisInclusiveStart, thisExclusiveEnd, g, 0, g.size());
	}

	/**
	 * Replace a subsequence of this genome with another sequence, not necessarily of the same length.
	 */
	@SuppressWarnings("unchecked")
	default void replace(int thisInclusiveStart, int thisExclusiveEnd, G g, int gInclusiveStart, int gExclusiveEnd) {
        Assert.notNull(g);
        Assert.splice((G) this, thisInclusiveStart, thisExclusiveEnd);
        Assert.splice(g, gInclusiveStart, gExclusiveEnd);

        int thislen = thisExclusiveEnd - thisInclusiveStart;
        int glen = gExclusiveEnd - gInclusiveStart;

        if (glen == thislen) {
            this.paste(thisInclusiveStart, g, gInclusiveStart, gExclusiveEnd);

        } else if (glen < thislen) {
            this.paste(thisInclusiveStart, g, gInclusiveStart, gExclusiveEnd);
            this.delete(thisInclusiveStart+glen, thisExclusiveEnd);

        } else if (glen > thislen) {
            // Can't read from g after writing to this, because they might be the same
            // So we overwrite whatever we can and copy what's left if necessary.
            G overspill = this.refersTo() == g.refersTo()
                    ? g.copy(gInclusiveStart+thislen, gExclusiveEnd)
                    : g.view(gInclusiveStart+thislen, gExclusiveEnd);
            this.paste(thisInclusiveStart, g, gInclusiveStart, gInclusiveStart+thislen);
            this.insert(thisExclusiveEnd, overspill);
        }
	}

	/**
	 * Catenate a sequence onto this genome.
	 * Equivalent to {@link #append(G, int, int)} applied to the whole target sequence from 0 to g.size().
	 */
	default void append(G g) {
		this.append(g, 0, g.size());
	}

	/**
	 * Append all the given genomes in order to this.
	 * @see #cat(List)
	 */
	default void append(Iterable<G> gs) {
		for (G g : gs) this.append(g);
	}

	/**
	 * Catenate a sequence onto this genome.
	 * Equivalent to {@link #paste(int, G, int, int)} at the index to this.size().
	 * @param g - Source of the sequence to past
	 * @param inclusiveStart - Start index of the sequence to insert
	 * @param exclusiveEnd - End index of the sequence to insert
	 */
	default void append(G g, int inclusiveStart, int exclusiveEnd) {
		this.paste(this.size(), g, inclusiveStart, exclusiveEnd);
	}

}