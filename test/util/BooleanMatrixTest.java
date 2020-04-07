package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import genome.RandomInit;

class BooleanMatrixTest extends RandomInit {

	public BooleanMatrix randomLarge;
	public BooleanMatrix randomLargeRef;
	public BooleanMatrix randomSmall;
	public BooleanMatrix randomSmallRef;
	
	@BeforeEach
	public void setupBooleanMatrix() {
		randomLarge = BooleanMatrix.getRandom(rng, 25, 30);
		randomLargeRef = BooleanMatrix.copy(randomLarge);
		randomSmall = BooleanMatrix.getRandom(rng, 5, 5);
		randomSmallRef = BooleanMatrix.copy(randomSmall);
	}

	@Test
	void testRowReduce_identity() {
		int dim = 25;
		BooleanMatrix eye = BooleanMatrix.identity(dim);
		BooleanMatrix eyeRef = BooleanMatrix.identity(dim);
		
		int rank = eye.rowReduceAndGetRank();
		
		assertThat(BooleanMatrix::same, eye, eyeRef);
		assertEquals(dim, rank);
	}
	
	@RepeatedTest(1000)
	void testRowReduce_randomLarge() {
		int rank = randomLarge.rowReduceAndGetRank();
		
		for (int row=0; row<rank; row++) {
			int j = randomLarge.leadingColumn(row);
			for (int i=0; i<randomLarge.height; i++) {
				if (i != row) assertFalse(randomLarge.matrix[i][j]);
			}
		}
	}
	
	@RepeatedTest(1000)
	void testRowReduce_randomSmall() {
		int rank = randomSmall.rowReduceAndGetRank();
		
		for (int row=0; row<rank; row++) {
			int j = randomSmall.leadingColumn(row);
			for (int i=0; i<randomSmall.height; i++) {
				if (i != row) assertFalse(randomSmall.matrix[i][j]);
			}
		}
	}
	
	@RepeatedTest(1000)
	void testNullSpace_size_randomLarge() {
		int rank = randomLarge.rowReduceAndGetRank();
		int nullity = randomLarge.width - rank;
		List<boolean[]> nullSpace = randomLargeRef.nullSpace();
		
		assertEquals(nullity, nullSpace.size());
	}
	
	@RepeatedTest(1000)
	void testNullSpace_isReallyNull_randomLarge() {
		List<boolean[]> nullSpace = randomLarge.nullSpace();
		
		for (boolean[] basisVector : nullSpace) {
			for (int row=0; row<randomLargeRef.height; row++) {
				boolean rowResult = false;
				for (int col=0; col<randomLargeRef.width; col++) {
					rowResult ^= randomLargeRef.matrix[row][col] && basisVector[col]; 
				}
				assertFalse(rowResult);
			}
		}
	}
	
	@RepeatedTest(1000)
	void testNullSpace_size_randomSmall() {
		int rank = randomSmall.rowReduceAndGetRank();
		int nullity = randomSmall.width - rank;
		List<boolean[]> nullSpace = randomSmallRef.nullSpace();
		
		assertEquals(nullity, nullSpace.size());
	}
	
	@RepeatedTest(1000)
	void testNullSpace_isReallyNull_randomSmall() {
		List<boolean[]> nullSpace = randomSmall.nullSpace();
		
		for (boolean[] basisVector : nullSpace) {
			for (int row=0; row<randomSmallRef.height; row++) {
				boolean rowResult = false;
				for (int col=0; col<randomSmallRef.width; col++) {
					rowResult ^= randomSmallRef.matrix[row][col] && basisVector[col]; 
				}
				assertFalse(rowResult);
			}
		}
	}
	
	

}
