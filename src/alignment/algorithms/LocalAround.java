package alignment.algorithms;

import static alignment.algorithms.Affine.INS_A;
import static alignment.algorithms.Affine.INS_B;
import static alignment.algorithms.Affine.MATCH;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import alignment.Alignment;
import alignment.algorithms.Affine.SmithWaterman;
import genome.LinearGenome;
import util.DiscreteDistribution;
import util.IntPair;

/**
 * Local alignment but fixing a particular location on the genomes.
 */
public class LocalAround {
	
	public static <G extends LinearGenome<G>> Alignment<G> align(int matchScore, int mismatchScore, int gapOpenScore, int gapExtendScore, Random rng, G a, G b, boolean targetIndexIsOnGenomeA, int targetIndex) {
		// Split one of the genomes in two at target index
		G left  = (targetIndexIsOnGenomeA ? a : b).view(0, targetIndex+1);
		G right = (targetIndexIsOnGenomeA ? a : b).view(targetIndex, (targetIndexIsOnGenomeA ? a : b).size());
		G other = (targetIndexIsOnGenomeA ? b : a);
		
		// Perfom two local alignments, with the right part in reverse
		// The final row of the score matrices is the best value for a local alignment beginning/ending at (target index, i) for each i on the other genome (and match/insertion status)
    	SmithWaterman<G> leftAlign = new SmithWaterman<G>(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, left, other);
    	if (left.size() >= right.size()) leftAlign.grabMatrix(); else leftAlign.newMatrix();
    	leftAlign.initializeScoreMatrix();
    	leftAlign.fillScoreMatrix();
    	
    	SmithWaterman<G> rightAlign = new SmithWaterman<G>(matchScore, mismatchScore, gapOpenScore, gapExtendScore, rng, right.reversedView(), other.reversedView());
    	if (left.size() >= right.size()) rightAlign.newMatrix(); else rightAlign.grabMatrix();
    	rightAlign.initializeScoreMatrix();
    	rightAlign.fillScoreMatrix();

    	// Find the best combination of beginning and end local alignments
        int besti = 0;
        int bestim = 0;
        int bestleftileft = 0;
        int bestleftiother = 0;
        int bestleftim = 0;
        int bestscore = -1;
        double exaequo = 0;
        for (int im : List.of(MATCH)) { // Should also work when allowing gap here but doesn't
	        for (int iother=1; iother<=other.size(); iother++) {
	        	// Trace back one step in left in order to avoid counting contribution of targetIndex twice
	        	int leftileft = (im == MATCH || im == INS_A) ? left.size()-1 : left.size();
	        	int leftiother = (im == MATCH || im == INS_B) ? iother-1 : iother;
	        	int leftim;
	        	if (im != MATCH) {
	        		leftAlign.scoreMatrix[leftileft][leftiother][im] += gapExtendScore - gapOpenScore;
	        		leftim = DiscreteDistribution.getBestIndexOf(rng, leftAlign.scoreMatrix[leftileft][leftiother]);
	        		leftAlign.scoreMatrix[leftileft][leftiother][im] -= gapExtendScore - gapOpenScore;
	        	} else {
	        		leftim = DiscreteDistribution.getBestIndexOf(rng, leftAlign.scoreMatrix[leftileft][leftiother]);
	        	}
	        	
	        	// Calculate score if targetIndex is aligned with index i on other
	        	int score = leftAlign.scoreMatrix[leftileft][leftiother][leftim]
	        			  + rightAlign.scoreMatrix[right.size()][other.size()-iother+1][im];
	        	
	        	// Update best case if appropriate
	        	if (score > bestscore) {
	        		besti = iother;
	        		bestim = im;
	        		bestleftileft = leftileft;
	        		bestleftiother = leftiother;
	        		bestleftim = leftim;
	        		bestscore = score;
	        		exaequo = 1;
	        	} else if (score == bestscore) {
	        		if (rng.nextDouble() < 1/(++exaequo)) {
	        			besti = iother;
	        			bestim = im;
	        			bestscore = score;
	        		}
	        	}
	        }
        }
        
        // Traceback start and end of optimal alignment (undo reversal in right alignment output)
        SortedSet<IntPair> pairs = leftAlign.traceback(bestleftileft, bestleftiother, bestleftim, 0);
        rightAlign.traceback(right.size(), other.size()-besti+1, bestim, 0)
        	.stream()
        	.map(pair -> IntPair.of(left.size()+right.size()-pair.x-2, other.size()-pair.y-1))
        	.forEach(pairs::add);
        if (!targetIndexIsOnGenomeA) pairs = pairs.stream()
    		.map(IntPair::flip)
    		.collect(Collectors.toCollection(TreeSet::new));
        
        return new Alignment<>(bestscore, pairs, a, b);
	}
	
}
