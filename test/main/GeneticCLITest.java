package main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.RepeatedTest;

import genome.binary.BitGenome;
import genome.binary.BitGenomeInit;
import main.binary.BinaryCommandLineInterface;
import mutation.MutationRule;
import mutation.MutationStats;
import mutation.binary.Insertion;
import mutation.binary.PointMutation;
import mutation.string.Deletion;
import mutation.string.Translocation;

public class GeneticCLITest extends BitGenomeInit {
	
	@RepeatedTest(100)
	public void testMutationExact() {
		double snpRate = 0.1;
		double indelRate = 0.2;
		double transRate = 0.1;
		BinaryCommandLineInterface<BitGenome> cli = new BinaryCommandLineInterface<BitGenome>(new String[]{"mutationtype=exact", "logger=/dev/null", "snpRate="+snpRate, "indelRate="+indelRate, "transRate="+transRate}) {
			@Override
			public void run(boolean dryrun) {}
		};
		
		MutationRule<BitGenome> mutation = MutationRule.withStats(cli.mutation.read());
		
		MutationStats stats = mutation.apply(rng).mutateAndGetStats(random1000A).get();
		
		assertEquals(1000. * snpRate, stats.count(PointMutation.TYPE));
		assertEquals(1000. * indelRate, stats.size(Deletion.TYPE) + stats.size(Insertion.TYPE));
		assertEquals(1000. * transRate, stats.size(Translocation.TYPE));
	}

}
