package genome.gene;

import java.util.Optional;

import genome.binary.BinaryGenome;
import util.Assert;
import util.DoublePair;
import util.IntPair;

/**
 * A {@link Gene} that encodes a number.
 * 
 * @author adriaan
 *
 */
public final class NumberGene {

	public NumberGene() {
		Assert.utilityClass();
	}

	public static <G extends BinaryGenome<G>> Gene.Reader<G,Double> readFractionWithDigits(int digits) {
		return (consensus, g, i) -> {
			int consensusSize = consensus.pattern.size();
			if (g.size() < i + consensusSize + digits) {
				return Optional.empty();
			} else {
				int intval = g.view(i+consensusSize, i+consensusSize+digits).decodeIntGray();
				double val = (intval) / ((Math.pow(2,digits)));
				return Optional.of(new Gene<>(
						g.view(i, i+consensusSize+digits),
						i,
						val
						));
			}
		};
	}

	public static <G extends BinaryGenome<G>> Gene.Reader<G,Integer> readIntWithDigits(int digits) {
		return (consensus, g, i) -> {
			int consensusSize = consensus.pattern.size();
			if (g.size() < i + consensusSize + digits) {
				return Optional.empty();
			} else {
				int intval = g.view(i+consensusSize, i+consensusSize+digits).decodeIntGray();
				return Optional.of(new Gene<>(
						g.view(i, i+consensusSize+digits),
						i,
						intval
						));
			}
		};
	}

	public static <G extends BinaryGenome<G>> Gene.Reader<G,IntPair> readIntsWithDigits(int digits) {
		return readIntsWithDigits(digits, digits);
	}

	public static <G extends BinaryGenome<G>> Gene.Reader<G,IntPair> readIntsWithDigits(int leftDigits, int rightDigits) {
		return (consensus, g, i) -> {
			int consensusSize = consensus.pattern.size();
			if (g.size() < i+consensusSize + leftDigits + rightDigits) {
				return Optional.empty();
			} else {
				int leftint = g.view(i+consensusSize, i+consensusSize+leftDigits).decodeIntGray();
				int rightint = g.view(i+consensusSize+leftDigits, i+consensusSize+leftDigits+rightDigits).decodeIntGray();
				return Optional.of(new Gene<>(
						g.view(i, i+consensusSize+leftDigits+rightDigits),
						i,
						IntPair.of(leftint, rightint)
						));
			}
		};
	}

	public static <G extends BinaryGenome<G>> Gene.Reader<G,DoublePair> readFractionsWithDigits(int digits) {
		return readFractionsWithDigits(digits, digits);
	}

	public static <G extends BinaryGenome<G>> Gene.Reader<G,DoublePair> readFractionsWithDigits(int leftDigits, int rightDigits) {
		return (consensus, g, i) -> {
			int consensusSize = consensus.pattern.size();
			if (g.size() < i+consensusSize + leftDigits + rightDigits) {
				return Optional.empty();
			} else {
				int leftint = g.view(i+consensusSize, i+consensusSize+leftDigits).decodeIntGray();
				int rightint = g.view(i+consensusSize+leftDigits, i+consensusSize+leftDigits+rightDigits).decodeIntGray();
				double leftval = (leftint) / ((Math.pow(2,leftDigits)));
				double rightval = (rightint) / ((Math.pow(2,rightDigits)));
				return Optional.of(new Gene<>(
						g.view(i, i+consensusSize+leftDigits+rightDigits),
						i,
						DoublePair.of(leftval, rightval)
						));
			}
		};
	}

}
