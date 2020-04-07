package crossover.score;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import crossover.CrossoverRule;
import genome.binary.BitGenome;
import genome.binary.BitGenomeWithHistory;
import mutation.MutationRule;
import mutation.MutationStats;

/**
 * Data structure to inspect a particular instance of crossover.
 */
public class CrossoverOutcome {

	public CrossoverOutcome(
		BitGenomeWithHistory ancestor,

		BitGenomeWithHistory parentA,
		BitGenomeWithHistory parentB,
		BitGenomeWithHistory offspringA,
		BitGenomeWithHistory offspringB,

		BitGenomeWithHistory parentResetA,
		BitGenomeWithHistory parentResetB,
		BitGenomeWithHistory offspringResetA,
		BitGenomeWithHistory offspringResetB,

		Optional<MutationStats> mutationStats)
	{

		this.ancestor = ancestor;

		this.parentA = parentA;
		this.parentB = parentB;
		this.offspringA = offspringA;
		this.offspringB = offspringB;

		this.parentResetA = parentResetA;
		this.parentResetB = parentResetB;
		this.offspringResetA = offspringResetA;
		this.offspringResetB = offspringResetB;

		this.mutationStats = mutationStats;

	}

	public final BitGenomeWithHistory ancestor;

	public final BitGenomeWithHistory parentA;
	public final BitGenomeWithHistory parentB;
	public final BitGenomeWithHistory offspringA;
	public final BitGenomeWithHistory offspringB;

	public final BitGenomeWithHistory parentResetA;
	public final BitGenomeWithHistory parentResetB;
	public final BitGenomeWithHistory offspringResetA;
	public final BitGenomeWithHistory offspringResetB;

	public final Optional<MutationStats> mutationStats;

	public static CrossoverOutcome fromParents(Random rng,
			CrossoverRule<? super BitGenomeWithHistory> crossoverRule,
			BitGenomeWithHistory ancestor,
			BitGenomeWithHistory parentA, BitGenomeWithHistory parentB,
			Optional<MutationStats> mutationStats) {
		long seed = rng.nextLong();

		BitGenomeWithHistory offspringA = parentA.copy();
		BitGenomeWithHistory offspringB = parentB.copy();

		BitGenomeWithHistory parentResetA = BitGenomeWithHistory.of(BitGenome.of(parentA));
		BitGenomeWithHistory parentResetB = BitGenomeWithHistory.of(BitGenome.of(parentB));
		BitGenomeWithHistory offspringResetA = parentResetA.copy();
		BitGenomeWithHistory offspringResetB = parentResetB.copy();

		crossoverRule.apply(new Random(seed)).accept(offspringA, offspringB);
		crossoverRule.apply(new Random(seed)).accept(offspringResetA, offspringResetB);

		return new CrossoverOutcome(ancestor, parentA, parentB, offspringA, offspringB, parentResetA, parentResetB, offspringResetA, offspringResetB, mutationStats);
	}

	/** @note Only works if factory generates parents with shared ancestry (i.e. homologous bits) */
	public static CrossoverOutcome fromParents(Random rng,
			CrossoverRule<? super BitGenomeWithHistory> crossoverRule,
			BitGenomeWithHistory ancestor,
			Function<Random, BitGenomeWithHistory> parentFactory) {
		BitGenomeWithHistory parentA = parentFactory.apply(rng);
		BitGenomeWithHistory parentB = parentFactory.apply(rng);
		return CrossoverOutcome.fromParents(rng, crossoverRule, ancestor, parentA, parentB, Optional.empty());
	}

	/**
	 * Generate CrossoverOutcome where both parents are copies of the ancestor with the mutation applied.
	 */
	public static CrossoverOutcome fromMutationBoth(Random rng,
			CrossoverRule<? super BitGenomeWithHistory> crossoverRule,
			BitGenomeWithHistory ancestor,
			MutationRule<? super BitGenomeWithHistory> mutationRule) {
		Optional<MutationStats> stats = mutationRule.apply(rng).newStats();

		BitGenomeWithHistory parentA = ancestor.copy();
		mutationRule.apply(rng).accept(parentA, stats);

		BitGenomeWithHistory parentB = ancestor.copy();
		mutationRule.apply(rng).accept(parentB, stats);

		return CrossoverOutcome.fromParents(rng, crossoverRule, ancestor, parentA, parentB, stats);
	}

	public static CrossoverOutcome fromMutationBoth(Random rng,
			CrossoverRule<? super BitGenomeWithHistory> crossoverRule,
			Function<Random, ? extends BitGenome> ancestorFactory,
			MutationRule<? super BitGenomeWithHistory> mutationRule) {
		BitGenomeWithHistory ancestor = BitGenomeWithHistory.of(ancestorFactory.apply(rng));
		return CrossoverOutcome.fromMutationBoth(rng, crossoverRule, ancestor, mutationRule);
	}

	/**
	 * Generate CrossoverOutcome where one parent is a copy of the ancestor and the other is mutated.
	 */
	public static CrossoverOutcome fromMutationOne(Random rng,
			CrossoverRule<? super BitGenomeWithHistory> crossoverRule,
			BitGenomeWithHistory ancestor,
			MutationRule<? super BitGenomeWithHistory> mutationRule) {
		BitGenomeWithHistory parentA = ancestor.copy();

		BitGenomeWithHistory parentB = ancestor.copy();
		Optional<MutationStats> stats = mutationRule.apply(rng).mutateAndGetStats(parentB);

		return CrossoverOutcome.fromParents(rng, crossoverRule, ancestor, parentA, parentB, stats);
	}

	public static <S extends MutationStats> CrossoverOutcome fromMutationOne(Random rng,
			CrossoverRule<? super BitGenomeWithHistory> crossoverRule,
			Function<Random, ? extends BitGenome> ancestorFactory,
			MutationRule<? super BitGenomeWithHistory> mutationRule) {
		BitGenomeWithHistory ancestor = BitGenomeWithHistory.of(ancestorFactory.apply(rng));
		return CrossoverOutcome.fromMutationOne(rng, crossoverRule, ancestor, mutationRule);
	}

	public CrossoverOutcome flip() {
		return new CrossoverOutcome(
				this.ancestor,

				this.parentB,
				this.parentA,
				this.offspringB,
				this.offspringA,

				this.parentResetB,
				this.parentResetA,
				this.offspringResetB,
				this.offspringResetA,

				this.mutationStats
				);
	}

}
