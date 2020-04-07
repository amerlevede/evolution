package util;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Disjoint sets data structure, supporting efficient union oprtators to partition an integer array.
 *
 * @author adriaan
 *
 */
public class IntPartition {

	public static IntPartition disconnected(int size) {
		int[] pointers = new int[size];
		for (int i=0; i<size; i++) pointers[i] = i;
		int[] depths = new int[size];
		return new IntPartition(pointers, depths);
	}

	private IntPartition(int[] pointers, int[] depths) {
		this.pointers = pointers;
	}

	private int[] pointers;

	public void join(int i, int j) {
		if (i<0 || i>= pointers.length || j<0 || j>=pointers.length) throw new IndexOutOfBoundsException();
		int ihead = this.head(i);
		int jhead = this.head(j);

		pointers[ihead] = jhead;
	}

	public int head(int i) {
		if (pointers[i] == i) return i;

		int parent = pointers[i];
		int result = this.head(parent);
		pointers[i] = result;
		return result;
	}

	public IntStream heads() {
		return IntStream.range(0,pointers.length).filter(i->pointers[i]==i);
	}

	@Override
	public String toString() {
		return Arrays.toString(IntStream.range(0,pointers.length).map(this::head).toArray());
	}

}
