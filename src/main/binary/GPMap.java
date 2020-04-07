package main.binary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import fitness.FitnessFunction;
import fitness.TravellingSalesman;
import fitness.binary.SumFunction;
import genome.LinearGenome;
import genome.LinearGenome.KMP;
import genome.binary.BitGenome;
import genome.gene.Gene;
import genome.gene.NumberGene;
import genome.gene.Triangle;
import util.CategoricalDistribution;
import util.DoublePair;
import util.Functional;

/**
 * Command line utility to inspect fitness functions and their calculations for a given genome.
 * 
 * @author adriaan
 *
 */
public class GPMap extends BinaryCommandLineInterface<BitGenome> {

	public GPMap(String[] args) {
		super(args);
	}

	public final Option<String> type = option("type", s->s);

	public final Option<Function<BitGenome,String>> gpmap = option("fitness", (optionValue) -> {
		switch (optionValue) {
		case "triangles":
		case "triangles_const":
		case "triangles_mean":
		case "triangles_meanpositioned":
		case "triangles_meanwithoutcenter": {
			String type = this.type.read();
			switch (type) {
			case "genes": {
				KMP<BitGenome> consensus = consensusSequence.read().kmpView();
				return (g) -> Gene.Reader
							.scan(consensus, g, optionValue == "triangles_meanwithoutcenter" ? Triangle::readWithCenterFromPosition : Triangle::read)
							.map(Gene::toString)
							.collect(Collectors.joining("\n"));
			}
			case "values": {
				KMP<BitGenome> consensus = consensusSequence.read().kmpView();
				return (g) -> {
					DoubleUnaryOperator f = SumFunction.genomeToSumFunction(consensus, Triangle::read, g);
					return DoubleStream
						.iterate(0,(x) -> x+0.01).limit(101) // range in [0,1]
						.map(f)
						.mapToObj(String::valueOf).collect(Collectors.joining(","));
				};
			}
			}
		}
		case "substrings": {
			int targetLength = this.targetLength.read();
			int targetAmount = this.targetAmount.read();
			long seed = this.seed.read();
			CategoricalDistribution<? extends BitGenome> targetGenerator = BitGenome.random(targetLength);
			Collection<KMP<BitGenome>> targets = Functional
					.randoms(seed)
					.map(targetGenerator::apply)
					.distinct()
					.map(LinearGenome::kmpView)
					.limit(targetAmount)
					.collect(Collectors.toList());
			return (g) -> targets.stream()
					.filter(g::contains)
					.map((seq) -> seq.toString() + "\t" + g.findAllOverlapping(seq).mapToObj(String::valueOf).collect(Collectors.joining(",")))
					.collect(Collectors.joining("\n"));
		}
		case "salesman": {
			LinearGenome.KMP<BitGenome> consensus = this.consensusSequence.read().kmpView();
			int n = this.targetAmount.read();

			List<DoublePair> cities = TravellingSalesman.randomCities(new Random(this.seed.read()), n);
			int digits = (int)Math.ceil(Math.log(n) / Math.log(2));
			Gene.Reader<BitGenome,Integer> reader = NumberGene.readIntWithDigits(digits);

			return (g) -> {
				List<Integer> visits = Gene.Reader.scan(consensus, g, reader)
						.map(Gene::getValue)
						.filter(v->v<n)
						.collect(Collectors.toList());
				StringBuilder result = new StringBuilder();
				result.append("Cities: ");
				result.append(cities);
				result.append("\n");
				result.append("Visits: ");
				result.append(visits);
				result.append("\n");
				result.append("Locations: ");
				result.append(visits.stream().map(i->cities.get(i)).collect(Collectors.toList()));
				return result.toString();
			};
		}
		case "salesman_tags": {
			Random rng = new Random(this.seed.read());
			int targetLength = this.targetLength.read();
			List<LinearGenome.KMP<BitGenome>> targets = this.targets.read();
			int n = this.targetAmount.read();
			List<DoublePair> cities = TravellingSalesman.randomCities(rng, n);

			return (g) -> {
				List<Integer> genes = targets.stream().flatMapToInt(g::findAllOverlapping).boxed().collect(Collectors.toCollection(ArrayList::new));
				log(targets.toString());
				log(genes.toString());
				genes.sort(Integer::compare);
				List<Integer> visits = genes.stream()
						.map(i -> IntStream.range(0, n).filter(j->targets.get(j).sameSequence(g.view(i,i+targetLength))).findFirst().getAsInt())
						.collect(Collectors.toCollection(ArrayList::new));;
				log(visits.toString());
				StringBuilder result = new StringBuilder();
				result.append("Cities: ");
				result.append(cities);
				result.append("\n");
				result.append("Visits: ");
				result.append(visits);
				result.append("\n");
				result.append("Locations: ");
				result.append(visits.stream().map(i->cities.get(i)).collect(Collectors.toList()));
				return result.toString();
			};

		}
		default:
			throw new IllegalArgumentException();
		}
	});

	public final Option<BitGenome> genome = option("genome", g->BitGenome.read(g).get());

	@Override
	public void run(boolean dryrun) {
		BitGenome g = genome.read();
		Function<BitGenome,String> gp = gpmap.read();
		FitnessFunction<BitGenome> ff = this.uncorrectedFitness.read();
		FitnessFunction<BitGenome> ffc = this.fitness.read();

		warnUnusedVariables();

		if (!dryrun) {
			println("fitness before length penalty: " + ff.applyAsDouble(g));
			println("fitness after  length penalty: " + ffc.applyAsDouble(g) + " (genome length " + g.size() + ")");
			println("phenotype:");
			println(gp.apply(g));
		}
	}

}
