package util;

import genome.LinearGenome;

/**
 *
 * @author adriaan
 */
public final class Assert {
    
    private Assert() {}
    
    /**
     * Assert that an index is within a genome.
     * @param g
     * @param i
     * @throws IndexOutOfBoundsException 
     */
    public static <G extends LinearGenome<G>> void index(G g, int i) throws IndexOutOfBoundsException {
        if (i < 0 || i >= g.size()) throw new IndexOutOfBoundsException(i + " of genome size " + g.size());
    }
    
    /**
     * Assert that a range of indices is within a genome.
     * This requires that both indices are in the genome, and that the first is strictly smaller than the other.
     * @param g
     * @param i
     * @param j
     * @throws IndexOutOfBoundsException 
     */
    public static <G extends LinearGenome<G>> void index(G g, int i, int j) throws IndexOutOfBoundsException {
        Assert.index(g, i);
        Assert.index(g, j);
        if (i > j) throw new IndexOutOfBoundsException("From index was larger than to index.");
    }
    
    /**
     * Assert that a given index indicates a position in between bits (i.e. it is between 0 and genome length inclusive, representing the section right before bit i).
     * @param g
     * @param i
     * @throws IndexOutOfBoundsException 
     */
    public static <G extends LinearGenome<G>> void splice(G g, int i) throws IndexOutOfBoundsException {
        if (i<0 || i > g.size()) throw new IndexOutOfBoundsException();
    }
    
    /**
     * Assert that a given range of indices indicates a subarray.
     * @param o 
     */
    public static <G extends LinearGenome<G>> void splice(G g, int i, int j) throws IndexOutOfBoundsException {
        Assert.index(g, i);
        Assert.splice(g, j);
        if (i >= j) throw new IndexOutOfBoundsException("From index was larger than or equal to to index.");
    }
    
    /**
     * Assert that an index refers to a splice *within* the genome (i.e. it is between 1 and the genome length exclusive, representing the section right before bit i).
     * @param g
     * @param i
     * @throws IndexOutOfBoundsException 
     */
    public static <G extends LinearGenome<G>> void innerSplice(G g, int i) throws IndexOutOfBoundsException {
        if (!g.innerSplice(i)) throw new IndexOutOfBoundsException("Index must be inner splice.");
    }
    
    public static <G extends LinearGenome<G>> void innerSplice(G g, int inclusiveStart, int exclusiveEnd) throws IndexOutOfBoundsException {
        if (!g.innerSplice(inclusiveStart, exclusiveEnd)) throw new IndexOutOfBoundsException("Range was not strictly inside genome.");
    }
    
    public static void notNull(Object o) {
        if (o == null) throw new NullPointerException();
    }
    
    public static void length0(int len) {
        if (len < 0) throw new IllegalArgumentException("Length must be zero or greater.");
    }
    
    public static void length1(int len) {
        if (len < 1) throw new IllegalArgumentException("Length must be positive.");
    }
    
    public static void utilityClass() {
    	throw new UnsupportedOperationException("Tried to create instance of abstract utility class.");
    }
    
    public static void unmodifiable() {
    	throw new UnsupportedOperationException("Attempted to modify unmodifiable object.");
    }
    
    public static void unreachableCode() {
    	throw new AssertionError("This code should be unreachable");
    }
    
}
