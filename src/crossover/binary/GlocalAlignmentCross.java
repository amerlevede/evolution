package crossover.binary;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import alignment.VarOAlignment;
import alignment.VarOAlignmentRule;
import crossover.CrossoverOp;
import crossover.CrossoverRule;
import genome.VarLengthGenome;
import genome.binary.BitGenome;
import genome.integer.Permutation;
import util.Assert;
import util.CategoricalDistribution;
import util.DiscreteDistribution;
import util.IntPair;

/**
 * A general crossover scheme guided by a glocal alignment (VarOAlignment).
 * These crossovers will exchange points in each ordered section of the alignment.
 */
public final class GlocalAlignmentCross {

	private GlocalAlignmentCross() {
		Assert.utilityClass();
	}

	/**
	 * A GlocalAlignmentCross picking an even number of crossover loci at each local segment of the glocal alignment.
	 * This results in exchanging genetic information only within segments. The offspring will inherit exactly the genome structure of its parent, with only local changes.
	 */
	public static class Segmented {

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(VarOAlignmentRule<G> glocalAlign, CrossoverRule.N n) {
			switch (n.type) {
			case UNIFORM:
				return nearlyUniform(glocalAlign);
			case VALUE:
				return distinctN(glocalAlign, n.value());
			default: throw new IllegalArgumentException();
			}
		}

		/**
		 * Perform glocal alignment cross.
		 * The given number of aligment points will be distributed across the segments (proportional to size).
		 * Distributed numbers will be adjusted so that each segment is even, so the actual number of crossover points may fluctuate in the range n +- segments.size().
		 */
		public static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints(List<SortedSet<IntPair>> segments, int n, Random rng, G a, G b) {
			int[] segmentSizes = segments.stream().mapToInt(Collection::size).toArray();
			int[] nPerPart = distributeCrossoverPoints(segmentSizes, n, rng);
			GlocalAlignmentCross.performWithoutAddingGapPoints(segments, nPerPart, rng, a, b);
		}

		static int[] distributeCrossoverPoints(int[] segmentSizes, int n, Random rng) {
			int[] halfsizes = IntStream.of(segmentSizes).map(s -> s/2).toArray();
			int maxHalfN = IntStream.of(halfsizes).sum();
			int nhalf = Math.max(0, Math.min(maxHalfN, DiscreteDistribution.getRandomRound(rng, n/2)));
			int[] nPerPart = DiscreteDistribution.getManyWeightedWithoutReplacement(rng, halfsizes, nhalf);
			for (int i=0; i<nPerPart.length; i++) nPerPart[i] *= 2;
			return nPerPart;
		}

		public static <G extends VarLengthGenome<G>> void perform(Random rng, List<SortedSet<IntPair>> segments, int n, G a, G b) {
			List<SortedSet<IntPair>> afterAddingGapPoints =
					segments.stream()
					.map((segment) -> CrossoverOp.alignmentToCrossoverPoints(segment, a, b))
					.collect(Collectors.toList());
			performWithoutAddingGapPoints(afterAddingGapPoints, n, rng, a, b);
		}


		public static <G extends VarLengthGenome<G>> void performNearlyUniform(List<SortedSet<IntPair>> segments, Random rng, G a, G b) {
			List<SortedSet<IntPair>> afterAddingGapPoints = segments.stream()
					.map(pairs -> CrossoverOp.alignmentToCrossoverPoints(pairs, a, b))
					.collect(Collectors.toList());
			int maxN = afterAddingGapPoints.stream().mapToInt(Collection::size).sum();

			int n = DiscreteDistribution.getBinomial(rng, maxN, 0.5);

			performWithoutAddingGapPoints(afterAddingGapPoints, n, rng , a, b);
		}

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(VarOAlignmentRule<G> glocalAlign, int n) {
			return (rng) -> (a, b) -> {
				VarOAlignment<G> ali = glocalAlign.apply(rng).apply(a, b);
				ali = ali.simplify();

				perform(rng, ali.getSegments(), n, a, b);
			};
		}

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(VarOAlignmentRule<G> glocalAlign) {
			return (rng) -> (a, b) -> {
				VarOAlignment<G> ali = glocalAlign.apply(rng).apply(a, b);
				ali = ali.simplify();

				performNearlyUniform(ali.getSegments(), rng, a, b);
			};
		}

	}

	public static class Unsegmented {

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(VarOAlignmentRule<G> glocalAlign, CrossoverRule.N n) {
			switch (n.type) {
			case UNIFORM:
				return nearlyUniform(glocalAlign);
			case VALUE:
				return distinctN(glocalAlign, n.value());
			default: throw new IllegalArgumentException();
			}
		}

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(VarOAlignmentRule<G> glocalAlign) {
			return (rng) -> (a, b) -> {
				VarOAlignment<G> ali = glocalAlign.apply(rng).apply(a, b);
				ali = ali.simplify();

				performNearlyUniform(ali.getSegments(), rng, a, b);
			};
		}

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(VarOAlignmentRule<G> glocalAlign, int n) {
			return (rng) -> (a, b) -> {
				VarOAlignment<G> ali = glocalAlign.apply(rng).apply(a, b);
				ali = ali.simplify();

				perform(rng, ali.getSegments(), n, a, b);
			};
		}

		public static <G extends VarLengthGenome<G>> void perform(Random rng, List<SortedSet<IntPair>> segments, int n, G a, G b) {
			List<SortedSet<IntPair>> afterAddingGapPoints =
					segments.stream()
					.map((segment) -> CrossoverOp.alignmentToCrossoverPoints(segment, a, b))
					.collect(Collectors.toList());
			performWithoutAddingGapPoints(rng, afterAddingGapPoints, n, a, b);
		}

		public static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints(Random rng, List<SortedSet<IntPair>> segments, int n, G a, G b) {
			int[] nPerPart = distributeCrossoverPoints(segments, n, rng);
			GlocalAlignmentCross.performWithoutAddingGapPoints(segments, nPerPart, rng, a, b);
		}

		public static <G extends VarLengthGenome<G>> void performNearlyUniform(List<SortedSet<IntPair>> segments, Random rng, G a, G b) {
			List<SortedSet<IntPair>> afterAddingGapPoints = segments.stream()
					.map(pairs -> CrossoverOp.alignmentToCrossoverPoints(pairs, a, b))
					.collect(Collectors.toList());
			int maxN = afterAddingGapPoints.stream().mapToInt(Collection::size).sum();

			int n = DiscreteDistribution.getBinomial(rng, maxN, 0.5);

			performWithoutAddingGapPoints(rng, afterAddingGapPoints, n , a, b);
		}

		static int[] distributeCrossoverPoints(List<SortedSet<IntPair>> segments, int n, Random rng) {
			Permutation order = Permutation.fromOrdering(segments, Comparator.comparing(set -> set.first().flip())).uncyclic();
			int m = order.size(); // number of segments + 1

			Permutation edgesA = Permutation.rot(m,1);
			Permutation edgesB = Permutation.action(order.inverse(), Permutation.rot(m), order);
			Permutation edgemap = Permutation.action(edgesB, Permutation.rot(m,-1));

			int[] cycles = new int[m];
			int c = edgemap.cyclesAndGetN(cycles);

			// Find a choice of crossover with at most the requested number of xpoints
			boolean[] chi = new boolean[c];
			int[] ndistOdd = new int[m-1];
			int xpoints;
			{
			int len;
			int i,j;
			do {
				do {
					len=0;
					i=0;
					j=0;
					for (int k=0; k<c; k++) chi[k] = rng.nextBoolean();
					do {
						len++;
						i = chi[cycles[i]-1] ? edgesA.get(i) : edgesB.get(i);
						j = chi[cycles[j]-1] ? edgesB.get(j) : edgesA.get(j);
					} while (i != 0 && j != 0);
				} while (len < m);

				for (int k=0; k<m-1; k++) ndistOdd[k] = chi[cycles[k]-1] ^ chi[cycles[k+1]-1] ? 1 : 0;
				xpoints = IntStream.of(ndistOdd).sum();
			} while (xpoints > n);
			}

			// Fill up remaining xpoints with even numbers
			int[] segmentSizes = IntStream.range(0, m-1).map(i->segments.get(i).size() - ndistOdd[i]).toArray();
			int[] ndistEven = Segmented.distributeCrossoverPoints(segmentSizes, n - xpoints, rng);

			return IntStream.range(0, m-1).map(i -> ndistEven[i] + ndistOdd[i]).toArray();
		}

	}


	@Deprecated
	public static class General {

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> of(VarOAlignmentRule<G> glocalAlign, CrossoverRule.N n) {
			switch (n.type) {
			case UNIFORM:
				return nearlyUniform(glocalAlign);
			case VALUE:
				return distinctN(glocalAlign, n.value());
			default: throw new IllegalArgumentException();
			}
		}

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> nearlyUniform(VarOAlignmentRule<G> glocalAlignment) {
			return (rng) -> (a, b) -> performNearlyUniform(glocalAlignment.apply(rng).apply(a, b).getSegments(), rng, a, b);
		}

		public static <G extends VarLengthGenome<G>> CrossoverRule<G> distinctN(VarOAlignmentRule<G> glocalAlign, int n) {
			return (rng) -> (a, b) -> perform(glocalAlign.apply(rng).apply(a, b).getSegments(), n, rng, a, b);
		}

		public static <G extends VarLengthGenome<G>> void performNearlyUniform(List<SortedSet<IntPair>> glocalAlignment, Random rng, G a, G b) {
			List<SortedSet<IntPair>> afterAddingGapPoints =
					glocalAlignment.stream()
					.map((segment) -> CrossoverOp.alignmentToCrossoverPoints(segment, a, b))
					.collect(Collectors.toList());
			int maxN = afterAddingGapPoints.stream().mapToInt(Collection::size).sum();
			int n = DiscreteDistribution.getBinomial(rng, maxN, 0.5);

			GlocalAlignmentCross.General.performWithoutAddingGapPoints(afterAddingGapPoints, n, rng, a, b);
		}

		public static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints(List<SortedSet<IntPair>> glocalAlignment, int n, Random rng, G a, G b) {
			List<SortedSet<IntPair>> simplified = new VarOAlignment<>(glocalAlignment, a, b).simplify().getSegments();
			int[] nPerPart = distributeCrossoverPoints(simplified, n, rng);
			GlocalAlignmentCross.performWithoutAddingGapPoints(simplified, nPerPart, rng, a, b);
		}

		public static <G extends VarLengthGenome<G>> void perform(List<SortedSet<IntPair>> glocalAlignment, int n, Random rng, G a, G b) {
			List<SortedSet<IntPair>> afterAddingGapPoints =
					glocalAlignment.stream()
					.map((segment) -> CrossoverOp.alignmentToCrossoverPoints(segment, a, b))
					.collect(Collectors.toList());
			GlocalAlignmentCross.General.performWithoutAddingGapPoints(afterAddingGapPoints, n, rng, a, b);
		}

		/**
		 * Distribute some number of crossover points over the segments of a glocal alignment using the general method.
		 * @note if k is odd, it may be impossible to generate a sound k-point crossover. In that case, there will be k-1 points.
		 * @note k is truncated if it is higher than the maximum size (i.e. the total number of pairs in the glocal alignment)
		 */
		static int[] distributeCrossoverPoints(List<SortedSet<IntPair>> glocalAlignment, int n, Random rng) {
			throw new UnsupportedOperationException();
//			int maxN = Stream.of(glocalAlignment).mapToInt(Collection::size).sum();
//			n = Math.min(n, maxN);
//
//			Permutation order = Permutation.fromOrdering(glocalAlignment, Comparator.comparing(set -> set.first().flip()));
//
//			// The subset of segments with an odd number of crossover points must correspond to an LRE subpermutation of the ordering
//			// If k is odd, then the number of segments with odd numbers must be odd, else it must be even
//			// Also, the number of segments with odd numbers must not exceed k
//			boolean[] odds;
//			if (n % 2 == 0) {
//				odds = order.getRandomLRESubsetWithEvenSizeUpTo(rng, n);
//			} else {
//				Optional<boolean[]> maybe = order.getRandomLRESubsetWithOddSizeUpTo(rng, n);
//				if (maybe.isPresent()) {
//					odds = maybe.get();
//				} else {
//					odds = order.getRandomLRESubsetWithEvenSizeUpTo(rng, n-1);
//				}
//			}
//
//			// Distribute (k - size of odds subpermutation) in even groups (i.e. segmented way), then add 1 to each segment that was designated to be odd
//			int[] sizes = glocalAlignment.stream().mapToInt(Collection::size).toArray();
//			int[] sizesWithoutOdds = IntStream.range(0, glocalAlignment.size()).map(i -> (sizes[i] - (odds[i]?1:0))/2).toArray();
//			int nWithoutOdds = IntStream.of(sizesWithoutOdds).sum();
//			int[] resultWithoutOdds = DiscreteDistribution.getManyWeightedWithoutReplacement(rng, sizesWithoutOdds, nWithoutOdds);
//			int[] result = IntStream.range(0, glocalAlignment.size()).map(i -> resultWithoutOdds[i]*2 + (odds[i]?1:0)).toArray();
//
//			return result;
		}
	}

	static List<SortedSet<IntPair>> crossoverPoints(List<SortedSet<IntPair>> glocalAlignment, int[] nPerPart, Random rng) {
		List<SortedSet<IntPair>> result = new LinkedList<>();
		for (int i=0; i<glocalAlignment.size(); i++) {
			SortedSet<IntPair> segmentCrosses = CategoricalDistribution.uniformUnindexed(glocalAlignment.get(i)).stream(rng)
				.distinct().limit(nPerPart[i])
				.collect(Collectors.toCollection(TreeSet::new));
			result.add(segmentCrosses);
		}
		return result;
	}

	/**
	 * Perform glocal alignment cross with the given number of crossover points per segment.
	 */
	public static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints(List<SortedSet<IntPair>> glocalAlignment, int[] nPerPart, Random rng, G a, G b) {
		List<SortedSet<IntPair>> crosses = crossoverPoints(glocalAlignment, nPerPart, rng);
		CrossoverOp.performUnorderedNPoint(crosses, a, b);
	}

	/**
	 * Alternative implementation of {@link #performWithoutAddingGapPoints(List, int[], Random, BitGenome, BitGenome)} that only works for segmented cross.
	 */
	static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints_repeatedNpoint(List<SortedSet<IntPair>> crosses, G a, G b) {
		Function<IntPair,IntPair> transform = pair->pair;

		for (int i=0; i<crosses.size(); i++) {
			SortedSet<IntPair> segmentCrosses = crosses.get(i).stream().map(transform).collect(Collectors.toCollection(TreeSet::new));
			if (segmentCrosses.size() == 0) continue;
			int aLength= a.size();
			CrossoverOp.performNPoint(segmentCrosses, a, b);
			int aLengthDelta = a.size() - aLength;
			int aPosition = segmentCrosses.first().x;
			int bPosition = segmentCrosses.first().y;
			if (aLengthDelta!=0) transform = transform.andThen(pair ->
				IntPair.of(
						pair.x > aPosition ? pair.x+aLengthDelta : pair.x,
						pair.y > bPosition ? pair.y-aLengthDelta : pair.y)
				);
		}
	}

	/**
	 * Alternative implementation of {@link #performWithoutAddingGapPoints(List, int[], Random, BitGenome, BitGenome)} that only works for segmented cross.
	 */
	static <G extends VarLengthGenome<G>> void performWithoutAddingGapPoints_repeatedNpoint(List<SortedSet<IntPair>> segments, int[] nPerPart, Random rng, G a, G b) {
		List<SortedSet<IntPair>> crosses = crossoverPoints(segments, nPerPart, rng);
		performWithoutAddingGapPoints_repeatedNpoint(crosses, a, b);
	}

}
