package mutation.string;

import java.util.Optional;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import genome.binary.BitGenomeWithHistory;
import genome.binary.BitGenomeWithHistoryInit;
import util.DiscreteDistribution;

class InversionTest extends BitGenomeWithHistoryInit {

	@Test
	void testInversion_zeroLength() {
		Inversion.perform(Optional.empty(), random1000A, 50, 0);

		assertThat(BitGenomeWithHistory::unmutated, random1000A, random1000Aref);
	}

	@Test
	void testInversion_oneLength() {
		Inversion.perform(Optional.empty(), random1000A, 50, 1);

		assertThat(BitGenomeWithHistory::unmutated, random1000A, random1000Aref);
	}

	@RepeatedTest(10)
	void testInversion_twoLength() {
		Inversion.perform(Optional.empty(), random1000A, 50, 2);

		assertThat(BitGenomeWithHistory::unmutated, random1000A.view(0,50), random1000Aref.view(0,50));
		assertThat(BitGenomeWithHistory::unmutated, random1000A.view(52,1000), random1000Aref.view(52,1000));
		assertThat(BitGenomeWithHistory::unmutated, random1000A.view(50,52).reversedView(), random1000Aref.view(50,52));
	}

	@RepeatedTest(100)
	void testInversion_randomLength() {
		int i = DiscreteDistribution.getUniform(rng, 0, 500);
		int len = DiscreteDistribution.getUniform(rng, 0, 500);
		Inversion.perform(Optional.empty(), random1000A, i, len);

		assertThat(BitGenomeWithHistory::unmutated, random1000A.view(0,i), random1000Aref.view(0,i));
		assertThat(BitGenomeWithHistory::unmutated, random1000A.view(i+len,1000), random1000Aref.view(i+len,1000));
		assertThat(BitGenomeWithHistory::unmutated, random1000A.view(i,i+len).reversedView(), random1000Aref.view(i,i+len));
	}

}
