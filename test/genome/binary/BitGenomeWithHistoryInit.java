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
import util.DiscreteDistribution;

/**
 *
 * @author adriaan
 */
public abstract class BitGenomeWithHistoryInit extends RandomInit {

    public BitGenomeWithHistory zeroes5;
    public BitGenomeWithHistory zeroes5ref;

    public BitGenomeWithHistory ones5;
    public BitGenomeWithHistory ones5ref;

    public BitGenomeWithHistory g11011;
    public BitGenomeWithHistory g11011ref;

    public BitGenomeWithHistory zero1;
    public BitGenomeWithHistory zero1ref;

    public BitGenomeWithHistory random5A;
    public BitGenomeWithHistory random5B;
    public BitGenomeWithHistory random5Aref;
    public BitGenomeWithHistory random5Bref;

    public BitGenomeWithHistory randomA;
    public BitGenomeWithHistory randomB;
    public BitGenomeWithHistory randomAref;
    public BitGenomeWithHistory randomBref;

    public BitGenomeWithHistory random1000A;
    public BitGenomeWithHistory random1000Aref;
    public BitGenomeWithHistory random1000B;
    public BitGenomeWithHistory random1000Bref;

    public BitGenomeWithHistory randomlong;
    public BitGenomeWithHistory randomlongref;

    public List<BitGenomeWithHistory> cases;

    @BeforeEach
    public void setUpGenomes() {
        zeroes5 = BitGenomeWithHistory.zeroes(5);
        zeroes5ref = zeroes5.copy().view();

        ones5 = BitGenomeWithHistory.of(true, true, true, true, true);
        ones5ref = ones5.copy().view();

        g11011 = BitGenomeWithHistory.of(BitGenome.readUnsafe("11011"));
        g11011ref = g11011.copy().view();

        zero1 = BitGenomeWithHistory.of(BitGenome.readUnsafe("0"));
        zero1ref = zero1.copy().view();

        random5A = BitGenomeWithHistory.random(5).apply(rng);
        random5Aref = random5A.copy().view();

        random5B = BitGenomeWithHistory.random(5).apply(rng);
        random5Bref = random5B.copy().view();

        randomA = BitGenomeWithHistory.random(DiscreteDistribution.uniform(5,1000)).apply(rng);
        randomA.append(BitGenomeWithHistory.random(20).apply(rng));
        randomAref = randomA.copy().view();

        randomB = BitGenomeWithHistory.random(DiscreteDistribution.uniform(5,1000)).apply(rng);
        randomBref = randomB.copy().view();

        random1000A = BitGenomeWithHistory.random(1000).apply(rng);
        random1000Aref = random1000A.copy().view();

        random1000B = BitGenomeWithHistory.random(1000).apply(rng);
        random1000Bref = random1000B.copy().view();

        randomlong = BitGenomeWithHistory.random(50000).apply(rng);
        randomlongref = randomlong.copy().view();

        this.cases = List.of(zeroes5, ones5, g11011, zero1, randomA, randomB, randomlong);
    }

    @Test // Not really
    public void printRandoms() {
        System.out.println("randomA: " + randomA);
        System.out.println("randomB: " + randomB);
    }

}
