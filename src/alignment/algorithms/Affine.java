package alignment.algorithms;

import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import alignment.Alignment;
import genome.LinearGenome;
import util.DiscreteDistribution;
import util.IntPair;

/**
 * Exact alignment algorithm for affine alignment scores, i.e. Needleman-Wunsch and Smith-Waterman implementations.
 * @author adriaan
 *
 * @param <G> - The genome type
 */
public abstract class Affine<G extends LinearGenome<G>> {
	
    public static final int MATCH = 0;
    public static final int INS_A = 1;
    public static final int INS_B = 2;
    public static final int END = -1;
    public static final int IMPOSSIBLE = -2;
	
	static final int stop = -100000000; // Don't know why Integer.MIN_VALUE doesn't work
	
	public final int matchScore;
	public final int mismatchScore;
	public final int gapOpenScore;
	public final int gapExtendScore;
	public final G a;
	public final G b;
	public final Random rng;
	
	public Affine(int matchScore ,int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
		this.matchScore = matchScore;
		this.mismatchScore = mismatchScore;
		this.gapOpenScore = gapOpenScore;
		this.gapExtendScore = gapExtendScore;
		this.a = a;
		this.b = b;
		this.rng = rng;
	}
	
	protected int[][][] scoreMatrix;
	
	/**
	 * Simple flyweight decreases computation time by 1/3 by removing the need to initialize large matrix.
	 * Only works if no two algorithms need the matrix at the same time. But this would require too much memory anyway.
	 */
	private static Optional<int[][][]> globalMatrix = Optional.empty();
	
	protected void grabMatrix() {
		if (globalMatrix.isEmpty() || globalMatrix.get().length < a.size()+1 || globalMatrix.get()[0].length < b.size()+1) {
			int adim = Math.max(a.size()+1, 2000);
			int bdim = Math.max(b.size()+1, 2000);
			globalMatrix = Optional.of(new int[adim][bdim][3]);
		}
		scoreMatrix = globalMatrix.get();
	}
	
	protected void newMatrix() {
		int[][][] newMatrix = new int[a.size()+1][b.size()+1][3];
		this.scoreMatrix = newMatrix;
	}
	
	protected void copyMatrix() {
		int[][][] newMatrix = new int[a.size()+1][b.size()+1][3];
		for (int im : List.of(MATCH,INS_A,INS_B)) {
			for (int ia=0; ia<=a.size(); ia++) {
				for (int ib=0; ib<=b.size(); ib++) {
					newMatrix[ia][ib][im] = scoreMatrix[ia][ib][im];
				}
			}
		}
		this.scoreMatrix = newMatrix;
	}
	
	public static class NeedlemanWunsch<G extends LinearGenome<G>> extends Affine<G> {
		
		private NeedlemanWunsch(int matchScore ,int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
			super(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
		}
		
	    public static <G extends LinearGenome<G>> Alignment<G> align(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
	    	NeedlemanWunsch<G> algorithm = new NeedlemanWunsch<>(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
	    	
	    	algorithm.grabMatrix();
	    	algorithm.initializeScoreMatrix();
	        algorithm.fillScoreMatrix();
	        algorithm.finalizeScoreMatrix();
	        
			int endia = a.size();
			int endib = b.size();
			int endim = DiscreteDistribution.getBestIndexOf(rng, algorithm.scoreMatrix[endia][endib]);
			int endscore = max3(algorithm.scoreMatrix[a.size()][b.size()]);
			
			SortedSet<IntPair> alignedPairs = algorithm.traceback(endia, endib, endim, stop);
			
	        return new Alignment<>(endscore, alignedPairs, a, b);
	    }

		public void fillScoreMatrix() {
			for (int ia=1; ia<=a.size(); ia++) {
	            for (int ib=1; ib<=b.size(); ib++) {
	            	this.step(ia, ib);
	            }
	        }
		}
	    
	    /**
	     * Sets the end of the score matrix to stop value.
	     * This allows using the same stop condition as for local alignment in traceback.
	     */
	    public void finalizeScoreMatrix() {
	    	scoreMatrix[0][0][MATCH] = stop;
	    	scoreMatrix[0][0][INS_A] = stop;
	    	scoreMatrix[0][0][INS_B] = stop;
	    }
	    
	    /**
		 * Initial score matrix for NW or SW algorithm with affine gap Score.
		 * Unlike the algorithms with constant gap Score, for affine gap Score the alignment has an "internal state" depending on which kind of gap if any is currently open.
		 * This leads to the need for an extra dimension in the score matrix (one value in the last dimension for each possible state).
		 */
		public void initializeScoreMatrix() {
			
			// Matrix corner -- alignment complete
			scoreMatrix[0][0][INS_A] = 0;
		    scoreMatrix[0][0][INS_B] = 0;
		    scoreMatrix[0][0][MATCH] = 0;
		    
		    // Boundary (b is in initial state, alignment opens with gap in a)
		    scoreMatrix[1][0][INS_A] = gapOpenScore;
		    scoreMatrix[1][0][INS_B] = stop;
		    scoreMatrix[1][0][MATCH] = stop;
		    for (int ia=2; ia<=a.size(); ia++) {
		        scoreMatrix[ia][0][INS_A] = scoreMatrix[ia-1][0][INS_A] + gapExtendScore;
		        scoreMatrix[ia][0][INS_B] = stop;
		        scoreMatrix[ia][0][MATCH] = stop;
		    }
		
		    // Boundary (a is in initial state, alignment opens with gap in b)
		    scoreMatrix[0][1][INS_A] = stop;
		    scoreMatrix[0][1][INS_B] = gapOpenScore;
		    scoreMatrix[0][1][MATCH] = stop;
		    for (int ib=2; ib<=b.size(); ib++) {
		        scoreMatrix[0][ib][INS_A] = stop;
		        scoreMatrix[0][ib][INS_B] = scoreMatrix[0][ib-1][INS_B] + gapExtendScore;
		        scoreMatrix[0][ib][MATCH] = stop;
		    }
		    
		    // Rest of the matrix to be filled
		}

	    /**
	     * Fill one cell in the matrix for Needleman-Wunsch with affine gap Score.
	     * This is the core of the algorithm; it finds and records the best way to align subsequences (0--ia) and (0--ib) of the two genomes, given the best alignments of the subsequences up until then.
	     * Because of the affine gap Score the algorithm needs to take into account the three separate cases of there having been an A-gap, B-gap or match in this step as well as the last step.
	     */
		public void step(int ia, int ib) {
			int matchCost = a.sameAt(ia-1, b, ib-1) ? matchScore : mismatchScore;
			
			scoreMatrix[ia][ib][MATCH] = max3(
				scoreMatrix[ia-1][ib-1][INS_A] + matchCost,
				scoreMatrix[ia-1][ib-1][INS_B] + matchCost,
				scoreMatrix[ia-1][ib-1][MATCH] + matchCost
				);
			
			scoreMatrix[ia][ib][INS_A] = max3(
				scoreMatrix[ia-1][ib][INS_A] + gapExtendScore,
				scoreMatrix[ia-1][ib][INS_B] + gapOpenScore,
				scoreMatrix[ia-1][ib][MATCH] + gapOpenScore
				);
			
			scoreMatrix[ia][ib][INS_B] = max3(
				scoreMatrix[ia][ib-1][INS_A] + gapOpenScore,
				scoreMatrix[ia][ib-1][INS_B] + gapExtendScore,
				scoreMatrix[ia][ib-1][MATCH] + gapOpenScore
				);
		}
	}
	
	public static class SmithWaterman<G extends LinearGenome<G>> extends Affine<G> {	
		
		public SmithWaterman(int matchScore ,int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
			super(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
		}
		
		/**
		 * Perform Smith-Waterman local alignment with affine gap Score.
		 * Same as {@link performAffineNW}, except alignment scores are capped at 0 and traceback starts from the smallest (most optimal) cell.
		 */
		public static <G extends LinearGenome<G>> Alignment<G> align(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b) {
			SmithWaterman<G> algorithm = new SmithWaterman<>(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, a, b);
			
			algorithm.grabMatrix();
			algorithm.initializeScoreMatrix();
			algorithm.fillScoreMatrix();
		    
		    int[] best = algorithm.maxValue();
		    int endia = best[0];
		    int endib = best[1];
		    int endim = best[2];
		    int endscore = best[3];
		    
		    SortedSet<IntPair> alignedPairs = algorithm.traceback(endia, endib, endim, 0);
		    
		    // Done
		    return new Alignment<>(endscore, alignedPairs, a, b);
		}

		public void fillScoreMatrix() {
			for (int ia=1; ia<=a.size(); ia++) {
		        for (int ib=1; ib<=b.size(); ib++) {
		        	this.step(ia, ib);
		        }
		    }
		}
		
		public void initializeScoreMatrix() {
			// Explicitly setting these to 0 might be necessary due to flyweight matrix
			for (int ia=0; ia<=a.size(); ia++) {
				scoreMatrix[ia][0][MATCH] = 0;
				scoreMatrix[ia][0][INS_A] = 0;
				scoreMatrix[ia][0][INS_B] = 0;
			}
			for (int ib=0; ib<=b.size(); ib++) {
				scoreMatrix[0][ib][MATCH] = 0;
				scoreMatrix[0][ib][INS_A] = 0;
				scoreMatrix[0][ib][INS_B] = 0;
			}
		}
		
		public void step(int ia, int ib) {
			int matchCost = a.sameAt(ia-1, b, ib-1) ? matchScore : mismatchScore;
			
			scoreMatrix[ia][ib][MATCH] = max4(0,
				scoreMatrix[ia-1][ib-1][INS_A] + matchCost,
				scoreMatrix[ia-1][ib-1][INS_B] + matchCost,
				scoreMatrix[ia-1][ib-1][MATCH] + matchCost
				);
			
			scoreMatrix[ia][ib][INS_A] = max4(0,
				scoreMatrix[ia-1][ib][INS_A] + gapExtendScore,
				scoreMatrix[ia-1][ib][INS_B] + gapOpenScore,
				scoreMatrix[ia-1][ib][MATCH] + gapOpenScore
				);
			
			scoreMatrix[ia][ib][INS_B] = max4(0,
				scoreMatrix[ia][ib-1][INS_A] + gapOpenScore,
				scoreMatrix[ia][ib-1][INS_B] + gapExtendScore,
				scoreMatrix[ia][ib-1][MATCH] + gapOpenScore
				);
		}
		
		public int[] maxValue() {
			int bestia = 0;
			int bestib = 0;
			int bestim = MATCH;
			int bestscore = -1;
			double exaequo = 0;
			
			for (int im : List.of(MATCH, INS_A, INS_B)) {
				for (int ia=0; ia<=a.size(); ia++) {
					for (int ib=0; ib<=b.size(); ib++) {
						int score = scoreMatrix[ia][ib][im];
						if (score > bestscore) {
							bestia = ia;
							bestib = ib;
							bestim = im;
							bestscore = score;
							exaequo = 1;
						} else if (score == bestscore) {
							if (rng.nextDouble() < 1/(++exaequo)) {
								bestia = ia;
								bestib = ib;
								bestim = im;
							}
						}
					}
				}
			}
			
			return new int[] {bestia,bestib,bestim,bestscore};
		}
	}
	
	/**
	 * Trace back, returning alignment based on score matrix and starting at the given position.
	 * @note This traceback method does not result in a uniform sampling of the possible alignments with optimal score.
	 * @param ia - Final index to align on genome a (first index of score matrix). Should be last index of a for global alignment.
	 * @param ib - Final index to align on genome b (second index of score matrix). Should be last index of b for global alignment.
	 * @param im - Match/gap state of the final part of the alignment (third index of score matrix)
	 * @param stop - Minimal score value, at which traceback should stop. Use {@link #stop} for global alignment, 0 for local alignment. 
	 */
	public SortedSet<IntPair> traceback(int ia, int ib, int im, int stop) {
		// Set of matches to build up and return
		NavigableSet<IntPair> matches = new TreeSet<>();
		
		// Trace back until all paths are worse than "stop" level (only true in corner of matrix for global)
		while (scoreMatrix[ia][ib][im] > stop) {
			// Add pair to output if processing a MATCH
			if (im == MATCH) matches.add(IntPair.of(ia-1, ib-1));
			// Match status informs of last position in alignment matrix
			if (im == MATCH || im == INS_A) ia--;
			if (im == MATCH || im == INS_B) ib--;
			// Given this position, which of the three states (MATCH, INS_A, INS_B)?
			// Reverse the max3 operation in affineNeedlemanWunschStep
			// Since the actual values don't matter, only which one is highest, we can make a shortcut
			int newim;
			if (im != MATCH) {
				scoreMatrix[ia][ib][im] += gapExtendScore - gapOpenScore;
				newim = DiscreteDistribution.getBestIndexOf(rng, scoreMatrix[ia][ib]);
				scoreMatrix[ia][ib][im] -= gapExtendScore - gapOpenScore;
			} else {
				newim = DiscreteDistribution.getBestIndexOf(rng, scoreMatrix[ia][ib]);
			}
			im = newim;
		}
		
		return matches;
	}
	
	public static int max3(int[] xs) {
		return max3(xs[0], xs[1], xs[2]);
	}
	
	public static int max3(int a, int b, int c) {
		return Math.max(Math.max(a, b), c);
	}
	
	public static int max4(int a, int b, int c, int d) {
		return Math.max(Math.max(a, b), Math.max(c, d));
	}
	
}