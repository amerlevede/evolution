package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility functions for boolean[][] matrices, which represent matrices over the boolean field F2.
 * 
 * @author adriaan
 *
 */
public class BooleanMatrix {
	
	public final boolean[][] matrix;
	public final int width;
	public final int height;
	
	private BooleanMatrix(boolean[][] matrix) {
		if (Stream.of(matrix).mapToInt(a->a.length).distinct().count() != 1) throw new IllegalArgumentException();
		this.matrix = matrix;
		this.width = matrix[0].length;
		this.height = matrix.length;
	}
	
	public static BooleanMatrix empty(int x, int y) {
		return BooleanMatrix.wrap(new boolean[x][y]);
	}
	
	/**
	 * Wrap a matrix in this class to add some arithmetic functionality.
	 * All changes are made in the underlying matrix.
	 * @param width - the width of the matrix; any extra columns are treated as part of an "augmented matrix"
	 */
	public static BooleanMatrix wrap(boolean[][] matrix) {
		return new BooleanMatrix(matrix);
	}
	
	/**
	 * Copy the given matrix and return a BooleanMatrix wrapped around it.
	 * No changes are made in the underlying matrix.
	 * @param width - the width of the matrix; any extra columns are treated as part of an "augmented matrix"
	 */
	public static BooleanMatrix copy(boolean[][] matrix) {
		return BooleanMatrix.wrap( 
				Stream.of(matrix).map(row -> Arrays.copyOf(row, row.length)).toArray(boolean[][]::new)
				);
	}
	
	public static BooleanMatrix copy(BooleanMatrix matrix) {
		return BooleanMatrix.copy(matrix.matrix);
	}
	
	
	public static BooleanMatrix copy(Iterable<boolean[]> matrix) {
		return BooleanMatrix.wrap(
				Functional.streamOfIterable(matrix).map(row -> Arrays.copyOf(row, row.length)).toArray(boolean[][]::new)
				);
	}
	
	public static BooleanMatrix identity(int dim) {
		boolean[][] result = new boolean[dim][dim];
		for (int i=0; i<dim; i++) result[i][i] = true;
		return BooleanMatrix.wrap(result);
	}
	
	public static BooleanMatrix getRandom(Random rng, int h, int w) {
		boolean[][] result = new boolean[h][w];
		for (int i=0; i<h; i++) for (int j=0; j<w; j++) {
			if (rng.nextBoolean()) result[i][j] = true; 
		}
		return BooleanMatrix.wrap(result);
	}
	
	/**
	 * Euler elimination algorithm to bring the matrix in reduced row echelon form.
	 */
	public int rowReduceAndGetRank() {
		int row = 0;
		int pivot = 0;
		while (row < matrix.length && pivot < width) {
			// Get 1 in pivot column
			if (!matrix[row][pivot]) {
				for (int swaprow=row+1; swaprow<matrix.length; swaprow++) {
					if (matrix[swaprow][pivot]) {
						for (int i=pivot; i<matrix[row].length; i++) {
							matrix[row][i] ^= matrix[swaprow][i];
						}
						break;
					}
				}
				// If all are zero, move pivot
				if (!matrix[row][pivot]) {
					pivot++;
					continue;
				}
			}
			// Remove 1 from other rows in pivot column
			for (int reducerow=0; reducerow<matrix.length; reducerow++) {
				if (reducerow == row) continue;
				if (matrix[reducerow][pivot]) {
					for (int i=pivot; i<matrix[row].length; i++) {
						matrix[reducerow][i] ^= matrix[row][i];
					}
				}
			}
			row++;
			pivot++;
		}
		return row;
	}
	
	public void rowReduce() {
		this.rowReduceAndGetRank();
	}
	
	/**
	 * Produce a basis for the null space of this matrix.
	 * A vector is a solution for the homogeneous linear equation system represented by the matrix iff it is a linear combination of the basis in the null space.
	 * @note Matrix is row-reduced in this process
	 */
	public List<boolean[]> nullSpace() {
		int rank = this.rowReduceAndGetRank();
		
		Set<Integer> freeColumns = IntStream.range(0, this.width).boxed().collect(Collectors.toCollection(HashSet::new));
		for (int row=0; row<rank; row++) {
			freeColumns.remove(leadingColumn(row));
		}
		
		List<boolean[]> result = new ArrayList<>();
		for (int col : freeColumns) {
			boolean[] basisVector = new boolean[width];
			basisVector[col] = true;
			for (int row=0; row<rank; row++) {
				if (matrix[row][col]) basisVector[leadingColumn(row)] = true;
			}
			result.add(basisVector);
		}
		
		return result;
	}
	
	/**
	 * Get a random element from the null space of this matrix.
	 * @note Matrix is row-reduced in this process
	 */
	public boolean[] getRandomFromNullSpace(Random rng) {
		boolean[] result = new boolean[width];
		
		for (boolean[] basisVector : this.nullSpace()) {
			if (rng.nextBoolean()) for (int i=0; i<width; i++) result[i] ^= basisVector[i];
		}
		
		return result;
	}
	
	protected int leadingColumn(int row) {
		for (int j=0; j<width; j++) {
			if (matrix[row][j]) return j;
		}
		throw new IllegalStateException();
	}
	
	public static boolean same(boolean[][] a, boolean[][] b) {
		if (a.length != b.length) return false;
		for (int i=0; i<a.length; i++) {
			if (a[i].length != b[i].length) return false;
			for (int j=0; j<a[i].length; j++) {
				if (a[i][j] != b[i][j]) return false;
			}
		}
		return true;
	}
	
	public static boolean same(BooleanMatrix a, boolean[][] b) {
		return same(a.matrix, b);
	}
	
	public static boolean same(BooleanMatrix a, BooleanMatrix b) {
		return same(a.matrix, b.matrix);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('+');
		result.append("-".repeat(1+2*width));
		result.append('+');
		result.append('\n');
		for (int x=0; x<height; x++) {
			result.append("| ");
			for (int y=0; y<width; y++) {
				result.append(matrix[x][y] ? '1' : '.');
				result.append(" ");
			}
			result.append("|\n");
		}
		result.append('+');
		result.append("-".repeat(1+2*width));
		result.append('+');
		
		return result.toString();
	}

}
