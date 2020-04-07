package crossover.binary;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;

import crossover.CrossoverOp;
import crossover.CrossoverRule;
import crossover.CrossoverRule.N;
import genome.RandomInit;
import genome.binary.BitGenomeWithHistory;
import mutation.MutationRule;
import mutation.binary.InDel;
import mutation.binary.PointMutation;
import util.DiscreteDistribution;

public abstract class BinaryCrossoverTest extends RandomInit {

	public abstract CrossoverOp<BitGenomeWithHistory> crossover(CrossoverRule.N n);

	public void otherConstraints(BitGenomeWithHistory a, BitGenomeWithHistory b, BitGenomeWithHistory aref, BitGenomeWithHistory bref) {
	}

	public int countCrossPoints(BitGenomeWithHistory a, BitGenomeWithHistory b, BitGenomeWithHistory aref, BitGenomeWithHistory bref) {
		int result = 0;
		boolean parentIsA = true;

		for (int i=1; i<a.size(); i++) {
			long thisid = a.getId(i);
			if (parentIsA) {
				boolean edgeAppearsOnParentA = aref.homologsOf(a, i-1).filter(k->k+1<aref.size()).mapToLong(k->aref.getId(k+1)).anyMatch(id->id==thisid);
				if (!edgeAppearsOnParentA) {
					result++;
					parentIsA = false;
				}
			} else {
				boolean edgeAppearsOnParentB = bref.homologsOf(a, i-1).filter(k->k+1<bref.size()).mapToLong(k->bref.getId(k+1)).anyMatch(id->id==thisid);
				if (!edgeAppearsOnParentB) {
					result++;
					parentIsA = true;
				}
			}
		}

		return result;
	}

	abstract class CrossTest {

		public abstract CrossoverRule.N xpoints();
		public abstract MutationRule<BitGenomeWithHistory> parentGen();

		public BitGenomeWithHistory a;
		public BitGenomeWithHistory b;
		public BitGenomeWithHistory aref;
		public BitGenomeWithHistory bref;

		@BeforeEach
		public void setupCrossover() {
			a = BitGenomeWithHistory.random(DiscreteDistribution.uniform(5,1000)).apply(rng);
			aref = a.copy().view();
			b = a.copy().view().copy();
			parentGen().apply(rng).mutate(b);
			bref = b.copy();

			BinaryCrossoverTest.this.crossover(xpoints()).accept(a, b);
		}

		@RepeatedTest(100)
		public void testHomologConservation() {
			assertTrue(
			LongStream.concat(aref.ids(), bref.ids()).distinct().allMatch(id ->
					aref.homologs(id).count() + bref.homologs(id).count()
					== a.homologs(id).count() + b.homologs(id).count()
					)
			);
		}

		@RepeatedTest(100)
		public void testCrossoverPoints() {
			if (xpoints().type == N.Type.VALUE) {
				int countedXpoints = countCrossPoints(a, b, aref, bref);
				assertTrue(countedXpoints <= xpoints().value());
			}
		}
	}

	public abstract class CrossoverCases {

		public abstract MutationRule<BitGenomeWithHistory> parentGen();

		public abstract class PointCase extends CrossTest {
			@Override
			public MutationRule<BitGenomeWithHistory> parentGen() {
				return CrossoverCases.this.parentGen();
			}
		}

		@Nested
		public class OnePoint extends PointCase {
			@Override
			public N xpoints() {
				return N.value(1);
			}
		}
		@Nested
		public class TwoPoint extends PointCase {
			@Override
			public N xpoints() {
				return N.value(2);
			}
		}
		@Nested
		public class ThreePoint extends PointCase {
			@Override
			public N xpoints() {
				return N.value(3);
			}
		}
		@Nested
		public class FourPoint extends PointCase {
			@Override
			public N xpoints() {
				return N.value(4);
			}
		}
		@Nested
		public class FivePoint extends PointCase {
			@Override
			public N xpoints() {
				return N.value(5);
			}
		}
		@Nested
		public class HundredPoint extends PointCase {
			@Override
			public N xpoints() {
				return N.value(100);
			}
		}
		@Nested
		public class Uniform extends PointCase {
			@Override
			public N xpoints() {
				return N.UNIFORM;
			}
		}
	}

	@Nested
	public class Identical extends CrossoverCases {
		@Override
		public MutationRule<BitGenomeWithHistory> parentGen() {
			return rng -> (g,stats) -> {return;};
		}
	}

	@Nested
	public class Random extends CrossoverCases {
		@Override
		public MutationRule<BitGenomeWithHistory> parentGen() {
			return rng -> (g,stats) -> {
				BitGenomeWithHistory b = BitGenomeWithHistory.getRandom(rng, DiscreteDistribution.uniform(5,1000));
				g.delete(2, g.size());
				g.paste(0, b);
				};
		}
	}

	@Nested
	public class Flips extends CrossoverCases {
		@Override
		public MutationRule<BitGenomeWithHistory> parentGen() {
			return PointMutation
					.distinctN(g -> DiscreteDistribution.uniform(1, g.size()+1));
		}
	}

	@Nested
	public class InDels extends CrossoverCases {
		@Override
		public MutationRule<BitGenomeWithHistory> parentGen() {
			return InDel
					.<BitGenomeWithHistory>withSize(g -> DiscreteDistribution.uniform(1, g.size()/3+2))
					.repeatN(g -> DiscreteDistribution.uniform(1, 10));
		}
	}

	@Nested
	public class Combination extends CrossoverCases {
		@Override
		public MutationRule<BitGenomeWithHistory> parentGen() {
			return MutationRule.compose(List.of(
					PointMutation
					.distinctN(g -> DiscreteDistribution.uniform(1, g.size()+1))
					,
					InDel
					.<BitGenomeWithHistory>withSize(g -> DiscreteDistribution.uniform(1, g.size()/3+2))
					.repeatN(g -> DiscreteDistribution.uniform(1, 5))
					));
		}
	}

}
