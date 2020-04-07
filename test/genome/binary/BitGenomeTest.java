/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genome.binary;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author adriaan
 */
class BitGenomeTest extends BitGenomeInit {

    public BitGenomeTest() {
    }

    @Test
    public void testZeroes() {
        assertEquals(false, this.zeroes5.get(0));
        assertEquals(false, this.zeroes5.get(1));
        assertEquals(false, this.zeroes5.get(2));
        assertEquals(false, this.zeroes5.get(3));
        assertEquals(false, this.zeroes5.get(4));
        assertEquals(5, this.zeroes5.size());
    }

    @Test
    public void testRandom() {
        assertEquals(40, this.randomA.size());
        assertEquals(50000, this.randomlong.size());
    }

    @Test
    public void testGet() {
        assertThrows(IndexOutOfBoundsException.class, () -> this.zeroes5.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> this.zeroes5.get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> this.zero1.get(1));
    }

    @Test
    public void testSet() {
        int i=1;
        this.zeroes5.set(i, true);
        assertEquals(true, this.zeroes5.get(i));

        this.zeroes5.set(i, false);
        assertEquals(false, this.zeroes5.get(i));

        this.ones5.set(i, true);
        assertEquals(true, this.ones5.get(i));

        this.ones5.set(i, false);
        assertEquals(false, this.ones5.get(i));

        assertThrows(IndexOutOfBoundsException.class, () -> this.zeroes5.set(-1, true));
        assertThrows(IndexOutOfBoundsException.class, () -> this.zeroes5.set(this.zeroes5.size(), true));
    }

    @Test
    public void testFlip() {
        int i=1;
        this.zeroes5.flip(i);
        assertEquals(true, this.zeroes5.get(i));

        this.zeroes5.flip(i);
        assertEquals(false, this.zeroes5.get(i));

        assertThrows(IndexOutOfBoundsException.class, () -> this.zeroes5.flip(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> this.zeroes5.flip(this.zeroes5.size()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 19, 20})
    public void testShiftRight(int i) {
        randomA.shiftRight(i);

        assertEquals(randomAref.size(), randomA.size());

        assertThat(BitGenome::sameSequence,
                randomA.view(0, i),
                BitGenome.zeroes(i)
                );
        assertThat(BitGenome::sameSequence,
                randomA.view(i, randomA.size()),
                randomAref.view(0, randomAref.size()-i)
                );
    }

    @Test
    public void testShiftRight_zero() {
        randomA.shiftRight(0);
        assertThat(BitGenome::sameSequence,
                randomA,
                randomAref
                );
    }

    @Test
    public void testShiftRight_overspill() {
        randomA.shiftRight(randomA.size()*2);

        assertEquals(randomAref.size(), randomA.size());

        assertThat(BitGenome::sameSequence,
                randomA,
                BitGenome.zeroes(randomA.size())
                );
    }

    @Test
    public void testShiftRight_negative() {
        int i=5;
        randomA.shiftRight(-i);

        BitGenome randomfull2 = randomAref.copy();
        randomfull2.shiftLeft(i);

        assertThat(BitGenome::sameSequence,
                randomA,
                randomfull2
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 20})
    public void testShiftLeft(int i) {
        randomA.shiftLeft(i);

        assertEquals(randomAref.size(), randomA.size());

        assertThat(BitGenome::sameSequence,
                randomA.view(0, randomA.size()-i),
                randomAref.view(i, randomAref.size())
                );
        assertThat(BitGenome::sameSequence,
                randomA.view(randomA.size()-i, randomA.size()),
                BitGenome.zeroes(i)
                );

    }

    @Test
    public void testShiftLeft_zero() {
        randomA.shiftLeft(0);
        assertThat(BitGenome::sameSequence,
                randomA,
                randomAref
                );
    }

    @Test
    public void testShiftLeft_overspill() {
        randomA.shiftLeft(randomA.size()*2);

        assertEquals(randomAref.size(), randomA.size());

        assertThat(BitGenome::sameSequence,
                randomA,
                BitGenome.zeroes(randomA.size())
                );
    }

    @Test
    public void testShiftLeft_negative() {
        int i=5;
        randomA.shiftLeft(-i);

        BitGenome randomfull2 = randomAref.copy();
        randomfull2.shiftRight(i);

        assertThat(BitGenome::sameSequence,
                randomA,
                randomfull2
                );
    }

    @Test
    public void testDelete() {
        int a=5;
        int b=10;
        randomA.delete(a, b);

        assertEquals(randomAref.size()-b+a, randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, a),
                randomA.view(0, a)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(b, randomAref.size()),
                randomA.view(a, randomA.size())
                );
    }

    @Test
    public void testDelete_empty() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                randomA.delete(0, 0)
                );
    }

    @Test
    public void testDelete_all() {
        assertThrows(IllegalArgumentException.class, () ->
                randomA.delete(0, randomA.size())
                );
    }

//    @ParameterizedTest
//    @ValueSource(ints = {0, 1, 5, 63, 64, 65, 199})
//    public void testDelete_index0(int len) {
//        randomfull.delete(0, len);
//        assertEquals(randomfullref.size()-len, randomfull.size());
//        assertThat(Genome::sameSequence,
//            randomfullref.view(len, randomfullref.size()),
//            randomfull);
//    }
//
//    @ParameterizedTest
//    @ValueSource(ints = {0, 1, 5, 63, 64, 65})
//    public void testCut_indexi(int len) {
//        int i=5;
//        randomfull.delete(i, len);
//        assertEquals(randomfullref.size()-len, randomfull.size());
//        assertThat(Genome::sameSequence,
//                randomfullref.view(0, i),
//                randomfull.view(0, i)); // Equal piece before cut
//        assertThat(Genome::sameSequence,
//                randomfullref.view(i+len, randomfullref.size()),
//                randomfull.view(i, randomfull.size())); // Equal piece after cut
//    }
//
//    @ParameterizedTest
//    @ValueSource(ints = {0, 1, 5})
//    public void testCut_indextail(int len) {
//        // Cut of 1, index at end
//        int i = randomfullref.size()-len;
//        randomfull.delete(i, len);
//        assertEquals(randomfullref.size()-len, randomfull.size());
//        assertThat(Genome::sameSequence,
//                randomfullref.view(0, i),
//                randomfull.view(0, randomfull.size()));
//    }
//
//    @Test
//    public void testCut_overflow() {
//        // Cut of longer than genome
//        int i = 5;
//        randomfull.delete(i, randomfullref.size()*2);
//        assertEquals(i, randomfull.size());
//        assertThat(Genome::sameSequence,
//                randomfullref.view(0, i),
//                randomfull.view(0, i));
//    }
//
//    @Test
//    public void testCut_whole() {
//        // Cut of whole genome (should leave 1 bit)
//        randomfull.delete(0, randomfull.size());
//        assertEquals(1, randomfull.size());
//        assertEquals(randomfullref.get(randomfullref.size()-1), randomfull.get(0));
//    }

    @Test
    public void testInsert_zeroesandones() {
        zeroes5.insert(3, ones5);
        assertThat(BitGenome::sameSequence,
                BitGenome.of(false, false, false, true, true, true, true, true, false, false),
                zeroes5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 19, 20})
    public void testInsert_expand(int i) {
        // Case where internal array will need to be expanded
        int start = 50;
        int end = 2344;
        randomA.insert(i, randomlong, start, end);
        if (i > 0) assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                randomlong.view(start, end),
                randomA.view(i, i+end-start)
                );
        if (i < randomAref.size()) assertThat(BitGenome::sameSequence,
                randomAref.view(i, randomAref.size()),
                randomA.view(i+end-start, randomA.size())
                );
    }

    @Test
    public void testInsert_self_full() {
        // Insert should work when writing to self
        int i=10;
        randomA.insert(i, randomA);
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                randomAref,
                randomA.view(i, i+randomAref.size())
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i, randomAref.size()),
                randomA.view(i+randomAref.size(), randomA.size())
                );
    }

    @Test
    public void testInsert_self_notfull() {
        int i=10;
        int start=3;
        int end=500;
        BitGenome randomlongref = randomlong.copy();

        randomlong.insert(i, randomlong, start, end);
        assertThat(BitGenome::sameSequence,
                randomlongref.view(0, i),
                randomlong.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                randomlongref.view(start, end),
                randomlong.view(i, i+end-start)
                );
        assertThat(BitGenome::sameSequence,
                randomlongref.view(i, randomlongref.size()),
                randomlong.view(i+end-start, randomlong.size())
                );
    }

    @Test
    public void testInsert_self_view() {
        int i=10;
        int start=3;
        int end=500;

        BitGenome randomlong2 = randomlong.copy();
        randomlong2.insert(i, randomlong, start, end);

        randomlong.insert(i, randomlong.view(start, end));

        assertThat(BitGenome::sameSequence,
                randomlong,
                randomlong2
                );

    }



    @Test
    public void testInsert() {
        BitGenome paste1 = this.zeroes5.copy();
        paste1.insert(3, this.ones5);
        System.out.println(paste1.toString());
        assertThat(BitGenome::sameSequence,
                BitGenome.of(false, false, false, true, true, true, true, true, false, false),
                paste1);

        // Long example to test case where array needs to be expanded
        BitGenome pastelong = this.randomA.copy();
        pastelong.insert(this.randomA.size(), this.randomlong);
        assertThat(BitGenome::sameSequence,
                this.randomA,
                pastelong.view(0, this.randomA.size()));
        assertThat(BitGenome::sameSequence,
                this.randomlong,
                pastelong.view(this.randomA.size(), pastelong.size()));
    }

    @Test
    public void testPaste_inside() {
        int index = 2;
        int gstart = 5;
        int gto = 10;

        randomA.paste(index, randomlong, gstart, gto);
        assertEquals(randomAref.size(), randomA.size());
        assertThat(BitGenome::sameSequence,
                randomA.view(0, index),
                randomAref.view(0, index)
                );
        assertThat(BitGenome::sameSequence,
                randomA.view(index+gto-gstart, randomA.size()),
                randomAref.view(index+gto-gstart, randomA.size())
                );
        assertThat(BitGenome::sameSequence,
                randomA.view(index, index+gto-gstart),
                randomlong.view(gstart, gto)
                );
    }

    @Test
    public void testPaste_overspill() {
        int index = 5;

        randomA.paste(index, randomlong);
        assertThat(BitGenome::sameSequence,
                randomA.view(0, index),
                randomAref.view(0, index)
                );
        assertThat(BitGenome::sameSequence,
                randomA.view(index, randomA.size()),
                randomlong
                );
    }

    @Test
    public void testPaste_self_inside() {
        int index=2;
        int gstart = 5;
        int gto = 10;

        randomA.paste(index, randomA, gstart, gto);

        assertEquals(randomAref.size(), randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, index),
                randomA.view(0, index)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(gstart, gto),
                randomA.view(index, index+gto-gstart)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(index+gto-gstart, randomA.size()),
                randomA.view(index+gto-gstart, randomA.size())
                );
    }

    @Test
    public void testPaste_self_overspill() {
        int index = 5;

        randomA.paste(index, randomA);
        assertThat(BitGenome::sameSequence,
                randomA.view(0, index),
                randomAref.view(0, index)
                );
        assertThat(BitGenome::sameSequence,
                randomA.view(index, randomA.size()),
                randomAref
                );
    }

    @Test
    public void testReplace_samelen() {
        int i=5;

        randomA.replace(i, i+zeroes5.size(), zeroes5);

        assertEquals(randomAref.size(), randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                zeroes5,
                randomA.view(i, i+zeroes5.size())
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i+zeroes5.size(), randomAref.size()),
                randomA.view(i+zeroes5.size(), randomA.size())
                );
    }

    @Test
    public void testReplace_shorter() {
        int i=5;
        int alen = 6;
        int blen = 3;

        randomA.replace(i, i+alen, zeroes5, 0, blen);

        assertEquals(randomAref.size() - alen + blen, randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                zeroes5.view(0, blen),
                randomA.view(i, i+blen)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i+alen, randomAref.size()),
                randomA.view(i+blen, randomA.size())
                );
    }

    @Test
    public void testReplace_longer() {
        int i=5;
        int alen = 3;
        int blen = 5;

        randomA.replace(i, i+alen, zeroes5, 0, blen);

        assertEquals(randomAref.size() - alen + blen, randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                zeroes5.view(0, blen),
                randomA.view(i, i+blen)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i+alen, randomAref.size()),
                randomA.view(i+blen, randomA.size())
                );
    }

    @Test
    public void testReplace_self_samelen() {
        int i=5;
        int j=12;
        int len = 7;

        randomA.replace(i, i+len, randomA, j, j+len);

        assertEquals(randomAref.size(), randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(j, j+len),
                randomA.view(i, i+len)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i+len, randomAref.size()),
                randomA.view(i+len, randomA.size())
                );
    }

    @Test
    public void testReplace_self_shorter() {
        int i=5;
        int j=12;
        int alen = 6;
        int blen = 3;

        randomA.replace(i, i+alen, randomA, j, j+blen);

        assertEquals(randomAref.size() - alen + blen, randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(j, j+blen),
                randomA.view(i, i+blen)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i+alen, randomAref.size()),
                randomA.view(i+blen, randomA.size())
                );
    }

    @Test
    public void testReplace_self_longer() {
        int i=5;
        int j=12;
        int alen = 3;
        int blen = 5;

        randomA.replace(i, i+alen, randomA, j, j+blen);

        assertEquals(randomAref.size() - alen + blen, randomA.size());
        assertThat(BitGenome::sameSequence,
                randomAref.view(0, i),
                randomA.view(0, i)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(j, j+blen),
                randomA.view(i, i+blen)
                );
        assertThat(BitGenome::sameSequence,
                randomAref.view(i+alen, randomAref.size()),
                randomA.view(i+blen, randomA.size())
                );
    }

    @Test
    public void testCopy() {
        BitGenome copy = randomA.copy();

        assertThat(BitGenome::sameSequence,
                randomA,
                copy
                );

        // Changing copy must not modify original
        copy.flip(2);
        assertEquals(!copy.get(2), randomA.get(2));
        copy.delete(0, 5);
        assertEquals(randomA.size()-5, copy.size());
    }

    @Test
    public void testView() {
        BitGenome view = randomA.view();

        assertThat(BitGenome::sameSequence, randomA, view);

        // Changing view should throw an exception.
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, true));

//        // Changing original should make this view unusable
//        // EDIT: this is no longer checked
//        randomA.set(0, true);
//        assertThrows(IllegalStateException.class, () -> view.get(0));
    }

    @Test
    public void testView_copy() {
    	// Copy of a view should not be a view
    	BitGenome view = randomA.view();

    	BitGenome copy = view.copy();

    	assertFalse(copy instanceof BitGenome.View);
    	assertDoesNotThrow(() -> copy.flip(0));
    }

    @Test public void testView_copy_part() {
    	BitGenome view = randomA.view(0,5);
    	BitGenome copy = view.copy(2,4);

    	assertFalse(copy instanceof BitGenome.View);
    	assertDoesNotThrow(() -> copy.flip(0));
    }

    @Test
    public void testReversed_random() {
    	BitGenome rev = randomA.reversedView();

    	for (int i=0; i<randomA.size(); i++) {
    		assertEquals(randomA.get(i), rev.get(randomA.size()-i-1));
    	}
    }

    @Test
    public void testReversed_twice() {
    	BitGenome rev = randomA.reversedView();
    	BitGenome rev2 = rev.reversedView();

    	assertThat(BitGenome::sameSequence, randomA, rev2);
    }

    @Test
    public void testReversed_reverseView() {
    	BitGenome rev = randomA.reversedView();
    	BitGenome view = randomA.view();

    	for (int i=0; i<randomA.size(); i++) {
    		assertEquals(view.get(i), rev.get(randomA.size()-i-1));
    	}
    }

    @Test
    public void testReversed_window() {
    	int start = 5;
    	int end = 17;
    	BitGenome rev = randomA.view(start, end).reversedView();

    	assertThat(BitGenome::sameSequence, rev.reversedView(), randomA.view(start, end));
    }

    @Test
    public void testDecodeIntBase_upto1() {
    	assertEquals(0, BitGenome.readUnsafe("0").decodeIntBase());
    	assertEquals(1, BitGenome.readUnsafe("1").decodeIntBase());
    }

    @Test
    public void testDecodeIntBase_upto7() {
    	assertEquals(0, BitGenome.readUnsafe("000").decodeIntBase());
    	assertEquals(1, BitGenome.readUnsafe("001").decodeIntBase());
    	assertEquals(2, BitGenome.readUnsafe("010").decodeIntBase());
    	assertEquals(3, BitGenome.readUnsafe("011").decodeIntBase());
    	assertEquals(4, BitGenome.readUnsafe("100").decodeIntBase());
    	assertEquals(5, BitGenome.readUnsafe("101").decodeIntBase());
    	assertEquals(6, BitGenome.readUnsafe("110").decodeIntBase());
    	assertEquals(7, BitGenome.readUnsafe("111").decodeIntBase());
    }

    @Test
    public void testDecodeIntBase_toolong() {
    	// Last bit gets ignored (no signed bit)
    	assertEquals(Integer.MAX_VALUE, BitGenome.ones(31).decodeIntBase());
    	assertEquals(Integer.MAX_VALUE, BitGenome.ones(32).decodeIntBase());
    	assertEquals(Integer.MAX_VALUE, BitGenome.ones(33).decodeIntBase());
    }

    @Test
    public void testDecodeIntGray_upto1() {
    	assertEquals(0, BitGenome.readUnsafe("0").decodeIntGray());
    	assertEquals(1, BitGenome.readUnsafe("1").decodeIntGray());
    }

    @Test
    public void testDecodeIntGray_upto7() {
    	assertEquals(0, BitGenome.readUnsafe("000").decodeIntGray());
    	assertEquals(1, BitGenome.readUnsafe("001").decodeIntGray());
    	assertEquals(2, BitGenome.readUnsafe("011").decodeIntGray());
    	assertEquals(3, BitGenome.readUnsafe("010").decodeIntGray());
    	assertEquals(4, BitGenome.readUnsafe("110").decodeIntGray());
    	assertEquals(5, BitGenome.readUnsafe("111").decodeIntGray());
    	assertEquals(6, BitGenome.readUnsafe("101").decodeIntGray());
    	assertEquals(7, BitGenome.readUnsafe("100").decodeIntGray());
    }

    @Test
    public void testDecodeBase_toolong() {
    	// Value of 31x1 in Gray code: 1431655765
    	// Last bit gets ignored (no signed bit)
    	assertEquals(1431655765, BitGenome.ones(31).decodeIntGray());
    	assertEquals(1431655765, BitGenome.ones(32).decodeIntGray());
    	assertEquals(1431655765, BitGenome.ones(33).decodeIntGray());
    }

    @Test
    public void testEncodeBase() {
    	int i=0;
    	assertEquals(i, BitGenome.encodeIntBase(i, 10).decodeIntBase());
    	i=1;
    	assertEquals(i, BitGenome.encodeIntBase(i, 10).decodeIntBase());
    	i=5;
    	assertEquals(i, BitGenome.encodeIntBase(i, 10).decodeIntBase());
    	i=954;
    	assertEquals(i, BitGenome.encodeIntBase(i, 10).decodeIntBase());
    }

    @Test
    public void testEncodeGray() {
    	int i=0;
    	assertEquals(i, BitGenome.encodeIntGray(i, 10).decodeIntGray());
    	i=1;
    	assertEquals(i, BitGenome.encodeIntGray(i, 10).decodeIntGray());
    	i=5;
    	assertEquals(i, BitGenome.encodeIntGray(i, 10).decodeIntGray());
    	i=954;
    	assertEquals(i, BitGenome.encodeIntGray(i, 10).decodeIntGray());
    }

    @Test
    public void testFindFirst_absent() {
    	assertEquals(-1, zeroes5.kmpView().findFirstIn(BitGenome.ones(1)));
    	assertEquals(-1, ones5.kmpView().findFirstIn(BitGenome.zeroes(1)));
    }

    @Test
    public void testFindFirst_present() {
    	assertEquals(0, g11011.kmpView().findFirstIn(BitGenome.ones(1)));
    	assertEquals(0, g11011.kmpView().findFirstIn(BitGenome.ones(2)));
    	assertEquals(2, g11011.kmpView().findFirstIn(BitGenome.zeroes(1)));
    }

    @RepeatedTest(100)
    public void testFindFirst_rand() {
    	while (-1 != randomlong.kmpView().findFirstIn(randomA)) {
    		this.setUpGenomes();
    	}
    	int i=123;
		randomlong.paste(i, randomA);
		assertEquals(i, randomlong.kmpView().findFirstIn(randomA));
    }

    @RepeatedTest(100)
    public void testFindFirst_prefix() {
    	randomA.append(randomB);
    	assertEquals(0, randomA.kmpView().findFirstIn(randomAref));
    }

    @RepeatedTest(100)
    public void testFindFirst_self() {
    	assertEquals(0, randomA.kmpView().findFirstIn(randomA));
    }

    @Test
    public void testFindAll_absent1() {
    	assertEquals(0, zeroes5.kmpView().findAllOverlappingIn(BitGenome.ones(1)).count());
    }

    @RepeatedTest(100)
    public void testFindAll_rand() {
    	while (-1 != randomlong.kmpView().findFirstIn(randomA)) {
    		this.setUpGenomes();
    	}
    	int i1=123;
    	int i2=567;
    	randomlong.paste(i1, randomA);
    	randomlong.paste(i2, randomA);

    	List<Integer> found = randomlong.kmpView().findAllOverlappingIn(randomA).boxed().collect(Collectors.toList());
    	assertEquals(2, found.size()); // It's technically possible that another copy of randomA is added when pasting
    	assertEquals(i1, found.get(0));
    	assertEquals(i2, found.get(1));
    }


}
