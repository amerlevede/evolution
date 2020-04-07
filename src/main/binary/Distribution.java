package main.binary;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import main.CommandLineInterface;
import util.ContinuousDistribution;
import util.DiscreteDistribution;

/**
 * Generate values and statistics from distributions. 
 * 
 * @author adriaan
 */
public class Distribution extends CommandLineInterface {
	
	public Distribution(String[] args) {
		super(args);
	}
	
	public final Option<Double> power = option("power",Double::valueOf);
	public final Option<Integer> support = option("support",Integer::valueOf);
	public final Option<Double> p = option("p",Double::valueOf);
	public final Option<Double> lambda = option("lambda", Double::valueOf);
	public final Option<Integer> target = option("target", Integer::valueOf);
			
	public final Option<DiscreteDistribution> distribution = option(
			"type",
			(optionValue) -> {
				switch (optionValue.toLowerCase()) {
				case "powerlaw":
					return ContinuousDistribution.powerLaw(this.power.read(), 1, this.support.read()).round();
				case "binomial":
					return DiscreteDistribution.binomial(this.support.read(), this.p.read());
				case "binomialtimespowerlaw":
					return ContinuousDistribution.powerLaw(this.power.read(), 1, this.support.read()).round()
							.sumTimes(DiscreteDistribution.binomial(this.support.read(), this.p.read()));
				case "poisson":
					return DiscreteDistribution.poisson(this.lambda.read());
				case "exponential":
					return ContinuousDistribution.exponential(1/this.lambda.read()).floor();
				case "exponentialtimespowerlaw":
					return ContinuousDistribution.powerLaw(this.power.read(), 1, this.support.read()).round()
							.sumTimes(ContinuousDistribution.exponential(1/this.lambda.read()).floor());
				default:
					throw new IllegalArgumentException();
			}});
	
	public final Option<Function<IntStream,String>> stat = optionWithStringDefault(
			"stat",
			"mean",
			(optionValue) -> {
				switch (optionValue) {
				case "mean": {
					int cycles = this.cycles.read();
					return (stream) -> {
						return String.valueOf(stream.limit(cycles).mapToDouble(Double::valueOf).average().orElse(Double.NaN));
					};
				}
				case "histogram": {
					int support = this.support.read();
					int cycles = this.cycles.read();
					return (stream) -> {
						int[] histo = new int[support+1];
						stream.limit(cycles).forEach((i) -> { if (i < histo.length) histo[i]++; });
						return Arrays.toString(histo);
					};
				}
				case "until": {
					int target = this.target.read();
					return (stream) -> {
						return String.valueOf(stream.takeWhile(x -> x != target).count());
					};
				}
				default:
					throw new IllegalArgumentException();
			}});
	
	@Override
	public void run(boolean dryrun) {
		DiscreteDistribution distribution = this.distribution.read();
		long seed = this.seed.read();
		Function<IntStream,String> stat = this.stat.read();
		
		warnUnusedVariables();
		
		if (!dryrun) {
			IntStream stream = LongStream
					.iterate(seed, (s) -> new Random(s).nextLong())
					.mapToInt((s) -> distribution.applyAsInt(new Random(s)));
			this.println(stat.apply(stream));
		}
	}

}
