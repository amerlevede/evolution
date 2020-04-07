/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genome.binary;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import genome.RandomInit;
import genome.binary.BitGenome;

/**
 *
 * @author adriaan
 */
public abstract class BitGenomeInit extends RandomInit {
    
    public BitGenome zeroes5;
    public BitGenome zeroes5ref;
    
    public BitGenome ones5;
    public BitGenome ones5ref;
    
    public BitGenome g11011;
    public BitGenome g11011ref;
    
    public BitGenome zero1;
    public BitGenome zero1ref;
    
    public BitGenome random5A;
    public BitGenome random5B;
    public BitGenome random5Aref;
    public BitGenome random5Bref;
    
    public BitGenome randomA;
    public BitGenome randomB;
    public BitGenome randomAref;
    public BitGenome randomBref;
    
    public BitGenome random1000A;
    public BitGenome random1000Aref;
    public BitGenome random1000B;
    public BitGenome random1000Bref;
    
    public BitGenome randomlong;
    public BitGenome randomlongref;
    
    public List<BitGenome> cases;

    @BeforeEach
    public void setUpGenomes() {
        zeroes5 = BitGenome.zeroes(5);
        zeroes5ref = zeroes5.copy().view();
        
        ones5 = BitGenome.of(true, true, true, true, true);
        ones5ref = ones5.copy().view();
        
        g11011 = BitGenome.readUnsafe("11011");
        g11011ref = g11011.copy().view();
        
        zero1 = BitGenome.readUnsafe("0");
        zero1ref = zero1.copy().view();
        
        random5A = BitGenome.random(5).apply(rng);
        random5Aref = random5A.copy().view();
        
        random5B = BitGenome.random(5).apply(rng);
        random5Bref = random5B.copy().view();
       
        // Since genomes normally have extra buffer in their internal array, array index problems may be hidden.
        // randomfull should be the default test case to avoid this
        randomA = BitGenome.random(20).apply(rng);
        randomA.append(BitGenome.random(20).apply(rng));
        // "Reference" value for randomfull to check original sequence
        randomAref = randomA.copy().view();
        
        randomB = BitGenome.random(30).apply(rng);
        randomBref = randomB.copy().view();
        
        random1000A = BitGenome.random(1000).apply(rng);
        random1000Aref = random1000A.copy().view();
        
        random1000B = BitGenome.random(1000).apply(rng);
        random1000Bref = random1000B.copy().view();
        
        randomlong = BitGenome.random(50000).apply(rng);
        randomlongref = randomlong.copy().view();
        
        this.cases = List.of(zeroes5, ones5, g11011, zero1, randomA, randomB, randomlong);
    }
    
    @Test // Not really
    public void printRandoms() {
        System.out.println("randomA: " + randomA);
        System.out.println("randomB: " + randomB);
    }
    
}
