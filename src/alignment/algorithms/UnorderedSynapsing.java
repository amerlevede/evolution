package alignment.algorithms;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.stream.Collectors;

import alignment.Alignment;
import alignment.AlignmentOp;
import alignment.AlignmentRule;
import alignment.Global;
import alignment.Local;
import alignment.VarOAlignment;
import alignment.VarOAlignmentRule;
import genome.LinearGenome;
import util.Assert;
import util.IntPair;
import util.Table;

/**
 * See {@link OrderedSynapsing}. This is a further generalisation that does not preserve the ordering of the segments.
 * That is, once a local alignment is found, the parents are each split into two parts. The next step in the algorithm includes alignments between all pairs of such segments, including comparing a left segment to a right segment.
 * Unlike the ordered version, this is not a divide-and-conquer algorithm, and computation becomes much longer when there are more segments to align.
 * 
 * @author adriaan
 *
 */
public final class UnorderedSynapsing {
	
	private UnorderedSynapsing() {
		Assert.utilityClass();
	}
	
	/**
	 * Align two genomes by recursively applying a local alignment until the best possible alignment has a score lower than the proveided threshold value.
	 * This alignment method differs from {@link Global#alignment(AlignmentRule, int)} in that it recursively aligns all pairs of segments in the genome, possibly yielding aligned pairs with inconsistent orders (as in VarOAlignment).
	 * The segments of the output alignment are not simplified (VarOAlignment#simplify());
	 */
	public static <G extends LinearGenome<G>> VarOAlignmentRule<G> alignment(AlignmentRule<G> localAlign, int minSegmentScore) {
		return (rng) -> (a, b) -> align(rng, localAlign.apply(rng), minSegmentScore, a, b);
	}
	
	public static <G extends LinearGenome<G>> VarOAlignment<G> align(Random rng, AlignmentOp<G> localAlign, int minSegmentScore, G a, G b) {
		List<SortedSet<IntPair>> segments = alignSegments(rng, localAlign, minSegmentScore, a, b).stream().map(Alignment::getPairs).collect(Collectors.toList());
		return new VarOAlignment<>(segments, a, b);
	}
	
	public static <G extends LinearGenome<G>> List<Alignment<G>> alignSegments(Random rng, AlignmentOp<G> localAlign, int minSegmentScore, G a, G b) {
		List<Alignment<G>> result = new LinkedList<>();
		
		Table<IntPair,IntPair,Alignment<G>> subAlignments = new Table<>(
				(aSpan, bSpan) -> Local.alignInRange(
						localAlign, 
						a, aSpan.x, aSpan.y, 
						b, bSpan.x, bSpan.y
						)
				);
		
		IntPair aEnclosingSpan = IntPair.of(0, a.size());
		IntPair bEnclosingSpan = IntPair.of(0, b.size());
		Optional<Alignment<G>> bestAlignment = Optional
				.of(subAlignments.getOrAdd(aEnclosingSpan, bEnclosingSpan))
				.filter((ali) -> ali.score >= minSegmentScore);
		;
		
		while (bestAlignment.isPresent()) {
			
			// Add current best alignment to output
			result.add(bestAlignment.get());
			
			// Remove enclosing span of current best alignment from candidates, but add remaining bits to the left and right
			subAlignments.removeX(aEnclosingSpan);
			subAlignments.removeY(bEnclosingSpan);
			
			IntPair aNewSpan = IntPair.of(bestAlignment.get().getPairs().first().x, bestAlignment.get().getPairs().last().x+1);
			if (aEnclosingSpan.x < aNewSpan.x) subAlignments.addX(IntPair.of(aEnclosingSpan.x, aNewSpan.x));
			if (aEnclosingSpan.y > aNewSpan.y) subAlignments.addX(IntPair.of(aNewSpan.y, aEnclosingSpan.y));
			
			IntPair bNewSpan = IntPair.of(bestAlignment.get().getPairs().first().y, bestAlignment.get().getPairs().last().y+1);
			if (bEnclosingSpan.x < bNewSpan.x) subAlignments.addY(IntPair.of(bEnclosingSpan.x, bNewSpan.x));
			if (bEnclosingSpan.y > bNewSpan.y) subAlignments.addY(IntPair.of(bNewSpan.y, bEnclosingSpan.y));
			
			// Remove candidate spans with no alignments above threshold (they will not have better alignments in the future when spans are more divided)
			subAlignments.removeXIf((x, ymap) -> ymap.values().stream()
					.mapToInt(Alignment::getScore).max().orElse(minSegmentScore-1) < minSegmentScore);
			subAlignments.removeYIf((y, xmap) -> xmap.values().stream()
					.mapToInt(Alignment::getScore).max().orElse(minSegmentScore-1) < minSegmentScore);
			
			// Find new best alignment
			bestAlignment = Optional.empty();
			double exaequo = 1;
			for (IntPair aSpan : subAlignments.getXs()) for (IntPair bSpan : subAlignments.getYs()) {
				Alignment<G> candidateSegment = subAlignments.get(aSpan, bSpan);
				
				if (candidateSegment.score >= minSegmentScore) {
					if (bestAlignment.isEmpty() || candidateSegment.score > bestAlignment.get().score) {
						bestAlignment = Optional.of(candidateSegment);
						aEnclosingSpan = aSpan;
						bEnclosingSpan = bSpan;
						exaequo = 0;
					} else if (candidateSegment.score == bestAlignment.get().score) {
						exaequo++;
						if (rng.nextDouble() < 1/exaequo) {
							bestAlignment = Optional.of(candidateSegment);
							aEnclosingSpan = aSpan;
							bEnclosingSpan = bSpan;
						}
					}
				}
			}
		}
		
		return result;
	}

	// Implementation re-aligns every pair with every match
	// Need to remember alignments
	/** @see #alignment(AlignmentOp, int) */
	@Deprecated
	public static <G extends LinearGenome<G>> VarOAlignment<G> alignBad(Random rng, AlignmentOp<G> localAlign, int minSegmentScore, G a, G b) {
		List<SortedSet<IntPair>> segments = new LinkedList<>();
		List<IntPair> aSpans = new LinkedList<>(List.of(IntPair.of(0, a.size())));
		List<IntPair> bSpans = new LinkedList<>(List.of(IntPair.of(0, b.size())));
		
		Alignment<G> bestAlignment = localAlign.apply(a, b);
		int aBestSegmenti = 0;
		int bBestSegmenti = 0;
		boolean bestAlignmentIsGoodEnough = bestAlignment.score >= minSegmentScore;
		
		while (bestAlignmentIsGoodEnough) {
			// Add best alignment to result
			segments.add(bestAlignment.getPairs());
			
			// Cut segment of current best alignment (keep heads or tails not part of the local alignment)
			{
			IntPair aNewSpan = IntPair.of(bestAlignment.getPairs().first().x, bestAlignment.getPairs().last().x+1);
			IntPair aEnclosingSpan = aSpans.get(aBestSegmenti);
			aSpans.remove(aBestSegmenti);
			if (aEnclosingSpan.y > aNewSpan.y) aSpans.add(IntPair.of(aNewSpan.y, aEnclosingSpan.y));
			if (aEnclosingSpan.x < aNewSpan.x) aSpans.add(IntPair.of(aEnclosingSpan.x, aNewSpan.x));
			}
			
			{
			IntPair bNewSpan = IntPair.of(bestAlignment.getPairs().first().y, bestAlignment.getPairs().last().y+1);
			IntPair bEnclosingSpan = bSpans.get(bBestSegmenti);
			bSpans.remove(bBestSegmenti);
			if (bEnclosingSpan.y > bNewSpan.y) bSpans.add(IntPair.of(bNewSpan.y, bEnclosingSpan.y));
			if (bEnclosingSpan.x < bNewSpan.x) bSpans.add(IntPair.of(bEnclosingSpan.x, bNewSpan.x));
			}
			
			// Align all pairs of segments
			bestAlignmentIsGoodEnough = false;
			List<IntPair> aSegmentsToKeep = new LinkedList<>();
			List<IntPair> bSegmentsToKeep = new LinkedList<>();
			for (IntPair aSpan : aSpans) for (IntPair bSpan : bSpans) {
				Alignment<G> ali = Local.alignInRange(localAlign,
						a, aSpan.x, aSpan.y,
						b, bSpan.x, bSpan.y);
				if (ali.score >= minSegmentScore) {
					aSegmentsToKeep.add(aSpan);
					bSegmentsToKeep.add(bSpan);
					if (bestAlignmentIsGoodEnough == false || bestAlignment.score < ali.score) {
						bestAlignmentIsGoodEnough = true;
						bestAlignment = ali;
						aBestSegmenti = aSegmentsToKeep.size()-1;
						bBestSegmenti = bSegmentsToKeep.size()-1;
					}
				}
			}
			
			// Throw away segments that did not have any good-enough alignment (they also will not have a good alignment with smaller parts in next iterations)
			aSpans = aSegmentsToKeep;
			bSpans = bSegmentsToKeep;
		}
		
		return new VarOAlignment<>(segments, a, b);
	}
	
	

}
