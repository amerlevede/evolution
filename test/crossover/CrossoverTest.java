/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crossover;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;

import alignment.Alignment;
import alignment.AlignmentRule;
import alignment.Global;
import alignment.Glocal;
import alignment.Local;
import alignment.VarOAlignmentRule;
import genome.binary.BitGenomeWithHistory;
import genome.binary.BitGenomeWithHistoryInit;
import util.CategoricalDistribution;
import util.DiscreteDistribution;
import util.IntPair;

/**
 *
 * @author adriaan
 */
class CrossoverTest extends BitGenomeWithHistoryInit {

    public CrossoverTest() {
    }

    @RepeatedTest(100)
    public void testOnepoint() {
        int ia = 3;
        int ib = 18;

        CrossoverOp.performOnePoint(randomA, ia, randomB, ib);

        assertThat(BitGenomeWithHistory::unmutated,
                randomA.view(0, ia),
                randomAref.view(0, ia)
                );
        assertThat(BitGenomeWithHistory::unmutated,
                randomA.view(ia, randomA.size()),
                randomBref.view(ib, randomBref.size())
                );

        assertThat(BitGenomeWithHistory::unmutated,
                randomB.view(0, ib),
                randomBref.view(0, ib)
                );
        assertThat(BitGenomeWithHistory::unmutated,
                randomB.view(ib, randomB.size()),
                randomAref.view(ia, randomAref.size())
                );
    }

    @RepeatedTest(100)
    public void testOnepoint_zerolen() {
        int i=5;
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performOnePoint(randomA, 0, randomB, i));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performOnePoint(randomA, randomA.size(), randomB, i));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performOnePoint(randomA, i, randomB, 0));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performOnePoint(randomA, i, randomB, randomB.size()));
    }

    @RepeatedTest(100)
    public void testTwopoint() {
        int ia1 = 3;
        int ia2 = 5;
        int ib1 = 18;
        int ib2 = 19;

        CrossoverOp.performTwoPoint(randomA, ia1, ia2, randomB, ib1, ib2);

        assertThat(BitGenomeWithHistory::unmutated,
                randomA.view(0, ia1),
                randomAref.view(0, ia1)
                );
        assertThat(BitGenomeWithHistory::unmutated,
                randomA.view(ia1, ia1+(ib2-ib1)),
                randomBref.view(ib1, ib2)
                );
        assertThat(BitGenomeWithHistory::unmutated,
                randomA.view(ia1+(ib2-ib1), randomA.size()),
                randomAref.view(ia2, randomAref.size())
                );

        assertThat(BitGenomeWithHistory::unmutated,
                randomB.view(0, ib1),
                randomBref.view(0, ib1)
                );
        assertThat(BitGenomeWithHistory::unmutated,
                randomB.view(ib1, ib1+(ia2-ia1)),
                randomAref.view(ia1, ia2)
                );
        assertThat(BitGenomeWithHistory::unmutated,
                randomB.view(ib1+(ia2-ia1), randomB.size()),
                randomBref.view(ib2, randomBref.size())
                );
    }

    @RepeatedTest(100)
    public void testTwopoint_zerolen() {
        int i=5;
        int j=10;
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, 0, j, randomB, i, j));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, i, randomA.size(), randomB, i, j));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, i, j, randomB, 0, j));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, i, j, randomB, i, randomB.size()));

        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, j, j, randomB, i, j));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, i, j, randomB, i, i));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, j, i, randomB, i, j));
        assertThrows(IndexOutOfBoundsException.class, () ->
            CrossoverOp.performTwoPoint(randomA, i, j, randomB, j, i));
    }

    @RepeatedTest(100)
    public void testNpoint_nopoint() {
        CrossoverOp.performNPoint(randomA, Collections.emptySortedSet(), randomB, Collections.emptySortedSet());

        assertThat(BitGenomeWithHistory::unmutated,
                randomA, randomAref);
        assertThat(BitGenomeWithHistory::unmutated,
                randomB, randomBref);
    }

    @RepeatedTest(100)
    public void testNpoint_onepoint() {
        int ia = 3;
        int ib = 18;

        BitGenomeWithHistory randomAcopy = randomAref.copy();
        BitGenomeWithHistory randomBcopy = randomBref.copy();

        CrossoverOp.performNPoint(randomA, new TreeSet<>(List.of(ia)), randomB, new TreeSet<>(List.of(ib)));
        CrossoverOp.performOnePoint(randomAcopy, ia, randomBcopy, ib);

        assertThat(BitGenomeWithHistory::unmutated,
                randomA, randomAcopy);
        assertThat(BitGenomeWithHistory::unmutated,
                randomB, randomBcopy);
    }

    @RepeatedTest(100)
    public void testNpoint_twopoint() {
        int ia1 = 3;
        int ia2 = 5;
        int ib1 = 18;
        int ib2 = 19;

        BitGenomeWithHistory randomAcopy = randomAref.copy();
        BitGenomeWithHistory randomBcopy = randomBref.copy();

        CrossoverOp.performNPoint(randomA, new TreeSet<>(List.of(ia1, ia2)), randomB, new TreeSet<>(List.of(ib1, ib2)));
        CrossoverOp.performTwoPoint(randomAcopy, ia1, ia2, randomBcopy, ib1, ib2);

        assertThat(BitGenomeWithHistory::unmutated,
                randomA, randomAcopy);
        assertThat(BitGenomeWithHistory::unmutated,
                randomB, randomBcopy);
    }

	@RepeatedTest(100)
	public void testOrdered_compare1point() {
		BitGenomeWithHistory refA = random5Aref;
		BitGenomeWithHistory refB = random5Bref;
		BitGenomeWithHistory testA = refA.copy();
		BitGenomeWithHistory testB = refB.copy();
		BitGenomeWithHistory compareA = refA.copy();
		BitGenomeWithHistory compareB = refB.copy();
		BitGenomeWithHistory test2A = refA.copy();
		BitGenomeWithHistory test2B = refB.copy();

		Alignment<BitGenomeWithHistory> alignment = Global.<BitGenomeWithHistory>alignmentWithAffineGapScore(1, -3, -5, -5).apply(rng).apply(refA, refB);
		SortedSet<IntPair> afterAddingGapPoints = CrossoverOp.alignmentToCrossoverPoints(alignment.getPairs(), refA, refB);
		int n = 1;
		SortedSet<IntPair> crosses = CategoricalDistribution.uniformUnindexed(afterAddingGapPoints).stream(rng)
				.distinct()
				.filter(pair -> refA.innerSplice(pair.x) && refB.innerSplice(pair.y))
				.limit(n)
				.collect(Collectors.toCollection(TreeSet::new));

		CrossoverOp.performNPoint(crosses, compareA, compareB);
		CrossoverOp.performUnorderedNPoint(List.of(crosses), testA, testB);
		CrossoverOp.performUnorderedNPoint(crosses, test2A, test2B);

		assertThat(BitGenomeWithHistory::unmutated, testA, compareA);
		assertThat(BitGenomeWithHistory::unmutated, testB, compareB);
		assertThat(BitGenomeWithHistory::unmutated, test2A, compareA);
		assertThat(BitGenomeWithHistory::unmutated, test2B, compareB);
	}

	@RepeatedTest(100)
	public void testOrdered_compare2point() {
		BitGenomeWithHistory refA = random1000Aref;
		BitGenomeWithHistory refB = random1000Bref;
		BitGenomeWithHistory testA = refA.copy();
		BitGenomeWithHistory testB = refB.copy();
		BitGenomeWithHistory compareA = refA.copy();
		BitGenomeWithHistory compareB = refB.copy();
		BitGenomeWithHistory test2A = refA.copy();
		BitGenomeWithHistory test2B = refB.copy();

		Alignment<BitGenomeWithHistory> alignment = Global.<BitGenomeWithHistory>alignmentWithAffineGapScore(1, -3, -5, -5).apply(rng).apply(refA, refB);
		SortedSet<IntPair> afterAddingGapPoints = CrossoverOp.alignmentToCrossoverPoints(alignment.getPairs(), refA, refB);
		int n = 2;
		SortedSet<IntPair> crosses = CategoricalDistribution.uniformUnindexed(afterAddingGapPoints).stream(rng)
				.distinct()
				.filter(pair -> refA.innerSplice(pair.x) && refB.innerSplice(pair.y))
				.limit(n)
				.collect(Collectors.toCollection(TreeSet::new));

		CrossoverOp.performNPoint(crosses, compareA, compareB);
		CrossoverOp.performUnorderedNPoint(List.of(crosses), testA, testB);
		CrossoverOp.performUnorderedNPoint(crosses, test2A, test2B);

		assertThat(BitGenomeWithHistory::unmutated, testA, compareA);
		assertThat(BitGenomeWithHistory::unmutated, testB, compareB);
		assertThat(BitGenomeWithHistory::unmutated, test2A, compareA);
		assertThat(BitGenomeWithHistory::unmutated, test2B, compareB);
	}

	@RepeatedTest(100)
	public void testOrdered_compare3point() {
		BitGenomeWithHistory refA = random1000Aref;
		BitGenomeWithHistory refB = random1000Bref;
		BitGenomeWithHistory testA = refA.copy();
		BitGenomeWithHistory testB = refB.copy();
		BitGenomeWithHistory compareA = refA.copy();
		BitGenomeWithHistory compareB = refB.copy();
		BitGenomeWithHistory test2A = refA.copy();
		BitGenomeWithHistory test2B = refB.copy();

		Alignment<BitGenomeWithHistory> alignment = Global.<BitGenomeWithHistory>alignmentWithAffineGapScore(1, -3, -5, -5).apply(rng).apply(refA, refB);
		SortedSet<IntPair> afterAddingGapPoints = CrossoverOp.alignmentToCrossoverPoints(alignment.getPairs(), refA, refB);
		int n = 3;
		SortedSet<IntPair> crosses = CategoricalDistribution.uniformUnindexed(afterAddingGapPoints).stream(rng)
				.distinct()
				.filter(pair -> refA.innerSplice(pair.x) && refB.innerSplice(pair.y))
				.limit(n)
				.collect(Collectors.toCollection(TreeSet::new));

		CrossoverOp.performNPoint(crosses, compareA, compareB);
		CrossoverOp.performUnorderedNPoint(List.of(crosses), testA, testB);
		CrossoverOp.performUnorderedNPoint(crosses, test2A, test2B);


		assertThat(BitGenomeWithHistory::unmutated, testA, compareA);
		assertThat(BitGenomeWithHistory::unmutated, testB, compareB);
		assertThat(BitGenomeWithHistory::unmutated, test2A, compareA);
		assertThat(BitGenomeWithHistory::unmutated, test2B, compareB);
	}

	@RepeatedTest(100)
	public void testOrdered_compareuniform() {
		BitGenomeWithHistory refA = random1000Aref;
		BitGenomeWithHistory refB = random1000Bref;
		BitGenomeWithHistory testA = refA.copy();
		BitGenomeWithHistory testB = refB.copy();
		BitGenomeWithHistory compareA = refA.copy();
		BitGenomeWithHistory compareB = refB.copy();
		BitGenomeWithHistory test2A = refA.copy();
		BitGenomeWithHistory test2B = refB.copy();

		Alignment<BitGenomeWithHistory> alignment = Global.<BitGenomeWithHistory>alignmentWithAffineGapScore(1, -3, -5, -5).apply(rng).apply(refA, refB);
		SortedSet<IntPair> afterAddingGapPoints = CrossoverOp.alignmentToCrossoverPoints(alignment.getPairs(), refA, refB);
		int n = DiscreteDistribution.getBinomial(rng, alignment.getPairs().size(), 0.5);
		SortedSet<IntPair> crosses = CategoricalDistribution.uniformUnindexed(afterAddingGapPoints).stream(rng)
				.distinct()
				.filter(pair -> refA.innerSplice(pair.x) && refB.innerSplice(pair.y))
				.limit(n)
				.collect(Collectors.toCollection(TreeSet::new));

		CrossoverOp.performNPoint(crosses, compareA, compareB);
		CrossoverOp.performUnorderedNPoint(List.of(crosses), testA, testB);
		CrossoverOp.performUnorderedNPoint(crosses, test2A, test2B);

		assertThat(BitGenomeWithHistory::unmutated, testA, compareA);
		assertThat(BitGenomeWithHistory::unmutated, testB, compareB);
		assertThat(BitGenomeWithHistory::unmutated, test2A, compareA);
		assertThat(BitGenomeWithHistory::unmutated, test2B, compareB);
	}

	@RepeatedTest(100)
	public void testOrdered_compareCrossEverywhere() {
		BitGenomeWithHistory refA = random1000Aref;
		BitGenomeWithHistory refB = random1000Bref;
		BitGenomeWithHistory testA = refA.copy();
		BitGenomeWithHistory testB = refB.copy();
		BitGenomeWithHistory compareA = refA.copy();
		BitGenomeWithHistory compareB = refB.copy();
		BitGenomeWithHistory test2A = refA.copy();
		BitGenomeWithHistory test2B = refB.copy();

		Alignment<BitGenomeWithHistory> alignment = Global.<BitGenomeWithHistory>alignmentWithAffineGapScore(1, -3, -5, -5).apply(rng).apply(refA, refB);
		SortedSet<IntPair> afterAddingGapPoints = CrossoverOp.alignmentToCrossoverPoints(alignment.getPairs(), refA, refB);
		int n = alignment.getPairs().size();
		SortedSet<IntPair> crosses = CategoricalDistribution.uniformUnindexed(afterAddingGapPoints).stream(rng)
				.distinct()
				.filter(pair -> refA.innerSplice(pair.x) && refB.innerSplice(pair.y))
				.limit(n)
				.collect(Collectors.toCollection(TreeSet::new));

		CrossoverOp.performNPoint(crosses, compareA, compareB);
		CrossoverOp.performUnorderedNPoint(List.of(crosses), testA, testB);
		CrossoverOp.performUnorderedNPoint(crosses, test2A, test2B);

		assertThat(BitGenomeWithHistory::unmutated, testA, compareA);
		assertThat(BitGenomeWithHistory::unmutated, testB, compareB);
		assertThat(BitGenomeWithHistory::unmutated, test2A, compareA);
		assertThat(BitGenomeWithHistory::unmutated, test2B, compareB);
	}

	@RepeatedTest(5)
	public void testDivideIntoSegments_onesegment() {
		BitGenomeWithHistory refA = randomAref;
		BitGenomeWithHistory refB = randomAref;

		AlignmentRule<BitGenomeWithHistory> localAlignment = Local.alignmentWithAffineGapScore(1, -3, -5, -5);
		VarOAlignmentRule<BitGenomeWithHistory> glocalAlignment = Glocal.unorderedRepeatedLocal(localAlignment, 10);

		List<SortedSet<IntPair>> segmentsRef = glocalAlignment.apply(rng).apply(refA, refB).getSegments();
		segmentsRef = new ArrayList<>(segmentsRef);
		segmentsRef.sort(Comparator.comparing(SortedSet::first));

		SortedSet<IntPair> segmentsCollected = segmentsRef.stream().flatMap(Collection::stream).collect(Collectors.toCollection(TreeSet::new));
		List<SortedSet<IntPair>> segmentsTest = CrossoverOp.divideIntoSegments(segmentsCollected);
		segmentsTest.sort(Comparator.comparing(SortedSet::first));

		assertEquals(segmentsRef, segmentsTest);
	}

	@RepeatedTest(100)
	public void testDivideIntoSegments_random() {
		BitGenomeWithHistory refA = random1000Aref;
		BitGenomeWithHistory refB = random1000Bref;

		AlignmentRule<BitGenomeWithHistory> localAlignment = Local.alignmentWithAffineGapScore(1, -3, -5, -5);
		VarOAlignmentRule<BitGenomeWithHistory> glocalAlignment = Glocal.unorderedRepeatedLocal(localAlignment, 10);

		List<SortedSet<IntPair>> segmentsRef = glocalAlignment.apply(rng).apply(refA, refB).simplify().getSegments();
		segmentsRef = new ArrayList<>(segmentsRef);
		segmentsRef.sort(Comparator.comparing(SortedSet::first));

		SortedSet<IntPair> segmentsCollected = segmentsRef.stream().flatMap(Collection::stream).collect(Collectors.toCollection(TreeSet::new));
		List<SortedSet<IntPair>> segmentsTest = CrossoverOp.divideIntoSegments(segmentsCollected);
		segmentsTest.sort(Comparator.comparing(SortedSet::first));

		assertEquals(segmentsRef, segmentsTest);
	}

}
