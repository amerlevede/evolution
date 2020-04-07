package alignment.algorithms;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import alignment.VarOAlignment;
import alignment.VarOAlignmentRule;
import genome.LinearGenome;
import genome.binary.BitGenome;
import util.Assert;
import util.IntPair;

/**
 * Variation on glocal-alignment-through-repeated-local ({@link UnorderedSynapsing}).
 * It works by doing local alignment, which splits the genomes in two and then continues finding the best local alignment among all combinations of genome pieces.
 * It differs from {@link UnorderedSynapsing} in that a new local alignment can overlap a previous one. In that case, the overlap will be resolved to optimise the score.
 */
public final class GreedyGlocal {
	
	private GreedyGlocal() {
		Assert.utilityClass();
	}
	
	public static <G extends LinearGenome<G>> int[][] localAlignmentMatrix(int matchScore, int mismatchScore, Random rng, G a, G b) {
		// Initialize
		int[][] score = new int[a.size()+1][b.size()+1];
		
		for (int iA=1; iA<=a.size(); iA++) for (int iB=1; iB<=b.size(); iB++) {
			int newScore = score[iA-1][iB-1];
			newScore += a.sameAt(iA-1, b, iB-1) ? matchScore : mismatchScore;
			score[iA][iB] = newScore > 0 ? newScore : 0;
		}
		
		return score;
	}
	
	public static IntPair findmax(Random rng, int[][] mat) {
		int maxiA = 0;
		int maxiB = 0;
		double exaequo = 0;
		for (int iA=0; iA<mat.length; iA++) {
			for (int iB=0; iB<mat[0].length; iB++) {
				if (mat[iA][iB] > mat[maxiA][maxiB]) {
					maxiA = iA;
					maxiB = iB;
					exaequo = 1;
				} else if (mat[iA][iB] == mat[maxiA][maxiB]) {
					if (rng.nextDouble() < 1/(++exaequo)) {
						maxiA = iA;
						maxiB = iB;
					}
				}
			}
		}
		return IntPair.of(maxiA, maxiB);
	}
	
	public static IntPair traceback(int[][] score, IntPair max) {
		int iA = max.x, iB = max.y;
		while (score[iA][iB] > 0) { iA--; iB--; }
		return IntPair.of(iA+1, iB+1);
	}
	
	public static <G extends LinearGenome<G>> void updateMatrix(int matchScore, int mismatchScore, int[][] score, int diag, IntPair segmentStart, IntPair segmentEnd, G a, G b) {
		// Find first indices on this diagonal ignoring initial zero row (diag == iA - iB)
		int iA = diag >= 0 ? diag+1 : 1;
		int iB = diag >= 0 ? 1 : -diag+1;
		
		// Skip first part before there has been any overlap (or skip execution altogether if this diagonal has no overlap with segment)
		if (iA > segmentEnd.x || iB > segmentEnd.y) return;
		int untilFirstOverlap = Math.min(segmentStart.x-iA, segmentStart.y-iB);
		if (untilFirstOverlap > 0) {
			iA += untilFirstOverlap;
			iB += untilFirstOverlap;
		}
		
		// There can be at most two contiguous parts of the diagonal that overlap with the segment: one on A (having same column in the matrix) and one on B (having same row in matrix).
		// This means in total each pair of bits in the "segment" alignment will be encountered twice while looping on the diagonal, once for each bit.
		// Because the alignment is contiguous and traversed in increasing index only, it suffices to remember the starting point of each of the two overlaps in order to avoid penalizing overlap with the same pair twice.  
		int overlapOnAStartOnB = Integer.MAX_VALUE;
		int overlapOnBStartOnA = Integer.MAX_VALUE;
		int overlapAccum = 0;
		
		for (; iA <= a.size() && iB <= b.size(); iA++, iB++) {
			// Reassign local score based on new previous value

			// Extra penalty for overlap
			boolean overlappingOnA = iA >= segmentStart.x && iA <= segmentEnd.x;
			boolean overlappingOnB = iB >= segmentStart.y && iB <= segmentEnd.y;
			
			if (overlappingOnA) {
				int overlapIndexOnB = segmentStart.y + (iA - segmentStart.x);
				if (iA < overlapOnBStartOnA || (overlappingOnB && overlapIndexOnB > iB)) {
					int overlapCost = score[iA][overlapIndexOnB] - score[iA-1][overlapIndexOnB-1];
					if (overlapCost > 0) overlapAccum += overlapCost;
				}
				if (overlapOnAStartOnB == Integer.MAX_VALUE) {
					overlapOnAStartOnB = overlapIndexOnB;
				}
			}
			
			if (overlappingOnB) {
				int overlapIndexOnA = segmentStart.x + (iB - segmentStart.y);
				if (iB < overlapOnAStartOnB || (overlappingOnA && overlapIndexOnA > iA)) {
					int overlapCost = score[overlapIndexOnA][iB] - score[overlapIndexOnA-1][iB-1];
					if (overlapCost > 0) overlapAccum += overlapCost;
				}
				if (overlapOnBStartOnA == Integer.MAX_VALUE) {
					overlapOnBStartOnA = overlapIndexOnA;
				}
			}
			
			// Update and continue, or reset if score is smaller than 0
			int newCost = score[iA][iB] - overlapAccum;
			if (newCost <= 0) {
				newCost = 0;
				overlapAccum = score[iA][iB];
				overlapOnAStartOnB = Integer.MAX_VALUE;
				overlapOnBStartOnA = Integer.MAX_VALUE;
			}
			score[iA][iB] = newCost;
		}
	};
	
	public static SortedSet<IntPair> boundsToPairs(IntPair segmentStart, IntPair segmentEnd) {
		SortedSet<IntPair> result = new TreeSet<>();
		
		for (int iA = segmentStart.x, iB = segmentStart.y; iA <= segmentEnd.x; iA++, iB++) {
			result.add(IntPair.of(iA-1, iB-1));
		}
		
		return result;
	}
	
	// TODO: probably don't need to calculate whole score, can just see if it exceeds minimum
	public static int segmentScore(int matchScore, int mismatchScore, SortedSet<IntPair> segment, BitGenome a, BitGenome b) {
		int result = 0;
		for (IntPair pair : segment) {
			result += a.get(pair.x)== b.get(pair.y) ? matchScore : mismatchScore;
		}
		return result;
	}
	
	public static <G extends LinearGenome<G>> VarOAlignment<G> align(int matchScore, int mismatchScore, int minScore, Random rng, G a, G b) {
		int[][] score = localAlignmentMatrix(matchScore, mismatchScore, rng, a, b);
		
		List<SortedSet<IntPair>> segments = new LinkedList<>();

		// Find optimal local alignment
		IntPair segmentEnd = findmax(rng, score); // INCLUSIVE
		int segmentScore = score[segmentEnd.x][segmentEnd.y];
		
		while (segmentScore >= minScore) {
			IntPair segmentStart = traceback(score, segmentEnd);
			
			// Add segment
			removeOverlap(matchScore, mismatchScore, minScore, a, b, segments, segmentEnd, segmentStart);
			segments.add(boundsToPairs(segmentStart, segmentEnd));
			
			// Adjust matrix to punish overlap
			int thisdiag = segmentStart.x - segmentStart.y;
			for (int diag = -b.size()+1; diag < a.size(); diag++) {
				if (diag != thisdiag) updateMatrix(matchScore, mismatchScore, score, diag, segmentStart, segmentEnd, a, b);
			}
			updateMatrix(matchScore, mismatchScore, score, thisdiag, segmentStart, segmentEnd, a, b);
			
			// Continue with next optimal local alignment
			segmentEnd = findmax(rng, score);
			segmentScore = score[segmentEnd.x][segmentEnd.y];
		}
		
		return new VarOAlignment<>(segments, a, b); // simplify?
	}

	private static <G extends LinearGenome<G>> void removeOverlap(int matchScore, int mismatchScore, int minScore, G a, G b, List<SortedSet<IntPair>> segments, IntPair segmentEnd, IntPair segmentStart) {
//		// Simple code does not split segments when overlap happens in the middle of an existing segment
//		segments.forEach(segment ->
//			segment.removeIf(pair ->
//				(pair.x+1 >= segmentStart.x && pair.x+1 <= segmentEnd.x)
//			 || (pair.y+1 >= segmentStart.y && pair.y+1 <= segmentEnd.y)
//		));
		
//		// Implementation with just one loop. But ran into concurrent modification exception
//		List<SortedSet<IntPair>> segmentsToAdd = new LinkedList<>(); 
//		for (SortedSet<IntPair> segment : segments) {
//			// Segment must be split when finding a pair that does not overlap, but previously have already found both overlapping and non-overlapping pairs.
//			// If that is the case, "target" will be set to a new segment which will be added to the total list
//			boolean hadOverlap = false;
//			boolean hadNoOverlap = false;
//			Optional<SortedSet<IntPair>> target = Optional.empty();
//			
//			for (IntPair pair : segment) {
//				boolean overlapping = 
//					(pair.x+1 >= segmentStart.x && pair.x+1 <= segmentEnd.x)
//				 || (pair.y+1 >= segmentStart.y && pair.y+1 <= segmentEnd.y);
//				
//				if (overlapping) {
//					hadOverlap = true;
//					segment.remove(pair);
//					target = Optional.empty();
//				} else if (hadOverlap && hadNoOverlap) {
//					if (target.isEmpty()) {
//						SortedSet<IntPair> targetSet = new TreeSet<>();
//						target = Optional.of(targetSet);
//						segmentsToAdd.add(targetSet);
//					}
//					segment.remove(pair);
//					target.get().add(pair);
//				} else {
//					hadNoOverlap = true;
//				}
//			}
//		}
//		
//		segments.addAll(segmentsToAdd);
		
		List<SortedSet<IntPair>> segmentsToRemove = new LinkedList<>();
		List<SortedSet<IntPair>> segmentsToAdd = new LinkedList<>();
		
		for (SortedSet<IntPair> segment : segments) {
			IntPair thisSegmentEnd = segmentEnd;
			
			boolean anyOverlap = segment.stream().anyMatch(pair -> 
				(pair.x+1 >= segmentStart.x && pair.x+1 <= thisSegmentEnd.x) 
			 || (pair.y+1 >= segmentStart.y && pair.y+1 <= thisSegmentEnd.y));
			
			if (anyOverlap) {
				SortedSet<IntPair> lftlft = new TreeSet<>();
				SortedSet<IntPair> lftrgt = new TreeSet<>();
				SortedSet<IntPair> rgtlft = new TreeSet<>();
				SortedSet<IntPair> rgtrgt = new TreeSet<>();
				
				for (IntPair pair : segment) {
					if (pair.x+1 < segmentStart.x) {
						if (pair.y+1 < segmentStart.y) {
							lftlft.add(pair);
						} else if (pair.y+1 > segmentEnd.y) {
							lftrgt.add(pair);
						}
					} else if (pair.x+1 > segmentEnd.x) {
						if (pair.y+1 < segmentStart.y) {
							rgtlft.add(pair);
						} else if (pair.y+1 > segmentEnd.y) {
							rgtrgt.add(pair);
						}
					}
				}
				
				segmentsToRemove.add(segment);
				if (!lftlft.isEmpty()) segmentsToAdd.add(lftlft);
				if (!lftrgt.isEmpty()) segmentsToAdd.add(lftrgt);
				if (!rgtlft.isEmpty()) segmentsToAdd.add(rgtlft);
				if (!rgtrgt.isEmpty()) segmentsToAdd.add(rgtrgt);
//				if (segmentScore(matchScore, mismatchScore, lftlft, a, b) >= minScore) segmentsToAdd.add(lftlft);
//				if (segmentScore(matchScore, mismatchScore, lftrgt, a, b) >= minScore) segmentsToAdd.add(lftrgt);
//				if (segmentScore(matchScore, mismatchScore, rgtlft, a, b) >= minScore) segmentsToAdd.add(rgtlft);
//				if (segmentScore(matchScore, mismatchScore, rgtrgt, a, b) >= minScore) segmentsToAdd.add(rgtrgt);
			}
		}
		segments.removeAll(segmentsToRemove);
		segments.addAll(segmentsToAdd);
	}
	
	
	public static <G extends LinearGenome<G>> VarOAlignmentRule<G> alignment(int matchScore, int mismatchScore, int minScore) {
		return (rng) -> (a, b) -> align(matchScore, mismatchScore, minScore, rng, a, b);
	}

}
