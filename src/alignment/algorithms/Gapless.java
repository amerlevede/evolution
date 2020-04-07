package alignment.algorithms;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import alignment.Alignment;
import alignment.AlignmentRule;
import genome.LinearGenome;
import util.IntPair;

/**
 * Exact implementation for gapless alignment scores.
 */
public class Gapless {
	
	public static <G extends LinearGenome<G>> AlignmentRule<G> alignment(int scoreMatch, int scoreMismatch) {
		return (rng) -> (a, b) -> Gapless.align(rng, scoreMatch, scoreMismatch, a, b);
	}
	
	public static <G extends LinearGenome<G>> Alignment<G> align(Random rng, int scoreMatch, int scoreMismatch, G a, G b) {
		SortedSet<IntPair> pairs = new TreeSet<>();
		int score;
		if (a.size() <= b.size()) {
			int[] ali = alignShortestToLargest(rng, scoreMatch, scoreMismatch, a, b);
			score = ali[0];
			int bStart = ali[1];
			int aStart = ali[2];
			int aEnd = ali[3];
			for (int i=aStart; i<aEnd; i++) {
				pairs.add(IntPair.of(i, bStart + i));
			}
		} else {
			int[] ali = alignShortestToLargest(rng, scoreMatch, scoreMismatch, b, a);
			score = ali[0];
			int aStart = ali[1];
			int bStart = ali[2];
			int bEnd = ali[3];
			for (int i=bStart; i<bEnd; i++) {
				pairs.add(IntPair.of(aStart + i, i));
			}
		}
		
		return new Alignment<>(score, pairs, a, b);
	}
	
	private static <G extends LinearGenome<G>> int[] alignShortestToLargest(Random rng, int scoreMatch, int scoreMismatch, G court, G longue) {
		double exaequo = 0;
		int bestscore = 0;
		int beststartcourt = 0;
		int beststartlongue = 0;
		int bestendcourt = 0; // exclusive
		
		for (int start=-court.size()+1; start<longue.size()-bestscore*scoreMatch; start++) { // Can skip checking last starting positions if we already have a longer alignment
			int score = 0;
			int scorestart = Math.max(0, -start);
			
			for (int i=scorestart; i<court.size() && start+i<longue.size(); i++) {
				score += court.sameAt(i, longue, start+i)
					? scoreMatch
					: scoreMismatch;
				
				if (score < 0) { // Local alignment: ignore any prefix with score lower than 0
					score = 0;
					scorestart = i+1;
				} else if (score > bestscore) { // Local alignment: end alignment at max value, don't only compare at the end
					exaequo = 0;
					bestscore = score;
					beststartlongue = start;
					beststartcourt = scorestart;
					bestendcourt = i+1;
				} else if (score == bestscore) {
					exaequo++;
					if (rng.nextDouble() < 1/exaequo) {
						bestscore = score;
						beststartlongue = start;
						beststartcourt = scorestart;
						bestendcourt = i+1;
					}
				}
			}
		}
		
		return new int[] {bestscore, beststartlongue, beststartcourt, bestendcourt};
	}

}
