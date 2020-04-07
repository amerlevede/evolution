package main.binary;

import java.util.Random;
import java.util.function.Function;

import alignment.AlignmentRule;
import alignment.Global;
import alignment.Glocal;
import alignment.Local;
import alignment.PerfectAlignment;
import alignment.VarOAlignment;
import alignment.VarOAlignmentRule;
import alignment.algorithms.GreedyGlocal;
import genome.binary.BitGenomeWithHistory;
import util.IntPair;

/**
 * Command line utility for aligning genomes.
 */
public class Align extends BinaryWithHistoryCommandLineInterface {
	
	public Align(String[] args) {
		super(args);
	}
	
	public final Option<VarOAlignmentRule<BitGenomeWithHistory>> type = option("type",
			(optionValue) -> {
				switch (optionValue) {
				case "global": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int scoreGapOpen = this.scoreGapOpen.read();
					int scoreGapExtend = this.scoreGapExtend.read();
					return Global.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
					}
				case "local": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int scoreGapOpen = this.scoreGapOpen.read();
					int scoreGapExtend = this.scoreGapExtend.read();
					return Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
					}
				case "synapsing": {
					int synapseSize = this.synapseSize.read();
					return Global.repeatedLocal(Local.<BitGenomeWithHistory>longestCommonSubstring(), synapseSize);
					}
				case "synapsing_general": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int scoreGapOpen = this.scoreGapOpen.read();
					int scoreGapExtend = this.scoreGapExtend.read();
					int synapseSize = this.synapseSize.read();
					AlignmentRule<BitGenomeWithHistory> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
					return Global.repeatedLocal(localAlign, synapseSize);
				}
				case "lcss":
				case "lcsstring": {
					return Local.longestCommonSubstring();
				}
				case "onegap": {
					return Global.oneGap();
				}
				case "greedyglocal": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int synapseSize = this.synapseSize.read();
					return GreedyGlocal.alignment(scoreMatch, scoreMismatch, synapseSize);
				}
				case "glocal_lcss": {
					int synapseSize = this.synapseSize.read();
					return Glocal.unorderedRepeatedLocal(Local.<BitGenomeWithHistory>longestCommonSubstring(), synapseSize);
				}
				case "glocal": {
					int scoreMatch = this.scoreMatch.read();
					int scoreMismatch = this.scoreMismatch.read();
					int scoreGapOpen = this.scoreGapOpen.read();
					int scoreGapExtend = this.scoreGapExtend.read();
					int synapseSize = this.synapseSize.read();
					AlignmentRule<BitGenomeWithHistory> localAlign = Local.alignmentWithAffineGapScore(scoreMatch, scoreMismatch, scoreGapOpen, scoreGapExtend);
					return Glocal.unorderedRepeatedLocal(localAlign, synapseSize);
				}
				case "perfect": {
					return rng -> PerfectAlignment::alignFromHistory;
				}
				default:
					throw new IllegalArgumentException();
			}});
	
	@Override
	public void run(boolean dryrun) {
		Function<Random,BitGenomeWithHistory> afun = this.genomeA.read();
		Function<Random,Function<BitGenomeWithHistory,BitGenomeWithHistory>> bfun = this.genomeB.read();
		VarOAlignmentRule<BitGenomeWithHistory> align = this.type.read();
		Random rng = new Random(this.seed.read());
		int size = this.genomeLength.read();
		int cycles = this.cycles.read();
		
		warnUnusedVariables();
		
		if (!dryrun) {
			
			double aligned = 0;
			double correct = 0;
			double segments = 0;
			double segmentsSimplified = 0;
			for (int i=0; i<cycles; i++) {
				BitGenomeWithHistory a = afun.apply(rng);
				BitGenomeWithHistory b = bfun.apply(rng).apply(a);
				
				VarOAlignment<BitGenomeWithHistory> alignment = align.apply(rng).apply(a, b);
				
				for (IntPair p : alignment.getPairs()) {
					aligned++;
					if (a.isHomologousTo(p.x, b, p.y)) correct++;
				}
				segments += alignment.getSegments().size();
				segmentsSimplified += alignment.simplify().getSegments().size();
				
				if (cycles == 1) println(alignment.display(90));
			}
			
			aligned /= (double)(cycles * size);
			correct /= (double)(cycles * size);
			segments /= (double)cycles;
			segmentsSimplified /= (double)cycles;
			
			System.out.println(aligned + "\t" + correct + "\t" + segments + "\t" + segmentsSimplified);
		}
	}

}
