package main.binary;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import alignment.AlignmentRule;
import alignment.Global;
import alignment.Glocal;
import alignment.Local;
import alignment.VarOAlignmentRule;
import alignment.algorithms.GreedyGlocal;
import crossover.CloningCross;
import crossover.CrossoverRule;
import crossover.MessyCross;
import crossover.binary.GlobalAlignmentCross;
import crossover.binary.GlocalAlignmentCross;
import crossover.binary.SynapsingCross;
import fitness.FitnessFunction;
import fitness.MeanFunction;
import fitness.StringMatch;
import fitness.Substrings;
import fitness.binary.SumFunction;
import genome.LinearGenome;
import genome.LinearGenome.KMP;
import genome.binary.BinaryGenome;
import genome.binary.BitGenome;
import genome.binary.BitGenomeWithHistory;
import genome.gene.Triangle;
import genome.integer.IntGenome;
import main.CommandLineInterface;
import mutation.MutationOp;
import mutation.MutationRule;
import mutation.binary.InDel;
import mutation.binary.PointMutation;
import mutation.string.Translocation;
import util.CategoricalDistribution;
import util.ContinuousDistribution;
import util.DiscreteDistribution;
import util.Functional;

/**
 * Class encapsulating shared properties of command line utilities that work with binary genomes. 
 * 
 * @author adriaan
 *
 * @param <G>
 */
public abstract class BinaryCommandLineInterface<G extends BinaryGenome<G>> extends CommandLineInterface {

	public BinaryCommandLineInterface(String[] args) {
		super(args);
	}

	public final Option<CrossoverRule.N> crossoverN = option("crossoverN", CrossoverRule.N.UNIFORM, CrossoverRule.N::valueOf);
	public final Option<Integer> synapseSize    = option("synapseSize",    10, Integer::valueOf);
	public final Option<Integer> scoreMatch     = option("scoreMatch",      1, (s) -> Integer.valueOf(s));
	public final Option<Integer> scoreMismatch  = option("scoreMismatch",  -5, (s) -> -Math.abs(Integer.valueOf(s)));
	public final Option<Integer> scoreGapOpen   = option("scoreGapOpen",  -20, (s) -> -Math.abs(Integer.valueOf(s)));
	public final Option<Integer> scoreGapExtend = option("scoreGapExtend", -3, (s) -> -Math.abs(Integer.valueOf(s)));

	public final Option<CrossoverRule<G>> crossover = option(
			"crossover",
			(optionValue) -> {
				switch (optionValue) {
				case "cloning":
					return CloningCross.crossover();
				case "mutate":
					MutationRule<G> mutation = this.mutation.read();
					return CloningCross.<G>crossover().andThenMutate(mutation);
				case "messy":
					return MessyCross.of(
							crossoverN.read()
							);
				case "onegap":
					return GlobalAlignmentCross.<G>of(
							Global.oneGap(),
							crossoverN.read()
							);
				case "synapsing":
//					// Carl version
//					return AlignmentCross.of(
//							GlobalAlignment.repeatedLocal(LocalAlignment.longestCommonSubstring(), this.synapseSize.read()),
//							crossoverN.read()
//							);
					return SynapsingCross.of(
							synapseSize.read(),
							crossoverN.read()
							);
				case "synapsing_general": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int scoreGapOpen = this.scoreGapOpen.read();
					int scoreGapExtend = this.scoreGapExtend.read();
					int synapseSize = this.synapseSize.read();
					AlignmentRule<G> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
					return SynapsingCross.Generalized.of(
							localAlign,
							synapseSize,
							crossoverN.read()
							);
				}
//				case "crisscross_lcss": {
//					int synapseSize = this.synapseSize.read();
//					return CrissCross.<G>of(
//							Local.longestCommonSubstring(),
//							synapseSize,
//							crossoverN.read());
//					}
//				case "crisscross": {
//					int scoreMatch = this.scoreMatch.read();
//					int scoreMismatch = this.scoreMismatch.read();
//					int scoreGapOpen = this.scoreGapOpen.read();
//					int scoreGapExtend = this.scoreGapExtend.read();
//					int synapseSize = this.synapseSize.read();
//					AlignmentRule<G> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
//					return CrissCross.of(
//							localAlign,
//							synapseSize,
//							crossoverN.read());
//				}
				case "greedyglocal": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int synapseSize = this.synapseSize.read();
					return GlocalAlignmentCross.Unsegmented.<G>of(
							GreedyGlocal.alignment(scoreMatch, scoreMismatch, synapseSize),
							crossoverN.read());
				}
//				case "randomlocal": {
//					int scoreMatch = this.scoreMatch.read();
//					int scoreMismatch = this.scoreMismatch.read();
//					int scoreGapOpen = this.scoreGapOpen.read();
//					int scoreGapExtend = this.scoreGapExtend.read();
//					AlignmentRule<G> localAlign = Local.alignmentAroundRandomWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
//					return LocalAlignmentCross.of(localAlign, crossoverN.read());
//				}
//				case "nocrisscross": {
//					int scoreMatch = this.scoreMatch.read();
//					int scoreMismatch = this.scoreMismatch.read();
//					int scoreGapOpen = this.scoreGapOpen.read();
//					int scoreGapExtend = this.scoreGapExtend.read();
//					int synapseSize = this.synapseSize.read();
//					AlignmentRule<G> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
//					return NoCrissCross.of(
//							localAlign,
//							synapseSize,
//							crossoverN.read());
//				}
//				case "closurecross": {
//					int scoreMatch = this.scoreMatch.read();
//					int scoreMismatch = this.scoreMismatch.read();
//					int scoreGapOpen = this.scoreGapOpen.read();
//					int scoreGapExtend = this.scoreGapExtend.read();
//					int synapseSize = this.synapseSize.read();
//					AlignmentRule<G> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
//					VarOAlignmentRule<G> glocalAlign = Global.unorderedRepeatedLocal(localAlign, synapseSize);
//					return GlocalAlignmentCross.General.of(
//							glocalAlign,
//							crossoverN.read());
//				}
				case "global": {
					return GlobalAlignmentCross.<G>of(
							Global.alignmentWithAffineGapScore(scoreMatch.read(), scoreMismatch.read(), scoreGapOpen.read(), scoreGapExtend.read()),
							crossoverN.read()
							);
				}
				case "glocal_lcss": {
					int synapseSize = this.synapseSize.read();
					AlignmentRule<G> localAlign = Local.longestCommonSubstring();
					VarOAlignmentRule<G> glocalAlign = Glocal.unorderedRepeatedLocal(localAlign, synapseSize);
					return GlocalAlignmentCross.Unsegmented.<G>of(
							glocalAlign,
							crossoverN.read()
							);
				}
				case "glocal": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int scoreGapOpen = this.scoreGapOpen.read();
					int scoreGapExtend = this.scoreGapExtend.read();
					int synapseSize = this.synapseSize.read();
					AlignmentRule<G> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
					VarOAlignmentRule<G> glocalAlign = Glocal.unorderedRepeatedLocal(localAlign, synapseSize);
					return GlocalAlignmentCross.Unsegmented.<G>of(
							glocalAlign,
							crossoverN.read()
							);
				}
				default:
					throw new IllegalArgumentException();
				}
			});

	public final Option<Double> indelRate = option("indelRate", 0.002, Double::valueOf);
	public final Option<Double> snpRate   = option("snpRate",   0.002, Double::valueOf);
	public final Option<Double> transRate = option("transRate", 0.,    Double::valueOf);
	public final Option<Double> indelPower = option("indelPower", -1.5, (s) -> -Math.abs(Double.valueOf(s)));
	public final Option<Double> transPower = option("transPower", -1.5, (s) -> -Math.abs(Double.valueOf(s)));
	public final Option<Boolean> indelBalanced = option("indelBalanced", false, Boolean::valueOf);
	public final Option<Integer> indelMinSize = option("indelMinSize", 1, Integer::valueOf);
	public final Option<Integer> transMinSize = option("transMinSize", 1, Integer::valueOf);

	/**
	 * Specify a mutation rule where the fractions of the genome specified by the rates of the different mutation types are exact, e.g. exactly snpRate*genome.size() of each genome will be mutated by snps.
	 */
	public MutationRule<G> readMutationExact() {
		double snpRate = this.snpRate.read();
		double transRate = this.transRate.read();
		double indelRate = this.indelRate.read();
		double indelPower = this.indelPower.read();
		double transPower = this.transPower.read();
		boolean indelBalanced = this.indelBalanced.read();
		int indelMinSize = this.indelMinSize.read();
		int transMinSize = this.transMinSize.read();

		Function<G,DiscreteDistribution> indelSizeDist =
				indelPower < -100 
					? (g) -> (rng) -> indelMinSize
					: (g) -> ContinuousDistribution.powerLaw(indelPower, g.size() > indelMinSize*2 ? indelMinSize : 1, g.size()+1).round();
		Function<G,DiscreteDistribution> transSizeDist =
				transPower < -100
					? (g) -> (rng) -> transMinSize
					: (g) -> ContinuousDistribution.powerLaw(transPower, g.size() > transMinSize*2 ? transMinSize : 1, g.size()+1).round();

		return (rng) -> (g, stats) -> {
			int targetIndel = DiscreteDistribution.getRandomRound(rng, indelRate * (g.size()));
			if (indelBalanced) targetIndel = targetIndel / 2; // Balanced indel performs both and thus has twice the mutation strength
			if (targetIndel < 5 && g.size() > 10) targetIndel = 0;
			int targetTrans = DiscreteDistribution.getRandomRound(rng, transRate * (g.size()));
			if (targetTrans < 5 && g.size() > 10) targetTrans = 0;
			int targetSnps  = DiscreteDistribution.getRandomRound(rng, snpRate   * (g.size()));

			// Reconstruct sequence of indels by generating them until reaching desired fraction of affected bits
			int affectedIndel = targetIndel-1; // set to any value not targetIndel
			List<Integer> indelSizes = null;
			while (affectedIndel != targetIndel) {
				indelSizes = new LinkedList<>();

//				nIndel = DiscreteDistribution.getBinomial(rng, g.size(), indelRate / avgIndelSize);

				affectedIndel = 0;
				while (affectedIndel < targetIndel) {
					int indelSize = indelSizeDist.apply(g).applyAsInt(rng);
					indelSizes.add(indelSize);
					affectedIndel += indelSize;
				}
			}
			int nIndel = indelSizes.size();

			// Reconstruct sequence of translocations by generating them until reaching desired fraction of affected bits
			int affectedTrans = targetTrans -1; // set to any value not targetIndel
			List<Integer> transSizes = null;
			while (affectedTrans != targetTrans) {
				transSizes = new LinkedList<>();

				affectedTrans= 0;
				while (affectedTrans < targetTrans) {
					int transSize = transSizeDist.apply(g).applyAsInt(rng);
					transSizes.add(transSize);
					affectedTrans += transSize;
				}
			}
			int nTrans = transSizes.size();

			// Reconstruct sequence of snps (interleaved with indels and translocations) affecting desired fraction of bits
			int[] snpSizes = new int[nTrans + nIndel + 1];
			int affectedSnps = 0;
			for (int i=0; i<nTrans+nIndel+1; i++) {
				int affectedSnp = DiscreteDistribution.getBinomial(rng, targetSnps - affectedSnps, 1./(nTrans+nIndel+1-i));
				snpSizes[i] = affectedSnp;
				affectedSnps += affectedSnp;
			}

			// Perform mutation
			int iSnp = 0;
			int iTrans = 0;
			int iIndel = 0;
			while (nIndel + nTrans > 0) {
				PointMutation.<G>distinctN(snpSizes[iSnp++]).apply(rng).accept(g, stats);

				boolean doIndelNotTrans = rng.nextDouble() < ((double)nIndel)/((double)(nIndel+nTrans));
				if (doIndelNotTrans) {
					nIndel--;
					if (indelBalanced) {
						InDel.<G>balancedAlwaysWithSize(indelSizes.get(iIndel++)).apply(rng).accept(g, stats);
					} else {
						InDel.<G>withSize(indelSizes.get(iIndel++)).apply(rng).accept(g, stats);
					}
				} else {
					nTrans--;
					Translocation.<G>withSize(transSizes.get(iTrans++)).apply(rng).accept(g, stats);
				}
			}
			PointMutation.<G>distinctN(snpSizes[iSnp++]).apply(rng).accept(g, stats);
		};
	}

	/**
	 * Specify a mutation rule where the actual number of bits affected by each mutation type for each mutation instance is drawn from a binomial distribution, affecting on average mutationrate*genome.size() bits.
	 * For mutations affecting more than one bit, the binomial draws the number of the mutations of that type to be performed, without affecting the size.
	 */
	public MutationRule<G> readMutationBinomial() {
		double snpRate = this.snpRate.read();
		double transRate = this.transRate.read();
		double indelRate = this.indelRate.read();
		double indelPower = this.indelPower.read();
		double transPower = this.transPower.read();
		boolean indelBalanced = this.indelBalanced.read();
		int indelMinSize = this.indelMinSize.read();
		int transMinSize = this.transMinSize.read();

		Function<G,DiscreteDistribution> indelSizeDist =
				(g) -> ContinuousDistribution.powerLaw(indelPower, g.size() > indelMinSize*2 ? indelMinSize : 1, g.size()+1).round();
		Function<G,DiscreteDistribution> transSizeDist =
				(g) -> ContinuousDistribution.powerLaw(transPower, g.size() > transMinSize*2 ? transMinSize : 1, g.size()+1).round();

		return (rng) -> (g, stats) -> {
			double avgIndelSize = ContinuousDistribution.powerLawAvg(indelPower, g.size() > 10 ? 5 : 1, g.size()+1);
			double avgTransSize = ContinuousDistribution.powerLawAvg(transPower, g.size() > 10 ? 5 : 1, g.size()+1);

			int nIndel = DiscreteDistribution.getBinomial(rng, g.size(), indelRate / avgIndelSize);
			int nTrans = DiscreteDistribution.getBinomial(rng, g.size(), transRate / avgTransSize);

			double partialSnpRate = snpRate/(nIndel+nTrans+1);
			MutationOp<G> pointMutation = PointMutation.<G>distinctN((g_again) -> DiscreteDistribution.binomial(g_again.size(), partialSnpRate)).apply(rng);
			MutationOp<G> indelMutation = indelBalanced ? InDel.<G>balancedSometimesWithSize(indelSizeDist).apply(rng) : InDel.withSize(indelSizeDist).apply(rng);
			MutationOp<G> transMutation = Translocation.<G>withSize(transSizeDist).apply(rng);

			while (nIndel+nTrans > 0) {
				pointMutation.accept(g, stats);

				boolean doIndelNotTrans = rng.nextDouble() < ((double)nIndel)/((double)(nIndel+nTrans));
				if (doIndelNotTrans) {
					nIndel--;
					indelMutation.accept(g, stats);
				} else {
					nTrans--;
					transMutation.accept(g, stats);
				}
			}
			pointMutation.accept(g, stats);
		};
	}

	public MutationRule<G> readMutationExponential() {
		double snpRate = this.snpRate.read();
		double transRate = this.transRate.read();
		double indelRate = this.indelRate.read();
		double indelPower = this.indelPower.read();
		double transPower = this.transPower.read();
		boolean indelBalanced = this.indelBalanced.read();
		int indelMinSize = this.indelMinSize.read();
		int transMinSize = this.transMinSize.read();

		Function<G,DiscreteDistribution> indelSizeDist =
				(g) -> ContinuousDistribution.powerLaw(indelPower, g.size() > indelMinSize*2 ? indelMinSize : 1, g.size()+1).round();
		Function<G,DiscreteDistribution> transSizeDist =
				(g) -> ContinuousDistribution.powerLaw(transPower, g.size() > transMinSize*2 ? transMinSize : 1, g.size()+1).round();

		return (rng) -> (g, stats) -> {
			double avgIndelSize = ContinuousDistribution.powerLawAvg(indelPower, g.size() > 10 ? 5 : 1, g.size()+1);
			double avgTransSize = ContinuousDistribution.powerLawAvg(transPower, g.size() > 10 ? 5 : 1, g.size()+1);

			int nIndel = ContinuousDistribution.exponential((g.size()) * indelRate / avgIndelSize).floor().applyAsInt(rng);
			int nTrans = ContinuousDistribution.exponential((g.size()) * transRate / avgTransSize).floor().applyAsInt(rng);

			double partialSnpRate = snpRate/(nIndel+nTrans+1);
			MutationOp<G> pointMutation = PointMutation.<G>distinctN((g_again) -> DiscreteDistribution.binomial(g_again.size(), partialSnpRate)).apply(rng);
			MutationOp<G> indelMutation = indelBalanced ? InDel.balancedSometimesWithSize(indelSizeDist).apply(rng) : InDel.withSize(indelSizeDist).apply(rng);
			MutationOp<G> transMutation = Translocation.withSize(transSizeDist).apply(rng);

			while (nIndel+nTrans > 0) {
				pointMutation.accept(g, stats);

				boolean doIndelNotTrans = rng.nextDouble() < ((double)nIndel)/((double)(nIndel+nTrans));
				if (doIndelNotTrans) {
					nIndel--;
					indelMutation.accept(g, stats);
				} else {
					nTrans--;
					transMutation.accept(g, stats);
				}
			}
			pointMutation.accept(g, stats);
		};
	}

	public MutationRule<G> readMutationExact1() {
		double snpRate = this.snpRate.read();
		double transRate = this.transRate.read();
		double indelRate = this.indelRate.read();
		boolean indelBalanced = this.indelBalanced.read();

		return (rng) -> (g, stats) -> {

			int transSize = DiscreteDistribution.getRandomRound(rng, (g.size()) * transRate);
			int indelSize = DiscreteDistribution.getRandomRound(rng, (g.size()) * indelRate);
			int snpSize   = DiscreteDistribution.getRandomRound(rng, (g.size()) * snpRate);

			if (transSize > 0) Translocation.<G>withSize(transSize).apply(rng).accept(g, stats);
			if (indelSize > 0) if (indelBalanced) InDel.<G>balancedSometimesWithSize(indelSize).apply(rng).accept(g, stats); else InDel.<G>withSize(indelSize).apply(rng).accept(g, stats);
			if (snpSize > 0)   PointMutation.<G>distinctN(snpSize).apply(rng).accept(g, stats);
		};
	}

	public MutationRule<G> readMutationPower1() {
		double snpRate = this.snpRate.read();
		double transRate = this.transRate.read();
		double indelRate = this.indelRate.read();
		double indelPower = this.indelPower.read();
		double transPower = this.transPower.read();
		boolean indelBalanced = this.indelBalanced.read();
		int indelMinSize = this.indelMinSize.read();
		int transMinSize = this.transMinSize.read();

		Function<G,DiscreteDistribution> indelSizeDist =
				(g) -> ContinuousDistribution.powerLaw(indelPower, g.size() > indelMinSize*2 ? indelMinSize : 1, g.size()+1).round();
		Function<G,DiscreteDistribution> transSizeDist =
				(g) -> ContinuousDistribution.powerLaw(transPower, g.size() > transMinSize*2 ? transMinSize : 1, g.size()+1).round();

		DiscreteDistribution mutationDist =
				DiscreteDistribution.weighted(new double[] {snpRate, indelRate, transRate});

		return (rng) -> (g, stats) -> {

			MutationOp<G> pointMutation = PointMutation.<G>distinctN((g_again) -> DiscreteDistribution.binomial(g_again.size(), snpRate)).apply(rng);
			MutationOp<G> indelMutation = indelBalanced ? InDel.<G>balancedSometimesWithSize(indelSizeDist).apply(rng) : InDel.<G>withSize(indelSizeDist).apply(rng);
			MutationOp<G> transMutation = Translocation.<G>withSize(transSizeDist).apply(rng);

			int mutation = mutationDist.applyAsInt(rng);
			switch (mutation) {
			case 0: // snp
				pointMutation.accept(g, stats);
				break;
			case 1: // indel
				indelMutation.accept(g, stats);
				pointMutation.accept(g, stats);
				break;
			case 2: // trans
				transMutation.accept(g, stats);
				pointMutation.accept(g, stats);
				break;
			}
		};
	}

	public final Option<MutationRule<G>> mutation = optionWithStringDefault("mutationtype", "binomial", (optionValue) -> {
		switch (optionValue) {
		case "binomial":
			return readMutationBinomial();
		case "exact":
			return readMutationExact();
		case "exponential":
			return readMutationExponential();
		case "exact1":
			return readMutationExact1();
		case "power1":
			return readMutationPower1();
		default:
			throw new IllegalArgumentException();
		}
	});

	public final Option<MutationRule<G>> mutationWithStats = autoOption(() -> (rng) -> MutationOp.withStats(this.mutation.read().apply(rng)));

//	// Carl version
//	public final Option<MutationRule> mutation = autoOption(() -> {
//		Function<Genome,DiscreteDistribution> indelSize =
//			(g) -> DiscreteDistribution.powerLaw(-2, g.size()+1); // +1 because otherwise undefined for length=1. Deletions with too high size will be redrawn.
//		double snpRate = this.snpRate.read();
//		double indelRate = this.indelRate.read();
//
//		return (rng) -> (g) -> {
//			int n = DiscreteDistribution.getPoisson(rng, ((double)g.size() * indelRate / 1.64493));
//			MutationOp pointMutation = PointMutation.distinctN((g_again) -> DiscreteDistribution.randomRound(((double)g.size())*snpRate/((double)n+1))).apply(rng);
//			MutationOp indelMutation = DupDel.withSize(indelSize).apply(rng);
//			for (int i=0; i<n; i++) {
//				pointMutation.accept(g);
//				indelMutation.accept(g);
//			}
//			pointMutation.accept(g);
//		};

	public final Option<Integer> genomeLength     = option("length", 1000, Integer::valueOf);
	public final Option<BitGenome> consensusSequence = option("consensus", BitGenome.readUnsafe("110011"), BitGenome::readUnsafe);
	public final Option<Integer> targetLength = option("targetLength", 16, Integer::valueOf);
	public final Option<Integer> targetAmount = option("targetAmount", 1024, Integer::valueOf);
	public final Option<List<KMP<BitGenome>>> targets = autoOption(() -> {
		int targetLength = this.targetLength.read();
		int targetAmount = this.targetAmount.read();
		long seed = this.seed.read();
		CategoricalDistribution<? extends BitGenome> targetGenerator = BitGenome.random(targetLength);
		return Functional
				.randoms(seed)
				.map(targetGenerator::apply)
				.distinct()
				.limit(targetAmount)
				.map(LinearGenome::kmpView)
				.collect(Collectors.toList());
	});
	public final Option<Double> decayFactor = option("decayfactor", 1 - 0.002, Double::valueOf);
	public final Option<Double> roadSigma = option("roadsigma", 0.05, Double::valueOf);
	public final Option<Double> distanceFactor = option("distancefactor", 2., Double::valueOf);

	public final Option<BitGenome> targetString = option("targetString",
//				"\"offensive\" is frequently but a synonym for \"unusual\"; and a great work of art is of course always original" // Carl version
				BitGenome.cat(
						"And what a science Natural History will be, when we are in our graves, when all the laws of change are thought one of the most important parts of Natural History."
						.chars()
						.mapToObj((c) -> BitGenome.encodeIntBase(c, 7))::iterator
						).copy(0,1000)
				, (optionValue) -> {
					Optional<BitGenome> asBinary = BitGenome.read(optionValue);
					if (asBinary.isPresent()) { // If possible, read target string as binary (only 01)
						return asBinary.get();
			 		} else { // else convert string to binary using 7-bit ascii
			 			return BitGenome.cat(optionValue.chars().mapToObj((c) -> BitGenome.encodeIntBase(c,7))::iterator);
			 		}
				});
	public final Option<FitnessFunction<BitGenome>> uncorrectedFitness = option(
				"fitness",
				(optionValue) -> {
					switch (optionValue) {
					case "triangles": {
						BitGenome consensus = this.consensusSequence.read();
						DoubleUnaryOperator target = (x) -> Math.sin(2*Math.PI*6*x);
						return SumFunction.triangles(
								target,
								consensus.kmpView());
						}
					case "triangles_const": {
						BitGenome consensus = this.consensusSequence.read();
						return SumFunction.triangles(
								(x) -> 1,
								consensus.kmpView());
						}
					case "triangles_mean": {
						BitGenome consensus = this.consensusSequence.read();
						return MeanFunction.of(
								(x) -> Math.sin(2*Math.PI*6*x),
								consensus.kmpView(),
								Triangle::read);
					}
					case "triangles_meanpositioned": {
						BitGenome consensus = this.consensusSequence.read();
						double decayFactor = this.decayFactor.read();
						return MeanFunction.of(
								(x) -> Math.sin(2*Math.PI*6*x),
								consensus.kmpView(),
								MeanFunction.weighByPosition(decayFactor, Triangle::read));
					}
					case "triangles_meanwithoutcenter": {
						BitGenome consensus = this.consensusSequence.read();
						return MeanFunction.of(
								(x) -> Math.sin(2*Math.PI*6*x),
								consensus.kmpView(),
								Triangle::readWithCenterFromPosition);
					}
					case "substrings": {
						Collection<LinearGenome.KMP<BitGenome>> targets = this.targets.read();
						return Substrings.of(targets);
						}
					case "substrings_ordered": {
						Collection<LinearGenome.KMP<BitGenome>> targets = this.targets.read();
						return Substrings.ordered(targets);
					}
					case "string": {
						Random rng = this.rng.read();
						BitGenome target = this.targetString.read();
						return StringMatch.of(target, Global.<BitGenome>alignmentWithAffineGapScore(0, -5, -6, -3).apply(rng)::score);
						}
//					case "train": {
//						BitGenome consensus = this.consensusSequence.read();
//						DoubleUnaryOperator target = (x) -> Math.sin(2*Math.PI*6*x);
//						return TrainSchedule.of(
//								consensus,
//								target,
//								0.1
//								);
//					}
//					case "salesman": {
//						Random rng = new Random(this.seed.read());
//						BitGenome consensus = this.consensusSequence.read();
//						double distanceFactor = this.distanceFactor.read();
//						int n = this.targetAmount.read();
//						double[][] distanceMatrix = TravellingSalesman.randomDistanceMatrix(rng, n);
//						return TravellingSalesman.numberTags(distanceMatrix, distanceFactor, consensus);
//					}
//					case "salesman_tags": {
//						Random rng = new Random(this.seed.read());
//						List<BitGenome> targets = this.targets.read();
//						int n = this.targetAmount.read();
//						double distanceFactor = this.distanceFactor.read();
//						double[][] distanceMatrix = TravellingSalesman.randomDistanceMatrix(rng, n);
//						return TravellingSalesman.sparseTags(targets, distanceMatrix, distanceFactor);
//					}
					default:
						throw new IllegalArgumentException();
					}
				});
	public final Option<FitnessFunction<BitGenome>> fitness = autoOption(() -> {
		int len = this.genomeLength.read();
		FitnessFunction<BitGenome> ff = this.uncorrectedFitness.read();
		return FitnessFunction.limitLength(len, ff);
	});
	
//
//	protected BitGenome genomeRead(String str) {
//		switch (str) {
//		case "random":
//			return BitGenome.random(this.genomeLength.read()).apply(new Random(this.seed.read()));
////		case "mutated": {
////			BitGenome result = genomeRead("random");
////			this.mutation.read().apply(new Random(this.seed.read())).mutate(result);
////			return result;
////		}
//		default:
//			return BitGenome.readUnsafe(str);
//		}
//	};


}
