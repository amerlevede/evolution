package main.permutation;

import java.util.List;
import java.util.Optional;

import crossover.CloningCross;
import crossover.CrossoverRule;
import crossover.permutation.CycleCross;
import crossover.permutation.EdgePreservingOptimalCross;
import crossover.permutation.EdgeRecombinationCross;
import crossover.permutation.PerfectEdgeCross;
import crossover.permutation.SymmetricEdgePreservingCross;
import fitness.FitnessFunction;
import fitness.StringMatch;
import fitness.TravellingSalesman;
import genome.integer.IntGenome;
import genome.integer.IntegerGenome;
import genome.integer.Permutation;
import main.CommandLineInterface;
import mutation.MutationOp;
import mutation.MutationRule;
import mutation.permutation.GrayFlip;
import mutation.permutation.RandomFlip;
import mutation.string.TransInv;
import mutation.string.Translocation;
import util.DiscreteDistribution;
import util.DoublePair;

/**
 * Class encapsulating shared properties of command line utilities working with integer type genomes (Permutations in particular).
 * 
 * @author adriaan
 *
 * @param <G>
 */
public abstract class PermutationCommandLineInterface<G extends IntegerGenome<G>> extends CommandLineInterface {

	public PermutationCommandLineInterface(String[] args) {
		super(args);
	}

	public Option<Integer> grayflips = option("grayflips", 0, Integer::valueOf);
	public 	Option<Integer> randflips = option("randflips", 1, Integer::valueOf);
	public Option<Integer> translocs = option("translocations", 0, Integer::valueOf);
	public Option<Integer> transinvs = option("transinvs", 0, Integer::valueOf);

	public 	Option<MutationRule<G>> mutation = optionWithStringDefault("mutationtype", "uniform", optionValue -> {
		switch (optionValue) {
		case "exact":
			return readMutationExact();
		case "uniform":
			return readMutationUniform();
		default:
			throw new IllegalArgumentException();
		}
	});

	public MutationRule<G> readMutationUniform() {
		int grayflips = this.grayflips.read();
		int randflips = this.randflips.read();
		int translocs = this.translocs.read();
		int transinvs = this.transinvs.read();

		return (rng) -> {
			MutationOp<G> grayflip = GrayFlip.<G>repeatN(r -> g -> DiscreteDistribution.getUniform(rng, 0, grayflips+1)).apply(rng);
			MutationOp<G> randflip = RandomFlip.<G>repeatN(g -> r -> DiscreteDistribution.getUniform(rng, randflips==0?0:1, randflips+1)).apply(rng);
			MutationOp<G> transloc = Translocation.<G>withSize(g -> DiscreteDistribution.uniform(1, g.size()/4)).repeatN(DiscreteDistribution.getUniform(rng, 0, translocs+1)).apply(rng);
			MutationOp<G> transinv = TransInv.<G>withSize(g -> DiscreteDistribution.uniform(1, g.size()/4)).repeatN(DiscreteDistribution.getUniform(rng, 0, transinvs+1)).apply(rng);

			return (g, stats) -> {
				grayflip.accept(g, stats);
				randflip.accept(g, stats);
				transloc.accept(g, stats);
				transinv.accept(g, stats);
			};
		};
	}

	public MutationRule<G> readMutationExact() {
		int grayflips = this.grayflips.read();
		int randflips = this.randflips.read();
		int translocs = this.translocs.read();
		int transinvs = this.transinvs.read();

		return (rng) -> {
			MutationOp<G> grayflip = GrayFlip.<G>repeatN(g -> r -> grayflips).apply(rng);
			MutationOp<G> randflip = RandomFlip.<G>repeatN(g -> r -> randflips).apply(rng);
			MutationOp<G> transloc = Translocation.<G>withSize(g -> DiscreteDistribution.uniform(1, g.size()/4)).repeatN(translocs).apply(rng);
			MutationOp<G> transinv = TransInv.<G>withSize(g -> DiscreteDistribution.uniform(1, g.size()/4)).repeatN(transinvs).apply(rng);

			return (g, stats) -> {
				grayflip.accept(g, stats);
				randflip.accept(g, stats);
				transloc.accept(g, stats);
				transinv.accept(g, stats);
			};
		};
	}

	public Option<Integer> size = option("size", optionValue -> {
		switch (optionValue) {
		case "tsplib":
			Optional<String> salesmanproblem = this.readArg("cities");
			boolean cyclic = this.cyclic.read();
			if (salesmanproblem.isPresent()) {
				// Add 1 to size if non-cyclic, so that symbol 0 can signify start of trajectory
				return TravellingSalesman.tsplib_problemSize(salesmanproblem.get()) + (cyclic ? 0 : 1);
			} else {
				throw new IllegalArgumentException("Must specify tsplib problem (cities=...) when using tsplib as problem size");
			}
		default:
			return Integer.valueOf(optionValue);
		}});

	public Option<Integer> maxtries = option("maxtries", 128, Integer::valueOf);

	public Option<CrossoverRule<G>> crossover = option("crossover", optionValue -> {
		switch (optionValue.toLowerCase()) {
		case "cloning":
		case "nox": {
			return CloningCross.crossover();
		}
		case "cycle":
		case "cx": {
			return CycleCross.crossover();
		}
		case "edgepreserving":
		case "epx": {
			return PerfectEdgeCross.crossover(i->10000);
		}
		case "epx_loud":
			return PerfectEdgeCross.loudCrossover();
		case "edgepreservingoptimal":
		case "epox": {
			TravellingSalesman.DistanceFunction cities = this.cities.read();
			int maxtries = this.maxtries.read();
			return EdgePreservingOptimalCross.crossover(cities, i->maxtries);
		}
		case "epox_loud": {
			TravellingSalesman.DistanceFunction cities = this.cities.read();
			return EdgePreservingOptimalCross.loudCrossover(cities);
		}
		case "symmetricedgepreserving":
		case "sepx": {
			return SymmetricEdgePreservingCross.crossover(10000);
		}
		case "sepx_loud": {
			return SymmetricEdgePreservingCross.loudCrossover();
		}
		case "edgerecombination":
		case "erx": {
			return EdgeRecombinationCross.crossover();
		}
		default:
			throw new IllegalArgumentException(optionValue+" is not a valid option for crossover.");
		}
	});

	public Option<IntGenome> target = autoOption(() -> IntGenome.getRandomPermutation(this.rng.read(), this.size.read()));
	public Option<Boolean> cyclic = option("cyclic", true, Boolean::valueOf);
	public Option<TravellingSalesman.DistanceFunction> cities = option("cities", optionValue -> {
		boolean cyclic = this.cyclic.read();
		switch (optionValue) {
		case "random":
			if (cyclic) {
				List<DoublePair> cities = TravellingSalesman.randomCities(this.rng.read(), this.size.read());
				return TravellingSalesman.DistanceFunction.fromCoords(cities);
			} else {
				// If problem is not meant to be cyclic, use 1 less city, so that 0 is reserved for the start of the trajectory
				List<DoublePair> cities = TravellingSalesman.randomCities(this.rng.read(), this.size.read() - 1);
				return TravellingSalesman.DistanceFunction.makeCyclic(TravellingSalesman.DistanceFunction.fromCoords(cities));
			}
		case "grid":
			int size = this.size.read();
			int dim = (int)Math.ceil(Math.sqrt(size));
			List<DoublePair> cities = TravellingSalesman.grid(dim);
			return cyclic
					? TravellingSalesman.DistanceFunction.fromCoords(cities)
					: TravellingSalesman.DistanceFunction.makeCyclic(TravellingSalesman.DistanceFunction.fromCoords(cities));
		default:
			if (cyclic) {
				// For TSPlib problems, number of cities is fixed, so increase genome size by 1 instead
				return TravellingSalesman.tsplib_distance(optionValue);
			} else {
				return TravellingSalesman.DistanceFunction.makeCyclic(TravellingSalesman.tsplib_distance(optionValue));
			}
		}
	});



	public Option<FitnessFunction<G>> fitness = option("fitness", optionValue -> {
		switch (optionValue) {
		case "stringKT": {
			IntGenome target = this.target.read();
			return StringMatch.of(target, (a,b) -> -Permutation.kendallTau(a.permutationView(), b.permutationView()));
		}
//		case "stringedges1": {
//			IntGenome target = this.target.read();
//			return StringMatch.of(target, (a, b) -> {
//				Permutation order = Permutation.fromMapping(a, b);
//				int score = 0;
//				for (int i=0; i<order.size()-1; i++) {
//					if (order.pi(i)+1 == order.pi(i+1)) score++;
//				}
//				return -order.size()+1+score;
//			});
//		}
//		case "stringedges2": {
//			IntGenome target = this.target.read();
//			return StringMatch.of(target, (a, b) -> {
//				Permutation order = Permutation.fromMapping(a, b);
//				int score = 0;
//				for (int i=0; i<order.size()-1; i++) {
//					if (order.pi(i)+1 == order.pi(i+1)
//					|| order.pi(i)-1 == order.pi(i+1)) score++;
//				}
//				return -2*order.size()+2+score;
//			});
//		}
		case "salesman":
		case "tsp": {
			TravellingSalesman.DistanceFunction cities = this.cities.read();
			return TravellingSalesman.cyclic(cities, 0);
		}
		default:
			throw new IllegalArgumentException(optionValue+" is not a valid option for fitness.");
		}
	});

}
