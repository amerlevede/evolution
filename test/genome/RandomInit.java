/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genome;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import util.Functional;

/**
 *
 * @author adriaan
 */
public abstract class RandomInit {
    
    public static long seed;
    public static Random rng;

    @BeforeAll
    public static void setUpSeed() {
        seed = new Random().nextLong();
        rng = new Random(seed); // Should really be non-static and in a @BeforeEach, so that each method gains the same rng seed, but that means @RepeatedTest always runs the same result
    }
    
    public Stream<Random> randoms() {
    	return Functional.randoms(rng);
    }
    
    @Test // Not really
    public void printSeed() {
//    	seed = -8719923383084951379L;
//    	rng = new Random(seed);
//    	System.err.println("Warning: fixed random seed");
        System.out.println("Random seed: " + seed);
    }

    public static <A, B> void assertThat(BiPredicate<A, B> f, A a, B b) {
        assertTrue(f.test(a, b));
    }
    
}
